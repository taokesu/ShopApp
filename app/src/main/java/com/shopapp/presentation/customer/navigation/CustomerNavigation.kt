package com.shopapp.presentation.customer.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.customer.screen.CartScreen
import com.shopapp.presentation.customer.screen.CatalogScreen
import com.shopapp.presentation.customer.screen.CheckoutScreen
import com.shopapp.presentation.customer.screen.FavoritesScreen
import com.shopapp.presentation.customer.screen.OrderDetailScreen
import com.shopapp.presentation.customer.screen.OrdersScreen
import com.shopapp.presentation.customer.screen.ProductDetailScreen

@Composable
fun CustomerNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { CustomerBottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.CustomerCatalog.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.CustomerCatalog.route) {
                CatalogScreen(
                    navController = navController
                )
            }
            
            composable(
                Screen.CustomerProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductDetailScreen(
                    navController = navController,
                    productId = productId
                )
            }
            
            composable(Screen.CustomerCart.route) {
                CartScreen(
                    navController = navController
                )
            }
            
            composable(Screen.CustomerCheckout.route) {
                CheckoutScreen(
                    navController = navController
                )
            }
            
            composable(Screen.CustomerOrders.route) {
                OrdersScreen(
                    navController = navController
                )
            }
            
            composable(
                Screen.CustomerOrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                OrderDetailScreen(
                    navController = navController,
                    orderId = orderId
                )
            }
            
            composable(Screen.CustomerFavorites.route) {
                FavoritesScreen(
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun CustomerBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Показываем нижнюю навигацию только на основных экранах
    val mainScreens = listOf(
        Screen.CustomerCatalog.route,
        Screen.CustomerCart.route,
        Screen.CustomerOrders.route,
        Screen.CustomerFavorites.route
    )
    
    if (currentRoute in mainScreens) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Каталог") },
                label = { Text("Каталог") },
                selected = currentRoute == Screen.CustomerCatalog.route,
                onClick = {
                    navController.navigate(Screen.CustomerCatalog.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Корзина") },
                label = { Text("Корзина") },
                selected = currentRoute == Screen.CustomerCart.route,
                onClick = {
                    navController.navigate(Screen.CustomerCart.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Outlined.Receipt, contentDescription = "Заказы") },
                label = { Text("Заказы") },
                selected = currentRoute == Screen.CustomerOrders.route,
                onClick = {
                    navController.navigate(Screen.CustomerOrders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Избранное") },
                label = { Text("Избранное") },
                selected = currentRoute == Screen.CustomerFavorites.route,
                onClick = {
                    navController.navigate(Screen.CustomerFavorites.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
