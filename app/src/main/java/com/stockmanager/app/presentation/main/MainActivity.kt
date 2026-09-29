package com.stockmanager.app.presentation.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.Surface
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.stockmanager.app.R
import com.stockmanager.app.presentation.dashboard.DashboardScreen
import com.stockmanager.app.presentation.products.ProductsScreen
import com.stockmanager.app.presentation.products.ProductDetailScreen
import com.stockmanager.app.presentation.products.ProductFormScreen
import com.stockmanager.app.presentation.reports.ReportsScreen
import com.stockmanager.app.presentation.settings.SettingsScreen
import com.stockmanager.app.presentation.stock.StockInScreen
import com.stockmanager.app.presentation.stock.StockOutScreen
import com.stockmanager.app.presentation.stock.StockAdjustmentScreen
import com.stockmanager.app.presentation.stock.StockTransferScreen
import com.stockmanager.app.presentation.stock.StockReturnScreen
import com.stockmanager.app.presentation.transactions.TransactionsScreen
import com.stockmanager.app.presentation.viewmodel.AuthViewModel
import com.stockmanager.app.presentation.viewmodel.DashboardViewModel
import com.stockmanager.app.presentation.viewmodel.ProductViewModel
import com.stockmanager.app.presentation.viewmodel.StockTransactionViewModel
import com.stockmanager.app.ui.theme.StockManagerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StockManagerTheme {
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.surface
                ) {
                    val navController = rememberNavController()
                    NavHost(navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            DashboardScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable("products") {
                            ProductsScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable(
                            route = "products/{productId}",
                            arguments = listOf(androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.LongType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.getLong("productId")
                            ProductDetailScreen(
                                navController = navController,
                                productId = productId,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable(
                            route = "products/add",
                        ) {
                            ProductFormScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }
                        composable(
                            route = "products/edit/{productId}",
                            arguments = listOf(androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.LongType })
                        ) { backStackEntry ->
                            val productId = backStackEntry.getLong("productId")
                            ProductFormScreen(
                                navController = navController,
                                productId = productId,
                                viewModel = hiltViewModel()
                            )
                        }

                        // Stock Transactions
                        composable("stock/in") {
                            StockInScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                productViewModel = hiltViewModel()
                            )
                        }
                        composable("stock/out") {
                            StockOutScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                productViewModel = hiltViewModel()
                            )
                        }
                        composable("stock/adjustment") {
                            StockAdjustmentScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                productViewModel = hiltViewModel()
                            )
                        }
                        composable("stock/transfer") {
                            StockTransferScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                productViewModel = hiltViewModel()
                            )
                        }
                        composable("stock/return") {
                            StockReturnScreen(
                                navController = navController,
                                viewModel = hiltViewModel(),
                                productViewModel = hiltViewModel()
                            )
                        }

                        composable("transactions") {
                            TransactionsScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }

                        composable("reports") {
                            ReportsScreen(
                                navController = navController,
                                viewModel = hiltViewModel()
                            )
                        }

                        composable("settings") {
                            SettingsScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}