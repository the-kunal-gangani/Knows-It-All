# 🌟 KnowItAll — P2P Skill Trading Platform

> *Bridging the Knowledge Gap, One Trade at a Time.*

[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-Material_3-blue?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![Architecture](https://img.shields.io/badge/Architecture-MVVM_Clean-red)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

KnowItAll is a peer-to-peer skill-exchange Android app that turns personal expertise into currency. Whether you're a coder wanting to learn pottery or a carpenter looking for English lessons, KnowItAll facilitates hyper-local skill swaps within a **5 km radius** using a **Hybrid Barter & Token System**.

---

## Table of Contents

1. [Mission & Vision](#-mission--vision)
2. [Screenshots](#-screenshots)
3. [Features](#-features)
4. [How It Works](#-how-it-works)
5. [Token Economy](#-token-economy-tokenomics)
6. [Trust & Security](#-trust--security)
7. [Tech Stack](#-tech-stack)
8. [Architecture](#-architecture)
9. [Project Structure](#-project-structure)
10. [Getting Started](#-getting-started)
11. [Firestore Data Model](#-firestore-data-model)
12. [Firestore Security Rules](#-firestore-security-rules)
13. [Push Notifications](#-push-notifications)
14. [Roadmap](#-roadmap)
15. [FAQ](#-faq)

---

## Mission & Vision

In many communities, valuable skills go untapped because there is no formal marketplace for them. KnowItAll aims to:

- **Monetize Time** — Allow users to "earn" by teaching others.
- **Democratize Learning** — Make education accessible without traditional monetary barriers.
- **Build Trust** — Use technology to create verifiable, immutable records of skill proficiency.

---

## Screenshots

> *Add screenshots here after first build — place images in `assets/screenshots/`.*

| Feed | Skill Radar | Trade Center | The Vault |
|------|------------|--------------|-----------|
| ![Feed](assets/screenshots/feed.png) | ![Radar](assets/screenshots/radar.png) | ![Trades](assets/screenshots/trades.png) | ![Vault](assets/screenshots/vault.png) |

---

## Features

### Core
- **Skill Radar** — OSMDroid map showing nearby mentors within 5 km; tap a pin to view profile and request a swap
- **Skill Feed** — Discovery feed: new skills, completed swaps, top mentors, trending categories, and Wishlist items
- **Three Swap Types** — Barter (1:1 QR handshake), Token, or Hybrid
- **In-App Chat** — Real-time per-swap messaging via Firestore
- **Jitsi Video Calls** — One-tap Jitsi Meet room, unique per swap, no account needed
- **QR Handshake** — ZXing-based mutual QR verification for in-person barter sessions

### Economy & Trust
- **SkillToken Escrow** — Tokens locked on swap acceptance; released after star-rated completion
- **Partial Hold System** — Low ratings hold a percentage in escrow for a 7-day dispute window
- **Blockchain-Inspired Trust Ledger** — SHA-256 hash-chained transaction history; tamper-proof
- **Trust Leaderboard** — Global (top 10) and Nearby (top 10 within 5 km) rankings with podium UI
- **Streak Rewards** — Weekly activity streaks with milestone badges and bonus token claims

### Profile & Passport
- **Skill Passport PDF** — iText 7-generated verified PDF for job applications and portfolios
- **Share Web Link** — HTML passport uploaded to Firebase Storage; shareable public URL
- **Availability Calendar** — Recurring and one-off teaching slots; auto-booked on swap acceptance
- **Skill Wishlist** — Post what you want to learn; matching mentors receive instant FCM push

### Onboarding & UX
- **4-Step Onboarding** — Welcome → Add skill → Set availability → Find mentors (no-skip enforced)
- **Push Notifications** — FCM for all major swap lifecycle events including session reminders
- **Activity Streaks** — 7 / 30 / 100-day milestones with badge and token reward
- **Deep Links** — `knowitall://swap/{id}` opens directly to a swap; shareable via WhatsApp
- **Dark Mode** — Full dark color scheme support via Material 3 theming

---

## How It Works

### 1. Onboard
New users complete a mandatory 4-step flow: **Welcome → Add First Skill → Set Availability → Find Mentors**. Returning users who skipped onboarding are redirected on next login.

### 2. Discover
Open the **Skill Feed** for a discovery scroll or switch to the **Skill Radar** for a live OSMDroid map of nearby mentors. Green dot = currently online.

### 3. Connect & Request
Tap a mentor's map pin → view their profile → **Connect & Request Swap**. Choose a swap type:

| Type | Mechanism |
|------|-----------|
| **Barter** | QR Handshake — mutual scan to confirm in-person session |
| **Token** | SkillTokens transferred via escrow on completion |
| **Hybrid** | Both tokens and a reciprocal skill exchange |

For Token/Hybrid swaps, pick the number of sessions, duration, and an available time slot from the mentor's Availability Calendar.

### 4. The Session

**Online (Token / Hybrid)**
1. Coordinate via in-app Chat.
2. Tap **Video Call** to open a Jitsi Meet room unique to this swap.
3. Mark session complete after each call.

**In-Person (Barter)**
1. Meet up at the agreed location.
2. Both users open the **QR Handshake** screen.
3. Each generates a signed QR; scan each other's.
4. When both sides verify, the session is marked complete automatically.

### 5. Proof & Rating

For Token/Hybrid swaps, submit a **Proof of Completion** (description + actual duration) before finalising. Then rate:

| Rating | Tokens Released to Mentor | Tokens Held in Escrow |
|--------|---------------------------|-----------------------|
| ⭐⭐⭐⭐⭐ | 100% | 0% |
| ⭐⭐⭐⭐ | 75% | 25% (7-day hold) |
| ⭐⭐⭐ | 50% | 50% (7-day hold) |
| ⭐⭐ or below | 25% | 75% (7-day hold) |

Held tokens auto-release to the mentor after 7 days if no dispute is raised.

### 6. Finalize
The **Trust Ledger** records every transaction with SHA-256 hash chaining. Trust Scores update automatically. Generate and share a verified **Skill Passport** from the Vault.

---

## Token Economy (Tokenomics)

KnowItAll uses a dual-currency model to solve the *double coincidence of wants*:

- **Direct Barter (1:1)** — QR handshake verifies the exchange; no tokens involved.
- **SkillTokens** — Earned by teaching; spent to buy lessons from any mentor.
- **Escrow Protection** — Tokens lock when a swap is accepted and release only after the learner rates the session.
- **Dispute Window** — Low ratings hold a portion in escrow for 7 days before auto-release.
- **Streak Bonuses** — Weekly activity earns bonus tokens at 7 / 30 / 100-day milestones.

---

## Trust & Security

### Blockchain-Inspired Trust Ledger
Every trade is recorded with SHA-256 hash chaining — each record includes the previous hash, making history tamper-proof without a blockchain node.

### Dual Verification
- **Online sessions** — Both parties submit proof; learner rates to release escrow.
- **In-person sessions** — Mutual QR scan; both codes must verify before session completes.

### Firestore Security
Row-level security rules lock every collection. Swap data is write-once (never deletable). Trust Ledger entries are immutable after creation.

---

## Tech Stack

### Mobile (Android)

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| DI | Manual (custom ViewModelFactory — no Hilt) |
| Navigation | Jetpack Navigation Compose |
| Maps | OSMDroid (free, no API key) |
| Video Calls | Jitsi Meet (browser link, no SDK install) |
| QR Codes | ZXing |
| PDF Generation | iText 7 |
| Deep Links | `knowitall://` custom scheme |

### Backend (Firebase — fully serverless)

| Service | Usage |
|---------|-------|
| Firebase Auth | Email/Password registration and session management |
| Firestore | All data storage and real-time sync |
| Firebase Cloud Messaging | Push notifications |
| Firebase Storage | Skill Passport HTML pages (public read) |

### Security & Utilities

| Tool | Usage |
|------|-------|
| SHA-256 | Trust Ledger hash chaining |
| FCM HTTP v1 API | Client-side push notification dispatch |
| Firestore Security Rules | Database access control |
| Firebase Storage Rules | Passport file access control |

---

## Architecture

### High-Level Component Diagram

```
┌─────────────────────────────────────────────────┐
│              Mobile Client (Android)             │
│                                                  │
│  ┌────────────────┐     ┌──────────────────────┐ │
│  │  Jetpack       │────▶│     ViewModels       │ │
│  │  Compose UI    │     │  (MVVM Clean Arch)   │ │
│  └────────────────┘     └──────────┬───────────┘ │
│                                    │              │
│                         ┌──────────▼───────────┐ │
│                         │  Firebase Repositories│ │
│                         └──────────┬───────────┘ │
└────────────────────────────────────┼─────────────┘
                                     │
            ┌────────────────────────┼──────────────────────┐
            │         Firebase Backend (Serverless)          │
            │                                               │
            │  ┌──────────┐  ┌──────────┐  ┌────────────┐  │
            │  │Firestore │  │ Firebase │  │  Firebase  │  │
            │  │ Database │  │   Auth   │  │  Storage   │  │
            │  └──────────┘  └──────────┘  └────────────┘  │
            │                                               │
            │  ┌──────────────────────────────────────────┐ │
            │  │      Firebase Cloud Messaging (FCM)      │ │
            │  └──────────────────────────────────────────┘ │
            └───────────────────────────────────────────────┘
```

### Layer Structure

```
app/
├── data/
│   ├── model/          # Data classes (User, Skill, Swap, etc.)
│   └── repository/     # Firebase repository implementations
├── ui/
│   ├── feed/           # Skill Feed screen
│   ├── radar/          # OSMDroid Skill Radar screen
│   ├── trade/          # Trade Center (Active / History tabs)
│   ├── vault/          # Vault, Trust Ledger, Skill Passport
│   ├── profile/        # Skill Profile, Availability Calendar, Streaks
│   ├── leaderboard/    # Trust Leaderboard
│   ├── chat/           # In-swap Chat
│   ├── qr/             # QR Handshake
│   ├── onboarding/     # 4-step Onboarding flow
│   └── auth/           # Login / Register
├── viewmodel/          # ViewModels with custom ViewModelFactory
├── service/
│   └── KnowItAllMessagingService.kt  # FCM token refresh & notifications
└── util/               # SHA-256 hashing, PDF generation helpers
```

---

## Project Structure

```
Knows-It-All/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/know_it_all/
│   │   │   ├── res/
│   │   │   │   ├── drawable/          # App icons and vector assets
│   │   │   │   ├── mipmap-*/          # Launcher icons (hdpi → xxxhdpi + anydpi-v26)
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       ├── backup_rules.xml
│   │   │   │       ├── data_extraction_rules.xml
│   │   │   │       ├── file_paths.xml         # FileProvider paths for PDF sharing
│   │   │   │       └── network_security_config.xml
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/               # Instrumented tests
│   │   └── test/                      # Unit tests
│   ├── build.gradle.kts
│   ├── google-services.json           ← Add your own (not committed to repo)
│   └── proguard-rules.pro
├── docs/
│   ├── model-selection-playbook.md
│   ├── runbook.md
│   └── token-optimization-guide.md
├── UML/
│   ├── Activity_Diagram_KIA.docx
│   ├── Activity.png
│   ├── Class_Diagrams_KIA.docx
│   ├── Class.png
│   ├── Data_Dictionary_Knows_It_All.docx
│   ├── ERD.jpeg
│   ├── Use_Case_KIA.docx
│   ├── UseCase.png
│   └── KnowsItAll.pptx                # Pitch deck
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml             # Version catalog
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── model_capabilities.yaml
├── settings.gradle.kts
└── README.md
```

---

## Getting Started

### Prerequisites

- Android Studio Ladybug (2024.2.1) or later
- Android SDK 26+ (minSdk 26)
- A Firebase project with the following services enabled:
  - Authentication (Email/Password)
  - Firestore Database
  - Cloud Messaging
  - Storage

### Installation

**1. Clone the repository**

```bash
git clone https://github.com/kunal-gangani/Knows-It-All.git
cd Knows-It-All
```

**2. Firebase Setup**

1. Go to [console.firebase.google.com](https://console.firebase.google.com) and create a project.
2. Register an Android app with package name `com.example.know_it_all`.
3. Download `google-services.json` and place it at `app/google-services.json`.
4. Enable **Authentication** → Email/Password.
5. Enable **Firestore Database** in production mode.
6. Enable **Cloud Messaging**.
7. Enable **Storage**.
8. Apply Firestore Security Rules from `firestore.rules`.
9. Apply Storage Rules from `storage.rules`.

**3. Firestore Composite Indexes**

When you first open the Skill Radar, Firestore will log an error with a direct link to create the required composite index. Click the link and wait ~2 minutes for it to build.

**4. Build and Run**

```bash
./gradlew installDebug
```

Or open the project in Android Studio and press **Run ▶**.

---

## Firestore Data Model

```
users/{userId}
  ├── name, email, latitude, longitude
  ├── skillTokenBalance (long)
  ├── trustScore (float)
  ├── isOnline (bool)
  └── fcmToken (string)

skills/{skillId}
  ├── userId, skillName, category
  ├── proficiencyLevel, tokenValue
  └── endorsements (int)

swaps/{swapId}
  ├── mentorId, learnerId
  ├── swapType (BARTER | TOKEN | HYBRID)
  ├── status (REQUESTED | ACTIVE | COMPLETED | CANCELLED)
  ├── tokenAmount, tokensInEscrow
  ├── totalSessions, completedSessions, durationMinutes
  ├── proofDescription, rating (float)
  └── availabilitySlotId

token_escrow/{swapId}
  ├── totalTokens, heldAmount
  ├── status
  └── autoReleaseAt (timestamp)

trust_ledger/{transactionId}
  ├── swapId, mentorId, learnerId
  ├── previousHash, currentHash (SHA-256)
  ├── ratingGiven (int)
  └── ratingComment

chats/{swapId}/messages/{messageId}
  ├── senderId, text
  └── timestamp

availability/{slotId}
  ├── userId, slotType (RECURRING | ONE_OFF)
  ├── dayOfWeek, specificDate
  ├── startHour, startMinute, durationMinutes
  └── status (AVAILABLE | BOOKED)

wishlist/{wishId}
  ├── userId, skillName, category
  ├── description, isOpen
  └── createdAt

streaks/{userId}
  ├── currentStreak, longestStreak
  ├── lastActivityAt, totalActivities
  └── claimedMilestones (list)
```

---

## Firestore Security Rules

| Collection | Read | Write |
|---|---|---|
| `users` | Any authenticated user | Owner only |
| `skills` | Any authenticated user | Owner only (endorsements open) |
| `swaps` | Participants only | Participants only, never deletable |
| `token_escrow` | Participants only | Participants only, never deletable |
| `trust_ledger` | Any authenticated user | Participants only, immutable |
| `chats/{swapId}/messages` | Participants only | Sender must be participant |
| `availability` | Any authenticated user | Owner only |
| `wishlist` | Any authenticated user | Owner only |
| `streaks` | Owner only | Owner only |
| Everything else | ❌ Blocked | ❌ Blocked |

**Firebase Storage Rules** for Skill Passport HTML pages:
- Public read (anyone with the link)
- Write restricted to the passport owner only

---

## Push Notifications

Real-time FCM notifications cover all major swap lifecycle events:

| Event | Recipient |
|-------|-----------|
| New swap request received | Mentor |
| Swap request accepted | Learner |
| Session starting in 30 minutes | Both parties |
| New chat message | Counterpart |
| Tokens released from escrow | Mentor |
| Swap cancelled | Counterpart |
| Session marked complete | Both parties |
| Skill Wishlist match found | Matching mentors |

FCM tokens are saved to Firestore on login and refreshed automatically by `KnowItAllMessagingService`.

---

## Streak Rewards

Users earn bonus tokens for weekly platform activity.

**What counts as activity:** Completing a swap, sending a chat message, or adding a new skill.

**Rule:** Stay active at least once every 7 days to keep the streak alive. Missing the 7-day window resets the streak to 0.

| Streak | Bonus Tokens | Badge |
|--------|-------------|-------|
| 7 days | +5T | ⚡ |
| 30 days | +20T | 🔥 |
| 100 days | +50T | 👑 |

Streak progress is shown in the **Skill Profile** screen with a progress bar, milestone badges, and a one-tap claim button when a milestone is reached.

---

## Onboarding Flow

New users are guided through 4 mandatory steps after registration:

1. **Welcome** — App overview with feature highlights
2. **Add First Skill** — Name, category, proficiency level, token value
3. **Set Availability** — First recurring teaching slot
4. **Find Mentors** — Next-steps guide

Onboarding completion is stored locally. Returning users go directly to the Feed. Users who registered but never finished onboarding are redirected on next login.

---

## Roadmap

The following features are planned for future releases. They are intentionally excluded from the current build to keep the scope focused.

### AI Skill Matching
When a user adds a skill, TensorFlow Lite (already a declared dependency) will suggest complementary skills to learn and surface nearby mentors who teach them. This moves discovery from purely manual search to intelligent recommendation.

### Skill Verification Badges
After a mentor teaches a skill in 5 or more swaps with a sustained 4★+ average rating, the skill is automatically marked as **Verified**. A badge appears on their profile and in the Feed — a self-regulating quality signal that requires no admin intervention.

### Token Economy Events
Weekly platform-wide events that double SkillToken rewards for a specific category (e.g. "2× tokens for all Tech swaps this week"). Creates urgency, spikes in daily active users, and surfaces underutilised skill categories.

### Offline Skills Archive / Session Certificates
Every completed swap session auto-generates a PDF certificate stored in The Vault alongside the Skill Passport. Users build a portfolio of timestamped, hash-verified proof of learning — shareable directly to job applications or LinkedIn.

---

## FAQs

**Q: Is it free to use?**
Barter trades require no tokens. Tokens are earned by teaching — no purchase needed.

**Q: What if someone scams me?**
Tokens are held in escrow until both parties complete and rate the session. Low ratings trigger a 7-day hold. The Trust Ledger records every transaction with SHA-256 hash chaining.

**Q: What happens to held escrow tokens after 7 days?**
They auto-release to the mentor if no dispute is raised.

**Q: Why 5 km radius?**
KnowItAll focuses on real-world local communities. 5 km is optimal for in-person trades.

**Q: Do I need a Google Maps API key?**
No. The Skill Radar uses OpenStreetMap via OSMDroid — completely free, no billing required.

**Q: How do video calls work?**
Tapping Video Call opens a Jitsi Meet room unique to that swap. Both users land in the same room with no account or app install needed.

**Q: How does the Skill Passport web link work?**
An HTML page is generated and uploaded to Firebase Storage. The public URL is shareable anywhere — anyone with the link can view it in a browser, no app required.

**Q: What resets my streak?**
Missing 7 consecutive days of activity resets the streak to 0. Stay active by completing swaps, chatting, or adding skills at least once a week.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repo
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

Distributed under the MIT License. See [`LICENSE`](LICENSE) for more information.

---

## Author

**Kunal Gangani**
- GitHub: [@kunal-gangani](https://github.com/kunal-gangani)
- Project: [github.com/kunal-gangani/Knows-It-All](https://github.com/kunal-gangani/Knows-It-All)

---

*© 2026 KnowItAll — Empowering Peer-to-Peer Learning.*