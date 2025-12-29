<<<<<<< HEAD
package com.example.esprit.di


import android.content.Context
import com.example.esprit.util.DataStoreManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context) = DataStoreManager(ctx)
}
=======
package com.example.esprit.di


import android.content.Context
import com.example.esprit.util.DataStoreManager

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context) = DataStoreManager(ctx)
}
>>>>>>> origin/messaging-announcement
