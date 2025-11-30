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
import com.example.esprit.ui.vieetudiante.*
import com.example.esprit.ui.admin.applications.AdminApplicationsListScreen
import com.example.esprit.ui.admin.applications.AdminApplicationDetailScreen
import com.example.esprit.ui.student.ai.AIChatScreen
import com.example.esprit.ui.student.ai.ChatHistoryScreen
import com.example.esprit.service.ChatHistoryManager

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
            LoginScreen(viewModel = loginViewModel) { role ->
                navController.navigate(roleToRoute(role)) { popUpTo(0) }
            }
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
                onNavigateVieEtudiante = { navController.navigate(Destinations.VIE_ETUDIANTE) },
                onNavigateStages = { navController.navigate(Destinations.STUDENT_INTERNSHIP_LIST) },
                onNavigateAIChat = { navController.navigate(Destinations.AI_CHAT) },
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
                onNavigateVieEtudiante = { navController.navigate(Destinations.VIE_ETUDIANTE) },
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

        // ----------------------------------------------------------
        // VIE ÉTUDIANTE
        // ----------------------------------------------------------
        composable(Destinations.VIE_ETUDIANTE) {
            val eventsVm: EventsViewModel = hiltViewModel()
            val clubsVm: ClubsViewModel = hiltViewModel()

            LaunchedEffect(Unit) {
                eventsVm.loadEvents()
                clubsVm.loadClubs()
            }

            VieEtudianteScreen(
                eventsViewModel = eventsVm,
                clubsViewModel = clubsVm,
                onAddEvent = { navController.navigate(Destinations.EVENT_FORM) },
                onAddClub = { navController.navigate(Destinations.CLUB_FORM) },
                onEventClick = { id ->
                    navController.navigate("${Destinations.EVENT_DETAILS}/$id")
                },
                onClubClick = { id ->
                    navController.navigate("${Destinations.CLUB_DETAILS}/$id")
                },
                onEditEvent = { id ->
                    navController.navigate("${Destinations.EVENT_FORM}?eventId=$id")
                },
                onEditClub = { id ->
                    navController.navigate("${Destinations.CLUB_FORM}?clubId=$id")
                },
                onDeleteEvent = { id ->
                    eventsVm.deleteEvent(id) { eventsVm.loadEvents() }
                },
                onDeleteClub = { id ->
                    clubsVm.deleteClub(id) { clubsVm.loadClubs() }
                }
            )
        }

        // ----------------------------------------------------------
        // EVENT FORM
        // ----------------------------------------------------------
        composable(
            route = Destinations.EVENT_FORM + "?eventId={eventId}",
            arguments = listOf(
                navArgument("eventId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val eventsVm: EventsViewModel = hiltViewModel()
            val eventId = backStackEntry.arguments?.getString("eventId")

            EventFormScreen(
                eventsViewModel = eventsVm,
                eventId = eventId,
                onSaved = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // CLUB FORM
        // ----------------------------------------------------------
        composable(
            route = Destinations.CLUB_FORM + "?clubId={clubId}",
            arguments = listOf(
                navArgument("clubId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val clubsVm: ClubsViewModel = hiltViewModel()
            val clubId = backStackEntry.arguments?.getString("clubId")

            ClubFormScreen(
                clubsViewModel = clubsVm,
                clubId = clubId,
                onSaved = { navController.popBackStack() }
            )
        }

        // ----------------------------------------------------------
        // EVENT DETAILS
        // ----------------------------------------------------------
        composable(
            route = "${Destinations.EVENT_DETAILS}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val eventsVm: EventsViewModel = hiltViewModel()
            EventDetailsScreen(eventId = id, eventsViewModel = eventsVm)
        }

        // ----------------------------------------------------------
        // CLUB DETAILS
        // ----------------------------------------------------------
        composable(
            route = "${Destinations.CLUB_DETAILS}/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            val clubsVm: ClubsViewModel = hiltViewModel()
            ClubDetailsScreen(clubId = id, clubsViewModel = clubsVm)
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
                onBack = { navController.popBackStack() },
                currentUserId = currentUserId,
                isAdmin = isAdmin,
                onApplyClick = if (!isAdmin) { id ->
                    val route = Destinations.STUDENT_APPLY.replace("{id}", id)
                    navController.navigate(route)
                } else null,
                onViewApplicationsClick = if (!isAdmin) {
                    { navController.navigate(Destinations.STUDENT_APPLICATIONS) }
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
    }
}
