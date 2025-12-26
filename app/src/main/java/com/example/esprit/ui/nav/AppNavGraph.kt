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
import com.example.esprit.ui.shared.MessagesScreen

// AUTH
import com.example.esprit.ui.auth.*

// HOME
import com.example.esprit.ui.student.StudentHomeScreen
import com.example.esprit.ui.teacher.TeacherHomeScreen
import com.example.esprit.ui.parent.ParentHomeScreen
import com.example.esprit.ui.admin.AdminHomeScreen

// COMMON
import com.example.esprit.ui.student.AbsenceScreen
import com.example.esprit.ui.student.TimetableScreen
import com.example.esprit.ui.shared.ProfileScreen
import com.example.esprit.ui.shared.ProfileViewModel

// ANNONCES + TON CHAT IA + TA MESSAGERIE
import com.example.esprit.ui.shared.*

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AuthManagerEntryPoint {
    fun authManager(): AuthManager
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

    val authManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            AuthManagerEntryPoint::class.java
        ).authManager()
    }

    fun handleLogout() {
        scope.launch {
            authManager.logout()
            navController.navigate(Destinations.LOGIN) { popUpTo(0) }
        }
    }

    fun roleToRoute(role: Role) = when (role) {
        Role.STUDENT, Role.PRESIDENT -> Destinations.STUDENT_HOME
        Role.TEACHER -> Destinations.TEACHER_HOME
        Role.PARENT -> Destinations.PARENT_HOME
        Role.ADMIN -> Destinations.ADMIN_HOME
        Role.CLUB -> Destinations.CLUB_HOME
    }

    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {

        // ---------------- SPLASH ----------------
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

        // ---------------- LOGIN ----------------
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
            ForgotPasswordScreen(onBackComp = { navController.popBackStack() })
        }

        // ---------------- STUDENT HOME ----------------
        composable(Destinations.STUDENT_HOME) {
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },

                // ✅ ICI: Messages -> TA MESSAGERIE (ui.shared.MessagesScreen)
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },

                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateClubs = { navController.navigate(Destinations.STUDENT_CLUBS) },
                onNavigateStages = { navController.navigate(Destinations.STUDENT_INTERNSHIP_LIST) },
                onNavigateAIChat = { navController.navigate(Destinations.AI_CHAT) },

                // ✅ GARDER chat club de ton amie
                onNavigateClubChat = { clubId ->
                    navController.navigate(Destinations.clubChatRoute(clubId, "Chat"))
                },
                onNavigateDirectMessages = { navController.navigate(Destinations.DIRECT_MESSAGES) }, // ta messagerie
                onLogout = { handleLogout() }
            )
        }

        // ---------------- TEACHER / PARENT / ADMIN ----------------
        composable(Destinations.TEACHER_HOME) {
            TeacherHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = { handleLogout() }
            )
        }

        composable(Destinations.PARENT_HOME) {
            ParentHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = { handleLogout() }
            )
        }

        composable(Destinations.ADMIN_HOME) {
            AdminHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = { handleLogout() }
            )
        }

        // ---------------- COMMON ----------------
        composable(Destinations.TIMETABLE) { TimetableScreen() }
        composable(Destinations.ABSENCES) { AbsenceScreen() }

        // ---------------- PROFILE ----------------
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

        // ======================= TON CHAT IA (PRIVATE) =======================
        composable(
            route = Destinations.PRIVATE_CHAT_SHARED,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId").orEmpty()
            val peerName = backStackEntry.arguments?.getString("peerName").orEmpty()

            com.example.esprit.ui.shared.ChatScreen(
                navController = navController,
                peerId = peerId,
                peerName = peerName
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

        // ======================= CHAT CLUB (TON AMIE) =======================
        composable(
            route = Destinations.CLUB_CHAT,
            arguments = listOf(
                navArgument("clubId") { type = NavType.StringType },
                navArgument("name") {
                    type = NavType.StringType
                    defaultValue = "Chat"
                }
            )
        ) {
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController,
                clubId = it.arguments?.getString("clubId"),
                initialTitle = it.arguments?.getString("name") ?: "Club Chat"
            )

        }
        composable(Destinations.MESSAGES) {
            com.example.esprit.ui.chat.ChatScreen(
                navController = navController
            )
        }


        composable(Destinations.DIRECT_MESSAGES) {
            com.example.esprit.ui.shared.MessagesScreen(
                navController = navController
            )
        }
            // (OPTIONNEL) Tu peux supprimer PRIVATE_CHAT de ton amie si tu ne l'utilises plus
            // mais je le laisse si votre app en dépend ailleurs.
            composable(
                route = Destinations.PRIVATE_CHAT,
                arguments = listOf(
                    navArgument("partnerId") { type = NavType.StringType },
                    navArgument("name") {
                        type = NavType.StringType
                        defaultValue = "Chat"
                    }
                )
            ) {
                com.example.esprit.ui.chat.ChatScreen(
                    navController = navController,
                    partnerId = it.arguments?.getString("partnerId"),
                    initialTitle = it.arguments?.getString("name") ?: "Chat"
                )
            }
        }
    }
