package com.example.esprit.ui.nav
import com.example.esprit.ui.shared.*
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.esprit.model.Role
import com.example.esprit.util.AuthManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import com.example.esprit.ui.admin.AdminHomeScreen
import com.example.esprit.ui.admin.internships.AdminInternshipFormScreen
import com.example.esprit.ui.admin.internships.AdminInternshipListScreen
import com.example.esprit.ui.auth.LoginScreen
import com.example.esprit.ui.auth.LoginViewModel
import com.example.esprit.ui.auth.SplashScreen
import com.example.esprit.ui.auth.SplashViewModel
import com.example.esprit.ui.parent.ParentHomeScreen
import com.example.esprit.ui.shared.AnnouncementListScreen
import com.example.esprit.ui.shared.ProfileScreen
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.shared.internships.InternshipOfferDetailScreen
import com.example.esprit.ui.student.AbsenceScreen
import com.example.esprit.ui.student.StudentHomeScreen
import com.example.esprit.ui.student.TimetableScreen
import com.example.esprit.ui.student.internships.StudentInternshipListScreen
import com.example.esprit.ui.student.internships.StudentFavoritesScreen
import com.example.esprit.ui.student.internships.StudentSearchScreen
import com.example.esprit.ui.student.internships.FavoritesViewModel
import com.example.esprit.ui.shared.internships.FavoriteViewModel
import com.example.esprit.ui.student.applications.StudentApplyScreen
import com.example.esprit.ui.student.applications.StudentApplicationsScreen
import com.example.esprit.ui.student.applications.StudentApplicationDetailScreen
import com.example.esprit.ui.student.applications.StudentApplicationEditScreen
import com.example.esprit.ui.teacher.TeacherHomeScreen
import com.example.esprit.ui.admin.applications.AdminApplicationsListScreen
import com.example.esprit.ui.admin.applications.AdminApplicationDetailScreen
import com.example.esprit.ui.admin.applications.ScheduleInterviewScreen
import com.example.esprit.ui.student.ai.AIChatScreen
import com.example.esprit.ui.student.ai.ChatHistoryScreen
import com.example.esprit.service.ChatHistoryManager
import com.example.esprit.ui.auth.SessionViewModel
import com.example.esprit.ui.messages.ContactListScreen
import com.example.esprit.ui.chat.ChatScreen
import com.example.esprit.ui.chat.ChatViewModel
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.ui.club.ClubHomeViewModel
import com.example.esprit.ui.club.ClubPostsViewModel
import com.example.esprit.ui.club.screens.ClubEventsScreen
import com.example.esprit.ui.club.screens.ClubHomeScreen
import com.example.esprit.ui.club.screens.ClubPostsScreen
import com.example.esprit.ui.club.screens.ClubSettingsScreen
import com.example.esprit.ui.club.screens.CreateClubEventScreen
import com.example.esprit.ui.club.screens.CreateClubPostScreen
import com.example.esprit.ui.club.screens.EditClubEventScreen
import com.example.esprit.ui.club.screens.EventDetailScreen
import com.example.esprit.ui.club.screens.MembersListScreen
import com.example.esprit.ui.notifications.NotificationsScreen

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthManagerEntryPoint {
    fun authManager(): AuthManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChatHistoryManagerEntryPoint {
    fun chatHistoryManager(): ChatHistoryManager
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    splashViewModel: SplashViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Get AuthManager from Hilt
    val authManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthManagerEntryPoint::class.java
        ).authManager()
    }
    
    // Helper function to handle logout
    fun handleLogout() {
        scope.launch {
            authManager.logout()
            navController.navigate(Destinations.LOGIN) { popUpTo(0) }
        }
    }
    
    // Helper function to convert role to route
    fun roleToRoute(role: Role): String =
        when (role) {
            Role.STUDENT -> Destinations.STUDENT_HOME
            Role.TEACHER -> Destinations.TEACHER_HOME
            Role.PARENT -> Destinations.PARENT_HOME
            Role.ADMIN -> Destinations.ADMIN_HOME
            Role.PRESIDENT -> Destinations.STUDENT_HOME
            Role.CLUB -> Destinations.CLUB_HOME
        }
    
    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {

        // ----------------------------------------------------------
        // SPLASH
        // ----------------------------------------------------------
        composable(Destinations.SPLASH) {
            SplashScreen(
                viewModel = splashViewModel,
                onUnauthenticated = {
                    navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                },
                onAuthenticated = { role ->
                    navController.navigate(roleToRoute(role)) { popUpTo(0) }
                }
            )
        }

        // ----------------------------------------------------------
        // LOGIN
        // ----------------------------------------------------------
        composable(Destinations.LOGIN) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { role ->
                    navController.navigate(roleToRoute(role)) { popUpTo(0) }
                },
                onForgotPasswordClick = {
                    navController.navigate(Destinations.FORGOT_PASSWORD)
                }
            )
        }
        
        composable(Destinations.FORGOT_PASSWORD) {
            com.example.esprit.ui.auth.ForgotPasswordScreen(onBackComp = { navController.popBackStack() })
        }

        // ----------------------------------------------------------
        // STUDENT HOME
        // ----------------------------------------------------------
        composable(Destinations.STUDENT_HOME) {
            val notifVm: com.example.esprit.ui.notifications.NotificationsViewModel = hiltViewModel()
            
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },

                // ✅ ICI: Messages -> TA MESSAGERIE (ui.shared.MessagesScreen)
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateClubs = { navController.navigate(Destinations.STUDENT_CLUBS) },
                onNavigateDocumentRequests = { navController.navigate(Destinations.DOCUMENT_REQUEST_HISTORY) },
                onNavigateStages = { navController.navigate(Destinations.STUDENT_INTERNSHIP_LIST) },
                onNavigateAIChat = { navController.navigate(Destinations.AI_CHAT) },
                onNavigateClubChat = { clubId ->
                    navController.navigate(Destinations.clubChatRoute(clubId, "Chat"))
                },
                onNavigateDirectMessages = { navController.navigate(Destinations.DIRECT_MESSAGES) }, // ta messagerie
                onNavigateNotifications = {
                    navController.navigate(Destinations.CLUB_NOTIFICATIONS)
                },
                onLogout = { handleLogout() },
                notificationsViewModel = notifVm
            )
        }

        // ----------------------------------------------------------
        // TEACHER
        // ----------------------------------------------------------
        composable(Destinations.TEACHER_HOME) {
            TeacherHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = { handleLogout() }
            )
        }

        // ----------------------------------------------------------
        // PARENT
        // ----------------------------------------------------------
        composable(Destinations.PARENT_HOME) {
            ParentHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = { handleLogout() }
            )
        }

        // ----------------------------------------------------------
        // ADMIN HOME
        // ----------------------------------------------------------
        composable(Destinations.ADMIN_HOME) {
            val notifVm: com.example.esprit.ui.notifications.NotificationsViewModel = hiltViewModel()
            AdminHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateStages = {
                    navController.navigate(Destinations.ADMIN_INTERNSHIP_LIST)
                },
                onNavigateApplications = {
                    navController.navigate(Destinations.ADMIN_APPLICATIONS_LIST)
                },
                onNavigateToRequests = { navController.navigate(Destinations.ADMIN_DOCUMENT_REQUESTS) },
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },
                onNavigateNotifications = { navController.navigate(Destinations.CLUB_NOTIFICATIONS) },
                onNavigateUsers = { navController.navigate(Destinations.ADMIN_USERS_LIST) },
                notificationsViewModel = notifVm,
                onLogout = { handleLogout() }
            )
        }

        composable(Destinations.ADMIN_USERS_LIST) {
            com.example.esprit.ui.admin.users.AdminUsersListScreen(navController = navController)
        }

        composable(Destinations.ADMIN_USER_CREATE) {
            com.example.esprit.ui.admin.users.AdminUserCreateScreen(navController = navController)
        }

        composable(Destinations.ADMIN_DOCUMENT_REQUESTS) {
            com.example.esprit.ui.admin.requests.AdminDocumentRequestListScreen(
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.navigate(
                        Destinations.ADMIN_DOCUMENT_REQUEST_DETAIL.replace("{requestId}", id)
                    )
                }
            )
        }

        composable(
            route = Destinations.ADMIN_DOCUMENT_REQUEST_DETAIL,
            arguments = listOf(navArgument("requestId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            val viewModel: com.example.esprit.ui.admin.requests.AdminDocumentRequestViewModel = hiltViewModel()
            
            LaunchedEffect(requestId) {
                 if (viewModel.uiState.value.selectedRequest?.id != requestId) {
                     viewModel.loadRequest(requestId)
                 }
            }
            
            com.example.esprit.ui.admin.requests.AdminDocumentRequestDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // COMMON
        composable(Destinations.TIMETABLE) { TimetableScreen() }
        composable(Destinations.ABSENCES) { AbsenceScreen() }
        // ANNOUNCEMENTS is handled below with AnnouncementsScreen

        // ----------------------------------------------------------
        // PROFILE
        // ----------------------------------------------------------
        composable(Destinations.PROFILE) {
            val vm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { vm.loadMe() }
            val ui = vm.uiState.collectAsState()

            ProfileScreen(
                uiState = ui.value,
                onChangePassword = { old, new -> vm.changePassword(old, new) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Destinations.DOCUMENT_REQUEST_FORM) {
            val vm: com.example.esprit.ui.demande.DocumentRequestViewModel = hiltViewModel()
            com.example.esprit.ui.demande.DocumentRequestScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenHistory = { navController.navigate(Destinations.DOCUMENT_REQUEST_HISTORY) }
            )
        }

        // --- DOCUMENT REQUEST HISTORY ---
        composable(Destinations.DOCUMENT_REQUEST_HISTORY) {
            val vm: com.example.esprit.ui.demande.DocumentRequestListViewModel = hiltViewModel()
            com.example.esprit.ui.demande.DocumentRequestListScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onCreateRequest = { navController.navigate(Destinations.DOCUMENT_REQUEST_FORM) },
                onRequestClick = { requestId ->
                    navController.navigate(
                        Destinations.DOCUMENT_REQUEST_DETAIL.replace("{requestId}", requestId)
                    )
                }
            )
        }

        // --- DOCUMENT REQUEST DETAIL ---
        composable(
            route = Destinations.DOCUMENT_REQUEST_DETAIL,
            arguments = listOf(navArgument("requestId") {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            android.util.Log.d("NAV", "Detail screen requestId: $requestId")

            val vm: com.example.esprit.ui.demande.DocumentRequestDetailViewModel = hiltViewModel()
            com.example.esprit.ui.demande.DocumentRequestDetailScreen(
                requestId = requestId,
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onViewFile = { fileUrl ->
                    val encodedUrl = java.net.URLEncoder.encode(fileUrl, "UTF-8")
                    navController.navigate(
                        Destinations.DOCUMENT_VIEWER.replace("{fileUrl}", encodedUrl)
                    )
                }
            )
        }


        composable(
            route = Destinations.DOCUMENT_VIEWER,
            arguments = listOf(navArgument("fileUrl") {
                type = androidx.navigation.NavType.StringType
            })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("fileUrl") ?: ""
            val fileUrl = try {
                java.net.URLDecoder.decode(encodedUrl, "UTF-8")
            } catch (e: Exception) {
                encodedUrl
            }
            com.example.esprit.ui.demande.DocumentViewerScreen(
                fileUrl = fileUrl,
                onBack = { navController.popBackStack() },
                onShare = { url ->
                    // TODO: Implement share functionality
                }
            )
        }

// CLUB
        composable(Destinations.CLUB_HOME) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinations.CLUB_HOME)
            }
            val eventsViewModel: ClubEventsViewModel = hiltViewModel(parentEntry)
            val refreshEvents = backStackEntry
                .savedStateHandle
                .getStateFlow("refreshEvents", false)
                .collectAsState()
            LaunchedEffect(refreshEvents.value) {
                if (refreshEvents.value) {
                    eventsViewModel.load()
                    backStackEntry.savedStateHandle["refreshEvents"] = false
                }
            }
            ClubHomeScreen(
                onCreateEvent = { navController.navigate(Destinations.CLUB_EVENT_CREATE) },
                onCreatePost = { clubId ->
                    if (clubId.isNotBlank()) {
                        navController.navigate(Destinations.clubPostCreateRoute(clubId))
                    }
                },
                onEditPost = { postId ->
                    navController.navigate(Destinations.clubPostEditRoute(postId))
                },
                onNavigatePosts = { navController.navigate(Destinations.CLUB_POSTS) },
                onNavigateMembers = { navController.navigate(Destinations.CLUB_MEMBERS) },
                onNavigateSettings = { navController.navigate(Destinations.CLUB_SETTINGS) },
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },
                onNavigateNotifications = { navController.navigate(Destinations.CLUB_NOTIFICATIONS) },
                onNavigateRequests = { navController.navigate(Destinations.CLUB_REQUESTS) },
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                },
                onEventClick = { eventId ->
                    navController.navigate(Destinations.clubEventDetailRoute(eventId))
                },
                onEventEdit = { eventId ->
                    navController.navigate(Destinations.clubEventEditRoute(eventId))
                }
            )
        }
        composable(Destinations.CLUB_NOTIFICATIONS) {
            NotificationsScreen(
                navController = navController
            )
        }
        composable(Destinations.CLUB_REQUESTS) {
            com.example.esprit.ui.club.screens.ClubRequestsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.CLUB_EVENTS) { backStackEntry ->
            val viewModel: ClubEventsViewModel = hiltViewModel(backStackEntry)
            val refreshEvents = backStackEntry
                .savedStateHandle
                .getStateFlow("refreshEvents", false)
                .collectAsState()
            LaunchedEffect(refreshEvents.value) {
                if (refreshEvents.value) {
                    viewModel.load()
                    backStackEntry.savedStateHandle["refreshEvents"] = false
                }
            }
            ClubEventsScreen(
                onCreateEvent = { navController.navigate(Destinations.CLUB_EVENT_CREATE) },
                onOpenEvent = { eventId ->
                    navController.navigate(Destinations.clubEventDetailRoute(eventId))
                },
                viewModel = viewModel
            )
        }
        composable(Destinations.CLUB_EVENT_CREATE) {
            CreateClubEventScreen(
                onBack = { navController.popBackStack() },
                onCreated = {
                    navController.getBackStackEntry(Destinations.CLUB_HOME)
                        .savedStateHandle["refreshEvents"] = true
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Destinations.CLUB_EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()
            EventDetailScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onEdit = { id ->
                    navController.navigate(Destinations.clubEventEditRoute(id))
                }
            )
        }
        composable(
            route = Destinations.CLUB_EVENT_EDIT,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId").orEmpty()
            EditClubEventScreen(
                eventId = eventId,
                onBack = { navController.popBackStack() },
                onUpdated = {
                    navController.getBackStackEntry(Destinations.CLUB_HOME)
                        .savedStateHandle["refreshEvents"] = true
                    navController.popBackStack()
                }
            )
        }
        composable(Destinations.CLUB_POSTS) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Destinations.CLUB_HOME)
            }
            val homeViewModel: ClubHomeViewModel = hiltViewModel(parentEntry)
            val clubState = homeViewModel.uiState.collectAsState()
            val postsViewModel: ClubPostsViewModel = hiltViewModel(backStackEntry)
            val refreshPosts = backStackEntry
                .savedStateHandle
                .getStateFlow("refreshPosts", false)
                .collectAsState()
            LaunchedEffect(refreshPosts.value) {
                if (refreshPosts.value) {
                    postsViewModel.refresh()
                    backStackEntry.savedStateHandle["refreshPosts"] = false
                }
            }
            ClubPostsScreen(
                clubId = clubState.value.club?.id.orEmpty(),
                clubName = clubState.value.club?.name,
                clubAvatarUrl = clubState.value.club?.imageUrl,
                onCreatePost = { id ->
                    if (id.isNotBlank()) {
                        navController.navigate(Destinations.clubPostCreateRoute(id))
                    }
                },
                onOpenPost = { /* TODO navigate detail */ },
                viewModel = postsViewModel
            )
        }
        composable(
            route = Destinations.CLUB_POST_CREATE,
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId").orEmpty()
            CreateClubPostScreen(
                clubId = clubId,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshPosts", true)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Destinations.CLUB_POST_EDIT,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            val postsViewModel: com.example.esprit.ui.club.ClubPostsViewModel = hiltViewModel()
            val postsState by postsViewModel.uiState.collectAsState()

            // Find the post from the current state
            val post = postsState.posts.find { it.id == postId }

            // Debug logging
            android.util.Log.d("AppNavGraph", "Edit post - postId: $postId")
            android.util.Log.d("AppNavGraph", "Edit post - found post: ${post != null}")
            android.util.Log.d("AppNavGraph", "Edit post - content: ${post?.content}")
            android.util.Log.d("AppNavGraph", "Edit post - imageUrl: ${post?.imageUrl}")

            CreateClubPostScreen(
                postId = postId,
                initialContent = post?.content ?: "",
                initialImageUrl = post?.imageUrl,
                onBack = { navController.popBackStack() },
                onSuccess = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshPosts", true)
                    navController.popBackStack()
                }
            )
        }
        composable(
            route = Destinations.CLUB_POST_DETAIL,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId").orEmpty()
            com.example.esprit.ui.club.screens.PostDetailScreen(
                postId = postId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.CLUB_MEMBERS) { MembersListScreen() }
        composable(Destinations.CLUB_SETTINGS) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            ClubSettingsScreen(
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                }
            )
        }


       // ======================= ANNONCES =======================
        composable(Destinations.ANNOUNCEMENTS) { AnnouncementsScreen(navController) }
        composable(Destinations.ANNOUNCEMENT_ADD) { AnnouncementAddScreen(navController) }

        composable(
            route = Destinations.ANNOUNCEMENT_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            AnnouncementDetailsScreen(
                navController = navController,
                announcementId = backStackEntry.arguments?.getString("id")!!
            )
        }

        composable(
            route = Destinations.ANNOUNCEMENT_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            AnnouncementEditScreen(
                navController = navController,
                announcementId = backStackEntry.arguments?.getString("id")!!
            )
        }

        composable(Destinations.AI_ANNOUNCEMENTS) {
            val senderId = "TEMP_USER_ID"
            AiAnnouncementsScreen(
                senderId = senderId,
                onSaved = { navController.popBackStack() }
            )
        }



        // ----------------------------------------------------------
        // ADMIN INTERNSHIPS
        // ----------------------------------------------------------
        composable(Destinations.ADMIN_INTERNSHIP_LIST) {
            AdminInternshipListScreen(
                navController = navController,
                onAddClick = { navController.navigate(Destinations.ADMIN_INTERNSHIP_CREATE) },
                onEditClick = { id ->
                    val route = Destinations.ADMIN_INTERNSHIP_EDIT.replace("{id}", id)
                    navController.navigate(route)
                }
            )
        }

        composable(Destinations.ADMIN_INTERNSHIP_CREATE) {
            AdminInternshipFormScreen(
                offerId = null,
                onDone = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshInternships", true)
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Destinations.ADMIN_INTERNSHIP_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            AdminInternshipFormScreen(
                offerId = id,
                onDone = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("refreshInternships", true)
                    navController.popBackStack()
                }
            )
        }

        // ----------------------------------------------------------
        // INTERNSHIP DETAILS (shared: admin + student)
        // ----------------------------------------------------------
        composable(
            route = Destinations.INTERNSHIP_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            // Récupérer l'utilisateur connecté pour obtenir son identifiant
            val profileVm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { profileVm.loadMe() }
            val profileUi by profileVm.uiState.collectAsState()

            // Utilise l'id du user renvoyé par /auth/me
            val currentUserId = profileUi.user?.id ?: ""

            // Déterminer si on vient de l’admin ou de l’étudiant
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            val isAdmin =
                previousRoute?.startsWith(Destinations.ADMIN_INTERNSHIP_LIST) == true ||
                        previousRoute?.startsWith(Destinations.ADMIN_HOME) == true

            InternshipOfferDetailScreen(
                navController = navController,
                onBack = { navController.popBackStack() },
                currentUserId = currentUserId,
                isAdmin = isAdmin,
                onApplyClick = if (!isAdmin) { id ->
                    val route = Destinations.STUDENT_APPLY.replace("{id}", id)
                    navController.navigate(route)
                } else null,
                onViewApplicationsClick = if (!isAdmin) {
                    { navController.navigate(Destinations.STUDENT_APPLICATIONS) }
                } else null,
                onEditClick = if (isAdmin) { id ->
                    val route = Destinations.ADMIN_INTERNSHIP_EDIT.replace("{id}", id)
                    navController.navigate(route)
                } else null
            )
        }

        // ----------------------------------------------------------
        // STUDENT INTERNSHIPS
        // ----------------------------------------------------------
        composable(Destinations.STUDENT_INTERNSHIP_LIST) {
            val profileVm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { profileVm.loadMe() }
            val profileUi by profileVm.uiState.collectAsState()
            val currentUserId = profileUi.user?.id ?: ""
            
            StudentInternshipListScreen(
                onOfferClick = { id ->
                    val route = Destinations.INTERNSHIP_DETAILS.replace("{id}", id)
                    navController.navigate(route)
                },
                onFavoritesClick = {
                    navController.navigate(Destinations.STUDENT_FAVORITES)
                },
                onSearchClick = {
                    navController.navigate(Destinations.STUDENT_SEARCH)
                },
                onNavigateAIChat = {
                    navController.navigate(Destinations.AI_CHAT)
                }
            )
        }
        
        composable(Destinations.STUDENT_SEARCH) {
            StudentSearchScreen(
                onBack = { navController.popBackStack() },
                onOfferClick = { id ->
                    val route = Destinations.INTERNSHIP_DETAILS.replace("{id}", id)
                    navController.navigate(route)
                }
            )
        }
        
        composable(Destinations.STUDENT_FAVORITES) {
            StudentFavoritesScreen(
                onBack = { navController.popBackStack() },
                onOfferClick = { id ->
                    val route = Destinations.INTERNSHIP_DETAILS.replace("{id}", id)
                    navController.navigate(route)
                }
            )
        }
        
        // ----------------------------------------------------------
        // STUDENT APPLICATIONS
        // ----------------------------------------------------------
        composable(
            route = Destinations.STUDENT_APPLY,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val internshipId = backStackEntry.arguments?.getString("id") ?: ""
            val profileVm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { profileVm.loadMe() }
            val profileUi by profileVm.uiState.collectAsState()
            val currentUserId = profileUi.user?.id ?: ""
            
            StudentApplyScreen(
                internshipId = internshipId,
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }
        
        composable(Destinations.STUDENT_APPLICATIONS) {
            val profileVm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { profileVm.loadMe() }
            val profileUi by profileVm.uiState.collectAsState()
            val currentUserId = profileUi.user?.id ?: ""
            
            StudentApplicationsScreen(
                currentUserId = currentUserId,
                onBack = { navController.popBackStack() },
                onApplicationClick = { appId ->
                    val route = Destinations.STUDENT_APPLICATION_DETAILS.replace("{id}", appId)
                    navController.navigate(route)
                }
            )
        }
        
        composable(
            route = Destinations.STUDENT_APPLICATION_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("id") ?: ""
            
            StudentApplicationDetailScreen(
                applicationId = applicationId,
                onBack = { navController.popBackStack() },
                onEdit = { appId ->
                    val route = Destinations.STUDENT_APPLICATION_EDIT.replace("{id}", appId)
                    navController.navigate(route)
                }
            )
        }
        
        composable(
            route = Destinations.STUDENT_APPLICATION_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("id") ?: ""
            
            StudentApplicationEditScreen(
                applicationId = applicationId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // ADMIN APPLICATIONS
        // ----------------------------------------------------------
        composable(Destinations.ADMIN_APPLICATIONS_LIST) {
            AdminApplicationsListScreen(
                onBack = { navController.popBackStack() },
                onApplicationClick = { appId ->
                    val route = Destinations.ADMIN_APPLICATION_DETAILS.replace("{id}", appId)
                    navController.navigate(route)
                }
            )
        }

        composable(
            route = Destinations.ADMIN_APPLICATION_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("id") ?: ""
            
            AdminApplicationDetailScreen(
                applicationId = applicationId,
                onBack = { navController.popBackStack() },
                onScheduleInterview = { appId, userEmail ->
                    val route = Destinations.SCHEDULE_INTERVIEW
                        .replace("{id}", appId)
                        .plus("?email=$userEmail")
                    navController.navigate(route)
                }
            )
        }

        // Schedule Interview Screen
        composable(
            route = Destinations.SCHEDULE_INTERVIEW + "?email={email}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("email") { 
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val applicationId = backStackEntry.arguments?.getString("id") ?: ""
            val studentEmail = backStackEntry.arguments?.getString("email") ?: ""
            
            ScheduleInterviewScreen(
                applicationId = applicationId,
                studentEmail = studentEmail,
                onBack = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // AI CHAT ASSISTANT
        // ----------------------------------------------------------
        composable(Destinations.AI_CHAT) {
            val internshipVm: com.example.esprit.ui.student.internships.StudentInternshipViewModel = hiltViewModel()
            LaunchedEffect(Unit) { internshipVm.loadOffers() }
            val internshipUi by internshipVm.uiState.collectAsState()
            
            AIChatScreen(
                onNavigateToHistory = { navController.navigate(Destinations.AI_CHAT_HISTORY) },
                offers = internshipUi.offers
            )
        }

        composable(Destinations.AI_CHAT_HISTORY) {
            val context = LocalContext.current
            val historyManager = remember {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ChatHistoryManagerEntryPoint::class.java
                ).chatHistoryManager()
            }
            val chatVm: com.example.esprit.ui.student.ai.AIChatViewModel = hiltViewModel()
            
            ChatHistoryScreen(
                historyManager = historyManager,
                onSelectSession = { session ->
                    chatVm.loadSession(session)
                },
                onDismiss = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // MESSAGING & CHAT
        // ----------------------------------------------------------
        composable(Destinations.CONTACT_LIST) {
             com.example.esprit.ui.messages.ContactListScreen(
                 navController = navController
             )
        }



        composable(
            route = Destinations.CLUB_CHAT,
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId")
            val name = backStackEntry.arguments?.getString("name")
            val chatVm: com.example.esprit.ui.chat.ChatViewModel = hiltViewModel()
            
            LaunchedEffect(clubId) {
                if (clubId != null) {
                    chatVm.loadMessages(clubId = clubId, partnerId = null)
                }
            }

            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                clubId = clubId,
                initialTitle = name ?: "Club Chat"
            )
        }
        // STUDENT CLUBS
        composable(Destinations.STUDENT_CLUBS) {
            com.example.esprit.ui.student.clubs.StudentClubsScreen(
                onNavigateClub = { clubId ->
                    navController.navigate(Destinations.studentClubProfileRoute(clubId))
                },
                onNavigateEventDetail = { eventId ->
                    navController.navigate(Destinations.clubEventDetailRoute(eventId))
                }
            )
        }

        // STUDENT CLUB PROFILE
        composable(
            route = Destinations.STUDENT_CLUB_PROFILE,
            arguments = listOf(navArgument("clubId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId") ?: return@composable
            com.example.esprit.ui.student.club.StudentClubProfileScreen(
                clubId = clubId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateChat = { name -> navController.navigate(Destinations.clubChatRoute(clubId, name)) }
            )
        }

        // CHAT
        composable(
            route = Destinations.CLUB_CHAT,
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; defaultValue = "Chat" }
            )
        ) { backStackEntry ->
            val clubId = backStackEntry.arguments?.getString("clubId") ?: return@composable
            val name = backStackEntry.arguments?.getString("name") ?: "Chat"
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                clubId = clubId,
                initialTitle = name
            )
        }

        composable(
            route = Destinations.PRIVATE_CHAT,
            arguments = listOf(
                navArgument("partnerId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType; defaultValue = "Chat" }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId") ?: return@composable
            val name = backStackEntry.arguments?.getString("name") ?: "Chat"
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                clubId = null,
                partnerId = partnerId,
                initialTitle = name
            )
        }

        composable(
            route = Destinations.PRIVATE_CHAT_SHARED,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId") ?: return@composable
            val encodedName = backStackEntry.arguments?.getString("peerName") ?: "Chat"
            val peerName = try {
                java.net.URLDecoder.decode(encodedName, "UTF-8")
            } catch (e: Exception) {
                encodedName
            }
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                clubId = null,
                partnerId = peerId,
                initialTitle = peerName
            )
        }

        composable(route = Destinations.CONTACT_LIST) {
            com.example.esprit.ui.messages.ContactListScreen(navController = navController)
        }

        composable(Destinations.MESSAGES) {
            val viewModel: com.example.esprit.ui.messages.MessagesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            com.example.esprit.ui.messages.MessagesScreen(navController = navController, viewModel = viewModel)
        }
         composable(Destinations.DIRECT_MESSAGES) {
            com.example.esprit.ui.shared.MessagesScreen(
                navController = navController
            )
        }



     // ======================= TA MESSAGERIE (IMPORTANT) =======================
        // ✅ REMPLACE COMPLETEMENT l'ancien bloc ui.messages.MessagesScreen
        composable(Destinations.MESSAGES) {
            com.example.esprit.ui.shared.MessagesScreen(
                navController = navController
            )
        }

        // ➕ Contact list : si tu utilises encore celle de ton amie, garde.
        // Si tu as TA propre ContactListScreen dans ui.shared, remplace ici.
        composable(Destinations.CONTACT_LIST) {
            com.example.esprit.ui.messages.ContactListScreen(navController)
        }

    }
}






