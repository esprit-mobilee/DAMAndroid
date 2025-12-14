package com.example.esprit.ui.nav

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
import com.example.esprit.ui.club.screens.NotificationsScreen

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
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateClubs = { navController.navigate(Destinations.STUDENT_CLUBS) },
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },
                onNavigateStages = { navController.navigate(Destinations.STUDENT_INTERNSHIP_LIST) },
                onNavigateAIChat = { navController.navigate(Destinations.AI_CHAT) },
                onNavigateClubChat = { clubId ->
                    navController.navigate(Destinations.clubChatRoute(clubId, "Chat"))
                },
                onLogout = { handleLogout() }
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
            AdminHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateStages = {
                    navController.navigate(Destinations.ADMIN_INTERNSHIP_LIST)
                },
                onNavigateApplications = {
                    navController.navigate(Destinations.ADMIN_APPLICATIONS_LIST)
                },
                onLogout = { handleLogout() }
            )
        }

        // COMMON
        composable(Destinations.TIMETABLE) { TimetableScreen() }
        composable(Destinations.ABSENCES) { AbsenceScreen() }
        composable(Destinations.ANNOUNCEMENTS) { AnnouncementListScreen() }

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
                onNavigateBack = { navController.popBackStack() }
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
            route = Destinations.PRIVATE_CHAT,
            arguments = listOf(
                navArgument("partnerId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val partnerId = backStackEntry.arguments?.getString("partnerId")
            val name = backStackEntry.arguments?.getString("name") 
            val chatVm: com.example.esprit.ui.chat.ChatViewModel = hiltViewModel()
            
            LaunchedEffect(partnerId) {
                if (partnerId != null) {
                    chatVm.loadMessages(clubId = null, partnerId = partnerId)
                }
            }
            
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                partnerId = partnerId,
                initialTitle = name ?: "Chat"
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

        composable(route = Destinations.CONTACT_LIST) {
            com.example.esprit.ui.messages.ContactListScreen(navController = navController)
        }

        composable(Destinations.MESSAGES) {
            val viewModel: com.example.esprit.ui.messages.MessagesViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            com.example.esprit.ui.messages.MessagesScreen(navController = navController, viewModel = viewModel)
        }


    }


}






