package com.example.know_it_all.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.know_it_all.data.model.Skill
import com.example.know_it_all.data.model.SkillCategory
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Location: util/SkillPassportShareHelper.kt
 *
 * Two share methods:
 *  1. sharePDF()      — opens Android share sheet with the PDF file
 *  2. shareWebLink()  — uploads an HTML passport to Firebase Storage
 *                       and returns a public URL the user can share
 */
object SkillPassportShareHelper {

    // ── Share PDF via Android share sheet ────────────────────────────────────

    fun sharePDF(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_SUBJECT,
                "My KnowItAll Skill Passport"
            )
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out my verified Skill Passport from KnowItAll — " +
                "a peer-to-peer skill trading platform."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(intent, "Share Skill Passport via")
        )
    }

    // ── Generate web link — uploads HTML to Firebase Storage ─────────────────

    /**
     * Generates a public HTML page and uploads it to Firebase Storage.
     * Returns the public download URL.
     *
     * Firebase Storage free tier: 1GB storage, 10GB/month download — plenty for HTML pages.
     */
    suspend fun generateWebLink(
        userId: String,
        userName: String,
        userEmail: String,
        trustScore: Float,
        completedSwaps: Int,
        skills: List<Skill>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val html      = buildPassportHTML(
                userName      = userName,
                userEmail     = userEmail,
                trustScore    = trustScore,
                completedSwaps = completedSwaps,
                skills        = skills
            )
            val htmlBytes = html.toByteArray(Charsets.UTF_8)
            val storage   = FirebaseStorage.getInstance()
            val ref       = storage.reference
                .child("passports/$userId/passport.html")

            // Set content type so browser renders it as HTML
            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                .setContentType("text/html")
                .build()

            ref.putBytes(htmlBytes, metadata).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Share the web link ────────────────────────────────────────────────────

    fun shareWebLink(context: Context, url: String, userName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "$userName's Skill Passport — KnowItAll")
            putExtra(
                Intent.EXTRA_TEXT,
                "🎓 Check out $userName's verified Skill Passport on KnowItAll!\n\n$url"
            )
        }
        context.startActivity(
            Intent.createChooser(intent, "Share Skill Passport link via")
        )
    }

    // ── Build the HTML passport page ──────────────────────────────────────────

    private fun buildPassportHTML(
        userName: String,
        userEmail: String,
        trustScore: Float,
        completedSwaps: Int,
        skills: List<Skill>
    ): String {
        val trustPercent = (trustScore * 10).toInt().coerceIn(0, 100)
        val skillsHTML   = skills.joinToString("\n") { skill ->
            val categoryColor = when (skill.category) {
                SkillCategory.DIGITAL  -> "#AAFF00"
                SkillCategory.PHYSICAL -> "#E6A817"
                SkillCategory.HYBRID   -> "#6B7280"
            }
            val categoryEmoji = when (skill.category) {
                SkillCategory.DIGITAL  -> "💻"
                SkillCategory.PHYSICAL -> "🔨"
                SkillCategory.HYBRID   -> "⚡"
            }
            """
            <div class="skill-card">
                <div class="skill-header">
                    <span class="skill-emoji">$categoryEmoji</span>
                    <div>
                        <div class="skill-name">${skill.skillName}</div>
                        <div class="skill-meta">${skill.proficiencyLevel.name} · ${skill.category.name}</div>
                    </div>
                    <div class="token-badge" style="background:${categoryColor}20;color:${categoryColor};border:1px solid ${categoryColor}40">
                        ${skill.tokenValue}T
                    </div>
                </div>
                ${if (skill.description.isNotBlank())
                    """<div class="skill-desc">${skill.description}</div>"""
                  else ""}
                ${if (skill.endorsements > 0)
                    """<div class="endorsements">★ ${skill.endorsements} endorsement${if (skill.endorsements > 1) "s" else ""}</div>"""
                  else ""}
            </div>
            """.trimIndent()
        }

        val generatedDate = java.text.SimpleDateFormat(
            "dd MMMM yyyy", java.util.Locale.getDefault()
        ).format(java.util.Date())

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$userName's Skill Passport — KnowItAll</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: #F5F0E8;
            color: #1A1A1A;
            min-height: 100vh;
            padding: 24px 16px;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
        }

        /* Header */
        .header {
            background: #1A1A1A;
            border-radius: 20px;
            padding: 28px;
            margin-bottom: 16px;
        }

        .brand {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 2px;
            color: #6B7280;
            margin-bottom: 16px;
        }

        .user-name {
            font-size: 32px;
            font-weight: 900;
            color: #F5F0E8;
            letter-spacing: -1px;
            line-height: 1.1;
        }

        .user-email {
            font-size: 14px;
            color: #6B7280;
            margin-top: 6px;
        }

        .verified-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #AAFF0020;
            color: #AAFF00;
            border: 1px solid #AAFF0040;
            border-radius: 20px;
            padding: 6px 14px;
            font-size: 12px;
            font-weight: 700;
            margin-top: 16px;
        }

        /* Stats */
        .stats-row {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 10px;
            margin-bottom: 16px;
        }

        .stat-card {
            background: #EDE8DF;
            border-radius: 14px;
            padding: 16px;
            text-align: center;
        }

        .stat-value {
            font-size: 26px;
            font-weight: 900;
            color: #1A1A1A;
            letter-spacing: -1px;
        }

        .stat-label {
            font-size: 10px;
            font-weight: 600;
            color: #6B7280;
            letter-spacing: 1px;
            margin-top: 4px;
        }

        /* Skills */
        .section-title {
            font-size: 11px;
            font-weight: 700;
            letter-spacing: 2px;
            color: #6B7280;
            margin-bottom: 12px;
        }

        .skill-card {
            background: #EDE8DF;
            border-radius: 14px;
            padding: 16px;
            margin-bottom: 10px;
        }

        .skill-header {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .skill-emoji { font-size: 22px; }

        .skill-name {
            font-size: 16px;
            font-weight: 800;
            color: #1A1A1A;
        }

        .skill-meta {
            font-size: 11px;
            color: #6B7280;
            margin-top: 2px;
        }

        .token-badge {
            margin-left: auto;
            padding: 4px 10px;
            border-radius: 8px;
            font-size: 12px;
            font-weight: 700;
        }

        .skill-desc {
            font-size: 13px;
            color: #4B5563;
            margin-top: 10px;
            line-height: 1.5;
        }

        .endorsements {
            font-size: 11px;
            color: #E6A817;
            font-weight: 600;
            margin-top: 8px;
        }

        /* Footer */
        .footer {
            background: #EDE8DF;
            border-radius: 14px;
            padding: 16px;
            margin-top: 16px;
            text-align: center;
        }

        .footer-title {
            font-size: 16px;
            font-weight: 900;
            color: #1A1A1A;
        }

        .footer-sub {
            font-size: 12px;
            color: #6B7280;
            margin-top: 4px;
            line-height: 1.5;
        }

        .generated {
            font-size: 11px;
            color: #9CA3AF;
            text-align: center;
            margin-top: 16px;
        }
    </style>
</head>
<body>
    <div class="container">

        <!-- Header -->
        <div class="header">
            <div class="brand">KNOWITALL · SKILL PASSPORT</div>
            <div class="user-name">$userName</div>
            <div class="user-email">$userEmail</div>
            <div class="verified-badge">✓ Verified via Trust Ledger</div>
        </div>

        <!-- Stats -->
        <div class="stats-row">
            <div class="stat-card">
                <div class="stat-value">${trustPercent}%</div>
                <div class="stat-label">TRUST SCORE</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">$completedSwaps</div>
                <div class="stat-label">SWAPS DONE</div>
            </div>
            <div class="stat-card">
                <div class="stat-value">${skills.size}</div>
                <div class="stat-label">SKILLS</div>
            </div>
        </div>

        <!-- Skills -->
        <div class="section-title">SKILLS & EXPERTISE</div>
        $skillsHTML

        <!-- Footer -->
        <div class="footer">
            <div class="footer-title">KnowItAll</div>
            <div class="footer-sub">
                Bridging the Knowledge Gap, One Trade at a Time.<br>
                This passport is backed by verified transaction hashes on the Trust Ledger.
            </div>
        </div>

        <div class="generated">Generated on $generatedDate</div>
    </div>
</body>
</html>
        """.trimIndent()
    }
}