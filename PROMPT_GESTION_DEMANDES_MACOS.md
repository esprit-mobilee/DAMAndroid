# PROMPT DÉTAILLÉ : GESTION DES DEMANDES DE DOCUMENTS - macOS/iOS avec Xcode et SwiftUI

## CONTEXTE ET OBJECTIF

Tu dois implémenter un module complet de **gestion des demandes de documents** pour une application macOS/iOS en utilisant **Xcode** et **SwiftUI**. Cette fonctionnalité permet aux utilisateurs de créer, consulter, gérer et supprimer des demandes de documents académiques (attestations, relevés de notes, conventions de stage).

## ARCHITECTURE ET STRUCTURE DU PROJET

### Structure de fichiers recommandée

```
YourApp/
├── Models/
│   ├── DocumentRequest.swift
│   ├── DocumentField.swift
│   ├── DocumentFile.swift
│   └── DocumentRequestPayload.swift
├── Services/
│   ├── DocumentRequestService.swift
│   └── NetworkService.swift (si partagé)
├── ViewModels/
│   ├── DocumentRequestViewModel.swift
│   ├── DocumentRequestListViewModel.swift
│   └── DocumentRequestDetailViewModel.swift
├── Views/
│   ├── DocumentRequest/
│   │   ├── DocumentRequestView.swift
│   │   ├── DocumentRequestListView.swift
│   │   ├── DocumentRequestDetailView.swift
│   │   └── Components/
│   │       ├── DocumentRequestCard.swift
│   │       ├── TypeSelector.swift
│   │       └── DynamicFieldView.swift
└── Utilities/
    └── DateFormatter+Extensions.swift
```

## MODÈLES DE DONNÉES (Models)

### 1. DocumentField

```swift
struct DocumentField: Codable, Identifiable {
    let id = UUID()
    let name: String
    let type: String  // "text", "number", "date", "paragraph"
    let label: String
    let required: Bool

    enum CodingKeys: String, CodingKey {
        case name, type, label, required
    }
}
```

### 2. DocumentRequestUser

```swift
struct DocumentRequestUser: Codable {
    let id: String?
    let firstName: String?
    let lastName: String?
    let email: String?
    let studentId: String?

    var fullName: String {
        [firstName, lastName]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
    }

    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case firstName, lastName, email, studentId
    }
}
```

### 3. DocumentRequestItem

```swift
struct DocumentRequestItem: Codable, Identifiable {
    let id: String
    let type: String  // "attestation", "relevé", "convention"
    let annee: String
    let user: DocumentRequestUser?
    let createdAt: String?
    let updatedAt: String?
    let details: [String: Any]?

    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case type, annee, user, createdAt, updatedAt, details
    }

    // Custom decoder pour gérer details comme [String: Any]
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        type = try container.decode(String.self, forKey: .type)
        annee = try container.decode(String.self, forKey: .annee)
        user = try container.decodeIfPresent(DocumentRequestUser.self, forKey: .user)
        createdAt = try container.decodeIfPresent(String.self, forKey: .createdAt)
        updatedAt = try container.decodeIfPresent(String.self, forKey: .updatedAt)

        // Gérer details comme dictionnaire générique
        if let detailsDict = try? container.decodeIfPresent([String: String].self, forKey: .details) {
            details = detailsDict
        } else {
            details = nil
        }
    }
}
```

### 4. DocumentFileItem

```swift
struct DocumentFileItem: Codable, Identifiable {
    let id: String
    let nomFichier: String?
    let url: String?
    let documentRequestId: String?
    let type: String?
    let annee: String?
    let user: DocumentRequestUser?
    let createdAt: String?
    let updatedAt: String?

    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case nomFichier, url, documentRequestId, type, annee, user, createdAt, updatedAt
    }
}
```

### 5. CreateDocumentRequestPayload

```swift
struct CreateDocumentRequestPayload: Codable {
    let type: String
    let annee: String
    let fileUrl: String?
    let details: [String: String]?
}
```

### 6. DocumentRequestCreateResponse

```swift
struct DocumentRequestCreateResponse: Codable {
    let documentRequest: DocumentRequestItem
    let fileUrl: String?
    let fileName: String?
}
```

### 7. DocumentFormFieldsResponse

```swift
struct DocumentFormFieldsResponse: Codable {
    let fields: [DocumentField]
}
```

### 8. DocumentRequestStats (optionnel)

```swift
struct DocumentRequestStats: Codable {
    let totalRequests: Int
    let totalFiles: Int
    let byType: [String: Int]
}
```

## SERVICES (API)

### DocumentRequestService

Créer un service qui gère toutes les requêtes API liées aux demandes de documents :

```swift
import Foundation
import Combine

class DocumentRequestService {
    private let baseURL: String
    private let session: URLSession

    init(baseURL: String, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    // Récupérer les champs de formulaire pour un type de document
    func getFormFields(type: String, token: String) async throws -> DocumentFormFieldsResponse {
        // GET /document-request/form-fields/{type}
    }

    // Créer une demande
    func createRequest(payload: CreateDocumentRequestPayload, token: String) async throws -> DocumentRequestCreateResponse {
        // POST /document-request
    }

    // Récupérer toutes les demandes
    func getRequests(token: String) async throws -> [DocumentRequestItem] {
        // GET /document-request
    }

    // Récupérer une demande par ID
    func getRequestById(id: String, token: String) async throws -> DocumentRequestItem {
        // GET /document-request/request/{id}
    }

    // Récupérer tous les fichiers
    func getFiles(token: String) async throws -> [DocumentFileItem] {
        // GET /document-request/files
    }

    // Récupérer le fichier d'une demande spécifique
    func getRequestFile(requestId: String, token: String) async throws -> DocumentFileItem {
        // GET /document-request/request/{requestId}/file
    }

    // Supprimer une demande
    func deleteRequest(id: String, token: String) async throws {
        // DELETE /document-request/{id}
    }

    // Récupérer les statistiques (optionnel)
    func getStats(token: String) async throws -> DocumentRequestStats {
        // GET /document-request/stats
    }
}
```

**Implémentation complète du service :**

```swift
import Foundation

enum NetworkError: LocalizedError {
    case invalidURL
    case noData
    case decodingError
    case serverError(Int)
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .invalidURL: return "URL invalide"
        case .noData: return "Aucune donnée reçue"
        case .decodingError: return "Erreur de décodage"
        case .serverError(let code): return "Erreur serveur (\(code))"
        case .unauthorized: return "Non autorisé, veuillez vous reconnecter"
        }
    }
}

class DocumentRequestService {
    private let baseURL: String
    private let session: URLSession

    init(baseURL: String, session: URLSession = .shared) {
        self.baseURL = baseURL
        self.session = session
    }

    private func makeRequest<T: Decodable>(
        endpoint: String,
        method: String = "GET",
        body: Encodable? = nil,
        token: String
    ) async throws -> T {
        guard let url = URL(string: "\(baseURL)/\(endpoint)") else {
            throw NetworkError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        if let body = body {
            request.httpBody = try JSONEncoder().encode(body)
        }

        let (data, response) = try await session.data(for: request)

        guard let httpResponse = response as? HTTPURLResponse else {
            throw NetworkError.noData
        }

        switch httpResponse.statusCode {
        case 200...299:
            break
        case 401:
            throw NetworkError.unauthorized
        default:
            throw NetworkError.serverError(httpResponse.statusCode)
        }

        do {
            let decoder = JSONDecoder()
            return try decoder.decode(T.self, from: data)
        } catch {
            throw NetworkError.decodingError
        }
    }

    func getFormFields(type: String, token: String) async throws -> DocumentFormFieldsResponse {
        try await makeRequest(
            endpoint: "document-request/form-fields/\(type)",
            token: token
        )
    }

    func createRequest(payload: CreateDocumentRequestPayload, token: String) async throws -> DocumentRequestCreateResponse {
        try await makeRequest(
            endpoint: "document-request",
            method: "POST",
            body: payload,
            token: token
        )
    }

    func getRequests(token: String) async throws -> [DocumentRequestItem] {
        try await makeRequest(
            endpoint: "document-request",
            token: token
        )
    }

    func getRequestById(id: String, token: String) async throws -> DocumentRequestItem {
        try await makeRequest(
            endpoint: "document-request/request/\(id)",
            token: token
        )
    }

    func getFiles(token: String) async throws -> [DocumentFileItem] {
        try await makeRequest(
            endpoint: "document-request/files",
            token: token
        )
    }

    func getRequestFile(requestId: String, token: String) async throws -> DocumentFileItem {
        try await makeRequest(
            endpoint: "document-request/request/\(requestId)/file",
            token: token
        )
    }

    func deleteRequest(id: String, token: String) async throws {
        let _: [String: String] = try await makeRequest(
            endpoint: "document-request/\(id)",
            method: "DELETE",
            token: token
        )
    }

    func getStats(token: String) async throws -> DocumentRequestStats {
        try await makeRequest(
            endpoint: "document-request/stats",
            token: token
        )
    }
}
```

**Points importants pour le service :**

- Utiliser `async/await` pour les appels réseau
- Gérer les erreurs réseau proprement avec une enum `NetworkError`
- Ajouter le token d'authentification dans le header `Authorization: Bearer {token}`
- Utiliser `JSONDecoder` avec les bonnes clés de codage
- Gérer les cas où les réponses peuvent être vides ou nulles
- Gérer les codes de statut HTTP (401, 500, etc.)

## VIEWMODELS (MVVM Pattern)

### 1. DocumentRequestViewModel

Gère l'état et la logique pour la création d'une demande :

```swift
import SwiftUI
import Combine

@MainActor
class DocumentRequestViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var successMessage: String?
    @Published var fields: [DocumentField] = []
    @Published var formValues: [String: String] = [:]
    @Published var availableTypes = ["attestation", "relevé", "convention"]
    @Published var selectedType = "attestation"
    @Published var annee = ""
    @Published var fileUrl = ""
    @Published var created: DocumentRequestItem?
    @Published var createdFileUrl: String?

    private let service: DocumentRequestService
    private let authManager: AuthManager // À adapter selon ton système d'auth

    init(service: DocumentRequestService, authManager: AuthManager) {
        self.service = service
        self.authManager = authManager
        loadFields(for: selectedType)
    }

    func selectType(_ type: String) {
        guard type != selectedType else { return }
        selectedType = type
        fields = []
        formValues = [:]
        successMessage = nil
        error = nil
        loadFields(for: type)
    }

    func updateFieldValue(name: String, value: String) {
        formValues[name] = value
    }

    func updateAnnee(_ value: String) {
        annee = value
    }

    func updateFileUrl(_ value: String) {
        fileUrl = value
    }

    func submitRequest() async {
        guard !annee.isEmpty else {
            error = "Veuillez renseigner l'année académique."
            return
        }

        guard let token = authManager.token else {
            error = "Token manquant, veuillez vous reconnecter."
            return
        }

        isLoading = true
        error = nil
        successMessage = nil

        do {
            let filteredDetails = formValues
                .filter { $0.key != "annee" && $0.key != "fileUrl" && !$0.value.isEmpty }
            let details = filteredDetails.isEmpty ? nil : filteredDetails

            let payload = CreateDocumentRequestPayload(
                type: selectedType,
                annee: annee,
                fileUrl: fileUrl.isEmpty ? nil : fileUrl,
                details: details
            )

            let response = try await service.createRequest(payload: payload, token: token)

            created = response.documentRequest
            createdFileUrl = response.fileUrl
            successMessage = "Demande créée avec succès."

            // Réinitialiser le formulaire
            formValues = [:]
            fileUrl = ""
            annee = ""
            isLoading = false

        } catch {
            isLoading = false
            error = error.localizedDescription
        }
    }

    func clearSuccess() {
        successMessage = nil
    }

    private func loadFields(for type: String) {
        Task {
            guard let token = authManager.token else {
                error = "Token manquant, veuillez vous reconnecter."
                return
            }

            isLoading = true
            error = nil

            do {
                let response = try await service.getFormFields(type: type, token: token)
                fields = response.fields

                // Initialiser les valeurs par défaut
                var defaultValues: [String: String] = [:]
                for field in response.fields {
                    if field.name == "annee" {
                        defaultValues[field.name] = annee
                    } else {
                        defaultValues[field.name] = ""
                    }
                }
                formValues = defaultValues

                isLoading = false
            } catch {
                isLoading = false
                error = error.localizedDescription
            }
        }
    }
}
```

### 2. DocumentRequestListViewModel

Gère la liste de toutes les demandes :

```swift
@MainActor
class DocumentRequestListViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var error: String?
    @Published var requests: [DocumentRequestItem] = []
    @Published var files: [DocumentFileItem] = []
    @Published var isDeleting = false
    @Published var deleteError: String?

    private let service: DocumentRequestService
    private let authManager: AuthManager

    init(service: DocumentRequestService, authManager: AuthManager) {
        self.service = service
        self.authManager = authManager
    }

    func loadRequests() async {
        guard let token = authManager.token else {
            error = "Token manquant, veuillez vous reconnecter."
            return
        }

        isLoading = true
        error = nil

        do {
            async let requestsTask = service.getRequests(token: token)
            async let filesTask = service.getFiles(token: token)

            requests = try await requestsTask

            // Les fichiers peuvent échouer sans bloquer l'affichage
            do {
                files = try await filesTask
            } catch {
                files = []
            }

            isLoading = false
        } catch {
            isLoading = false
            error = error.localizedDescription
        }
    }

    func refresh() {
        Task {
            await loadRequests()
        }
    }

    func deleteRequest(id: String) async {
        guard let token = authManager.token else {
            deleteError = "Token manquant, veuillez vous reconnecter."
            return
        }

        isDeleting = true
        deleteError = nil

        do {
            try await service.deleteRequest(id: id, token: token)
            requests.removeAll { $0.id == id }
            isDeleting = false
        } catch {
            isDeleting = false
            deleteError = error.localizedDescription
        }
    }

    func hasFile(for requestId: String) -> Bool {
        files.contains { $0.documentRequestId == requestId && $0.url != nil }
    }

    func clearErrors() {
        error = nil
        deleteError = nil
    }
}
```

### 3. DocumentRequestDetailViewModel

Gère les détails d'une demande spécifique :

```swift
@MainActor
class DocumentRequestDetailViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var isLoadingFile = false
    @Published var error: String?
    @Published var request: DocumentRequestItem?
    @Published var file: DocumentFileItem?
    @Published var isDeleting = false
    @Published var deleteError: String?

    private let service: DocumentRequestService
    private let authManager: AuthManager

    init(service: DocumentRequestService, authManager: AuthManager) {
        self.service = service
        self.authManager = authManager
    }

    func loadRequest(id: String) async {
        guard let token = authManager.token else {
            error = "Token manquant, veuillez vous reconnecter."
            return
        }

        isLoading = true
        error = nil

        do {
            request = try await service.getRequestById(id: id, token: token)
            isLoading = false

            // Charger le fichier associé
            await loadFile(requestId: id)
        } catch {
            isLoading = false
            error = error.localizedDescription
        }
    }

    func loadFile(requestId: String) async {
        guard let token = authManager.token else { return }

        isLoadingFile = true

        do {
            // Essayer d'abord l'endpoint spécifique
            file = try await service.getRequestFile(requestId: requestId, token: token)
            isLoadingFile = false
        } catch {
            // Si l'endpoint spécifique échoue, chercher dans la liste des fichiers
            do {
                let allFiles = try await service.getFiles(token: token)

                // Chercher par documentRequestId
                var foundFile = allFiles.first { $0.documentRequestId == requestId }

                // Si pas trouvé et qu'on a les infos de la demande, chercher par type et année
                if foundFile == nil, let request = request {
                    foundFile = allFiles.first {
                        $0.type == request.type && $0.annee == request.annee
                    }
                }

                file = foundFile
                isLoadingFile = false
            } catch {
                isLoadingFile = false
                // Le fichier peut ne pas exister, ce n'est pas une erreur critique
            }
        }
    }

    func refresh(requestId: String) {
        Task {
            await loadRequest(id: requestId)
        }
    }

    func deleteRequest(id: String) async {
        guard let token = authManager.token else {
            deleteError = "Token manquant, veuillez vous reconnecter."
            return
        }

        isDeleting = true
        deleteError = nil

        do {
            try await service.deleteRequest(id: id, token: token)
            request = nil // Marquer comme supprimé
            isDeleting = false
        } catch {
            isDeleting = false
            deleteError = error.localizedDescription
        }
    }

    func clearErrors() {
        error = nil
        deleteError = nil
    }
}
```

## VUES SWIFTUI

### 1. DocumentRequestView (Création de demande)

```swift
import SwiftUI

struct DocumentRequestView: View {
    @StateObject private var viewModel: DocumentRequestViewModel
    @Environment(\.dismiss) private var dismiss

    var onOpenHistory: () -> Void

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Sélecteur de type
                    TypeSelectorView(
                        types: viewModel.availableTypes,
                        selectedType: viewModel.selectedType,
                        onTypeSelected: viewModel.selectType
                    )

                    // Champ année académique
                    TextField("Année académique", text: $viewModel.annee)
                        .textFieldStyle(.roundedBorder)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(viewModel.annee.isEmpty && viewModel.error != nil ?
                                       Color.red : Color.clear, lineWidth: 1)
                        )

                    // Champs dynamiques
                    ForEach(viewModel.fields.filter { $0.name != "annee" }) { field in
                        DynamicFieldView(
                            field: field,
                            value: Binding(
                                get: { viewModel.formValues[field.name] ?? "" },
                                set: { viewModel.updateFieldValue(name: field.name, value: $0) }
                            )
                        )
                    }

                    // Bouton de soumission
                    Button(action: {
                        Task {
                            await viewModel.submitRequest()
                        }
                    }) {
                        if viewModel.isLoading {
                            ProgressView()
                                .progressViewStyle(.circular)
                                .scaleEffect(0.8)
                        } else {
                            Text("Créer la demande")
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(viewModel.isLoading || viewModel.annee.isEmpty)
                    .frame(maxWidth: .infinity)

                    // Carte de confirmation après création
                    if let created = viewModel.created {
                        CreatedDocumentCard(
                            request: created,
                            fileUrl: viewModel.createdFileUrl,
                            onOpenHistory: onOpenHistory
                        )
                    }
                }
                .padding()
            }
            .navigationTitle("Demande de document")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Retour") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: onOpenHistory) {
                        Image(systemName: "clock.arrow.circlepath")
                    }
                }
            }
            .alert("Erreur", isPresented: .constant(viewModel.error != nil)) {
                Button("OK") {
                    viewModel.error = nil
                }
            } message: {
                if let error = viewModel.error {
                    Text(error)
                }
            }
            .alert("Succès", isPresented: .constant(viewModel.successMessage != nil)) {
                Button("OK") {
                    viewModel.clearSuccess()
                }
            } message: {
                if let message = viewModel.successMessage {
                    Text(message)
                }
            }
        }
    }
}
```

### 2. DocumentRequestListView (Liste des demandes)

```swift
struct DocumentRequestListView: View {
    @StateObject private var viewModel: DocumentRequestListViewModel
    @Environment(\.dismiss) private var dismiss

    var onCreateRequest: () -> Void
    var onRequestClick: (String) -> Void

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.requests.isEmpty {
                    ProgressView()
                } else if viewModel.requests.isEmpty {
                    VStack(spacing: 8) {
                        Text("Aucune demande pour le moment.")
                            .font(.body)
                        Text("Cliquez sur le bouton + pour créer une demande")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                } else {
                    List(viewModel.requests) { request in
                        DocumentRequestCard(
                            request: request,
                            hasFile: viewModel.hasFile(for: request.id),
                            onClick: {
                                onRequestClick(request.id)
                            }
                        )
                        .listRowSeparator(.hidden)
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Mes demandes")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Retour") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.refresh()
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .toolbar {
                ToolbarItem(placement: .bottomBar) {
                    Button(action: onCreateRequest) {
                        Image(systemName: "plus.circle.fill")
                            .font(.title2)
                    }
                }
            }
            .task {
                await viewModel.loadRequests()
            }
            .alert("Erreur", isPresented: .constant(viewModel.error != nil || viewModel.deleteError != nil)) {
                Button("OK") {
                    viewModel.clearErrors()
                }
            } message: {
                if let error = viewModel.error ?? viewModel.deleteError {
                    Text(error)
                }
            }
        }
    }
}
```

### 3. DocumentRequestDetailView (Détails d'une demande)

```swift
struct DocumentRequestDetailView: View {
    let requestId: String
    @StateObject private var viewModel: DocumentRequestDetailViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var showDeleteConfirmation = false

    var onViewFile: (String) -> Void

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading {
                    ProgressView()
                } else if viewModel.request == nil {
                    Text("Demande introuvable")
                        .foregroundColor(.secondary)
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            // Carte d'information
                            RequestInfoCard(request: viewModel.request!)

                            // Section fichier
                            FileSectionCard(
                                file: viewModel.file,
                                isLoadingFile: viewModel.isLoadingFile,
                                onViewFile: { url in
                                    if let url = url {
                                        onViewFile(url)
                                    }
                                }
                            )

                            // Bouton de suppression
                            Button(action: {
                                showDeleteConfirmation = true
                            }) {
                                if viewModel.isDeleting {
                                    ProgressView()
                                        .progressViewStyle(.circular)
                                        .scaleEffect(0.8)
                                } else {
                                    Label("Supprimer la demande", systemImage: "trash")
                                }
                            }
                            .buttonStyle(.bordered)
                            .disabled(viewModel.isDeleting)
                            .frame(maxWidth: .infinity)
                        }
                        .padding()
                    }
                }
            }
            .navigationTitle("Détail de la demande")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Retour") {
                        dismiss()
                    }
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button(action: {
                        viewModel.refresh(requestId: requestId)
                    }) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .task {
                await viewModel.loadRequest(id: requestId)
            }
            .confirmationDialog(
                "Confirmer la suppression",
                isPresented: $showDeleteConfirmation,
                titleVisibility: .visible
            ) {
                Button("Supprimer", role: .destructive) {
                    Task {
                        await viewModel.deleteRequest(id: requestId)
                        if viewModel.request == nil {
                            dismiss()
                        }
                    }
                }
                Button("Annuler", role: .cancel) {}
            } message: {
                Text("Êtes-vous sûr de vouloir supprimer cette demande ? Cette action est irréversible.")
            }
            .alert("Erreur", isPresented: .constant(viewModel.error != nil || viewModel.deleteError != nil)) {
                Button("OK") {
                    viewModel.clearErrors()
                }
            } message: {
                if let error = viewModel.error ?? viewModel.deleteError {
                    Text(error)
                }
            }
        }
    }
}
```

### 4. Composants réutilisables

#### DocumentRequestCard

```swift
struct DocumentRequestCard: View {
    let request: DocumentRequestItem
    let hasFile: Bool
    let onClick: () -> Void

    var body: some View {
        Button(action: onClick) {
            HStack {
                Image(systemName: documentTypeIcon(for: request.type))
                    .foregroundColor(.blue)
                    .font(.title2)

                VStack(alignment: .leading, spacing: 4) {
                    Text(documentTypeLabel(for: request.type))
                        .font(.headline)
                    Text("Année : \(request.annee)")
                        .font(.caption)
                        .foregroundColor(.secondary)

                    if let createdAt = request.createdAt {
                        Text("Créée le : \(formatDate(createdAt))")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                    }
                }

                Spacer()

                if hasFile {
                    Image(systemName: "doc.fill")
                        .foregroundColor(.blue)
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }

    private func documentTypeIcon(for type: String) -> String {
        switch type.lowercased() {
        case "attestation": return "checkmark.seal.fill"
        case "relevé", "releve": return "doc.text.fill"
        case "convention": return "doc.fill"
        default: return "doc.fill"
        }
    }

    private func documentTypeLabel(for type: String) -> String {
        switch type.lowercased() {
        case "attestation": return "Attestation"
        case "relevé", "releve": return "Relevé de notes"
        case "convention": return "Convention de stage"
        default: return type.capitalized
        }
    }

    private func formatDate(_ dateString: String) -> String {
        // Extraire juste la partie date (avant le T)
        if let datePart = dateString.split(separator: "T").first {
            return String(datePart)
        }
        return dateString
    }
}
```

#### TypeSelectorView

```swift
struct TypeSelectorView: View {
    let types: [String]
    @Binding var selectedType: String
    let onTypeSelected: (String) -> Void

    var body: some View {
        Picker("Type de document", selection: $selectedType) {
            ForEach(types, id: \.self) { type in
                Text(documentTypeLabel(for: type))
                    .tag(type)
            }
        }
        .pickerStyle(.menu)
        .onChange(of: selectedType) { newValue in
            onTypeSelected(newValue)
        }
    }

    private func documentTypeLabel(for type: String) -> String {
        switch type.lowercased() {
        case "attestation": return "Attestation"
        case "relevé", "releve": return "Relevé de notes"
        case "convention": return "Convention de stage"
        default: return type.capitalized
        }
    }
}
```

#### DynamicFieldView

```swift
struct DynamicFieldView: View {
    let field: DocumentField
    @Binding var value: String

    var body: some View {
        Group {
            if field.type == "paragraph" {
                TextEditor(text: $value)
                    .frame(height: 100)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color.gray.opacity(0.3), lineWidth: 1)
                    )
            } else {
                TextField(field.label, text: $value)
                    .textFieldStyle(.roundedBorder)
                    .keyboardType(keyboardType(for: field.type))
            }
        }
        .overlay(
            Group {
                if field.required {
                    HStack {
                        Spacer()
                        Text("Requis")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .padding(.trailing, 8)
                    }
                }
            },
            alignment: .trailing
        )
    }

    #if os(iOS)
    private func keyboardType(for type: String) -> UIKeyboardType {
        switch type {
        case "number": return .numberPad
        case "date": return .numbersAndPunctuation
        default: return .default
        }
    }
    #else
    // Sur macOS, le type de clavier n'est pas applicable
    #endif
}
```

#### RequestInfoCard, FileSectionCard, CreatedDocumentCard

```swift
struct RequestInfoCard: View {
    let request: DocumentRequestItem

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Informations de la demande")
                .font(.headline)

            InfoRow(label: "Type", value: documentTypeLabel(for: request.type))
            InfoRow(label: "Année académique", value: request.annee)

            if let createdAt = request.createdAt {
                InfoRow(label: "Date de création", value: createdAt)
            }

            if let updatedAt = request.updatedAt {
                InfoRow(label: "Dernière mise à jour", value: updatedAt)
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(12)
    }

    private func documentTypeLabel(for type: String) -> String {
        switch type.lowercased() {
        case "attestation": return "Attestation"
        case "relevé", "releve": return "Relevé de notes"
        case "convention": return "Convention de stage"
        default: return type.capitalized
        }
    }
}

struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.secondary)
            Spacer()
            Text(value)
        }
    }
}

struct FileSectionCard: View {
    let file: DocumentFileItem?
    let isLoadingFile: Bool
    let onViewFile: (String?) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Image(systemName: "doc.fill")
                Text("Fichier")
                    .font(.headline)
            }

            if isLoadingFile {
                HStack {
                    ProgressView()
                        .scaleEffect(0.8)
                    Text("Vérification du fichier...")
                }
            } else if let file = file, let url = file.url {
                if let nomFichier = file.nomFichier {
                    Text("Nom : \(nomFichier)")
                        .font(.body)
                }
                Button(action: {
                    onViewFile(url)
                }) {
                    Label("Télécharger/Ouvrir le fichier", systemImage: "arrow.down.doc")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
            } else {
                VStack(alignment: .leading, spacing: 4) {
                    Text("Fichier non disponible pour le moment")
                        .font(.body)
                        .foregroundColor(.secondary)
                    Text("Le fichier sera disponible une fois la demande traitée.")
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
            }
        }
        .padding()
        .background(Color(.systemBlue).opacity(0.1))
        .cornerRadius(12)
    }
}

struct CreatedDocumentCard: View {
    let request: DocumentRequestItem
    let fileUrl: String?
    let onOpenHistory: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("✅ Demande créée avec succès")
                .font(.headline)

            Text("Type : \(documentTypeLabel(for: request.type))")
            Text("Année : \(request.annee)")

            if let createdAt = request.createdAt {
                Text("Créé le : \(formatDate(createdAt))")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            if let fileUrl = fileUrl {
                VStack(alignment: .leading, spacing: 8) {
                    Text("📄 Fichier disponible")
                        .font(.subheadline)
                        .fontWeight(.semibold)
                    Text(fileUrl)
                        .font(.caption)
                        .foregroundColor(.secondary)
                }
                .padding()
                .background(Color(.systemGray5))
                .cornerRadius(8)
            } else {
                Text("⏳ Fichier en attente de génération")
                    .font(.caption)
                    .foregroundColor(.secondary)
            }

            HStack {
                Spacer()
                Button("Voir l'historique") {
                    onOpenHistory()
                }
                .buttonStyle(.bordered)
            }
        }
        .padding()
        .background(Color(.systemBlue).opacity(0.1))
        .cornerRadius(12)
    }

    private func documentTypeLabel(for type: String) -> String {
        switch type.lowercased() {
        case "attestation": return "Attestation"
        case "relevé", "releve": return "Relevé de notes"
        case "convention": return "Convention de stage"
        default: return type.capitalized
        }
    }

    private func formatDate(_ dateString: String) -> String {
        if let datePart = dateString.split(separator: "T").first {
            return String(datePart)
        }
        return dateString
    }
}
```

## POINTS IMPORTANTS À RESPECTER

### 1. Gestion de l'authentification

- Récupérer le token depuis ton système d'authentification existant
- Ajouter le header `Authorization: Bearer {token}` à toutes les requêtes
- Gérer les cas où le token est manquant ou expiré

### 2. Gestion des erreurs

- Afficher des messages d'erreur clairs et en français
- Gérer les erreurs réseau (pas de connexion, timeout, etc.)
- Gérer les erreurs serveur (404, 500, etc.)

### 3. États de chargement

- Afficher des indicateurs de chargement pendant les requêtes
- Désactiver les boutons pendant les opérations en cours
- Gérer les états vides (liste vide, demande introuvable)

### 4. Navigation

- Utiliser `NavigationStack` (iOS 16+) ou `NavigationView` (iOS 15)
- Implémenter la navigation entre les vues (liste → détail, création → historique)
- Gérer le retour après suppression

### 5. Design et UX

- Suivre les guidelines Apple Human Interface Guidelines
- Utiliser les composants natifs SwiftUI
- Adapter pour macOS (fenêtres, barres d'outils, etc.)
- Support du mode sombre
- Accessibilité (VoiceOver, Dynamic Type)

### 6. Performance

- Utiliser `async/await` pour les opérations asynchrones
- Charger les fichiers en parallèle quand possible
- Éviter les rechargements inutiles

### 7. Gestion des fichiers

- Construire les URLs complètes si nécessaire
- Ouvrir les fichiers dans le navigateur ou l'application appropriée
- Gérer les cas où le fichier n'est pas encore disponible

## ENDPOINTS API À IMPLÉMENTER

Assure-toi que ton service correspond exactement à ces endpoints :

1. `GET /document-request/form-fields/{type}` - Récupérer les champs de formulaire
2. `POST /document-request` - Créer une demande
3. `GET /document-request` - Récupérer toutes les demandes
4. `GET /document-request/request/{id}` - Récupérer une demande par ID
5. `GET /document-request/files` - Récupérer tous les fichiers
6. `GET /document-request/request/{requestId}/file` - Récupérer le fichier d'une demande
7. `DELETE /document-request/{id}` - Supprimer une demande
8. `GET /document-request/stats` - Récupérer les statistiques (optionnel)

## TESTS ET VALIDATION

- Tester la création d'une demande avec tous les types
- Tester l'affichage de la liste
- Tester l'affichage des détails
- Tester la suppression
- Tester la gestion des erreurs (token manquant, réseau, etc.)
- Tester avec des fichiers disponibles et non disponibles
- Tester la navigation entre les vues

## INTÉGRATION DANS L'APPLICATION

### 1. Configuration et Injection de Dépendances

Créer un fichier de configuration ou utiliser un système d'injection de dépendances :

```swift
// AppConfig.swift ou similaire
struct AppConfig {
    static let baseURL = "https://ton-api.com/api" // À adapter
}

// Dans ton App ou SceneDelegate
@main
struct MyApp: App {
    @StateObject private var authManager = AuthManager() // Ton gestionnaire d'auth

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(authManager)
        }
    }
}
```

### 2. Initialisation des ViewModels

```swift
// Exemple d'utilisation dans une vue parente
struct DocumentRequestContainerView: View {
    @EnvironmentObject var authManager: AuthManager

    private var service: DocumentRequestService {
        DocumentRequestService(baseURL: AppConfig.baseURL)
    }

    var body: some View {
        DocumentRequestListView(
            viewModel: DocumentRequestListViewModel(
                service: service,
                authManager: authManager
            ),
            onCreateRequest: {
                // Navigation vers création
            },
            onRequestClick: { id in
                // Navigation vers détail
            }
        )
    }
}
```

### 3. Navigation avec NavigationStack

```swift
enum DocumentRequestRoute: Hashable {
    case list
    case create
    case detail(String)
}

struct DocumentRequestNavigationView: View {
    @State private var path = NavigationPath()
    @EnvironmentObject var authManager: AuthManager

    private var service: DocumentRequestService {
        DocumentRequestService(baseURL: AppConfig.baseURL)
    }

    var body: some View {
        NavigationStack(path: $path) {
            DocumentRequestListView(
                viewModel: DocumentRequestListViewModel(
                    service: service,
                    authManager: authManager
                ),
                onCreateRequest: {
                    path.append(DocumentRequestRoute.create)
                },
                onRequestClick: { id in
                    path.append(DocumentRequestRoute.detail(id))
                }
            )
            .navigationDestination(for: DocumentRequestRoute.self) { route in
                switch route {
                case .create:
                    DocumentRequestView(
                        viewModel: DocumentRequestViewModel(
                            service: service,
                            authManager: authManager
                        ),
                        onOpenHistory: {
                            path.removeLast()
                        }
                    )
                case .detail(let id):
                    DocumentRequestDetailView(
                        requestId: id,
                        viewModel: DocumentRequestDetailViewModel(
                            service: service,
                            authManager: authManager
                        ),
                        onViewFile: { url in
                            // Ouvrir l'URL dans le navigateur
                            if let url = URL(string: url) {
                                #if os(macOS)
                                NSWorkspace.shared.open(url)
                                #else
                                UIApplication.shared.open(url)
                                #endif
                            }
                        }
                    )
                case .list:
                    EmptyView()
                }
            }
        }
    }
}
```

### 4. Gestion des URLs de fichiers

Créer une extension pour construire les URLs complètes :

```swift
extension DocumentFileItem {
    func fullURL(baseURL: String) -> String? {
        guard let url = url else { return nil }

        if url.hasPrefix("http://") || url.hasPrefix("https://") {
            return url
        }

        let cleanBase = baseURL.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let cleanPath = url.trimmingCharacters(in: CharacterSet(charactersIn: "/"))

        return "\(cleanBase)/\(cleanPath)"
    }
}
```

### 5. Gestionnaire d'authentification (exemple)

```swift
class AuthManager: ObservableObject {
    @Published var token: String?
    @Published var isAuthenticated: Bool = false

    private let keychain = Keychain(service: "com.yourapp.auth")

    init() {
        loadToken()
    }

    func saveToken(_ token: String) {
        self.token = token
        isAuthenticated = true
        // Sauvegarder dans Keychain ou UserDefaults
        try? keychain.set(token, key: "authToken")
    }

    func loadToken() {
        if let token = try? keychain.get("authToken") {
            self.token = token
            isAuthenticated = true
        }
    }

    func logout() {
        token = nil
        isAuthenticated = false
        try? keychain.delete("authToken")
    }
}
```

## DIFFÉRENCES macOS vs iOS

### macOS spécifique :

1. **Ouvrir les fichiers** :

```swift
#if os(macOS)
import AppKit

func openFile(url: String) {
    if let fileURL = URL(string: url) {
        NSWorkspace.shared.open(fileURL)
    }
}
#endif
```

2. **Navigation** : Sur macOS, utiliser `NavigationSplitView` pour une meilleure expérience :

```swift
#if os(macOS)
NavigationSplitView {
    // Sidebar avec liste
    DocumentRequestListView(...)
} detail: {
    // Détail dans le panneau principal
    if let selectedId = selectedId {
        DocumentRequestDetailView(requestId: selectedId, ...)
    }
}
#endif
```

3. **Toolbar** : Sur macOS, les toolbars sont différentes :

```swift
.toolbar {
    ToolbarItemGroup(placement: .primaryAction) {
        Button(action: {}) {
            Label("Créer", systemImage: "plus")
        }
    }
}
```

### iOS spécifique :

1. **Ouvrir les fichiers** :

```swift
#if os(iOS)
import UIKit

func openFile(url: String) {
    if let fileURL = URL(string: url) {
        UIApplication.shared.open(fileURL)
    }
}
#endif
```

2. **Pull to refresh** :

```swift
.refreshable {
    await viewModel.loadRequests()
}
```

## GESTION DES ERREURS DÉTAILLÉE

Créer une extension pour améliorer la gestion des erreurs :

```swift
extension ViewModel {
    func handleError(_ error: Error) -> String {
        if let networkError = error as? NetworkError {
            return networkError.errorDescription ?? "Erreur inconnue"
        }

        // Gérer d'autres types d'erreurs
        if let urlError = error as? URLError {
            switch urlError.code {
            case .notConnectedToInternet:
                return "Pas de connexion Internet"
            case .timedOut:
                return "La requête a expiré"
            default:
                return "Erreur réseau : \(urlError.localizedDescription)"
            }
        }

        return error.localizedDescription
    }
}
```

## VALIDATION DES DONNÉES

Ajouter des validations dans le ViewModel :

```swift
extension DocumentRequestViewModel {
    func validateForm() -> (isValid: Bool, error: String?) {
        guard !annee.isEmpty else {
            return (false, "L'année académique est requise")
        }

        // Valider le format de l'année si nécessaire
        let yearPattern = #"^\d{4}(-\d{4})?$"#
        if !annee.range(of: yearPattern, options: .regularExpression) != nil {
            return (false, "Format d'année invalide. Utilisez : 2024 ou 2024-2025")
        }

        // Valider les champs requis
        for field in fields where field.required {
            if formValues[field.name]?.isEmpty ?? true {
                return (false, "Le champ '\(field.label)' est requis")
            }
        }

        return (true, nil)
    }
}
```

## NOTES FINALES

- Adapte les noms de classes et de fichiers selon les conventions de ton projet
- Intègre avec ton système d'authentification existant
- Utilise les mêmes URLs de base que le reste de l'application
- Respecte les conventions de code Swift/SwiftUI
- Documente le code si nécessaire
- Gère les cas limites (données nulles, listes vides, etc.)
- Teste sur macOS ET iOS si l'application est multiplateforme
- Utilise `#if os(macOS)` et `#if os(iOS)` pour les différences de plateforme
- Implémente le cache si nécessaire pour améliorer les performances
- Ajoute des logs de débogage pour faciliter le développement

---

**Ce prompt est complet et détaillé. Suis-le étape par étape pour implémenter la gestion des demandes de documents sur macOS/iOS avec Xcode et SwiftUI.**
