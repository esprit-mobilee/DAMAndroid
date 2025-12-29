<<<<<<< HEAD
# Configuration de la clé API OpenAI

Pour utiliser le chatbot IA, vous devez configurer votre clé API OpenAI.

## Étapes de configuration

1. **Obtenir une clé API OpenAI**
   - Allez sur https://platform.openai.com/api-keys
   - Créez un compte ou connectez-vous
   - Générez une nouvelle clé API

2. **Ajouter la clé dans local.properties**
   - Ouvrez le fichier `local.properties` à la racine du projet
   - Ajoutez cette ligne (remplacez par votre vraie clé):
   ```
   OPENAI_API_KEY=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
   ```

3. **Rebuild le projet**
   - Dans Android Studio: Build > Rebuild Project
   - Ou en ligne de commande: `./gradlew clean build`

## Important

⚠️ **NE JAMAIS commiter le fichier local.properties dans Git!**

Le fichier `local.properties` est déjà dans `.gitignore` pour éviter d'exposer votre clé API.

## Coûts

- L'API OpenAI est payante (facturation par token)
- Le modèle utilisé est `gpt-4o-mini` (moins cher que GPT-4)
- Surveillez votre utilisation sur https://platform.openai.com/usage

## Alternative pour la production

Pour une application en production, il est recommandé de:
1. Créer un backend proxy qui appelle OpenAI
2. Ne jamais exposer la clé API dans l'application mobile
3. Implémenter des limites de taux et de coûts côté serveur
=======
# Configuration de la clé API OpenAI

Pour utiliser le chatbot IA, vous devez configurer votre clé API OpenAI.

## Étapes de configuration

1. **Obtenir une clé API OpenAI**
   - Allez sur https://platform.openai.com/api-keys
   - Créez un compte ou connectez-vous
   - Générez une nouvelle clé API

2. **Ajouter la clé dans local.properties**
   - Ouvrez le fichier `local.properties` à la racine du projet
   - Ajoutez cette ligne (remplacez par votre vraie clé):
   ```
   OPENAI_API_KEY=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
   ```

3. **Rebuild le projet**
   - Dans Android Studio: Build > Rebuild Project
   - Ou en ligne de commande: `./gradlew clean build`

## Important

⚠️ **NE JAMAIS commiter le fichier local.properties dans Git!**

Le fichier `local.properties` est déjà dans `.gitignore` pour éviter d'exposer votre clé API.

## Coûts

- L'API OpenAI est payante (facturation par token)
- Le modèle utilisé est `gpt-4o-mini` (moins cher que GPT-4)
- Surveillez votre utilisation sur https://platform.openai.com/usage

## Alternative pour la production

Pour une application en production, il est recommandé de:
1. Créer un backend proxy qui appelle OpenAI
2. Ne jamais exposer la clé API dans l'application mobile
3. Implémenter des limites de taux et de coûts côté serveur
>>>>>>> origin/messaging-announcement
