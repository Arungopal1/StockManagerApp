# StockManager - Mobile Shop Stock Management App

A comprehensive Android application for mobile phone shop inventory management built with modern Android technologies.

## Features

### Core Features
- **Product Management**: Full CRUD for products (phones, accessories, parts, etc.)
- **Stock Transactions**: Stock In, Stock Out, Returns, Adjustments, Transfers
- **Barcode/IMEI Scanning**: CameraX + ML Kit integration for quick product lookup
- **Real-time Dashboard**: Live stats, low stock alerts, recent transactions
- **Reports & Analytics**: Daily/monthly summaries, top selling products, profit reports, stock movement
- **Multi-user Support**: Role-based access (Admin, Manager, Staff, Viewer)
- **Supplier & Customer Management**: Track suppliers, customers, balances
- **Offline-First**: Room database with optional cloud sync

### Technical Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Clean Architecture layers
- **Database**: Room (SQLite) with TypeConverters
- **Dependency Injection**: Hilt
- **Navigation**: Navigation Compose
- **Async**: Coroutines & Flow
- **Camera**: CameraX with ML Kit Barcode Scanning
- **Image Loading**: Coil
- **Date/Time**: Kotlinx Datetime

## Project Structure

```
app/
├── data/
│   ├── database/          # Room database & DAOs
│   ├── entity/            # Data models (Product, Transaction, User, etc.)
│   ├── dao/               # Data Access Objects
│   ├── repository/        # Repository implementations
│   └── util/              # TypeConverters
├── di/                    # Hilt modules
├── presentation/
│   ├── dashboard/         # Dashboard screen
│   ├── products/          # Product list, detail, form
│   ├── stock/             # Stock In/Out/Adjustment/Transfer/Return
│   ├── transactions/      # Transaction history
│   ├── reports/           # Analytics & reports
│   ├── settings/          # App settings
│   ├── main/              # MainActivity & Navigation
│   └── viewmodel/         # ViewModels
└── ui/
    ├── components/        # Reusable UI components
    └── theme/             # Material3 theming
```

## Getting Started

### Prerequisites
- Android Studio Koala (2024.1.1) or later
- JDK 17
- Android SDK 34
- Minimum SDK 24 (Android 7.0)

### Building the Project

1. Clone the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Run on device/emulator

### Configuration

The app uses a version catalog (`gradle/libs.versions.toml`) for dependency management. Key versions:
- AGP: 8.5.0
- Kotlin: 1.9.23
- Compose BOM: 2024.08.00
- Room: 2.6.1
- Hilt: 2.48
- CameraX: 1.3.4
- ML Kit: 17.2.0

## Database Schema

### Products Table
- id, sku, barcode, name, brand, model, category, color, storage
- condition, purchase_price, selling_price, current_stock
- min_stock_level, max_stock_level, location, supplier_id
- warranty_months, description, image_url, is_active
- created_at, updated_at

### Stock Transactions Table
- id, product_id, transaction_type, quantity
- unit_cost, unit_price, transaction_date
- reference_number, supplier_id, customer_id, user_id
- notes, status, related_transaction_id
- location_from, location_to

### Users Table
- id, username, email, full_name, password_hash
- role, phone, avatar_url, is_active
- last_login_at, created_at, updated_at

### Suppliers & Customers
- Full contact details, balances, payment terms

## Permissions Required
- `CAMERA` - Barcode scanning
- `INTERNET` - Cloud sync (future)
- `READ_EXTERNAL_STORAGE` / `READ_MEDIA_IMAGES` - Image import

## User Roles & Permissions

| Permission | Admin | Manager | Staff | Viewer |
|------------|-------|---------|-------|--------|
| Product CRUD | ✓ | ✓ | View | View |
| Stock In/Out | ✓ | ✓ | ✓ | - |
| Adjustments | ✓ | ✓ | - | - |
| Transfers | ✓ | ✓ | - | - |
| Reports | ✓ | ✓ | View | View |
| User Management | ✓ | View | - | - |
| Settings | ✓ | - | - | - |

## Future Enhancements

- [ ] Firebase/Firestore sync for multi-device
- [ ] Barcode label printing
- [ ] Purchase order management
- [ ] Sales invoicing
- [ ] Multi-location/warehouse support
- [ ] Push notifications for low stock
- [ ] Export to Excel/PDF
- [ ] Dark mode system integration
- [ ] Biometric authentication
- [ ] Unit tests & UI tests

## License

Proprietary - All rights reserved.