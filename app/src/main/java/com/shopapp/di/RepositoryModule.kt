package com.shopapp.di

import com.shopapp.data.repository.CartRepository
import com.shopapp.data.repository.CartRepositoryImpl
import com.shopapp.data.repository.FavoriteRepository
import com.shopapp.data.repository.FavoriteRepositoryImpl
import com.shopapp.data.repository.OrderRepository
import com.shopapp.data.repository.OrderRepositoryImpl
import com.shopapp.data.repository.ProductRepository
import com.shopapp.data.repository.ProductRepositoryImpl
import com.shopapp.data.repository.UserRepository
import com.shopapp.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindOrderRepository(
        orderRepositoryImpl: OrderRepositoryImpl
    ): OrderRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        cartRepositoryImpl: CartRepositoryImpl
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        favoriteRepositoryImpl: FavoriteRepositoryImpl
    ): FavoriteRepository
}
