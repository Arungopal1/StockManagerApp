package com.stockmanager.app.data.sync

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesKeys
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesDataStore
import androidx.datastore.preferences.rx.preferences
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.stockmanager.app.data.dao.ProductDao
import com.stockmanager.app.data.dao.StockTransactionDao
import com.stockmanager.app.data.entity.Product
import com.stockmanager.app.data.entity.StockTransaction
import com.stockmanager.app.data.entity.TransactionType
import io.supabase.flutter.Gotrue
import io.supabase.flutter.Postgrest
import io.supabase.flutter.Realtime
import io.supabase.flutter.SupabaseClient
import io.supabase.flutter.RealtimeChannel
import io.supabase.flutter.RealtimeEvent
import io.supabase.flutter.RealtimeListener
import io.supabase.flutter.PostgrestFilterBuilder
import io.supabase.flutter.PostgrestTransformBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.NotNull
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SupabaseSync @Inject constructor(
    private val context: Context,
    private val productDao: ProductDao,
    private val transactionDao: StockTransactionDao,
    @Named("supabaseUrl") private val supabaseUrl: String,
    @Named("supabaseAnonKey") private val supabaseAnonKey: String
) {

    // ========================================================================
    // STATE
    // ========================================================================
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState = _syncState.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long>(0)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    private val _shopId: String? = null
    private var currentUserId: String? = null

    // ========================================================================
    // CLIENTS
    // ========================================================================
    private val gson = Gson()
    private val json = Json { ignoreUnknownKeys = true }

    private val supabase = SupabaseClient.create(supabaseUrl, supabaseAnonKey) {
        install(Gotrue)
        install(Postgrest)
        install(Realtime)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var realtimeChannel: RealtimeChannel? = null
    private var isLoggedIn = false

    // ========================================================================
    // DATASTORE (for last sync time persistence)
    // ========================================================================
    private val dataStore = context.preferencesDataStore("sync_prefs")
    private val LAST_SYNC_KEY = PreferencesKey<Long>("last_sync_time_")
    private val SHOP_ID_KEY = PreferencesKey<String>("current_shop_id")

    // ========================================================================
    // PUBLIC API
    // ========================================================================

    /** Login with email/password - returns shop_id and role from metadata */
    suspend fun login(email: String, password: String): Result<LoginResult> = scope.await {
        try {
            val session = supabase.gotrue.signInWith(EmailPassword(email, password))
            
            if (session != null) {
                currentUserId = session.user.id
                _shopId = session.user.userMetadata?.getString("shop_id")
                val role = session.user.userMetadata?.getString("role") ?: "staff"
                
                if (_shopId == null) {
                    return@await Result.failure(Exception("No shop assigned to user. Contact admin."))
                }
                
                // Persist shop_id
                dataStore.edit { it.putString(SHOP_ID_KEY, _shopId!!) }
                
                // Load last sync time
                val lastSync = dataStore.data.first().getLong(LAST_SYNC_KEY + _shopId!!, 0)
                _lastSyncTime.value = lastSync
                
                isLoggedIn = true
                
                // Start realtime subscription
                subscribeToRealtime()
                
                Result.success(LoginResult(_shopId!!, role, session.accessToken))
            } else {
                Result.failure(Exception("Login failed: Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Full bidirectional sync: push local → pull remote */
    suspend fun fullSync(): Result<SyncResult> = scope.await {
        if (!isLoggedIn || _shopId == null) {
            return@await Result.failure(Exception("Not logged in"))
        }
        
        _syncState.value = SyncState.Syncing
        var pushed = 0
        var pulled = 0
        var errors = 0
        
        try {
            // 1. Push local changes
            pushed = pushLocalChanges()
            
            // 2. Pull remote changes
            pulled = pullRemoteChanges()
            
            // 3. Update last sync time
            val now = System.currentTimeMillis()
            _lastSyncTime.value = now
            dataStore.edit { it.putLong(LAST_SYNC_KEY + _shopId!!, now) }
            
            _syncState.value = SyncState.Success(now)
            Result.success(SyncResult(pushed, pulled, 0))
            
        } catch (e: Exception) {
            errors++
            _syncState.value = SyncState.Error(e.message ?: "Sync failed")
            Result.failure(e)
        }
    }

    /** Push only local unsynced changes */
    suspend fun pushOnly(): Result<Int> = scope.await {
        if (!isLoggedIn) return@await Result.failure(Exception("Not logged in"))
        try {
            val count = pushLocalChanges()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Pull only remote changes since last sync */
    suspend fun pullOnly(): Result<Int> = scope.await {
        if (!isLoggedIn) return@await Result.failure(Exception("Not logged in"))
        try {
            val count = pullRemoteChanges()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Logout and cleanup */
    fun logout() {
        scope.launch {
            supabase.gotrue.signOut()
            realtimeChannel?.unsubscribe()
            realtimeChannel = null
            isLoggedIn = false
            _shopId = null
            currentUserId = null
            _syncState.value = SyncState.Idle
        }
    }

    /** Get current shop ID */
    val shopId: String?
        get() = _shopId

    /** Check if currently syncing */
    val isSyncing: Boolean
        get() = _syncState.value is SyncState.Syncing

    // ========================================================================
    // PRIVATE: PUSH LOCAL CHANGES
    // ========================================================================
    private suspend fun pushLocalChanges(): Int {
        var count = 0
        
        // Push products
        val unsyncedProducts = productDao.getUnsyncedProducts()
        for (product in unsyncedProducts) {
            try {
                val json = productToSupabaseJson(product)
                supabase.postgrest["products"]
                    .upsert(json)
                    .execute()
                    .await()
                
                productDao.markSynced(product.id)
                count++
            } catch (e: Exception) {
                // Log error but continue
                e.printStackTrace()
            }
        }
        
        // Push transactions
        val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
        for (transaction in unsyncedTransactions) {
            try {
                val json = transactionToSupabaseJson(transaction)
                supabase.postgrest["stock_transactions"]
                    .upsert(json)
                    .execute()
                    .await()
                
                transactionDao.markSynced(transaction.id)
                count++
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        return count
    }

    // ========================================================================
    // PRIVATE: PULL REMOTE CHANGES
    // ========================================================================
    private suspend fun pullRemoteChanges(): Int {
        val shopId = _shopId!!
        val lastSync = _lastSyncTime.value
        var count = 0
        
        // Pull products (updated since last sync)
        val productsResponse = supabase.postgrest["products"]
            .select()
            .eq("shop_id", shopId)
            .gte("updated_at", LocalDateTime.ofEpochMilli(lastSync).toString())
            .order("updated_at")
            .limit(500)
            .execute()
            .await()
        
        val remoteProducts = json.decodeFromString<List<SupabaseProduct>>(productsResponse.data.toString())
        for (remote in remoteProducts) {
            mergeProduct(remote)
            count++
        }
        
        // Pull transactions (created since last sync)
        val transactionsResponse = supabase.postgrest["stock_transactions"]
            .select()
            .eq("shop_id", shopId)
            .gte("created_at", LocalDateTime.ofEpochMilli(lastSync).toString())
            .order("created_at")
            .limit(500)
            .execute()
            .await()
        
        val remoteTransactions = json.decodeFromString<List<SupabaseTransaction>>(transactionsResponse.data.toString())
        for (remote in remoteTransactions) {
            mergeTransaction(remote)
            count++
        }
        
        return count
    }

    // ========================================================================
    // PRIVATE: MERGE (Conflict Resolution: Server Wins)
    // ========================================================================
    private suspend fun mergeProduct(remote: SupabaseProduct) {
        val local = productDao.getByRemoteIdSync(remote.id)
        
        if (local == null || remote.updatedAt > local.updatedAt.toEpochMilliseconds()) {
            // Server is newer or no local copy - use server
            productDao.upsertFromRemote(remote.toEntity())
        }
        // If local is newer, it will be pushed on next pushLocalChanges()
    }

    private suspend fun mergeTransaction(remote: SupabaseTransaction) {
        val local = transactionDao.getByRemoteIdSync(remote.id)
        
        if (local == null || remote.createdAt > local.transactionDate.toEpochMilliseconds()) {
            transactionDao.upsertFromRemote(remote.toEntity())
        }
    }

    // ========================================================================
    // REALTIME SUBSCRIPTION
    // ========================================================================
    private fun subscribeToRealtime() {
        val shopId = _shopId!!
        
        realtimeChannel = supabase.realtime.channel("shop-$shopId-changes") {
            onPostgresChanges(
                event = "*",
                schema = "public",
                table = "products",
                filter = PostgrestFilterBuilder.Eq("shop_id", shopId)
            ) { event: RealtimeEvent ->
                scope.launch { pullRemoteChanges() }
            }
            
            onPostgresChanges(
                event = "*",
                schema = "public",
                table = "stock_transactions",
                filter = PostgrestFilterBuilder.Eq("shop_id", shopId)
            ) { event: RealtimeEvent ->
                scope.launch { pullRemoteChanges() }
            }
            
            onPostgresChanges(
                event = "*",
                schema = "public",
                table = "suppliers",
                filter = PostgrestFilterBuilder.Eq("shop_id", shopId)
            ) { event: RealtimeEvent ->
                scope.launch { pullRemoteChanges() }
            }
            
            onPostgresChanges(
                event = "*",
                schema = "public",
                table = "customers",
                filter = PostgrestFilterBuilder.Eq("shop_id", shopId)
            ) { event: RealtimeEvent ->
                scope.launch { pullRemoteChanges() }
            }
        }
        
        realtimeChannel?.subscribe()
    }

    // ========================================================================
    // JSON CONVERSION
    // ========================================================================
    private fun productToSupabaseJson(product: Product): String {
        val shopId = _shopId!!
        val map = mutableMapOf<String, Any>(
            "id" to product.remoteId ?: "",
            "shop_id" to shopId,
            "sku" to product.sku,
            "barcode" to product.barcode,
            "name" to product.name,
            "brand" to product.brand,
            "model" to product.model,
            "category" to product.category.name.toLowerCase(),
            "color" to product.color,
            "storage" to product.storage,
            "condition" to product.condition.name.toLowerCase(),
            "purchase_price" to product.purchasePrice,
            "selling_price" to product.sellingPrice,
            "current_stock" to product.currentStock,
            "min_stock_level" to product.minStockLevel,
            "max_stock_level" to product.maxStockLevel,
            "location" to product.location,
            "supplier_id" to product.supplierId,
            "warranty_months" to product.warrantyMonths,
            "description" to product.description,
            "image_url" to product.imageUrl,
            "is_active" to product.isActive,
            "created_at" to product.createdAt.toString(),
            "updated_at" to product.updatedAt.toString()
        )
        return gson.toJson(map)
    }

    private fun transactionToSupabaseJson(tx: StockTransaction): String {
        val shopId = _shopId!!
        val map = mutableMapOf<String, Any>(
            "id" to tx.remoteId ?: "",
            "shop_id" to shopId,
            "product_id" to tx.productRemoteId,
            "transaction_type" to tx.transactionType.name.toLowerCase(),
            "quantity" to tx.quantity,
            "unit_cost" to tx.unitCost,
            "unit_price" to tx.unitPrice,
            "reference_number" to tx.referenceNumber,
            "supplier_id" to tx.supplierId,
            "customer_id" to tx.customerId,
            "user_id" to currentUserId,
            "notes" to tx.notes,
            "status" to tx.status.name.toLowerCase(),
            "related_transaction_id" to tx.relatedTransactionRemoteId,
            "location_from" to tx.locationFrom,
            "location_to" to tx.locationTo,
            "created_at" to tx.transactionDate.toString()
        )
        return gson.toJson(map)
    }

    // ========================================================================
    // DATA CLASSES
    // ========================================================================
    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        data class Success(val timestamp: Long) : SyncState()
        data class Error(val message: String) : SyncState()
    }

    data class LoginResult(
        val shopId: String,
        val role: String,
        val accessToken: String
    )

    data class SyncResult(
        val pushed: Int,
        val pulled: Int,
        val errors: Int
    )

    // Supabase response models
    data class SupabaseProduct(
        val id: String,
        val shop_id: String,
        val sku: String,
        val barcode: String?,
        val name: String,
        val brand: String,
        val model: String,
        val category: String,
        val color: String,
        val storage: String,
        val condition: String,
        val purchase_price: Double,
        val selling_price: Double,
        val current_stock: Int,
        val min_stock_level: Int,
        val max_stock_level: Int,
        val location: String?,
        val supplier_id: String?,
        val warranty_months: Int,
        val description: String?,
        val image_url: String?,
        val is_active: Boolean,
        val created_at: String,
        val updated_at: String
    ) {
        fun toEntity(): Product = Product(
            remoteId = id,
            shopId = shop_id,
            sku = sku,
            barcode = barcode,
            name = name,
            brand = brand,
            model = model,
            category = com.stockmanager.app.data.entity.ProductCategory.valueOf(category.uppercase()),
            color = color,
            storage = storage,
            condition = com.stockmanager.app.data.entity.ProductCondition.valueOf(condition.uppercase()),
            purchasePrice = purchase_price,
            sellingPrice = selling_price,
            currentStock = current_stock,
            minStockLevel = min_stock_level,
            maxStockLevel = max_stock_level,
            location = location,
            supplierId = supplier_id?.toLongOrNull(),
            warrantyMonths = warranty_months,
            description = description,
            imageUrl = image_url,
            isActive = is_active,
            createdAt = LocalDateTime.parse(created_at),
            updatedAt = LocalDateTime.parse(updated_at)
        )
    }

    data class SupabaseTransaction(
        val id: String,
        val shop_id: String,
        val product_id: String,
        val transaction_type: String,
        val quantity: Int,
        val unit_cost: Double,
        val unit_price: Double,
        val reference_number: String,
        val supplier_id: String?,
        val customer_id: String?,
        val user_id: String?,
        val notes: String?,
        val status: String,
        val related_transaction_id: String?,
        val location_from: String?,
        val location_to: String?,
        val created_at: String
    ) {
        fun toEntity(): StockTransaction = StockTransaction(
            remoteId = id,
            shopId = shop_id,
            productRemoteId = product_id,
            transactionType = TransactionType.valueOf(transaction_type.uppercase()),
            quantity = quantity,
            unitCost = unit_cost,
            unitPrice = unit_price,
            referenceNumber = reference_number,
            supplierRemoteId = supplier_id,
            customerRemoteId = customer_id,
            userId = user_id?.toLongOrNull() ?: 1L,
            notes = notes,
            status = com.stockmanager.app.data.entity.TransactionStatus.valueOf(status.uppercase()),
            relatedTransactionRemoteId = related_transaction_id,
            locationFrom = location_from,
            locationTo = location_to,
            transactionDate = LocalDateTime.parse(created_at)
        )
    }
}

// ========================================================================
// EXTENSIONS FOR DAO (Add these to your DAOs)
// ========================================================================
/*
// In ProductDao.kt - add these methods:
@Query("SELECT * FROM products WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
suspend fun getUnsyncedProducts(): List<Product>

@Query("SELECT * FROM products WHERE remoteId = :remoteId")
suspend fun getByRemoteIdSync(remoteId: String): Product?

@Query("UPDATE products SET isSynced = 1, updatedAt = :now WHERE id = :id")
suspend fun markSynced(id: Long, now: LocalDateTime)

@Query("INSERT OR REPLACE INTO products (...) VALUES (...)")
suspend fun upsertFromRemote(product: Product)

// In StockTransactionDao.kt - similar methods:
@Query("SELECT * FROM stock_transactions WHERE remoteId IS NOT NULL AND (isSynced = 0 OR isSynced IS NULL)")
suspend fun getUnsyncedTransactions(): List<StockTransaction>

@Query("SELECT * FROM stock_transactions WHERE remoteId = :remoteId")
suspend fun getByRemoteIdSync(remoteId: String): StockTransaction?

@Query("UPDATE stock_transactions SET isSynced = 1 WHERE id = :id")
suspend fun markSynced(id: Long)

@Query("INSERT OR REPLACE INTO stock_transactions (...) VALUES (...)")
suspend fun upsertFromRemote(transaction: StockTransaction)
*/

// ========================================================================
// HILT MODULE (Add to DatabaseModule.kt)
// ========================================================================
/*
@Provides
@Named("supabaseUrl")
fun provideSupabaseUrl(): String = BuildConfig.SUPABASE_URL

@Provides
@Named("supabaseAnonKey")
fun provideSupabaseAnonKey(): String = BuildConfig.SUPABASE_ANON_KEY

@Provides
@Singleton
fun provideSupabaseSync(
    context: Context,
    productDao: ProductDao,
    transactionDao: StockTransactionDao,
    @Named("supabaseUrl") url: String,
    @Named("supabaseAnonKey") key: String
): SupabaseSync = SupabaseSync(context, productDao, transactionDao, url, key)
*/

// ========================================================================
// BUILD CONFIG (Add to app/build.gradle.kts)
// ========================================================================
/*
android {
    defaultConfig {
        buildConfigField "String", "SUPABASE_URL", "\"https://your-project.supabase.co\""
        buildConfigField "String", "SUPABASE_ANON_KEY", "\"your-anon-key\""
    }
}
*/

// ========================================================================
// USAGE IN VIEWMODEL
// ========================================================================
/*
@HiltViewModel
class SyncViewModel @Inject constructor(
    private val supabaseSync: SupabaseSync
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val uiState = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Loading
            supabaseSync.login(email, password)
                .onSuccess { result ->
                    _uiState.value = SyncUiState.Authenticated(result.shopId, result.role)
                    // Auto-start sync
                    sync()
                }
                .onFailure { e ->
                    _uiState.value = SyncUiState.Error(e.message ?: "Login failed")
                }
        }
    }
    
    fun sync() {
        viewModelScope.launch {
            _uiState.value = SyncUiState.Syncing
            supabaseSync.fullSync()
                .onSuccess { result ->
                    _uiState.value = SyncUiState.Synced(result.pushed, result.pulled)
                }
                .onFailure { e ->
                    _uiState.value = SyncUiState.Error(e.message ?: "Sync failed")
                }
        }
    }
    
    fun logout() {
        supabaseSync.logout()
        _uiState.value = SyncUiState.Idle
    }
}

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Loading : SyncUiState()
    data class Authenticated(val shopId: String, val role: String) : SyncUiState()
    object Syncing : SyncUiState()
    data class Synced(val pushed: Int, val pulled: Int) : SyncUiState()
    data class Error(val message: String) : SyncUiState()
}
*/