package com.example.know_it_all

import android.app.Application
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
import com.google.firebase.FirebaseApp

class KnowItAllApplication : Application() {

    val sessionManager: SessionManager by lazy {
        SessionManager(this)
    }

    val userRepository: FirebaseUserRepository by lazy {
        FirebaseUserRepository()
    }

    val skillRepository: FirebaseSkillRepository by lazy {
        FirebaseSkillRepository()
    }

    val swapRepository: FirebaseSwapRepository by lazy {
        FirebaseSwapRepository()
    }

    val ledgerRepository: FirebaseLedgerRepository by lazy {
        FirebaseLedgerRepository()
    }

    val chatRepository: FirebaseChatRepository by lazy {
        FirebaseChatRepository()
    }

    val feedRepository: FeedRepository by lazy { 
        FeedRepository() 
    }

    val availabilityRepository: AvailabilityRepository by lazy {
        AvailabilityRepository()
    }

    val leaderboardRepository: LeaderboardRepository by lazy {
        LeaderboardRepository()
    }

    val wishlistRepository: WishlistRepository by lazy {
        WishlistRepository()
    }

    val streakRepository: StreakRepository by lazy {
        StreakRepository()
    }

    val groupSessionRepository: GroupSessionRepository by lazy {
        GroupSessionRepository()
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}