# 🍎 Prompt pour Agent AI - Implémentation iOS Swift

## 📋 CONTEXTE DU PROJET

Tu es un expert développeur iOS Swift chargé d'implémenter une **feature de prédiction AI du temps de traitement** pour une application de gestion de demandes de documents universitaires (ESPRIT - École d'ingénieurs en Tunisie).

Cette feature existe déjà en **Android Kotlin** et utilise **Google Gemini AI** pour fournir des estimations intelligentes. Je vais te fournir toutes les spécifications pour que tu puisses créer l'équivalent iOS natif.

---

## 🎯 OBJECTIF PRINCIPAL

Implémenter un **service de prédiction AI** qui :
- Utilise **Gemini AI API** (Google Generative AI)
- Fournit des **estimations intelligentes** du temps de traitement des demandes
- Affiche des **explications contextuelles** et des **conseils personnalisés**
- Présente une **UI SwiftUI moderne et interactive**
- **IMPORTANT** : Pas de système de fallback - l'AI doit toujours être utilisée

---

## 📱 SPÉCIFICATIONS TECHNIQUES

### Architecture iOS à Implémenter

```
┌─────────────────────────────────────────┐
│    DocumentRequestView (SwiftUI)        │
│         Interface Utilisateur           │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│   DocumentRequestViewModel              │
│   (ObservableObject / @Published)       │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│      GeminiPredictionService            │
│    (Service AI pur - no fallback)       │
└──────────────────┬──────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────┐
│        Google Gemini API                │
│    (via GoogleGenerativeAI SDK)         │
└─────────────────────────────────────────┘
```

---

## 🔧 IMPLÉMENTATION REQUISE

### 1️⃣ Configuration et Dépendances

#### **A. Ajouter le SDK Gemini**

**Utilise Swift Package Manager (SPM)** :
```
Package URL: https://github.com/google/generative-ai-swift
Version: 0.5.0 ou plus récent
```

**Ou avec CocoaPods** :
```ruby
pod 'GoogleGenerativeAI', '~> 0.5.0'
```

#### **B. Configuration de la Clé API**

Crée un fichier `Config.xcconfig` :
```
GEMINI_API_KEY = YOUR_GEMINI_API_KEY_HERE
```

**Important** : Ajoute `Config.xcconfig` au `.gitignore` !

Dans `Info.plist`, ajoute :
```xml
<key>GEMINI_API_KEY</key>
<string>$(GEMINI_API_KEY)</string>
```

---

### 2️⃣ Modèles de Données (Data Models)

#### **A. PredictionResult.swift**

Crée une structure pour stocker les résultats de prédiction :

```swift
struct PredictionResult: Codable, Identifiable {
    let id = UUID()
    let estimatedHours: Double
    let estimatedText: String
    let confidence: ConfidenceLevel
    let explanation: String
    let tip: String
    
    enum ConfidenceLevel: String, Codable {
        case high = "high"
        case medium = "medium"
        case low = "low"
        
        var displayName: String {
            switch self {
            case .high: return "Haute"
            case .medium: return "Moyenne"
            case .low: return "Faible"
            }
        }
        
        var color: Color {
            switch self {
            case .high: return .green
            case .medium: return .blue
            case .low: return .orange
            }
        }
    }
}
```

#### **B. DocumentType.swift**

Enum pour les types de documents :

```swift
enum DocumentType: String, CaseIterable, Identifiable {
    case attestation = "attestation"
    case releveNotes = "relevé"
    case conventionStage = "convention"
    
    var id: String { rawValue }
    
    var displayName: String {
        switch self {
        case .attestation: return "Attestation d'inscription"
        case .releveNotes: return "Relevé de notes"
        case .conventionStage: return "Convention de stage"
        }
    }
}
```

---

### 3️⃣ Service AI Principal

#### **GeminiPredictionService.swift**

**Responsabilités** :
- Construire des prompts contextuels intelligents
- Appeler l'API Gemini
- Parser les réponses JSON
- Gérer les erreurs

**Structure de base** :

```swift
import Foundation
import GoogleGenerativeAI

@MainActor
class GeminiPredictionService: ObservableObject {
    
    private let model: GenerativeModel
    
    init() {
        // Récupérer la clé API depuis Info.plist
        guard let apiKey = Bundle.main.infoDictionary?["GEMINI_API_KEY"] as? String else {
            fatalError("GEMINI_API_KEY not found in Info.plist")
        }
        
        // Configuration du modèle Gemini
        self.model = GenerativeModel(
            name: "gemini-1.5-flash",
            apiKey: apiKey,
            generationConfig: GenerationConfig(
                temperature: 0.7,
                topP: 0.95,
                topK: 40,
                maxOutputTokens: 200
            )
        )
    }
    
    // FONCTION PRINCIPALE DE PRÉDICTION
    func predictProcessingTime(
        for documentType: DocumentType,
        recentRequestsCount: Int = 0
    ) async throws -> PredictionResult {
        
        // 1. Construire le contexte
        let context = buildContext(
            documentType: documentType,
            recentRequestsCount: recentRequestsCount
        )
        
        // 2. Créer le prompt
        let prompt = createPrompt(context: context, documentType: documentType)
        
        // 3. Appeler Gemini
        let response = try await model.generateContent(prompt)
        
        // 4. Parser la réponse
        guard let text = response.text else {
            throw PredictionError.emptyResponse
        }
        
        return try parsePrediction(from: text, documentType: documentType)
    }
    
    // FONCTION DE CONSEIL INTELLIGENT
    func getSmartTip(
        for documentType: DocumentType?
    ) async throws -> SmartTip {
        
        let timeContext = buildTimeContext()
        let prompt = createTipPrompt(
            documentType: documentType,
            timeContext: timeContext
        )
        
        let response = try await model.generateContent(prompt)
        
        guard let text = response.text else {
            throw PredictionError.emptyResponse
        }
        
        return try parseSmartTip(from: text)
    }
    
    // CONSTRUCTION DU CONTEXTE
    private func buildContext(
        documentType: DocumentType,
        recentRequestsCount: Int
    ) -> String {
        let now = Date()
        let formatter = DateFormatter()
        formatter.dateFormat = "dd/MM/yyyy HH:mm"
        formatter.locale = Locale(identifier: "fr_FR")
        
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: now)
        let hour = calendar.component(.hour, from: now)
        
        let weekdayName = formatter.weekdaySymbols[weekday - 1]
        let dateString = formatter.string(from: now)
        
        return """
        Date/Heure : \(dateString) (\(weekdayName) à \(hour)h)
        Type de document demandé : \(documentType.rawValue)
        Demandes en attente dans le système : \(recentRequestsCount)
        Période académique : Janvier 2026 (après vacances de fin d'année)
        """
    }
    
    private func buildTimeContext() -> String {
        let now = Date()
        let calendar = Calendar.current
        let weekday = calendar.component(.weekday, from: now)
        let hour = calendar.component(.hour, from: now)
        
        switch (weekday, hour) {
        case (6, 14...23): // Vendredi après-midi
            return "Vendredi après-midi (avant weekend)"
        case (_, 0..<8):
            return "Tôt le matin (hors heures bureau)"
        case (_, 18...23):
            return "Soirée (hors heures bureau)"
        case (_, 10..<12):
            return "Milieu de matinée (période active)"
        default:
            return "Heures de bureau normales"
        }
    }
    
    // CRÉATION DU PROMPT DE PRÉDICTION
    private func createPrompt(context: String, documentType: DocumentType) -> String {
        return """
        Tu es un assistant intelligent pour ESPRIT (école d'ingénieurs en Tunisie).
        
        Contexte actuel :
        \(context)
        
        Tâche : Estime le TEMPS DE TRAITEMENT pour une demande de "\(documentType.rawValue)".
        
        Réponds UNIQUEMENT au format JSON suivant (sans code markdown, sans ```json) :
        {
            "estimatedHours": <nombre_heures_decimal>,
            "estimatedText": "<texte_duree_lisible>",
            "confidence": "<high|medium|low>",
            "explanation": "<explication_courte_50_mots_max>",
            "tip": "<conseil_personnalise_40_mots_max>"
        }
        
        Exemples de estimatedText : "2-4h", "1-2 jours", "30 min - 1h"
        
        Sois précis et contextuel dans tes estimations.
        """
    }
    
    // CRÉATION DU PROMPT DE CONSEIL
    private func createTipPrompt(
        documentType: DocumentType?,
        timeContext: String
    ) -> String {
        let docTypeText = documentType?.rawValue ?? "non spécifié"
        
        return """
        Tu es un assistant ESPRIT. Donne un conseil intelligent à un étudiant.
        
        Contexte :
        - Type de document : \(docTypeText)
        - Moment : \(timeContext)
        
        Réponds en JSON (sans code markdown, sans ```json) :
        {
            "icon": "<emoji_pertinent>",
            "title": "<titre_court_4_mots_max>",
            "message": "<conseil_personnalise_30_mots_max>"
        }
        """
    }
    
    // PARSING DE LA PRÉDICTION
    private func parsePrediction(
        from text: String,
        documentType: DocumentType
    ) throws -> PredictionResult {
        
        // Nettoyer le texte (enlever markdown si présent)
        let cleanText = text
            .replacingOccurrences(of: "```json", with: "")
            .replacingOccurrences(of: "```", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        
        // Décoder le JSON
        guard let data = cleanText.data(using: .utf8) else {
            throw PredictionError.invalidResponse
        }
        
        let decoder = JSONDecoder()
        
        // Structure temporaire pour le décodage
        struct APIResponse: Codable {
            let estimatedHours: Double
            let estimatedText: String
            let confidence: String
            let explanation: String
            let tip: String
        }
        
        let apiResponse = try decoder.decode(APIResponse.self, from: data)
        
        // Convertir en PredictionResult
        let confidenceLevel = PredictionResult.ConfidenceLevel(
            rawValue: apiResponse.confidence
        ) ?? .medium
        
        return PredictionResult(
            estimatedHours: apiResponse.estimatedHours,
            estimatedText: apiResponse.estimatedText,
            confidence: confidenceLevel,
            explanation: apiResponse.explanation,
            tip: apiResponse.tip
        )
    }
    
    // PARSING DU CONSEIL
    private func parseSmartTip(from text: String) throws -> SmartTip {
        let cleanText = text
            .replacingOccurrences(of: "```json", with: "")
            .replacingOccurrences(of: "```", with: "")
            .trimmingCharacters(in: .whitespacesAndNewlines)
        
        guard let data = cleanText.data(using: .utf8) else {
            throw PredictionError.invalidResponse
        }
        
        let decoder = JSONDecoder()
        return try decoder.decode(SmartTip.self, from: data)
    }
}

// MODÈLE POUR SMART TIP
struct SmartTip: Codable {
    let icon: String
    let title: String
    let message: String
}

// ERREURS PERSONNALISÉES
enum PredictionError: LocalizedError {
    case emptyResponse
    case invalidResponse
    case apiError(String)
    
    var errorDescription: String? {
        switch self {
        case .emptyResponse:
            return "Réponse vide de l'API Gemini"
        case .invalidResponse:
            return "Format de réponse invalide"
        case .apiError(let message):
            return "Erreur API : \(message)"
        }
    }
}
```

---

### 4️⃣ ViewModel

#### **DocumentRequestViewModel.swift**

```swift
import Foundation
import Combine

@MainActor
class DocumentRequestViewModel: ObservableObject {
    
    // Published properties pour SwiftUI
    @Published var selectedDocumentType: DocumentType = .attestation
    @Published var aiPrediction: PredictionResult?
    @Published var smartTip: SmartTip?
    @Published var isLoadingPrediction = false
    @Published var errorMessage: String?
    
    // Services
    private let geminiService: GeminiPredictionService
    
    init(geminiService: GeminiPredictionService = GeminiPredictionService()) {
        self.geminiService = geminiService
        
        // Charger les prédictions initiales
        Task {
            await loadInitialData()
        }
    }
    
    // CHARGEMENT INITIAL
    func loadInitialData() async {
        await loadSmartTip()
        await loadPrediction()
    }
    
    // CHARGER LA PRÉDICTION AI
    func loadPrediction() async {
        isLoadingPrediction = true
        errorMessage = nil
        
        do {
            let prediction = try await geminiService.predictProcessingTime(
                for: selectedDocumentType,
                recentRequestsCount: 5 // À remplacer par les vraies données
            )
            self.aiPrediction = prediction
        } catch {
            self.errorMessage = error.localizedDescription
        }
        
        isLoadingPrediction = false
    }
    
    // CHARGER LE CONSEIL
    func loadSmartTip() async {
        do {
            let tip = try await geminiService.getSmartTip(
                for: selectedDocumentType
            )
            self.smartTip = tip
        } catch {
            print("Erreur chargement conseil: \(error)")
        }
    }
    
    // CHANGEMENT DE TYPE DE DOCUMENT
    func selectDocumentType(_ type: DocumentType) {
        selectedDocumentType = type
        
        // Recharger les prédictions
        Task {
            await loadPrediction()
        }
    }
}
```

---

### 5️⃣ Interface Utilisateur SwiftUI

#### **A. AIPredictionCard.swift**

Composant réutilisable pour afficher la prédiction :

```swift
import SwiftUI

struct AIPredictionCard: View {
    let prediction: PredictionResult
    @State private var isExpanded = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // HEADER
            Button(action: { withAnimation { isExpanded.toggle() } }) {
                HStack(spacing: 12) {
                    // Badge IA
                    HStack(spacing: 4) {
                        Image(systemName: "brain.head.profile")
                            .font(.caption)
                        Text("IA")
                            .font(.caption)
                            .fontWeight(.bold)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(Color.blue.opacity(0.2))
                    .foregroundColor(.blue)
                    .cornerRadius(6)
                    
                    // Titre et estimation
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Estimation intelligente")
                            .font(.subheadline)
                            .fontWeight(.bold)
                        
                        Text(prediction.estimatedText)
                            .font(.title3)
                            .fontWeight(.semibold)
                            .foregroundColor(.blue)
                    }
                    
                    Spacer()
                    
                    // Badge de confiance
                    Text(prediction.confidence.displayName)
                        .font(.caption)
                        .fontWeight(.medium)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(prediction.confidence.color.opacity(0.15))
                        .foregroundColor(prediction.confidence.color)
                        .cornerRadius(6)
                    
                    // Icône expand
                    Image(systemName: isExpanded ? "chevron.up" : "chevron.down")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
            }
            .buttonStyle(PlainButtonStyle())
            
            // CONTENU DÉTAILLÉ (expandable)
            if isExpanded {
                VStack(alignment: .leading, spacing: 12) {
                    Divider()
                        .padding(.horizontal)
                    
                    // Explication
                    HStack(alignment: .top, spacing: 10) {
                        Image(systemName: "clock")
                            .foregroundColor(.blue)
                        
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Pourquoi ce délai ?")
                                .font(.subheadline)
                                .fontWeight(.bold)
                            
                            Text(prediction.explanation)
                                .font(.caption)
                                .foregroundColor(.secondary)
                        }
                    }
                    .padding(.horizontal)
                    
                    // Conseil
                    if !prediction.tip.isEmpty {
                        HStack(alignment: .top, spacing: 10) {
                            Image(systemName: "lightbulb")
                                .foregroundColor(.orange)
                            
                            VStack(alignment: .leading, spacing: 4) {
                                Text("💡 Conseil")
                                    .font(.subheadline)
                                    .fontWeight(.bold)
                                
                                Text(prediction.tip)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                        }
                        .padding()
                        .background(Color.orange.opacity(0.1))
                        .cornerRadius(12)
                        .padding(.horizontal)
                    }
                }
                .padding(.bottom)
                .transition(.opacity.combined(with: .scale))
            }
        }
        .background(
            LinearGradient(
                colors: [
                    Color.blue.opacity(0.1),
                    Color.purple.opacity(0.05)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
        )
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: 4)
    }
}
```

#### **B. SmartTipCard.swift**

```swift
import SwiftUI

struct SmartTipCard: View {
    let tip: SmartTip
    
    var body: some View {
        HStack(spacing: 12) {
            Text(tip.icon)
                .font(.title2)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(tip.title)
                    .font(.subheadline)
                    .fontWeight(.bold)
                
                Text(tip.message)
                    .font(.caption)
                    .foregroundColor(.secondary)
            }
            
            Spacer()
        }
        .padding()
        .background(Color.cyan.opacity(0.1))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 4, x: 0, y: 2)
    }
}
```

#### **C. DocumentRequestView.swift**

Vue principale intégrant tout :

```swift
import SwiftUI

struct DocumentRequestView: View {
    @StateObject private var viewModel = DocumentRequestViewModel()
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 16) {
                    // Smart Tip
                    if let tip = viewModel.smartTip {
                        SmartTipCard(tip: tip)
                    }
                    
                    // AI Prediction Card
                    if let prediction = viewModel.aiPrediction {
                        AIPredictionCard(prediction: prediction)
                    } else if viewModel.isLoadingPrediction {
                        ProgressView("Analyse en cours...")
                            .padding()
                    } else if let error = viewModel.errorMessage {
                        ErrorView(message: error) {
                            Task {
                                await viewModel.loadPrediction()
                            }
                        }
                    }
                    
                    // Type de document
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Type de document")
                            .font(.subheadline)
                            .fontWeight(.medium)
                        
                        Picker("Type", selection: $viewModel.selectedDocumentType) {
                            ForEach(DocumentType.allCases) { type in
                                Text(type.displayName).tag(type)
                            }
                        }
                        .pickerStyle(MenuPickerStyle())
                        .onChange(of: viewModel.selectedDocumentType) { newType in
                            viewModel.selectDocumentType(newType)
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(color: .black.opacity(0.05), radius: 4)
                    
                    // ... Autres champs du formulaire
                    
                    // Bouton de soumission
                    Button(action: submitRequest) {
                        Text("Créer la demande")
                            .fontWeight(.bold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(12)
                    }
                }
                .padding()
            }
            .navigationTitle("Nouvelle demande")
            .navigationBarTitleDisplayMode(.large)
        }
    }
    
    private func submitRequest() {
        // Logique de soumission
    }
}

// Vue d'erreur
struct ErrorView: View {
    let message: String
    let retry: () -> Void
    
    var body: some View {
        VStack(spacing: 12) {
            Image(systemName: "exclamationmark.triangle")
                .font(.largeTitle)
                .foregroundColor(.orange)
            
            Text("Une erreur est survenue")
                .font(.headline)
            
            Text(message)
                .font(.caption)
                .foregroundColor(.secondary)
                .multilineTextAlignment(.center)
            
            Button("Réessayer", action: retry)
                .font(.subheadline)
                .fontWeight(.medium)
        }
        .padding()
        .background(Color.orange.opacity(0.1))
        .cornerRadius(12)
    }
}
```

---

## ✅ CHECKLIST D'IMPLÉMENTATION

Assure-toi de :

- [ ] **SDK Gemini installé** via SPM ou CocoaPods
- [ ] **Clé API configurée** dans `Config.xcconfig` et `Info.plist`
- [ ] **Config.xcconfig ajouté au .gitignore**
- [ ] **PredictionResult.swift** créé avec tous les modèles
- [ ] **GeminiPredictionService.swift** implémenté
- [ ] **DocumentRequestViewModel.swift** créé avec @Published properties
- [ ] **AIPredictionCard.swift** créé (UI SwiftUI)
- [ ] **SmartTipCard.swift** créé
- [ ] **DocumentRequestView.swift** intégrant tous les composants
- [ ] **Gestion d'erreurs** avec messages clairs
- [ ] **Loading states** pour l'UX
- [ ] **Tests** sur simulateur et device réel

---

## 🚫 CE QU'IL NE FAUT PAS FAIRE

❌ **PAS de système de fallback** - L'AI doit toujours être utilisée  
❌ **PAS de prédictions heuristiques statiques**  
❌ **PAS de valeurs hardcodées** comme estimations par défaut  
❌ Si l'API échoue → **afficher une erreur** avec option de retry  

---

## 🎨 DESIGN GUIDELINES

- **Utilise SwiftUI** (pas UIKit)
- **Couleurs** : Principalement bleu/violet (Material Design inspired)
- **Animations** : Smooth et subtiles (spring animations)
- **Coins arrondis** : 12-16pt
- **Shadows** : Légères (opacity 0.05-0.1)
- **Typography** : SF Pro (système) avec bold pour les titres
- **Spacing** : 12-16pt entre les éléments

---

## 📝 NOTES IMPORTANTES

1. **Async/Await** : Utilise les nouvelles APIs concurrency Swift
2. **@MainActor** : Pour les ViewModels qui modifient l'UI
3. **Error Handling** : Toujours avec do-catch et messages clairs
4. **Performance** : Cache les résultats si possible
5. **Tests** : Teste avec différents types de documents

---

## 🎯 RÉSULTAT ATTENDU

À la fin, l'utilisateur iOS devrait voir :

1. **Card Smart Tip** avec emoji et conseil contextuel
2. **Card AI Prediction** avec :
   - Badge "IA"
   - Estimation (ex: "2-4 heures")
   - Badge de confiance coloré
   - Section expandable avec explications
   - Conseil personnalisé
3. **Changement dynamique** lors de la sélection d'un autre type de document
4. **Loading indicator** pendant le chargement
5. **Message d'erreur** si l'API échoue (avec bouton retry)

---

## 📚 RESSOURCES

- [GoogleGenerativeAI Swift SDK](https://github.com/google/generative-ai-swift)
- [Documentation Gemini](https://ai.google.dev/tutorials/swift_quickstart)
- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)

---

**Bonne implémentation ! 🚀**
