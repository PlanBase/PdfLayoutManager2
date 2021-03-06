package com.planbase.pdf.lm2.attributes

import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.attributes.Orientation.*
import com.planbase.pdf.lm2.attributes.TextStyle.Companion.defaultLineHeight
import com.planbase.pdf.lm2.pages.SinglePage
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import com.planbase.pdf.lm2.utils.CMYK_WHITE
import com.planbase.pdf.lm2.utils.Dim
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.*
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Assert.assertEquals
import java.io.File
import java.io.FileOutputStream
import kotlin.test.Test
import kotlin.test.assertNotEquals

class TextStyleTest {
    private val quickBrownFox = "The quick brown fox jumps over the lazy dog"

    @Test fun basics() {

        // Natural height
        assertEquals(115.6, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK).lineHeight, 0.0)

        // Should take whatever height we give it!
        assertEquals(50.0, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 50.0).lineHeight, 0.0)
        assertEquals(200.0, TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 200.0).lineHeight, 0.0)

        // Natural heights of other fonts
        assertEquals(119.0, TextStyle(PDType1Font.HELVETICA_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(115.6, TextStyle(PDType1Font.HELVETICA_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(119.0, TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(111.6, TextStyle(PDType1Font.TIMES_ROMAN, 100.0, CMYK_BLACK).lineHeight, 0.0)

        assertEquals(115.3, TextStyle(PDType1Font.TIMES_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(110.0, TextStyle(PDType1Font.TIMES_ITALIC, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(113.9, TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.5, TextStyle(PDType1Font.COURIER, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.1, TextStyle(PDType1Font.COURIER_BOLD, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.5, TextStyle(PDType1Font.COURIER_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)
        assertEquals(105.1, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 100.0, CMYK_BLACK).lineHeight, 0.0)

        val fontFile = File("target/test-classes/EmilysCandy-Regular.ttf")
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
        assertEquals(125.19531, TextStyle(liberationFont, 100.0, CMYK_BLACK).lineHeight, 0.00001)

        // TODO: Test character spacing and word spacing!

        val helvetica100 = TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK)
        val basicWidth = 516.7

        assertEquals(basicWidth, helvetica100.stringWidthInDocUnits("Hello World"), 0.0)

        assertEquals(basicWidth + 10.0,
                     TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 100.0, 0.0, 0.0, 10.0)
                             .stringWidthInDocUnits("Hello World"), 0.0)

        assertEquals(basicWidth + ("Hello World".length * 1.0),
                     TextStyle(PDType1Font.HELVETICA, 100.0, CMYK_BLACK, null, 100.0, 0.0, 1.0, 0.0)
                             .stringWidthInDocUnits("Hello World"), 0.0)

        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val page: SinglePage = pageMgr.page(0)


        val times20 = TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK)
        val leading = times20.lineHeight
        page.drawStyledText(lp.body.topLeft.minusY(leading), times20, quickBrownFox)

        page.drawStyledText(lp.body.topLeft.minusY(leading * 2), TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 1.0, 0.0),
                            quickBrownFox)

        page.drawStyledText(lp.body.topLeft.minusY(leading * 3), TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, -1.0, 0.0),
                            quickBrownFox)

        page.drawStyledText(lp.body.topLeft.minusY(leading * 4), TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 0.0, 2.0),
                            quickBrownFox)

        page.drawStyledText(lp.body.topLeft.minusY(leading * 5), TextStyle(PDType1Font.TIMES_ROMAN, 20.0, CMYK_BLACK, null, 20.0, 0.0, 0.0, -2.0),
                            quickBrownFox)

        val helloOff = lp.body.topLeft.minusY(leading * 6)
        page.drawStyledText(helloOff, times20, "Hello")
        page.drawStyledText(helloOff.plusX(times20.stringWidthInDocUnits("Hello")), TextStyle(PDType1Font.TIMES_ROMAN, 11.0, CMYK_BLACK, null, 11.0, -4.0, 0.0, 0.0),
                            "subscript")

        val stuffOff = helloOff.plusX(times20.stringWidthInDocUnits("hellosubscript"))
        page.drawStyledText(stuffOff, times20, "Stuff")
        page.drawStyledText(stuffOff.plusX(times20.stringWidthInDocUnits("Stuff") + 1.0), TextStyle(PDType1Font.TIMES_ROMAN, 11.0, CMYK_BLACK, null, 11.0, 10.0, 0.0, 0.0),
                            "superscript")

        pageMgr.commit()
        val os = FileOutputStream("textStyleTest.pdf")
        pageMgr.save(os)


//        val fd = PDType1Font.HELVETICA_BOLD.fontDescriptor
//        println("fd=${fd}")
//        println("ascent=${fd.ascent}")
//        println("capHeight=${fd.capHeight}")
//        println("descent=${fd.descent}")
//        println("fontBoundingBox=${fd.fontBoundingBox}")
//        println("leading=${fd.leading}")
//        println("maxWidth=${fd.maxWidth}")
//        println("xHeight=${fd.xHeight}")
//        println("stemV=${fd.stemV}")
//        println("stemH=${fd.stemH}")
//
//        val ts = TextStyle(PDType1Font.HELVETICA_BOLD, 10.0, CMYK_BLACK)
//        println("ts=$ts")
    }

    @Test fun testLineHeightAdjustment() {
//        listOf(HELVETICA, HELVETICA_BOLD, HELVETICA_OBLIQUE, HELVETICA_OBLIQUE,
//               TIMES_ROMAN, TIMES_BOLD, TIMES_ITALIC, TIMES_BOLD_ITALIC,
//               COURIER, COURIER_BOLD, COURIER_OBLIQUE, COURIER_BOLD_OBLIQUE)
//                .sortedBy { defaultLineHeight(it, 1.0) }
//                .forEach { println("$it=${defaultLineHeight(it, 1.0)}") }
//
////PDType1Font Courier-Bold=       1.051
////PDType1Font Courier-BoldOblique=1.051
////PDType1Font Courier=            1.055
////PDType1Font Courier-Oblique=    1.055
////PDType1Font Times-Italic=       1.1
////PDType1Font Times-Roman=        1.116
////PDType1Font Times-BoldItalic=   1.139
////PDType1Font Times-Bold=         1.153
////PDType1Font Helvetica=          1.156
////PDType1Font Helvetica-Oblique=  1.156
////PDType1Font Helvetica-Oblique=  1.156
////PDType1Font Helvetica-Bold=     1.19

        // Most different font-heights for built-in fonts are COURIER_BOLD and HELVETICA_BOLD

        assertNotEquals(defaultLineHeight(COURIER_BOLD, 100.0), defaultLineHeight(HELVETICA_BOLD, 100.0))
        assertEquals(105.1, defaultLineHeight(COURIER_BOLD, 100.0), 0.005)
        assertEquals(119.0, defaultLineHeight(HELVETICA_BOLD, 100.0), 0.005)

        assertEquals(TextStyle(HELVETICA_BOLD, 100.0, CMYK_BLACK),
                     TextStyle(COURIER_BOLD, 100.0, CMYK_BLACK).withFontNewLineHeight(HELVETICA_BOLD))

        assertEquals(TextStyle(HELVETICA_BOLD, 100.0, CMYK_BLACK, null, defaultLineHeight(COURIER_BOLD, 100.0)),
                     TextStyle(COURIER_BOLD, 100.0, CMYK_BLACK).withFontOldLineHeight(HELVETICA_BOLD))
    }

    @Test fun testToString() {
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_WHITE)",
                     TextStyle(HELVETICA, 11.125, CMYK_WHITE).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_WHITE, 13.5)",
                     TextStyle(HELVETICA, 11.125, CMYK_WHITE, null, 13.5).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, -0.75, 1.0)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0).toString())
        assertEquals("hello",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0).toString())
        assertEquals("hello+cSpace=0.11+wSpace=0.22",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0)
                             .withCharWordSpacing(0.11, 0.22).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, 0.11, 0.22)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0)
                             .withCharWordSpacing(0.11, 0.22).toString())
        assertEquals("hello+wSpace=0.33",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, "hello", 13.5, 0.25, -0.75, 1.0)
                             .withWordSpacing(0.33).toString())
        assertEquals("TextStyle(HELVETICA, 11.125, CMYK_BLACK, 13.5, 0.25, -0.75, 0.33)",
                     TextStyle(HELVETICA, 11.125, CMYK_BLACK, null, 13.5, 0.25, -0.75, 1.0)
                             .withWordSpacing(0.33).toString())
    }
}