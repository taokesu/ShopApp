package com.shopapp.data.util

import android.util.Log
import com.shopapp.data.local.ShopDatabase
import com.shopapp.data.model.User
import com.shopapp.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Класс для предварительной загрузки тестовых данных в базу данных.
 * Используется для демонстрации функциональности приложения и тестирования.
 */
@Singleton
class DataPreloader @Inject constructor(
    private val database: ShopDatabase
) {
    /**
     * Проверяет наличие предустановленных данных и загружает их, если необходимо.
     * Должен вызываться при инициализации приложения.
     */
    fun preloadData() {
        // Используем runBlocking для синхронного выполнения
        runBlocking(Dispatchers.IO) {
            try {
                Log.d("DataPreloader", "Начало загрузки тестовых данных")
                val userDao = database.userDao()
                
                // Проверяем, есть ли уже пользователи в базе данных
                val userCount = userDao.getUserCount()
                Log.d("DataPreloader", "Количество пользователей в базе: $userCount")
                
                // Проверяем, есть ли менеджер с логином "manager"
                val existingManager = userDao.getUserByUsername("manager")
                Log.d("DataPreloader", "Проверка наличия менеджера: ${existingManager != null}")
                
                // Если менеджера нет, добавляем его
                if (existingManager == null) {
                    Log.d("DataPreloader", "Добавляем тестовых пользователей")
                    
                    // Добавляем тестового менеджера
                    val manager = User(
                        username = "manager",
                        password = "manager123",
                        email = "manager@shopapp.com",
                        fullName = "Тестовый Менеджер",
                        phone = "+7 (999) 123-45-67",
                        address = "г. Москва, ул. Примерная, д. 1",
                        role = UserRole.MANAGER
                    )
                    val managerId = userDao.insertUser(manager)
                    Log.d("DataPreloader", "Менеджер добавлен с ID: $managerId")
                    
                    // Проверяем, что менеджер был добавлен успешно
                    val managerUser = userDao.login("manager", "manager123")
                    Log.d("DataPreloader", "Проверка менеджера: ${managerUser != null}, роль: ${managerUser?.role}")
                } else {
                    Log.d("DataPreloader", "Менеджер уже существует в системе")
                    
                    // Проверяем данные менеджера для отладки
                    val managerLoginCheck = userDao.login("manager", "manager123")
                    Log.d("DataPreloader", "Проверка входа менеджера: ${managerLoginCheck != null}, роль: ${managerLoginCheck?.role}")
                }
                
                // Проверяем, есть ли тестовый покупатель
                val existingCustomer = userDao.getUserByUsername("customer")
                Log.d("DataPreloader", "Проверка наличия покупателя: ${existingCustomer != null}")
                
                // Если покупателя нет, добавляем его
                if (existingCustomer == null) {
                    Log.d("DataPreloader", "Добавляем тестового покупателя")
                    
                    // Добавляем тестового покупателя
                    val customer = User(
                        username = "customer",
                        password = "customer123",
                        email = "customer@shopapp.com",
                        fullName = "Тестовый Покупатель",
                        phone = "+7 (999) 765-43-21",
                        address = "г. Санкт-Петербург, ул. Тестовая, д. 2",
                        role = UserRole.CUSTOMER
                    )
                    val customerId = userDao.insertUser(customer)
                    Log.d("DataPreloader", "Покупатель добавлен с ID: $customerId")
                } else {
                    Log.d("DataPreloader", "Покупатель уже существует в системе")
                }
            } catch (e: Exception) {
                Log.e("DataPreloader", "Ошибка при загрузке тестовых данных", e)
            }
        }
    }
}
