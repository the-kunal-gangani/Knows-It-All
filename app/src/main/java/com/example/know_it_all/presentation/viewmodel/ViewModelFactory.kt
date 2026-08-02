package com.example.know_it_all.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.know_it_all.presentation.viewmodel.ChatViewModel
import com.example.know_it_all.data.repository.FirebaseLedgerRepository
import com.example.know_it_all.data.repository.FirebaseSkillRepository
import com.example.know_it_all.data.repository.FirebaseSwapRepository
import com.example.know_it_all.data.repository.FirebaseUserRepository
import com.example.know_it_all.data.repository.FirebaseChatRepository
import com.example.know_it_all.data.repository.AvailabilityRepository
import com.example.know_it_all.data.repository.FeedRepository
import com.example.know_it_all.data.repository.LeaderboardRepository
import com.example.know_it_all.data.repository.WishlistRepository
import com.example.know_it_all.data.repository.StreakRepository
import com.example.know_it_all.data.repository.GroupSessionRepository
import com.example.know_it_all.util.SessionManager

class ViewModelFactory(
    private val userRepository: FirebaseUserRepository? = null,
    private val skillRepository: FirebaseSkillRepository? = null,
    private val swapRepository: FirebaseSwapRepository? = null,
    private val ledgerRepository: FirebaseLedgerRepository? = null,
    private val sessionManager: SessionManager? = null,
    private val chatRepository: FirebaseChatRepository? = null,
    private val feedRepository: FeedRepository? = null,
    private val availabilityRepository: AvailabilityRepository? = null,
    private val leaderboardRepository: LeaderboardRepository? = null,
    private val wishlistRepository: WishlistRepository? = null,
    private val streakRepository: StreakRepository? = null,
    private val groupSessionRepository: GroupSessionRepository? = null
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    userRepository = requireNotNull(userRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(RadarViewModel::class.java) -> {
                RadarViewModel(
                    userRepository = requireNotNull(userRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(FeedViewModel::class.java) -> {
                FeedViewModel(
                    feedRepository = requireNotNull(feedRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(TradeViewModel::class.java) -> {
                TradeViewModel(
                    swapRepository = requireNotNull(swapRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(LedgerViewModel::class.java) -> {
                LedgerViewModel(
                    ledgerRepository = requireNotNull(ledgerRepository),
                    skillRepository  = requireNotNull(skillRepository),
                    userRepository   = requireNotNull(userRepository),
                    sessionManager   = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(ChatViewModel::class.java) -> {
                ChatViewModel(
                    chatRepository = requireNotNull(chatRepository)
                ) as T
            }
            modelClass.isAssignableFrom(SkillViewModel::class.java) -> {
                SkillViewModel(
                    skillRepository = requireNotNull(skillRepository),
                    sessionManager  = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(LeaderboardViewModel::class.java) -> {
                LeaderboardViewModel(
                    leaderboardRepository = requireNotNull(leaderboardRepository),
                    sessionManager        = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(AvailabilityViewModel::class.java) -> {
                AvailabilityViewModel(
                    availabilityRepository = requireNotNull(availabilityRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(WishlistViewModel::class.java) -> {
                WishlistViewModel(
                    wishlistRepository = requireNotNull(wishlistRepository),
                    sessionManager = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(StreakViewModel::class.java) -> {
                StreakViewModel(
                    streakRepository = requireNotNull(streakRepository),
                    sessionManager   = requireNotNull(sessionManager)
                ) as T
            }
            modelClass.isAssignableFrom(GroupSessionViewModel::class.java) -> {
                GroupSessionViewModel(
                    groupSessionRepository = requireNotNull(groupSessionRepository),
                    sessionManager         = requireNotNull(sessionManager)
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}