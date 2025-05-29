package com.shopapp.presentation.manager.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.manager.screen.ManagerAnalyticsScreen
import com.shopapp.presentation.manager.screen.ManagerInventoryScreen
import com.shopapp.presentation.manager.screen.ManagerOrderDetailsScreen
import com.shopapp.presentation.manager.screen.ManagerOrdersScreen
import com.shopapp.presentation.manager.screen.ManagerProfileScreen
import com.shopapp.presentation.manager.screen.ProductEditScreen
import com.shopapp.presentation.manager.screen.StatisticsExportScreen
import com.shopapp.presentation.common.navigation.LogoutCallback

@Composable
fun ManagerNavigation(logoutCallback: LogoutCallback? = null) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { ManagerBottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.ManagerOrders.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.ManagerOrders.route) {
                ManagerOrdersScreen(
                    navigateToOrderDetails = { orderId ->
                        navController.navigate(Screen.ManagerOrderDetail.createRoute(orderId))
                    }
                )
            }
            
            composable(
                Screen.ManagerOrderDetail.route,
                arguments = listOf(navArgument("orderId") { type = NavType.LongType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                ManagerOrderDetailsScreen(
                    orderId = orderId,
                    navigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ManagerInventory.route) {
                ManagerInventoryScreen(
                    navigateToAddProduct = {
                        navController.navigate(Screen.ManagerProductDetail.createRoute(0L))
                    },
                    navigateToEditProduct = { productId ->
                        navController.navigate(Screen.ManagerProductDetail.createRoute(productId))
                    },
                    navController = navController,
                    logoutCallback = logoutCallback
                )
            }
            
            composable(
                Screen.ManagerProductDetail.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
                ProductEditScreen(
                    productId = if (productId == 0L) null else productId,
                    navigateBack = { navController.popBackStack() }
                )
            }
            
            composable(Screen.ManagerAnalytics.route) {
                ManagerAnalyticsScreen()
            }
            
            composable(Screen.ManagerStatisticsExport.route) {
                StatisticsExportScreen(
                    navController = navController
                )
            }
            
            composable(Screen.ManagerProfile.route) {
                ManagerProfileScreen(
                    navController = navController,
                    logoutCallback = logoutCallback
                )
            }
        }
    }
}

@Composable
fun ManagerBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Показываем нижнюю навигацию только на основных экранах
    val mainScreens = listOf(
        Screen.ManagerOrders.route,
        Screen.ManagerInventory.route,
        Screen.ManagerAnalytics.route,
        Screen.ManagerStatisticsExport.route,
        Screen.ManagerProfile.route
    )
    
    if (currentRoute in mainScreens) {
        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Заказы") },
                label = { Text(
                    text = "Заказы",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                ) },
                selected = currentRoute == Screen.ManagerOrders.route,
                onClick = {
                    navController.navigate(Screen.ManagerOrders.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.Inventory, contentDescription = "Инвентарь") },
                label = { Text(
                    text = "Инвентарь",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                ) },
                selected = currentRoute == Screen.ManagerInventory.route,
                onClick = {
                    navController.navigate(Screen.ManagerInventory.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.Analytics, contentDescription = "Аналитика") },
                label = { Text(
                    text = "Аналитика",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                ) },
                selected = currentRoute == Screen.ManagerAnalytics.route,
                onClick = {
                    navController.navigate(Screen.ManagerAnalytics.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.FileDownload, contentDescription = "Экспорт") },
                label = { Text(
                    text = "Экспорт",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                ) },
                selected = currentRoute == Screen.ManagerStatisticsExport.route,
                onClick = {
                    navController.navigate(Screen.ManagerStatisticsExport.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                label = { Text(
                    text = "Профиль",
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                ) },
                selected = currentRoute == Screen.ManagerProfile.route,
                onClick = {
                    navController.navigate(Screen.ManagerProfile.route) {
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
