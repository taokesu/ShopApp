package com.shopapp.di

import android.content.Context
import com.shopapp.data.local.CartDao
import com.shopapp.data.local.FavoriteDao
import com.shopapp.data.local.OrderDao
import com.shopapp.data.local.OrderItemDao
import com.shopapp.data.local.ProductDao
import com.shopapp.data.local.ShopDatabase
import com.shopapp.data.local.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): ShopDatabase {
        return ShopDatabase.getDatabase(context)
    }

    @Provides
    fun provideUserDao(database: ShopDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideProductDao(database: ShopDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideOrderDao(database: ShopDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideOrderItemDao(database: ShopDatabase): OrderItemDao {
        return database.orderItemDao()
    }

    @Provides
    fun provideCartDao(database: ShopDatabase): CartDao {
        return database.cartDao()
    }

    @Provides
    fun provideFavoriteDao(database: ShopDatabase): FavoriteDao {
        return database.favoriteDao()
    }
}
