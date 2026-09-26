package com.inventra.app.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.inventra.app.presentation.analytics.AnalyticsScreen
import com.inventra.app.presentation.dashboard.DashboardScreen
import com.inventra.app.presentation.inventory.InventoryScreen
import com.inventra.app.presentation.products.AddEditProductScreen
import com.inventra.app.presentation.products.ProductsScreen
import com.inventra.app.presentation.sales.AddSaleScreen
import com.inventra.app.presentation.stock.StockInScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Products : Screen("products")
    object Analytics : Screen("analytics")
    object Inventory : Screen("inventory")
    object AddEditProduct : Screen("add_edit_product?productId={productId}") {
        fun route(productId: String? = null) =
            if (productId != null) "add_edit_product?productId=$productId" else "add_edit_product"
    }
    object StockIn : Screen("stock_in?productId={productId}") {
        fun route(productId: String? = null) =
            if (productId != null) "stock_in?productId=$productId" else "stock_in"
    }
    object AddSale : Screen("add_sale?productId={productId}") {
        fun route(productId: String? = null) =
            if (productId != null) "add_sale?productId=$productId" else "add_sale"
    }
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Products, "Products", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    BottomNavItem(Screen.Analytics, "Analytics", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(Screen.Inventory, "Inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory),
)

private val bottomNavRoutes = bottomNavItems.map { it.screen.route }.toSet()

@Composable
fun InventraNavGraph() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showBottomBar = bottomNavRoutes.any {
        currentDestination?.hierarchy?.any { d -> d.route == it } == true
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(if (selected) item.selectedIcon else item.unselectedIcon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300)) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onAddSale = { navController.navigate(Screen.AddSale.route()) },
                    onStockIn = { navController.navigate(Screen.StockIn.route()) }
                )
            }
            composable(Screen.Products.route) {
                ProductsScreen(
                    onAddProduct = { navController.navigate(Screen.AddEditProduct.route()) },
                    onEditProduct = { id -> navController.navigate(Screen.AddEditProduct.route(id)) },
                    onStockIn = { id -> navController.navigate(Screen.StockIn.route(id)) }
                )
            }
            composable(Screen.Analytics.route) { AnalyticsScreen() }
            composable(Screen.Inventory.route) { InventoryScreen() }

            composable(
                route = Screen.AddEditProduct.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStack ->
                AddEditProductScreen(
                    productId = backStack.arguments?.getString("productId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.StockIn.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStack ->
                StockInScreen(
                    preselectedProductId = backStack.arguments?.getString("productId"),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.AddSale.route,
                arguments = listOf(navArgument("productId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStack ->
                AddSaleScreen(
                    preselectedProductId = backStack.arguments?.getString("productId"),
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
