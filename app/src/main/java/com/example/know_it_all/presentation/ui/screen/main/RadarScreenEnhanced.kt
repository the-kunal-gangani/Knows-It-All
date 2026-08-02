package com.example.know_it_all.presentation.ui.screen.main

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.SwapType
import com.example.know_it_all.data.model.TimeSlot
import com.example.know_it_all.data.model.User
import com.example.know_it_all.data.model.dto.SwapRequestBody
import com.example.know_it_all.data.repository.FirebaseSkillRepository
import com.example.know_it_all.data.repository.FirebaseSwapRepository
import com.example.know_it_all.presentation.ui.components.BottomNavigationBar
import com.example.know_it_all.presentation.viewmodel.RadarViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.CreamDeep
import com.example.know_it_all.ui.theme.ErrorContainerColor
import com.example.know_it_all.ui.theme.ErrorRed
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.Ochre
import com.example.know_it_all.ui.theme.WarmGray
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RadarScreenEnhanced(
    navController: NavHostController,
    radarViewModel: RadarViewModel,
    userId: String,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val radarState by radarViewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    var showMap by remember { mutableStateOf(true) }

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showRequestSheet by remember { mutableStateOf(false) }
    var requestTargetUser by remember { mutableStateOf<User?>(null) }

    val profileSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val requestSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = context.packageName
            osmdroidBasePath = context.filesDir
            osmdroidTileCache = context.filesDir.resolve("osm_tiles")
        }
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            radarViewModel.refreshLocationAndLoad(context)
        }
    }

    Scaffold(
        containerColor = Cream,
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = navController.currentBackStackEntry?.destination?.route
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SKILL RADAR",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = WarmGray
                        )
                        Text(
                            "Nearby mentors",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream),
                actions = {
                    IconButton(onClick = { radarViewModel.toggleOnlineFilter() }) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (radarState.onlineOnly) AcidGreen else CreamDark,
                                    RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (radarState.onlineOnly) NearBlack else WarmGray,
                                        CircleShape
                                    )
                            )
                        }
                    }
                    IconButton(onClick = { showMap = !showMap }) {
                        Icon(
                            imageVector = if (showMap) Icons.AutoMirrored.Filled.List
                                        else Icons.Default.Map,
                            contentDescription = null,
                            tint = NearBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = { radarViewModel.refreshLocationAndLoad(context) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = NearBlack,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(innerPadding)
        ) {
            if (!locationPermissionState.status.isGranted) {
                LocationPermissionPrompt(
                    onRequest = { locationPermissionState.launchPermissionRequest() }
                )
            } else if (showMap) {
                OSMMapView(
                    users = radarState.nearbyUsers,
                    currentLat = radarState.currentLat,
                    currentLon = radarState.currentLon,
                    onMarkerTap = { user -> selectedUser = user },
                    context = context,
                    modifier = Modifier.fillMaxSize()
                )
                RadarStatsStrip(
                    count = radarState.nearbyUsers.size,
                    radiusKm = radarState.radiusKm,
                    isLoading = radarState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter).padding(12.dp)
                )
                radarState.error?.let { err ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .background(ErrorContainerColor, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(err, fontSize = 12.sp, color = ErrorRed)
                    }
                }
            } else {
                MentorListView(
                    users = radarState.nearbyUsers,
                    currentLat = radarState.currentLat,
                    currentLon = radarState.currentLon,
                    isLoading = radarState.isLoading,
                    error = radarState.error,
                    onUserTap = { user -> selectedUser = user },
                    onRetry = {
                        radarViewModel.clearError()
                        radarViewModel.refreshLocationAndLoad(context)
                    }
                )
            }
        }
    }

    selectedUser?.let { user ->
        ModalBottomSheet(
            onDismissRequest = { selectedUser = null },
            sheetState = profileSheetState,
            containerColor = Cream
        ) {
            MentorProfileSheet(
                user = user,
                currentLat = radarState.currentLat,
                currentLon = radarState.currentLon,
                onDismiss = { selectedUser = null },
                onConnect = {
                    requestTargetUser = user
                    selectedUser = null
                    showRequestSheet = true
                }
            )
        }
    }

    if (showRequestSheet && requestTargetUser != null) {
        ModalBottomSheet(
            onDismissRequest = { showRequestSheet = false },
            sheetState = requestSheetState,
            containerColor = Cream
        ) {
            RequestSwapSheet(
                mentor = requestTargetUser!!,
                currentUserId = userId,
                onDismiss = { showRequestSheet = false },
                onRequestSent = {
                    showRequestSheet = false
                    navController.navigate("trade") {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RequestSwapSheet(
    mentor: User,
    currentUserId: String,
    onDismiss: () -> Unit,
    onRequestSent: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val skillRepository = remember { FirebaseSkillRepository() }
    val swapRepository  = remember { FirebaseSwapRepository() }

    var mentorSkills by remember { mutableStateOf<List<Skill>>(emptyList()) }
    var mySkills     by remember { mutableStateOf<List<Skill>>(emptyList()) }
    var selectedMentorSkill by remember { mutableStateOf<Skill?>(null) }
    var selectedMySkill     by remember { mutableStateOf<Skill?>(null) }
    var selectedSwapType    by remember { mutableStateOf(SwapType.TOKEN) }
    var tokenAmount  by remember { mutableStateOf(10L) }
    var isLoading    by remember { mutableStateOf(false) }
    var isSending    by remember { mutableStateOf(false) }
    var error        by remember { mutableStateOf<String?>(null) }
    var success      by remember { mutableStateOf(false) }
    
    // Explicit type state parameters fixed cleanly
    var totalSessions    by remember { mutableIntStateOf(1) }
    var durationMinutes  by remember { mutableIntStateOf(60) }
    var showSessionSetup by remember { mutableStateOf(false) }
    var showSlotPicker   by remember { mutableStateOf(false) }
    var selectedSlot     by remember { mutableStateOf<TimeSlot?>(null) }

    LaunchedEffect(Unit) {
        isLoading = true
        skillRepository.getUserSkills(mentor.uid).onSuccess { mentorSkills = it }
        skillRepository.getUserSkills(currentUserId).onSuccess { mySkills = it }
        isLoading = false
    }

    // Wrapped the layout hierarchy inside a smooth vertical scroll viewport
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Request a Swap",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack
                )
                Text(
                    "with ${mentor.name}",
                    fontSize = 14.sp,
                    color = CharcoalGray
                )
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(NearBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    mentor.name.take(1).uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = AcidGreen
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(80.dp), Alignment.Center) {
                CircularProgressIndicator(color = NearBlack, strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp))
            }
        } else if (success) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("✅", fontSize = 40.sp)
                Text(
                    "Request Sent!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = NearBlack
                )
                Text(
                    "${mentor.name} will be notified.\nCheck the Trade screen for updates.",
                    fontSize = 14.sp,
                    color = CharcoalGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp
                )
                Button(
                    onClick = onRequestSent,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
                ) {
                    Text("View in Trade", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionLabel("Swap Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SwapType.values().forEach { type ->
                        val isSelected = selectedSwapType == type
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) NearBlack else CreamDark,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedSwapType = type }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                type.name,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AcidGreen else CharcoalGray
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionLabel("Skill you want to learn")
                if (mentorSkills.isEmpty()) {
                    Text(
                        "${mentor.name} hasn't added any skills yet",
                        fontSize = 13.sp,
                        color = WarmGray
                    )
                } else {
                    // Changed from LazyColumn to a standard layout iteration loop
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        mentorSkills.forEach { skill ->
                            SkillSelectRow(
                                skill = skill,
                                isSelected = selectedMentorSkill?.skillId == skill.skillId,
                                onClick = { selectedMentorSkill = skill }
                            )
                        }
                    }
                }
            }

            if (selectedSwapType == SwapType.BARTER || selectedSwapType == SwapType.HYBRID) {
                Column {
                    SectionLabel("Skill you offer in return")
                    if (mySkills.isEmpty()) {
                        Text(
                        "You haven't added any skills yet.\nGo to Skill Profile to add one.",
                        fontSize = 13.sp,
                        color = WarmGray,
                        lineHeight = 20.sp
                    )
            } else {
                // Enforce a Composable Layout Scope using Column
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    mySkills.forEach { skill ->
                        SkillSelectRow(
                            skill = skill,
                            isSelected = selectedMySkill?.skillId == skill.skillId,
                            onClick = { selectedMySkill = skill }
                        )
                    }
                }
            }
        }
    }

            if (selectedSwapType == SwapType.TOKEN || selectedSwapType == SwapType.HYBRID) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Token amount to offer")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5L, 10L, 20L, 50L).forEach { amount ->
                            val isSelected = tokenAmount == amount
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) AcidGreen else CreamDark,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { tokenAmount = amount }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    "${amount}T",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) NearBlack else CharcoalGray
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "$totalSessions session(s) × ${durationMinutes}min",
                            fontSize = 13.sp, color = CharcoalGray
                        )
                        androidx.compose.material3.TextButton(
                            onClick = { showSessionSetup = true }
                        ) {
                            Text("Change", color = AcidGreen, fontSize = 13.sp)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Select a Time Slot")
                    selectedSlot?.let { slot ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AcidGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "📅 ${slot.displayLabel}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = NearBlack
                            )
                        }
                    }
                    Button(
                        onClick = { showSlotPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(CreamDark, NearBlack)
                    ) {
                        Text(
                            if (selectedSlot != null) "Change slot" else "Pick a time slot",
                            fontSize = 13.sp
                        )
                    }
                }
            }

            error?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ErrorContainerColor, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(it, fontSize = 13.sp, color = ErrorRed)
                }
            }

            Button(
                onClick = {
                    if (selectedMentorSkill == null) {
                        error = "Please select a skill to learn"
                        return@Button
                    }
                    if ((selectedSwapType == SwapType.BARTER ||
                        selectedSwapType == SwapType.HYBRID) &&
                        selectedMySkill == null) {
                        error = "Please select a skill to offer"
                        return@Button
                    }

                    error = null
                    isSending = true

                    scope.launch {
                        val request = SwapRequestBody(
                            mentorId        = mentor.uid,
                            learnerId       = currentUserId,
                            mentorSkillId   = selectedMentorSkill!!.skillId,
                            learnerSkillId  = selectedMySkill?.skillId,
                            swapType        = selectedSwapType,
                            tokenAmount     = if (selectedSwapType == SwapType.TOKEN ||
                                            selectedSwapType == SwapType.HYBRID)
                                            tokenAmount else 0L,
                            totalSessions   = totalSessions,
                            durationMinutes = durationMinutes
                        )

                        swapRepository.requestSwap(request).fold(
                            onSuccess = { success = true },
                            onFailure = { e ->
                                error = e.message ?: "Failed to send request"
                            }
                        )
                        isSending = false
                    }
                },
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AcidGreen,
                    contentColor = NearBlack,
                    disabledContainerColor = CreamDark,
                    disabledContentColor = WarmGray
                )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = NearBlack,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Sending...", fontWeight = FontWeight.Bold)
                } else {
                    Text("Send Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel", color = CharcoalGray)
            }
        }
    }

    if (showSessionSetup) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showSessionSetup = false },
            containerColor = Cream
        ) {
            SessionSetupSheet(
                swapTypeName = selectedSwapType.name,
                onConfirm = { sessions, duration ->
                    totalSessions   = sessions
                    durationMinutes = duration
                    showSessionSetup = false
                },
                onDismiss = { showSessionSetup = false }
            )
        }
    }

    if (showSlotPicker) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showSlotPicker = false },
            containerColor = Cream
        ) {
            SlotPickerSheet(
                mentorUserId = mentor.uid,
                mentorName = mentor.name,
                onSlotSelected = { slot ->
                    selectedSlot = slot
                    showSlotPicker = false
                },
                onDismiss = { showSlotPicker = false }
            )
        }
    }
}

@Composable
private fun SkillSelectRow(skill: Skill, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) NearBlack else CreamDark,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    if (isSelected) AcidGreen else WarmGray,
                    CircleShape
                )
        )
        Column(Modifier.weight(1f)) {
            Text(
                skill.skillName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Cream else NearBlack
            )
            Text(
                "${skill.proficiencyLevel.name} · ${skill.tokenValue}T",
                fontSize = 11.sp,
                color = if (isSelected) WarmGray else CharcoalGray
            )
        }
        if (isSelected) {
            Text("✓", fontSize = 14.sp, color = AcidGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = CharcoalGray,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun OSMMapView(
    users: List<User>,
    currentLat: Double,
    currentLon: Double,
    onMarkerTap: (User) -> Unit,
    context: Context,
    modifier: Modifier = Modifier
) {
    val centerLat = if (currentLat != 0.0) currentLat else 18.6298
    val centerLon = if (currentLon != 0.0) currentLon else 73.7997

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(centerLat, centerLon))
            minZoomLevel = 4.0
            maxZoomLevel = 19.0
            val myLocationOverlay = MyLocationNewOverlay(
                GpsMyLocationProvider(context), this
            )
            myLocationOverlay.enableMyLocation()
            overlays.add(myLocationOverlay)
        }
    }

    LaunchedEffect(users, currentLat, currentLon) {
        if (mapView.overlays.size > 1) {
            mapView.overlays.subList(1, mapView.overlays.size).clear()
        }
        users.forEach { user ->
            if (user.latitude != 0.0 && user.longitude != 0.0) {
                val marker = Marker(mapView).apply {
                    position = GeoPoint(user.latitude, user.longitude)
                    icon = createInitialsMarker(
                        context = context,
                        initials = user.name.take(1).uppercase(),
                        isOnline = user.isOnline
                    )
                    title = user.name
                    snippet = "★ ${String.format("%.1f", user.trustScore)}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    setOnMarkerClickListener { _, _ ->
                        onMarkerTap(user)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        }
        if (currentLat != 0.0) {
            mapView.controller.animateTo(GeoPoint(currentLat, currentLon))
        }
        mapView.invalidate()
    }

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose { mapView.onPause() }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}

private fun createInitialsMarker(
    context: Context,
    initials: String,
    isMe: Boolean = false,
    isOnline: Boolean = false
): BitmapDrawable {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val bgColor = if (isMe) android.graphics.Color.parseColor("#AAFF00")
                else android.graphics.Color.parseColor("#1A1A1A")
    val textColor = if (isMe) android.graphics.Color.parseColor("#1A1A1A")
                else android.graphics.Color.parseColor("#F5F0E8")
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = bgColor })
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f,
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.STROKE; strokeWidth = 3f
        })
    val tp = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor; textSize = 36f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(initials, size / 2f, size / 2f - (tp.descent() + tp.ascent()) / 2f, tp)
    if (isOnline) {
        canvas.drawCircle(size - 14f, 14f, 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#AAFF00")
            })
        canvas.drawCircle(size - 14f, 14f, 10f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                style = Paint.Style.STROKE; strokeWidth = 2f
            })
    }
    return BitmapDrawable(context.resources, bitmap)
}

// =============================================================================
// Supporting composables
// =============================================================================

@Composable
private fun MentorProfileSheet(
    user: User,
    currentLat: Double,
    currentLon: Double,
    onDismiss: () -> Unit,
    onConnect: () -> Unit           // now triggers swap request flow
) {
    val distance = calculateDistanceKm(currentLat, currentLon, user.latitude, user.longitude)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.size(72.dp).background(NearBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.name.take(1).uppercase(),
                    fontSize = 30.sp, fontWeight = FontWeight.Black, color = AcidGreen
                )
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(user.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = NearBlack)
                    if (user.isOnline) Box(Modifier.size(8.dp).background(AcidGreen, CircleShape))
                }
                Text("${String.format("%.1f", distance)} km away", fontSize = 13.sp, color = CharcoalGray)
                Text(
                    "★ ${String.format("%.1f", user.trustScore)} · ${user.skillTokenBalance} tokens",
                    fontSize = 12.sp, color = Ochre, fontWeight = FontWeight.Medium
                )
            }
        }

        if (user.profileVerified) {
            Box(
                modifier = Modifier
                    .background(AcidGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .border(1.dp, AcidGreen.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("✓ Verified Profile", fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold, color = NearBlack)
            }
        }

        // Connect now opens the Request Swap sheet
        Button(
            onClick = onConnect,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)
        ) {
            Text("Connect & Request Swap", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Close", color = CharcoalGray)
        }
    }
}

@Composable
private fun MentorListView(
    users: List<User>,
    currentLat: Double,
    currentLon: Double,
    isLoading: Boolean,
    error: String?,
    onUserTap: (User) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        RadarStatsStrip(users.size, 5.0, isLoading, Modifier.padding(vertical = 8.dp))
        error?.let {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ErrorContainerColor, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(it, fontSize = 12.sp, color = ErrorRed)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(NearBlack, Cream),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Retry", fontSize = 13.sp) }
            }
        }
        if (!isLoading && users.isEmpty() && error == null) RadarEmptyState()
        users.forEach { user ->
            MentorListCard(
                user = user,
                distanceKm = calculateDistanceKm(currentLat, currentLon, user.latitude, user.longitude),
                onClick = { onUserTap(user) }
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MentorListCard(user: User, distanceKm: Double, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NearBlack)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(48.dp).background(AcidGreen, CircleShape),
                contentAlignment = Alignment.Center) {
                Text(user.name.take(1).uppercase(), fontSize = 20.sp,
                    fontWeight = FontWeight.Black, color = NearBlack)
            }
            Column(Modifier.weight(1f)) {
                Text(user.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Cream)
                Text("${String.format("%.1f", distanceKm)} km · ${user.skillTokenBalance}T",
                    fontSize = 12.sp, color = WarmGray)
            }
            if (user.isOnline) Box(Modifier.size(10.dp).background(AcidGreen, CircleShape))
        }
    }
}

@Composable
private fun RadarStatsStrip(
    count: Int, radiusKm: Double, isLoading: Boolean, modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .background(Cream.copy(alpha = 0.95f), RoundedCornerShape(14.dp))
            .border(1.dp, CreamDark, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(if (isLoading) "Scanning..." else "$count people found",
                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NearBlack)
            Text("Within ${radiusKm}km radius", fontSize = 12.sp, color = CharcoalGray)
        }
        Box(Modifier.size(8.dp).background(if (isLoading) Ochre else AcidGreen, CircleShape))
    }
}

@Composable
private fun LocationPermissionPrompt(onRequest: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("📍", fontSize = 48.sp)
        Spacer(Modifier.height(20.dp))
        Text("Enable Location", fontSize = 26.sp, fontWeight = FontWeight.Black, color = NearBlack)
        Spacer(Modifier.height(8.dp))
        Text("We need your location to find\nnearby mentors on the map.",
            fontSize = 14.sp, color = CharcoalGray, lineHeight = 21.sp)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onRequest,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(AcidGreen, NearBlack)) {
            Text("Grant Permission", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RadarEmptyState() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🔍", fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text("No mentors nearby", fontSize = 20.sp, fontWeight = FontWeight.Black, color = NearBlack)
        Spacer(Modifier.height(6.dp))
        Text("Try increasing the radius\nor check back later",
            fontSize = 13.sp, color = CharcoalGray, lineHeight = 20.sp)
    }
}

private fun calculateDistanceKm(
    lat1: Double, lon1: Double, lat2: Double, lon2: Double
): Double {
    if (lat1 == 0.0 || lat2 == 0.0) return 0.0
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}