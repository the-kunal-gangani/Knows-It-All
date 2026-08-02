package com.example.know_it_all.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// KnowItAll Design System — Color Tokens
// Calibrated to the Dribbble reference (SkillSwap by Julius Bučelis)
//
// Light theme: cream/off-white backgrounds, near-black text, acid-green accent
// Dark theme:  deep charcoal backgrounds, cream text, acid-green accent
//
// RULE: AcidGreen is used ONLY for:
//   - Primary CTAs ("Connect", "Sign In", "Add Skill")
//   - The online presence dot
//   - Active token balance numerals
// All other UI uses near-black or cream depending on surface.
// =============================================================================

// ======== ACID GREEN — the single electric accent ========
val AcidGreen        = Color(0xFFAAFF00)    // Primary CTA, presence dot
val AcidGreenDark    = Color(0xFF88CC00)    // Pressed state
val AcidGreenMuted   = Color(0x1AAAFF00)    // 10% tint for chip backgrounds

// ======== NEAR-BLACK — primary text and dark surfaces ========
val NearBlack        = Color(0xFF1A1A1A)    // Primary text
val CharcoalGray     = Color(0xFF4A4A4A)    // Secondary text
val WarmGray         = Color(0xFF8A8480)    // Placeholder, disabled, subtle labels

// ======== CREAM — light theme backgrounds and surfaces ========
val Cream            = Color(0xFFF5F0E8)    // Primary background (light)
val CreamDark        = Color(0xFFEDE8DC)    // Card / elevated surface (light)
val CreamDeep        = Color(0xFFE0D8CC)    // Input background, pressed (light)

// ======== WARM OCHRE — secondary accent (active states, calendar) ========
val Ochre            = Color(0xFFE8A020)    // Secondary accent, star ratings
val OchreDark        = Color(0xFFCC8810)    // Pressed ochre

// ======== DARK THEME SURFACES ========
val DeepNavy         = Color(0xFF0F1117)    // Dark background
val DarkSurface      = Color(0xFF1C1F26)    // Dark card surface
val DarkSurfaceRaised= Color(0xFF252830)    // Dark elevated surface

// ======== STATUS ========
val ErrorRed         = Color(0xFFCC3333)
val ErrorContainer   = Color(0xFFFFF0F0)
val SuccessGreen     = AcidGreen            // reuse brand green for success
val WarningAmber     = Ochre                // reuse ochre for warnings

// ======== LIGHT THEME — Material3 role mapping ========
val Primary          = NearBlack
val OnPrimary        = Cream
val PrimaryContainer = AcidGreen            // acid green container (CTA chips)
val OnPrimaryContainer = NearBlack

val Secondary        = Ochre
val OnSecondary      = NearBlack
val SecondaryContainer = OchreDark
val OnSecondaryContainer = Cream

val Tertiary         = CharcoalGray
val OnTertiary       = Cream
val TertiaryContainer = CreamDark
val OnTertiaryContainer = NearBlack

val Background       = Cream
val OnBackground     = NearBlack

val Surface          = CreamDark
val OnSurface        = NearBlack
val SurfaceVariant   = CreamDeep
val OnSurfaceVariant = CharcoalGray

val Outline          = WarmGray
val OutlineVariant   = CreamDeep

val Error            = ErrorRed
val OnError          = Cream
val ErrorContainerColor = ErrorContainer

// ======== DARK THEME — Material3 role mapping ========
val PrimaryDark          = AcidGreen        // acid green stays as CTA in dark mode
val OnPrimaryDark        = NearBlack
val PrimaryContainerDark = Color(0xFF1F2A14) // dark green tint container
val OnPrimaryContainerDark = AcidGreen

val SecondaryDark        = Ochre
val OnSecondaryDark      = NearBlack
val SecondaryContainerDark = Color(0xFF2A1E00)
val OnSecondaryContainerDark = Ochre

val TertiaryDark         = Color(0xFFD4CEBF) // cream-tinted in dark
val OnTertiaryDark       = NearBlack
val TertiaryContainerDark = DarkSurfaceRaised
val OnTertiaryContainerDark = Color(0xFFD4CEBF)

val BackgroundDark       = DeepNavy
val OnBackgroundDark     = Color(0xFFF0EBE3) // slightly warm white

val SurfaceDark          = DarkSurface
val OnSurfaceDark        = Color(0xFFF0EBE3)
val SurfaceVariantDark   = DarkSurfaceRaised
val OnSurfaceVariantDark = Color(0xFFB0A89E)

val OutlineDark          = Color(0xFF4A4A4A)
val OutlineVariantDark   = Color(0xFF2A2A2A)

val ErrorDark            = Color(0xFFFF6B6B)
val OnErrorDark          = Color(0xFF3D0000)
val ErrorContainerDark   = Color(0xFF4A1010)

// ======== GRADIENT HELPERS (for decorative use only) ========
val GradientStart        = NearBlack
val GradientEnd          = CharcoalGray