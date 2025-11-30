package com.example.esprit.di

import com.example.esprit.network.ApiService
import com.example.esprit.repository.InternshipOfferRepository
import com.example.esprit.repository.ApplicationRepository
import com.example.esprit.repository.FavoriteRepository
import com.example.esprit.util.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InternshipModule {

    @Provides
    @Singleton
    fun provideInternshipOfferRepository(
        apiService: ApiService
    ): InternshipOfferRepository {
        return InternshipOfferRepository(apiService)
    }

    // ----------------------------------------------------------
    // NEW: Provide ApplicationRepository (postuler + admin)
    // ----------------------------------------------------------
    @Provides
    @Singleton
    fun provideApplicationRepository(
        apiService: ApiService
    ): ApplicationRepository {
        return ApplicationRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideFavoriteRepository(
        dataStoreManager: DataStoreManager
    ): FavoriteRepository {
        return FavoriteRepository(dataStoreManager)
    }
}
