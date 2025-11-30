package com.example.esprit.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.esprit.model.Role
import com.example.esprit.ui.admin.AdminHomeScreen
import com.example.esprit.ui.auth.LoginScreen
import com.example.esprit.ui.auth.LoginViewModel
import com.example.esprit.ui.auth.SplashScreen
import com.example.esprit.ui.auth.SplashViewModel
import com.example.esprit.ui.parent.ParentHomeScreen
import com.example.esprit.ui.shared.*
import com.example.esprit.ui.student.AbsenceScreen
import com.example.esprit.ui.student.StudentHomeScreen
import com.example.esprit.ui.student.TimetableScreen
import com.example.esprit.ui.teacher.TeacherHomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    splashViewModel: SplashViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = Destinations.SPLASH) {

        // --------------------------------------------------------------------
        // SPLASH
        // --------------------------------------------------------------------
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

        // --------------------------------------------------------------------
        // LOGIN
        // --------------------------------------------------------------------
        composable(Destinations.LOGIN) {
            LoginScreen(viewModel = loginViewModel) { role ->
                navController.navigate(roleToRoute(role)) { popUpTo(0) }
            }
        }

        // --------------------------------------------------------------------
        // STUDENT HOME
        // --------------------------------------------------------------------
        composable(Destinations.STUDENT_HOME) {
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },   // <--- NEW
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                }
            )
        }

        // --------------------------------------------------------------------
        // TEACHER
        // --------------------------------------------------------------------
        composable(Destinations.TEACHER_HOME) {
            TeacherHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                }
            )
        }

        // --------------------------------------------------------------------
        // PARENT
        // --------------------------------------------------------------------
        composable(Destinations.PARENT_HOME) {
            ParentHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                }
            )
        }

        // --------------------------------------------------------------------
        // ADMIN
        // --------------------------------------------------------------------
        composable(Destinations.ADMIN_HOME) {
            AdminHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                }
            )
        }

        // --------------------------------------------------------------------
        // TIMETABLE
        // --------------------------------------------------------------------
        composable(Destinations.TIMETABLE) {
            TimetableScreen()
        }

        // --------------------------------------------------------------------
        // ABSENCES
        // --------------------------------------------------------------------
        composable(Destinations.ABSENCES) {
            AbsenceScreen(token = "")
        }

        // --------------------------------------------------------------------
        // ANNOUNCEMENTS LIST
        // --------------------------------------------------------------------
        composable(Destinations.ANNOUNCEMENTS) {
            AnnouncementsScreen(navController)
        }

        // --------------------------------------------------------------------
        // ANNOUNCEMENT ADD
        // --------------------------------------------------------------------
        composable(Destinations.ANNOUNCEMENT_ADD) {
            AnnouncementCreateScreen(navController)
        }

        // --------------------------------------------------------------------
        // ANNOUNCEMENT EDIT
        // --------------------------------------------------------------------
        composable(
            route = Destinations.ANNOUNCEMENT_EDIT,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("id") ?: ""
            AnnouncementEditScreen(navController, id)
        }

        // --------------------------------------------------------------------
        // ANNOUNCEMENT DETAILS
        // --------------------------------------------------------------------
        composable(
            route = Destinations.ANNOUNCEMENT_DETAILS,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val id = it.arguments?.getString("id") ?: ""
            AnnouncementDetailsScreen(navController, id)
        }

        // --------------------------------------------------------------------
        // MESSAGES LIST
        // --------------------------------------------------------------------
        composable(Destinations.MESSAGES) {
            MessagesScreen(navController)
        }

        // --------------------------------------------------------------------
        // CHAT
        // --------------------------------------------------------------------
        composable(
            route = Destinations.CHAT,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("peerName") { type = NavType.StringType }
            )
        ) { backStack ->
            val peerId = backStack.arguments?.getString("peerId") ?: ""
            val peerName = backStack.arguments?.getString("peerName") ?: ""

            ChatScreen(
                navController = navController,
                peerId = peerId,
                peerName = peerName
            )
        }
    }
}

private fun roleToRoute(role: Role): String = when (role) {
    Role.STUDENT -> Destinations.STUDENT_HOME
    Role.TEACHER -> Destinations.TEACHER_HOME
    Role.PARENT -> Destinations.PARENT_HOME
    Role.ADMIN -> Destinations.ADMIN_HOME
}
