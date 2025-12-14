package com.example.esprit.ui.nav


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
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
import com.example.esprit.ui.auth.SessionViewModel
import com.example.esprit.ui.auth.SplashScreen
import com.example.esprit.ui.auth.SplashViewModel
import com.example.esprit.ui.club.ClubEventsViewModel
import com.example.esprit.ui.club.ClubHomeViewModel
import com.example.esprit.ui.club.ClubPostsViewModel
import com.example.esprit.ui.club.screens.*
import com.example.esprit.ui.parent.ParentHomeScreen
import com.example.esprit.ui.shared.AnnouncementListScreen
import com.example.esprit.ui.shared.ProfileScreen
import com.example.esprit.ui.shared.ProfileViewModel
import com.example.esprit.ui.student.AbsenceScreen
import com.example.esprit.ui.student.StudentHomeScreen
import com.example.esprit.ui.student.TimetableScreen
import com.example.esprit.ui.teacher.TeacherHomeScreen


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    splashViewModel: SplashViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // Track current mode for President (Student / Club)


    NavHost(
        navController = navController,
        startDestination = Destinations.SPLASH
    ) {
        // SPLASH
        composable(Destinations.SPLASH) {
            SplashScreen(
                viewModel = splashViewModel,
                onUnauthenticated = { navController.navigate(Destinations.LOGIN) { popUpTo(0) } },
                onAuthenticated = { role -> navController.navigate(roleToRoute(role)) { popUpTo(0) } }
            )
        }

        // LOGIN
        composable(Destinations.LOGIN) {
            LoginScreen(viewModel = loginViewModel) { role ->
                navController.navigate(roleToRoute(role)) { popUpTo(0) }
            }
        }

        // STUDENT HOME
        composable(Destinations.STUDENT_HOME) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            StudentHomeScreen(
                onNavigateTimetable = { navController.navigate(Destinations.TIMETABLE) },
                onNavigateAbsences = { navController.navigate(Destinations.ABSENCES) },
                onNavigateAnnouncements = { navController.navigate(Destinations.ANNOUNCEMENTS) },
                onNavigateProfile = { navController.navigate(Destinations.PROFILE) },
                onNavigateStages = { navController.navigate(Destinations.STUDENT_INTERNSHIP_LIST) },
                onNavigateClubs = { navController.navigate(Destinations.STUDENT_CLUBS) },
                onNavigateMessages = { navController.navigate(Destinations.MESSAGES) },
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                }
            )
        }

        // TEACHER + PARENT + ADMIN
        composable(Destinations.TEACHER_HOME) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            TeacherHomeScreen(
                onNavigateProfile = {},
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                })
        }
        composable(Destinations.PARENT_HOME) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            ParentHomeScreen(
                onNavigateProfile = {},
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                })
        }
        composable(Destinations.ADMIN_HOME) { backStackEntry ->
            val sessionViewModel: SessionViewModel = hiltViewModel(backStackEntry)
            AdminHomeScreen(
                onNavigateProfile = {},
                onNavigateVieEtudiante = {},
                onNavigateStages = {},
                onLogout = {
                    sessionViewModel.logout {
                        navController.navigate(Destinations.LOGIN) { popUpTo(0) }
                    }
                })
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

        // COMMON PAGES
        composable(Destinations.TIMETABLE) { TimetableScreen() }
        composable(Destinations.ABSENCES) { AbsenceScreen() }
        composable(Destinations.ANNOUNCEMENTS) { AnnouncementListScreen() }
        composable(Destinations.PROFILE) {
            val vm: ProfileViewModel = hiltViewModel()
            LaunchedEffect(Unit) { vm.loadMe() }
            val uiState = vm.uiState.collectAsState()
            ProfileScreen(
                uiState.value,
                onChangePassword = { old, new -> vm.changePassword(old, new) },
                onBack = { navController.popBackStack() })
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

// MAP ROLE → START ROUTE
private fun roleToRoute(role: Role): String = when (role) {
    Role.ADMIN -> Destinations.ADMIN_HOME
    Role.TEACHER -> Destinations.TEACHER_HOME
    Role.PARENT -> Destinations.PARENT_HOME
    Role.PRESIDENT -> Destinations.STUDENT_HOME
    Role.CLUB -> Destinations.CLUB_HOME
    Role.STUDENT -> Destinations.STUDENT_HOME
}
