package com.example.esprit.di



import com.example.esprit.network.ApiService
import com.example.esprit.repository.AnnouncementRepository
import com.example.esprit.repository.MessageRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideAnnouncementRepository(api: ApiService): AnnouncementRepository {
        return AnnouncementRepository(api)
    }

    @Provides
    fun provideMessageRepository(api: ApiService): MessageRepository {
        return MessageRepository(api)
    }
}
