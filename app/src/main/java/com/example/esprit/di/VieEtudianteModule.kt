package com.example.esprit.di


import com.example.esprit.network.ApiService
import com.example.esprit.repository.ClubRepository
import com.example.esprit.repository.EventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module spécifique à la feature "Vie Étudiante"
 * On y fournit les repositories qui s'appuient sur ApiService.
 *
 * Les ViewModels (EventsViewModel, ClubsViewModel) sont déjà annotés @HiltViewModel,
 * donc Hilt saura les créer tant que leurs dépendances (les repositories) sont fournies ici.
 */
@Module
@InstallIn(SingletonComponent::class)
object VieEtudianteModule {

    @Provides
    @Singleton
    fun provideEventRepository(
        apiService: ApiService
    ): EventRepository = EventRepository(apiService)

    @Provides
    @Singleton
    fun provideClubRepository(
        apiService: ApiService
    ): ClubRepository = ClubRepository(apiService)
}
