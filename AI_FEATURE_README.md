# 🤖 Feature AI : Prédiction Intelligente de Temps de Traitement

## 📋 Vue d'ensemble

Cette feature utilise **Google Gemini AI** pour fournir des estimations intelligentes du temps de traitement des demandes de documents, avec des explications contextuelles et des conseils personnalisés.

## ✨ Fonctionnalités

### 1. **Prédictions AI en Temps Réel**
- Analyse du type de document demandé
- Prise en compte de la charge système actuelle
- Considération du moment de la demande (jour, heure)
- Analyse de l'historique des demandes similaires

### 2. **Explications Détaillées**
- Raisons du délai estimé
- Facteurs impactant le traitement
- Niveau de confiance (Haute, Moyenne, Faible)

### 3. **Conseils Personnalisés**
- Suggestions pour accélérer le traitement
- Meilleur moment pour soumettre une demande
- Alertes sur les périodes de forte charge

### 4. **Interface Interactive**
- Card expandable avec détails
- Badge de confiance coloré
- Icônes et design moderne
- Animation fluide

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│      DocumentRequestViewModel           │
│  (Gestion de l'état et logique métier)  │
└──────────────────┬──────────────────────┘
                   │
                   ├─► SmartPredictionService
                   │   (Service unifié avec fallback)
                   │
                   └─► GeminiPredictionService
                       (Service AI spécialisé)
                       │
                       └─► Gemini API
                           (Google Generative AI)
```

### Composants Clés

#### 1. **GeminiPredictionService.kt**
Service principal d'intelligence artificielle :
- Construit des prompts contextuels intelligents
- Appelle l'API Gemini pour les prédictions
- Parse les réponses JSON de l'AI
- Gère les erreurs avec fallback

#### 2. **SmartPredictionService.kt** (Amélioré)
Service de prédiction avec double stratégie :
- **Priorité AI** : Utilise Gemini si disponible
- **Fallback heuristique** : Règles statiques si l'AI échoue
- Interface unifiée pour le ViewModel

#### 3. **AIPredictionCard.kt**
Composant UI riche et interactif :
- Affichage de l'estimation
- Badge de confiance
- Section expandable avec explications
- Conseils personnalisés
- Design moderne et animé

#### 4. **DocumentRequestViewModel.kt** (Mis à jour)
- Nouveau state : `aiPrediction: PredictionResult?`
- Chargement asynchrone des prédictions
- Mise à jour lors du changement de type

## 🎨 Expérience Utilisateur

### Vue Condensée
```
┌─────────────────────────────────────────┐
│ 🧠 IA | Estimation intelligente    Haute│
│      2-4 heures                       ▼ │
└─────────────────────────────────────────┘
```

### Vue Expandée
```
┌─────────────────────────────────────────┐
│ 🧠 IA | Estimation intelligente    Haute│
│      2-4 heures                       ▲ │
├─────────────────────────────────────────┤
│ 🕒 Pourquoi ce délai ?                  │
│ La période actuelle est peu chargée.    │
│ Les demandes d'attestation sont         │
│ traitées rapidement.                    │
│                                         │
│ ┌─────────────────────────────────────┐ │
│ │ 💡 Conseil                          │ │
│ │ Soumettez maintenant pour un        │ │
│ │ traitement optimal !                │ │
│ └─────────────────────────────────────┘ │
└─────────────────────────────────────────┘
```

## 📊 Format de Prédiction

```kotlin
data class PredictionResult(
    val estimatedHours: Double,      // 2.5
    val estimatedText: String,       // "2-4h"
    val confidence: String,          // "high", "medium", "low"
    val explanation: String,         // Explication du délai
    val tip: String                  // Conseil personnalisé
)
```

## 🔄 Flux de Prédiction

1. **Utilisateur sélectionne un type de document**
2. **ViewModel** appelle `loadAIPrediction(type)`
3. **SmartPredictionService** tente la prédiction AI
4. **GeminiPredictionService** :
   - Construit le contexte (date, heure, charge)
   - Génère un prompt intelligent
   - Appelle Gemini API
   - Parse la réponse JSON
5. **Retour au ViewModel** avec `PredictionResult`
6. **UI mise à jour** : `AIPredictionCard` s'affiche
7. **Si erreur** : Fallback sur heuristiques statiques

## 🎯 Exemples de Prompts AI

### Prompt de Prédiction
```
Tu es un assistant intelligent pour ESPRIT.

Contexte actuel :
- Date/Heure : 04/01/2026 14:30 (SATURDAY à 14h)
- Type de document : attestation
- Demandes en attente : 5
- Temps moyen récent : 2-3 jours

Tâche : Estime le TEMPS DE TRAITEMENT pour "attestation".

Réponds en JSON :
{
  "estimatedHours": 2.5,
  "estimatedText": "2-4h",
  "confidence": "high",
  "explanation": "...",
  "tip": "..."
}
```

## 🧪 Tests et Validation

### Scénarios de Test

1. **Test basique** : Sélection de type → Prédiction affichée
2. **Test de fallback** : Clé API invalide → Heuristiques utilisées
3. **Test de charge** : Multiples requêtes → Performances OK
4. **Test UI** : Expand/Collapse → Animations fluides

### Validation de la Qualité AI

- ✅ Cohérence des estimations
- ✅ Pertinence des explications
- ✅ Utilité des conseils
- ✅ Confiance appropriée

## 🚀 Évolutions Futures

### Phase 1 (Actuel) ✅
- Prédictions par type de document
- Contexte temporel basique
- UI interactive

### Phase 2 (À venir)
- [ ] Analyse de l'historique utilisateur réel
- [ ] Prédictions basées sur le profil étudiant
- [ ] Intégration des statistiques serveur
- [ ] Notification proactive de changement de délai

### Phase 3 (Avancé)
- [ ] ML local pour prédictions offline
- [ ] Analyse de sentiment des messages
- [ ] Recommandation de documents connexes
- [ ] Chatbot complet d'assistance

## 📝 Maintenance

### Mise à jour du Modèle
Pour changer de modèle Gemini :
```kotlin
// Dans GeminiPredictionService.kt
modelName = "gemini-1.5-flash"  // ou "gemini-pro"
```

### Ajustement des Prompts
Les prompts sont dans `GeminiPredictionService.kt` :
- `getPredictedProcessingTime()` : Prompt de prédiction
- `getSmartTip()` : Prompt de conseil

### Configuration de Temperature
```kotlin
temperature = 0.7f  // Créativité vs Précision
// 0.0 = très précis, déterministe
// 1.0 = très créatif, varié
```

## 🔒 Sécurité & Coûts

- ✅ Clé API dans `local.properties` (ignorée par Git)
- ✅ Quota gratuit : 60 req/min, 1500 req/jour
- ✅ Fallback automatique si quota dépassé
- ✅ Pas de données sensibles dans les prompts

## 📚 Ressources

- [Documentation Gemini](https://ai.google.dev/docs)
- [Google AI Studio](https://makersuite.google.com/)
- [Kotlin SDK](https://github.com/google/generative-ai-android)

---

**Développé avec ❤️ et 🤖 pour ESPRIT**
