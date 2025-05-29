package com.shopapp.presentation.common.navigation

sealed class Screen(val route: String) {
    // Экраны выбора роли и авторизации
    object RoleSelection : Screen("role_selection")
    object CustomerLogin : Screen("customer_login")
    object CustomerRegistration : Screen("customer_registration")
    object ManagerLogin : Screen("manager_login")
    
    // Экраны покупателя
    object CustomerCatalog : Screen("customer_catalog")
    object CustomerProductDetail : Screen("customer_product_detail/{productId}") {
        fun createRoute(productId: Long) = "customer_product_detail/$productId"
    }
    object CustomerCart : Screen("customer_cart")
    object CustomerCheckout : Screen("customer_checkout")
    object CustomerOrders : Screen("customer_orders")
    object CustomerOrderDetail : Screen("customer_order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "customer_order_detail/$orderId"
    }
    object CustomerFavorites : Screen("customer_favorites")
    object CustomerProfile : Screen("customer_profile")
    
    // Экраны менеджера
    object ManagerOrders : Screen("manager_orders")
    object ManagerOrderDetail : Screen("manager_order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "manager_order_detail/$orderId"
    }
    object ManagerInventory : Screen("manager_inventory")
    object ManagerProductDetail : Screen("manager_product_detail/{productId}") {
        fun createRoute(productId: Long) = "manager_product_detail/$productId"
    }
    object ManagerAnalytics : Screen("manager_analytics") // Для аналитики продаж
    object ManagerStatisticsExport : Screen("manager_statistics_export") // Для экспорта статистики в Excel
    object ManagerProfile : Screen("manager_profile")
}
