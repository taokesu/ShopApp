package com.shopapp

import android.app.Application
import com.shopapp.data.util.DataPreloader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShopApplication : Application() {
    
    @Inject
    lateinit var dataPreloader: DataPreloader
    
    override fun onCreate() {
        super.onCreate()
        
        // Загружаем тестовые данные при первом запуске
        dataPreloader.preloadData()
    }
}
