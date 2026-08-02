package com.example.know_it_all.util

import android.content.Context
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.File

/**
 * Fixes applied:
 *
 *  1. Broken Paragraph chain: the original code called
 *       document.add(Paragraph("VERIFIED SKILLS"))
 *           .setFont(...).setFontSize(...)
 *     This doesn't compile — document.add() returns Document, not Paragraph,
 *     so chaining .setFont() on the result fails. Every styled paragraph must
 *     be fully constructed BEFORE being passed to document.add().
 *     All paragraphs are now built as local variables first, then added.
 *
 *  2. Table header cells now use Cell() with a bold Paragraph inside
 *     instead of addHeaderCell(String) — the String overload doesn't
 *     support font styling and produces plain unstyled headers.
 *
 *  3. File written to context.getExternalFilesDir("passports") instead of
 *     context.cacheDir. Cache files can be deleted by the OS at any time,
 *     which would silently break the share/view intent. External files
 *     directory is app-owned and persists until explicitly deleted or the
 *     app is uninstalled. Falls back to cacheDir if external storage is
 *     unavailable.
 *
 *  4. trustScore formatted to one decimal place in the PDF — previously
 *     showed the raw Float which could render as "4.800000190734863".
 */
object SkillPassportGenerator {

    fun generatePDF(
        context: Context,
        userName: String,
        email: String,
        foundedDate: String,
        skills: List<SkillInfo>,
        trustScore: Float
    ): File? {
        return try {
            val fileName = "Skill_Passport_${System.currentTimeMillis()}.pdf"

            // ✅ Use external files dir — survives OS cache eviction
            val dir = context.getExternalFilesDir("passports") ?: context.cacheDir
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            val pdfWriter = PdfWriter(file)
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument, PageSize.A4)

            val boldFont   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)

            // ✅ Build paragraph first, then add — not chained after document.add()
            val titleParagraph = Paragraph("SKILL PASSPORT")
                .setFont(boldFont)
                .setFontSize(24f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(titleParagraph)

            document.add(Paragraph("\n"))

            document.add(Paragraph("User: $userName").setFont(normalFont))
            document.add(Paragraph("Email: $email").setFont(normalFont))
            document.add(Paragraph("Member Since: $foundedDate").setFont(normalFont))

            // ✅ trustScore formatted — no raw Float like "4.800000190734863"
            val trustParagraph = Paragraph("Trust Score: ${String.format("%.1f", trustScore)} / 5.0")
                .setFont(boldFont)
            document.add(trustParagraph)

            document.add(Paragraph("\n"))

            // ✅ Section header built before add — was the broken chain in original
            val sectionHeader = Paragraph("VERIFIED SKILLS")
                .setFont(boldFont)
                .setFontSize(14f)
            document.add(sectionHeader)

            document.add(Paragraph("\n"))

            // ✅ Table headers with Cell + styled Paragraph (not plain addHeaderCell(String))
            val table = Table(UnitValue.createPercentArray(floatArrayOf(2f, 2f, 2f, 1f)))
                .useAllAvailableWidth()

            listOf("Skill", "Category", "Proficiency", "Year").forEach { header ->
                val cell = Cell().add(
                    Paragraph(header)
                        .setFont(boldFont)
                        .setFontSize(11f)
                )
                table.addHeaderCell(cell)
            }

            skills.forEach { skill ->
                table.addCell(skill.name)
                table.addCell(skill.category)
                table.addCell(skill.proficiency)
                table.addCell(skill.year.toString())
            }

            document.add(table)
            document.add(Paragraph("\n"))

            val footerParagraph = Paragraph(
                "This is an official micro-credential issued by KnowItAll Platform."
            )
                .setFont(italicFont)
                .setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(footerParagraph)

            document.close()
            pdfDocument.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    data class SkillInfo(
        val name: String,
        val category: String,
        val proficiency: String,
        val year: Int
    )
}