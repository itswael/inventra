# Inventra — Architecture

## Overview

Inventra follows **Clean Architecture** with a unidirectional data flow (UDF) pattern using Kotlin Coroutines and Jetpack Compose.

```
UI Layer (Compose Screens)
       ↓ observes StateFlow
ViewModel Layer (Hilt ViewModels)
       ↓ calls
Repository Layer (InventraRepository)
       ↓ queries
Data Layer (Room DAOs → SQLite)
```

## Data Layer

### Entities
| Entity              | Purpose                                      |
|---------------------|----------------------------------------------|
| `ProductEntity`     | Products with denormalized WAC stock fields  |
| `StockPurchaseEntity` | Every incoming stock record               |
| `SaleEntity`        | Every sale, including WAC snapshot at time   |
| `CategoryEntity`    | Optional product categories                  |

### Weighted Average Cost (WAC)
The most critical accounting decision in Inventra.

**Problem:** A product is restocked at different prices over time. Which cost do we use for profit calculation?

**Solution — WAC:**
```
new_avg_cost = (existing_qty × existing_avg + incoming_qty × incoming_cost)
               ─────────────────────────────────────────────────────────────
                              (existing_qty + incoming_qty)
```

**Example:**
- Buy 10 units @ $20 → avg_cost = $20.00, stock = 10
- Sell 5 @ $25 → profit = (25 − 20) × 5 = **$25**, stock = 5, avg unchanged
- Buy 10 more @ $15 → avg = (5×$20 + 10×$15)/15 = **$16.67**, stock = 15
- Sell 3 @ $25 → profit = (25 − 16.67) × 3 = **$24.99**

The `currentAvgCost` and `currentStockQty` fields on `ProductEntity` are updated on **every purchase or sale** for O(1) lookup.
The `avgCostAtSale` field in `SaleEntity` captures the snapshot at time of sale for immutable historical records.

## Screens

| Screen             | Route              | Purpose                               |
|--------------------|--------------------|---------------------------------------|
| Dashboard          | `dashboard`        | Today's stats, quick actions, alerts  |
| Products           | `products`         | List, search, category filter         |
| Add/Edit Product   | `add_edit_product` | Product form                          |
| Stock In           | `stock_in`         | Record incoming stock + WAC preview   |
| Add Sale           | `add_sale`         | Record a sale + profit preview        |
| Analytics          | `analytics`        | Tabbed: Overview / Products / Trends  |
| Inventory          | `inventory`        | Current stock status + share report   |

## Dependency Injection

Hilt is used throughout. The `DatabaseModule` provides the Room database, all DAOs, and the repository as singletons.

## State Management

Each screen has a corresponding `@HiltViewModel` that exposes `StateFlow` objects. Screens collect state via `collectAsStateWithLifecycle()` which respects the Android lifecycle.
