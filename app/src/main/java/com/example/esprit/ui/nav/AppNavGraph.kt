package com.example.esprit.ui.nav

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.esprit.model.Role
import com.example.esprit.ui.admin.AdminHomeScreen
import com.example.esprit.ui.auth.LoginScreen
import com.example.esprit.ui.auth.LoginViewModel
import com.example.esprit.ui.auth.SplashScreen
import com.example.esprit.ui.auth.SplashViewModel
import com.example.esprit.ui.demande.DocumentRequestDetailScreen
import com.example.esprit.ui.demande.DocumentRequestDetailViewModel
import com.example.esprit.ui.demande.DocumentRequestListScreen
import com.example.esprit.ui.demande.DocumentRequestListViewModel
import com.example.esprit.ui.demande.DocumentRequestScreen
import com.example.esprit.ui.demande.DocumentRequestViewModel
import com.example.esprit.ui.demande.DocumentViewerScreen
import com.example.esprit.ui.parent.ParentHomeScreen
import com.example.esprit.ui.shared.AnnouncementListScreen
import com.example.esprit.ui.shared.ProfileScreen
import com.example.esprit.ui.shared.ProfileViewModel
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

        // --- SPLASH ---
        composable(Destinations.SPLASH) {
            SplashScreen(
                viewModel = splashViewModel,
                onUnauthenticated = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                },
                onAuthenticated = { role ->
                    val destination = roleToRoute(role)
                    Log.d("AppNavGraph", "Splash authenticated - Role: $role, Navigating to: $destination")
                    navController.navigate(destination) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- LOGIN ---
        composable(Destinations.LOGIN) {
            LoginScreen(viewModel = loginViewModel) { role ->
                val destination = roleToRoute(role)
                Log.d("AppNavGraph", "Login success - Role: $role, Navigating to: $destination")
                navController.navigate(destination) {
                    popUpTo(0)
                }
            }
        }

        // --- STUDENT ---
        composable(Destinations.STUDENT_HOME) {
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },
                onNavigateDocumentRequests = { navController.navigate(Destinations.DOCUMENT_REQUEST_FORM) },
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- TEACHER ---
        composable(Destinations.TEACHER_HOME) {
            TeacherHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- PARENT ---
        composable(Destinations.PARENT_HOME) {
            ParentHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
        }

        // --- ADMIN ---
        composable(Destinations.ADMIN_HOME) {
            AdminHomeScreen(
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateToRequests = { navController.navigate(Destinations.ADMIN_DOCUMENT_REQUESTS) },
                onLogout = {
                    navController.navigate(Destinations.LOGIN) {
                        popUpTo(0)
                    }
                }
            )
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
            // Use the same ViewModel instance if scoped to graph, or let Hilt provide a new one.
            // For simplicity, we let Hilt provide one. If we needed shared state, we'd scope it.
            // Here we just pass the ID via the ViewModel's saved state handle or load it.
            // The ViewModel we built doesn't take ID in constructor but has a load method.
            // Ideally we should pass it or have the VM read from SavedStateHandle.
            // But our VM has `selectRequest` which we called in the list.
            // However, `hiltViewModel()` gives a scoped instance. If we want to share data between List and Detail,
            // we need to scope them to a navigation graph or pass data.
            // Our VM `AdminDocumentRequestViewModel` is used in both.
            // If we use `hiltViewModel()` in both, they might be different instances unless scoped.
            // Let's check if we can scope it or if we should just reload.
            // The `AdminDocumentRequestViewModel` has `selectRequest`.
            // If we get a new instance, `selectedRequest` will be null.
            // So we should probably load the request by ID in the detail screen if it's null.
            // But our VM doesn't have `loadRequestById`.
            // Let's update the VM to support loading by ID or just rely on the list being loaded.
            // Actually, `hiltViewModel(backStackEntry)` would be scoped to the entry.
            // To share, we need a parent entry.
            // For now, let's assume we can reload or we need to update VM.
            // Wait, I didn't add `loadRequestById` to `AdminDocumentRequestViewModel`.
            // I should probably add it or pass the request data.
            // Or better, since I'm in the same flow, I can try to scope the ViewModel to the navigation graph if possible.
            // But simpler: Update VM to load by ID.
            
            // Let's just add the composable for now and I will update the VM to be robust.
            val requestId = backStackEntry.arguments?.getString("requestId") ?: ""
            val viewModel: com.example.esprit.ui.admin.requests.AdminDocumentRequestViewModel = hiltViewModel()
            
            // We need to ensure the request is loaded. 
            // If we came from List, we might have it in memory if the VM is shared (it's not by default between composables).
            // So we should trigger a load.
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

        // --- TIMETABLE ---
        composable(Destinations.TIMETABLE) {
            TimetableScreen()
        }

        // --- ABSENCES ---
        composable(Destinations.ABSENCES) {
            AbsenceScreen(token = "") // à remplacer par le vrai token
        }

        // --- ANNOUNCEMENTS ---
        composable(Destinations.ANNOUNCEMENTS) {
            AnnouncementListScreen()
        }

        // --- PROFILE ---
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
            val vm: DocumentRequestViewModel = hiltViewModel()
            DocumentRequestScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenHistory = { navController.navigate(Destinations.DOCUMENT_REQUEST_HISTORY) }
            )
        }

        // --- DOCUMENT REQUEST HISTORY ---
        composable(Destinations.DOCUMENT_REQUEST_HISTORY) {
            val vm: DocumentRequestListViewModel = hiltViewModel()
            DocumentRequestListScreen(
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
            Log.d("NAV", "Detail screen requestId: $requestId")

            val vm: DocumentRequestDetailViewModel = hiltViewModel()
            DocumentRequestDetailScreen(
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
            DocumentViewerScreen(
                fileUrl = fileUrl,
                onBack = { navController.popBackStack() },
                onShare = { url ->
                    // TODO: Implement share functionality
                }
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
