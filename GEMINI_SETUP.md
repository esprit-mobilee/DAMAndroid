# Configuration de l'API Gemini pour les Prédictions AI

## 🔑 Obtenir votre clé API Gemini (GRATUIT)

1. **Visitez Google AI Studio** :
   - Allez sur : https://makersuite.google.com/app/apikey
   - Connectez-vous avec votre compte Google

2. **Créez une clé API** :
   - Cliquez sur "Get API Key" ou "Create API Key"
   - Sélectionnez "Create API key in new project" (ou utilisez un projet existant)
   - Copiez la clé générée (format: `AIzaSy...`)

3. **Configurez votre projet Android** :
   - Ouvrez le fichier `local.properties` à la racine du projet
   - Ajoutez cette ligne :
     ```
     GEMINI_API_KEY=VOTRE_CLE_API_ICI
     ```
   - Exemple :
     ```
     GEMINI_API_KEY=AIzaSyDxxxxxxxxxxxxxxxxxxxxxxxxxxx
     OPENAI_API_KEY=sk-proj-xxxxxxxxxxxx
     ```

## ✅ Vérification

Après avoir ajouté la clé API :

1. **Synchronisez le projet** : `File > Sync Project with Gradle Files`
2. **Recompilez** : `Build > Clean Project` puis `Build > Rebuild Project`
3. **Lancez l'application** et testez la création d'une demande

## 🎯 Fonctionnalités activées

Avec Gemini AI configuré, vous bénéficiez de :

- ✨ **Prédictions intelligentes** du temps de traitement
- 📊 **Explications contextuelles** des délais estimés
- 💡 **Conseils personnalisés** basés sur l'historique
- 🎯 **Niveau de confiance** des prédictions
- 🧠 **Analyse en temps réel** de la charge du système

## 🔒 Sécurité

⚠️ **Important** : Ne partagez JAMAIS votre clé API publiquement !

- ✅ Le fichier `local.properties` est dans `.gitignore` (sécurisé)
- ✅ La clé n'apparaît jamais dans le code source versionné
- ✅ Utilisez uniquement cette clé pour le développement local

## 🆓 Quotas gratuits

Gemini offre un quota généreux GRATUIT :
- **60 requêtes par minute**
- **1,500 requêtes par jour**
- Parfait pour le développement et les tests !

## 🐛 Dépannage

### Erreur "API key not valid"
- Vérifiez que vous avez copié la clé complète
- Assurez-vous qu'il n'y a pas d'espaces avant/après

### Les prédictions ne s'affichent pas
- Vérifiez votre connexion Internet
- Consultez les logs Android (`Logcat`) pour plus de détails
- L'app utilise automatiquement un fallback heuristique si l'API échoue

## 📞 Support

Si vous rencontrez des problèmes :
1. Vérifiez les logs dans Android Studio
2. Testez votre clé API sur : https://makersuite.google.com/
3. Assurez-vous que l'API Gemini est activée pour votre projet

---

**Bon développement avec l'AI ! 🚀**
