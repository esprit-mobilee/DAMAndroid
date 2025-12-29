<<<<<<< HEAD
package com.example.esprit.repository

import com.example.esprit.util.DataStoreManager
import com.example.esprit.util.Resource

class FavoriteRepository(
    private val dataStoreManager: DataStoreManager
) {
    
    // Récupérer tous les IDs des favoris
    suspend fun getFavorites(): Resource<Set<String>> {
        return try {
            val favorites = dataStoreManager.getFavorites()
            Resource.Success(favorites)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des favoris")
        }
    }
    
    // Ajouter un favori
    suspend fun addFavorite(internshipId: String): Resource<Unit> {
        return try {
            dataStoreManager.addFavorite(internshipId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de l'ajout aux favoris")
        }
    }
    
    // Supprimer un favori
    suspend fun removeFavorite(internshipId: String): Resource<Unit> {
        return try {
            dataStoreManager.removeFavorite(internshipId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la suppression des favoris")
        }
    }
    
    // Vérifier si un stage est en favori
    suspend fun isFavorite(internshipId: String): Resource<Boolean> {
        return try {
            val isFav = dataStoreManager.isFavorite(internshipId)
            Resource.Success(isFav)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la vérification")
        }
    }
}

=======
package com.example.esprit.repository

import com.example.esprit.util.DataStoreManager
import com.example.esprit.util.Resource

class FavoriteRepository(
    private val dataStoreManager: DataStoreManager
) {
    
    // Récupérer tous les IDs des favoris
    suspend fun getFavorites(): Resource<Set<String>> {
        return try {
            val favorites = dataStoreManager.getFavorites()
            Resource.Success(favorites)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors du chargement des favoris")
        }
    }
    
    // Ajouter un favori
    suspend fun addFavorite(internshipId: String): Resource<Unit> {
        return try {
            dataStoreManager.addFavorite(internshipId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de l'ajout aux favoris")
        }
    }
    
    // Supprimer un favori
    suspend fun removeFavorite(internshipId: String): Resource<Unit> {
        return try {
            dataStoreManager.removeFavorite(internshipId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la suppression des favoris")
        }
    }
    
    // Vérifier si un stage est en favori
    suspend fun isFavorite(internshipId: String): Resource<Boolean> {
        return try {
            val isFav = dataStoreManager.isFavorite(internshipId)
            Resource.Success(isFav)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Erreur lors de la vérification")
        }
    }
}

>>>>>>> origin/messaging-announcement
