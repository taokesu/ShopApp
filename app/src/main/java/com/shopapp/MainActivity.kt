package com.shopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge -- недоступно в текущей версии
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shopapp.presentation.auth.screen.CustomerLoginScreen
import com.shopapp.presentation.auth.screen.CustomerRegistrationScreen
import com.shopapp.presentation.auth.screen.ManagerLoginScreen
import com.shopapp.presentation.auth.screen.RoleSelectionScreen
import com.shopapp.presentation.common.navigation.LogoutCallback
import com.shopapp.presentation.common.navigation.Screen
import com.shopapp.presentation.customer.navigation.CustomerNavigation
import com.shopapp.presentation.manager.navigation.ManagerNavigation
import com.shopapp.ui.theme.ShopAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge() -- недоступно в текущей версии
        setContent {
            ShopAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        // Экраны авторизации
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(navController)
        }
        
        composable(Screen.CustomerLogin.route) {
            CustomerLoginScreen(
                navController = navController
            )
        }
        
        composable(Screen.CustomerRegistration.route) {
            CustomerRegistrationScreen(
                navController = navController
            )
        }
        
        composable(Screen.ManagerLogin.route) {
            ManagerLoginScreen(
                navController = navController
            )
        }
        
        // Экраны менеджера
        composable(Screen.ManagerOrders.route) {
            val logoutCallback = object : LogoutCallback {
                override fun onLogout() {
                    // Очищаем стек навигации и переходим к экрану выбора роли
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            ManagerNavigation(logoutCallback)
        }
        
        // Экраны покупателя
        composable(Screen.CustomerCatalog.route) {
            val logoutCallback = object : LogoutCallback {
                override fun onLogout() {
                    // Очищаем стек навигации и переходим к экрану выбора роли
                    navController.navigate(Screen.RoleSelection.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            CustomerNavigation(logoutCallback)
        }
    }
}
