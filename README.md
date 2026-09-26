# Inventra

> A modern Android inventory management app for local sellers — track stock, record sales, and understand your business at a glance.

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![Platform](https://img.shields.io/badge/platform-Android-brightgreen)
![Min SDK](https://img.shields.io/badge/minSdk-26-blue)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12-orange)

---

## Features

### 📦 Product Management
- Add products with name (required), category, product code, and unit
- Set a **low-stock alert threshold** per product — get alerted on the dashboard
- Search products by name; filter by category

### 📥 Stock In
- Record incoming stock with quantity, cost per unit, date, and supplier
- **Real-time WAC preview** — see your new weighted average cost before confirming
- Full purchase history per product

### 🛒 Sales Recording
- Record daily sales: product, quantity, selling price
- **Live profit preview** — shows revenue, COGS, and profit before saving
- Stock validation — prevents selling more than you have

### 📊 Analytics
| Tab | What you see |
|-----|-------------|
| Overview | Revenue, Profit, Margin, Units Sold for selected period |
| Products | Best sellers and most profitable products ranked with bars |
| Trends | Daily revenue vs profit bar chart |

Period selector: **Today / 7 Days / 30 Days / 1 Year**

### 🏪 Inventory Status
- View all products with current stock, average cost, and total stock value
- Color-coded: 🟢 In Stock · 🟡 Low Stock · 🔴 Out of Stock
- **One-tap Share** — generates a formatted stock report to send via WhatsApp, email, or any app

---

## The Pricing Problem — Solved

When you restock at a different price, Inventra uses **Weighted Average Cost (WAC)**:

```
new_avg = (existing_qty × existing_avg + incoming_qty × incoming_cost)
          ─────────────────────────────────────────────────────────────
                         (existing_qty + incoming_qty)
```

**Example:**
- Buy 10 units @ $20 → avg cost = **$20.00**
- Sell 5 @ $25 → profit = **$25.00** (5 × $5 margin)
- Restock 10 @ $15 → new avg cost = **$16.67**
- Next sale uses $16.67 as cost → accurate profit every time

Every sale permanently records the cost at the time of sale, so historical records are always accurate.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0 |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | MVVM + Clean Architecture |
| Database | Room (SQLite) |
| DI | Hilt |
| Navigation | Compose Navigation |
| Async | Coroutines + Flow |
| Preferences | DataStore |

---

## Getting Started

### Requirements
- Android Studio Ladybug (2024.2.x) or newer
- JDK 17+
- Android device / emulator with API 26+

### Build & Run

1. Clone the repo:
   ```bash
   git clone https://github.com/itswael/inventra.git
   cd inventra
   ```

2. Open in **Android Studio** → *File → Open* → select the `inventra` folder

3. Let Android Studio sync Gradle (it will download dependencies automatically)

4. Run on device or emulator: **Run → Run 'app'** (Shift+F10)

> **Note:** The Gradle wrapper JAR is not committed. Android Studio downloads it automatically on first sync. If building from CLI, run `gradle wrapper` once to generate it.

---

## Project Structure

```
app/src/main/java/com/inventra/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAOs
│   │   ├── entity/       # Room entities
│   │   └── InventraDatabase.kt
│   └── repository/       # InventraRepository (WAC logic lives here)
├── di/                   # Hilt modules
├── domain/model/         # Domain models + mappers
├── presentation/
│   ├── dashboard/        # Home screen
│   ├── products/         # Product list + add/edit
│   ├── stock/            # Stock In screen
│   ├── sales/            # Sales screens
│   ├── analytics/        # Analytics tabs
│   ├── inventory/        # Inventory + share
│   └── navigation/       # NavGraph
└── ui/
    ├── theme/            # Colors, Typography, Theme
    └── components/       # Shared composables + charts
```

See [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for a deeper architecture walkthrough.

---

## Roadmap

- [ ] Google Drive backup & restore
- [ ] CSV / PDF export
- [ ] Barcode scanner for product lookup
- [ ] Multiple currency support
- [ ] Supplier management
- [ ] iOS support (Kotlin Multiplatform)

---

## License

[MIT](LICENSE) © 2026 itswael
