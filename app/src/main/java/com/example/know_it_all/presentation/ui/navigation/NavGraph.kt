package com.example.know_it_all.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.content.Intent
import androidx.navigation.NavController
import com.example.know_it_all.util.DeepLinkHandler
import com.example.know_it_all.KnowItAllApplication
import com.example.know_it_all.presentation.ui.screen.auth.LoginScreen
import com.example.know_it_all.presentation.ui.screen.auth.RegisterScreen
import com.example.know_it_all.presentation.ui.screen.auth.SplashScreen
import com.example.know_it_all.presentation.ui.screen.main.ChatScreen
import com.example.know_it_all.presentation.ui.screen.main.CreateGroupSessionScreen
import com.example.know_it_all.presentation.ui.screen.main.FeedScreen
import com.example.know_it_all.presentation.ui.screen.main.GroupSessionDetailScreen
import com.example.know_it_all.presentation.ui.screen.main.QRHandshakeScreen
import com.example.know_it_all.presentation.ui.screen.main.RadarScreenEnhanced
import com.example.know_it_all.presentation.ui.screen.main.SkillProfileScreenEnhanced
import com.example.know_it_all.presentation.ui.screen.main.TradeScreenEnhanced
import com.example.know_it_all.presentation.ui.screen.main.VaultScreenEnhanced
import com.example.know_it_all.presentation.ui.screen.onboarding.OnboardingScreen
import com.example.know_it_all.presentation.viewmodel.AuthViewModel
import com.example.know_it_all.presentation.viewmodel.ChatViewModel
import com.example.know_it_all.presentation.viewmodel.FeedViewModel
import com.example.know_it_all.presentation.viewmodel.GroupSessionViewModel
import com.example.know_it_all.presentation.viewmodel.LedgerViewModel
import com.example.know_it_all.presentation.viewmodel.RadarViewModel
import com.example.know_it_all.presentation.viewmodel.SkillViewModel
import com.example.know_it_all.presentation.viewmodel.StreakViewModel
import com.example.know_it_all.presentation.viewmodel.TradeViewModel
import com.example.know_it_all.presentation.viewmodel.ViewModelFactory
import com.example.know_it_all.presentation.viewmodel.LeaderboardViewModel
import com.example.know_it_all.presentation.ui.screen.main.LeaderboardScreen 
import com.example.know_it_all.presentation.viewmodel.AvailabilityViewModel


sealed class Screen(val route: String) {
    object Splash       : Screen("splash")
    object Login        : Screen("login")
    object Register     : Screen("register")
    object Feed         : Screen("feed")
    object Radar        : Screen("radar")
    object Trade        : Screen("trade")
    object Vault        : Screen("vault")
    object SkillProfile : Screen("skill_profile")
    object Chat         : Screen("chat/{swapId}/{skillName}/{counterpartName}")
    object QRHandshake  : Screen("qr_handshake/{swapId}/{skillName}")
    object Onboarding : Screen("onboarding")
    object Leaderboard : Screen("leaderboard")
    object CreateGroupSession : Screen("create_group_session")
    object GroupSessionDetail : Screen("group_session/{sessionId}")
}

@Composable
fun KnowItAllNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Splash.route,
    onNavControllerReady: (NavController) -> Unit = {},  
    pendingIntent: Intent? = null 
) {
    val context = LocalContext.current
    val app = context.applicationContext as KnowItAllApplication

    val authViewModel: AuthViewModel = viewModel(
        factory = ViewModelFactory(
            userRepository = app.userRepository,
            sessionManager = app.sessionManager
        )
    )
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                navController = navController,
                isLoggedIn = authState.isAuthenticated
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // ── Feed ──────────────────────────────────────────────────────────────
        composable(Screen.Feed.route) { backStackEntry ->
            val feedViewModel: FeedViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    feedRepository = app.feedRepository,
                    sessionManager = app.sessionManager
                )
            )
            FeedScreen(
                navController = navController,
                feedViewModel = feedViewModel,
                onMentorConnect = {
                    navController.navigate(Screen.Radar.route)
                }
            )
        }

        // ── Radar ─────────────────────────────────────────────────────────────
        composable(Screen.Radar.route) { backStackEntry ->
            val radarViewModel: RadarViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    userRepository = app.userRepository,
                    sessionManager = app.sessionManager
                )
            )
            RadarScreenEnhanced(
                navController = navController,
                radarViewModel = radarViewModel,
                userId = authState.userId ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Trade ─────────────────────────────────────────────────────────────
        composable(Screen.Trade.route) { backStackEntry ->
            val tradeViewModel: TradeViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    swapRepository = app.swapRepository,
                    sessionManager = app.sessionManager
                )
            )
            TradeScreenEnhanced(
                navController = navController,
                tradeViewModel = tradeViewModel,
                userId = authState.userId ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Vault ─────────────────────────────────────────────────────────────
        composable(Screen.Vault.route) { backStackEntry ->
            val ledgerViewModel: LedgerViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    ledgerRepository = app.ledgerRepository,
                    skillRepository  = app.skillRepository,
                    userRepository   = app.userRepository,
                    sessionManager   = app.sessionManager
                )
            )
            VaultScreenEnhanced(
                navController = navController,
                ledgerViewModel = ledgerViewModel,
                userId   = authState.userId ?: "",
                userName = authState.userName ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Skill Profile ─────────────────────────────────────────────────────
        composable(Screen.SkillProfile.route) { backStackEntry ->
            val skillViewModel: SkillViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    skillRepository = app.skillRepository,
                    sessionManager  = app.sessionManager
                )
            )
            val streakViewModel: StreakViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    streakRepository = app.streakRepository,
                    sessionManager   = app.sessionManager
                )
            )
            val availabilityViewModel: AvailabilityViewModel = viewModel( 
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    availabilityRepository = app.availabilityRepository,
                    sessionManager         = app.sessionManager
                )
            )
            
            SkillProfileScreenEnhanced(
                navController = navController,
                skillViewModel = skillViewModel,
                streakViewModel = streakViewModel,
                availabilityViewModel = availabilityViewModel, 
                userId = authState.userId ?: "",
                userName = authState.userName ?: "",
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ── Chat ──────────────────────────────────────────────────────────────
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("swapId")          { type = NavType.StringType },
                navArgument("skillName")       { type = NavType.StringType },
                navArgument("counterpartName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatViewModel: ChatViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(chatRepository = app.chatRepository)
            )
            ChatScreen(
                navController   = navController,
                chatViewModel   = chatViewModel,
                swapId          = backStackEntry.arguments?.getString("swapId") ?: "",
                swapSkillName   = backStackEntry.arguments?.getString("skillName") ?: "",
                counterpartName = backStackEntry.arguments?.getString("counterpartName") ?: "",
                currentUserId   = authState.userId ?: ""
            )
        }

        // ── QR Handshake ──────────────────────────────────────────────────────
        composable(
            route = Screen.QRHandshake.route,
            arguments = listOf(
                navArgument("swapId")    { type = NavType.StringType },
                navArgument("skillName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tradeViewModel: TradeViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    swapRepository = app.swapRepository,
                    sessionManager = app.sessionManager
                )
            )
            QRHandshakeScreen(
                navController  = navController,
                tradeViewModel = tradeViewModel,
                swapId         = backStackEntry.arguments?.getString("swapId") ?: "",
                currentUserId  = authState.userId ?: "",
                skillName      = backStackEntry.arguments?.getString("skillName") ?: ""
            )
        }

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                navController = navController,
                authViewModel = authViewModel,
                userId   = authState.userId ?: "",
                userName = authState.userName ?: ""
            )
        }

        // ── Leaderboard ────────────────────────────────────────────────────────
        composable(Screen.Leaderboard.route) { backStackEntry ->
            val leaderboardViewModel: LeaderboardViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    leaderboardRepository = app.leaderboardRepository,
                    sessionManager        = app.sessionManager
                )
            )
            LeaderboardScreen(
                navController         = navController,
                leaderboardViewModel  = leaderboardViewModel,
                currentLat            = 0.0, 
                currentLon            = 0.0
            )
        }

        // ── Group Session Creation ─────────────────────────────────────────────
        composable(Screen.CreateGroupSession.route) { backStackEntry ->
            val vm: GroupSessionViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    groupSessionRepository = app.groupSessionRepository,
                    sessionManager = app.sessionManager
                )
            )
            CreateGroupSessionScreen(navController, vm)
        }

        composable(
            route = Screen.GroupSessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val vm: GroupSessionViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = ViewModelFactory(
                    groupSessionRepository = app.groupSessionRepository,
                    sessionManager = app.sessionManager
                )
            )
            GroupSessionDetailScreen(
                navController  = navController,
                groupSessionViewModel = vm,
                sessionId      = sessionId,
                currentUserId  = authState.userId ?: ""
            )
        }
    }

    // Pass nav controller back to MainActivity
    LaunchedEffect(Unit) {
        onNavControllerReady(navController)
        // Handle cold-start deep link
        pendingIntent?.let {
            DeepLinkHandler.handle(it, navController)
        }
    }
}