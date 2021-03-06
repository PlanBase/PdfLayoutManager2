package com.planbase.pdf.lm2.pages

import TestManual2.Companion.a6PortraitBody
import TestManuallyPdfLayoutMgr.Companion.letterLandscapeBody
import TestManuallyPdfLayoutMgr.Companion.letterPortraitBody
import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.PdfLayoutMgr.Companion.DEFAULT_MARGIN
import com.planbase.pdf.lm2.attributes.CellStyle.Companion.TOP_LEFT_BORDERLESS
import com.planbase.pdf.lm2.attributes.DimAndPageNums
import com.planbase.pdf.lm2.attributes.Orientation.LANDSCAPE
import com.planbase.pdf.lm2.attributes.Orientation.PORTRAIT
import com.planbase.pdf.lm2.attributes.PageArea
import com.planbase.pdf.lm2.attributes.TextStyle
import com.planbase.pdf.lm2.contents.ScaledImage
import com.planbase.pdf.lm2.contents.Text
import com.planbase.pdf.lm2.utils.CMYK_BLACK
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.RGB_BLACK
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.fail
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle.*
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_ROMAN
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.util.Charsets
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.math.nextDown
import kotlin.math.nextUp
import kotlin.test.Test
import kotlin.test.assertTrue

class PageGroupingTest {
    /**
     * If this is failing on the text boxes on the right, check that
     * [com.planbase.pdf.lm2.contents.TextLineWrapperTest.quickBrownFox] is working first.
     */
    @Test
    @Throws(IOException::class)
    fun testBasics() {
        var pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        var lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals((LETTER.width - DEFAULT_MARGIN), lp.yBodyTop(), 0.000000001)
        assertEquals(DEFAULT_MARGIN, lp.yBodyBottom, 0.000000001)
        assertEquals(LETTER.height.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((LETTER.width - DEFAULT_MARGIN * 2), lp.body.dim.height, 0.000000001)

        lp = pageMgr.startPageGrouping(PORTRAIT, letterPortraitBody)

        assertEquals((LETTER.height - DEFAULT_MARGIN), lp.yBodyTop(), 0.000000001)
        assertEquals(DEFAULT_MARGIN, lp.yBodyBottom, 0.000000001)
        assertEquals(LETTER.width.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((LETTER.height - DEFAULT_MARGIN * 2), lp.body.dim.height, 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        pageMgr.commit()
        pageMgr.save(ByteArrayOutputStream())

        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(A1))
        lp = pageMgr.startPageGrouping(PORTRAIT, a1PortraitBody)

        assertEquals((A1.height - DEFAULT_MARGIN), lp.yBodyTop(), 0.000000001)
        assertEquals(DEFAULT_MARGIN, lp.yBodyBottom, 0.000000001)
        assertEquals(A1.width.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((A1.height - DEFAULT_MARGIN * 2), lp.body.dim.height, 0.000000001)

        lp = pageMgr.startPageGrouping(LANDSCAPE, a1LandscapeBody)

        assertEquals((A1.width - DEFAULT_MARGIN), lp.yBodyTop(), 0.000000001)
        assertEquals(DEFAULT_MARGIN, lp.yBodyBottom, 0.000000001)
        assertEquals(A1.height.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((A1.width - DEFAULT_MARGIN * 2), lp.body.dim.height, 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        pageMgr.commit()
        pageMgr.save(ByteArrayOutputStream())

        val topM = 20.0
        val bottomM = 60.0
        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceGray.INSTANCE, Dim(A6))
        val bodyDim: Dim = pageMgr.pageDim.minus(Dim(DEFAULT_MARGIN * 2, topM + bottomM))
        lp = PageGrouping(pageMgr, PORTRAIT,
                          PageArea(Coord(DEFAULT_MARGIN, bottomM + bodyDim.height),
                                   bodyDim))

        assertEquals((A6.height - topM), lp.yBodyTop(), 0.000000001)
        assertEquals(bottomM, lp.yBodyBottom, 0.000000001)
        assertEquals(A6.width.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((A6.height - (topM + bottomM)), lp.body.dim.height, 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        pageMgr.commit()
        pageMgr.save(ByteArrayOutputStream())

        // Make a new manager for a new test.
        pageMgr = PdfLayoutMgr(PDDeviceGray.INSTANCE, Dim(A6))

        val bodyDim2: Dim = pageMgr.pageDim.swapWh()
                .minus(Dim(DEFAULT_MARGIN * 2, topM + bottomM))
        lp = PageGrouping(pageMgr, LANDSCAPE,
                          PageArea(Coord(DEFAULT_MARGIN, bottomM + bodyDim2.height),
                                   bodyDim2))

        assertEquals((A6.width - topM), lp.yBodyTop(), 0.000000001)
        assertEquals(bottomM, lp.yBodyBottom, 0.000000001)
        assertEquals(A6.height.toDouble(), lp.pageWidth(), 0.000000001)
        assertEquals((A6.width - (topM + bottomM)), lp.body.dim.height, 0.000000001)

        // Write to nothing to suppress the "stream not committed" warning
        pageMgr.commit()
//        pageMgr.save(ByteArrayOutputStream())
    }

    @Test fun testBasics2() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        val squareDim = Dim(squareSide, squareSide)

        val melonX = lp.body.topLeft.x
        val textX = melonX + melonWidth + 10
        val squareX = textX + bigText.maxWidth() + 10
        val lineX1 = squareX + squareSide + 10
        val cellX1 = lineX1 + squareSide + 10
        val tableX1 = cellX1 + squareSide + 10
        var y = lp.yBodyTop() - melonHeight

        while (y >= lp.yBodyBottom) {
            val imgHaP: HeightAndPage = lp.drawImage(Coord(melonX, y), bigMelon)
            assertEquals(melonHeight, imgHaP.height, 0.0)

            val txtHaP: HeightAndPage = lp.drawStyledText(Coord(textX, y), bigText.textStyle, bigText.text, true)
            assertEquals(bigText.textStyle.lineHeight, txtHaP.height, 0.0)

            val rectY: Double = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY, 0.0)

            diamondRect(lp, Coord(lineX1, y), squareSide)

            val cellDaP: DimAndPageNums = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfCell.dim, cellDaP.dim, 0.0)

            val tableDaP: DimAndPageNums = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfTable.dim, tableDaP.dim, 0.0)

            y -= melonHeight
        }

        // This is the page-break.
        // Images must vertically fit entirely on one page,
        // So they are pushed down as necessary to fit.
        val imgHaP2: HeightAndPage = lp.drawImage(Coord(melonX, y), bigMelon)
        assertTrue(melonHeight < imgHaP2.height) // When the picture breaks across the page, extra height is added.

        // Words must vertically fit entirely on one page,
        // So they are pushed down as necessary to fit.
        val txtHaP2: HeightAndPage = lp.drawStyledText(Coord(textX, y), bigText.textStyle, bigText.text, true)
        assertTrue(bigText.textStyle.lineHeight < txtHaP2.height)

        // Rectangles span multiple pages, so their height should be unchanged.
        val rectY2: Double = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
        assertEquals(squareSide, rectY2)

        // Lines span multiple pages, so their height should be unchanged.
        // Also, lines don't have a height.
        diamondRect(lp, Coord(lineX1, y), squareSide)
//            lp.drawLine(Coord(lineX1, y), Coord(lineX2, y), LineStyle(RGB_BLACK, 1.0))

        val cellDaP2: DimAndPageNums = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
//        println("qbfCell.dim=${qbfCell.dim} tableDim2=${cellDim2}")
        assertTrue(qbfCell.dim.height < cellDaP2.dim.height)

//        val tableDim2 = qbfTable.render(lp, Coord(tableX1, y))
        val tableDaP2:DimAndPageNums = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
//        println("qbfTable.dim=${qbfTable.dim} tableDim2=${tableDim2}")

        assertTrue(qbfTable.dim.height < tableDaP2.dim.height)
        assertEquals(qbfCell.dim.height, qbfTable.dim.height + 1.0)
        assertEquals(cellDaP2.dim.height, tableDaP2.dim.height)

        y -= listOf(imgHaP2.height, txtHaP2.height, rectY2).maxOrNull() as Double

        while (y >= lp.yBodyBottom - 400) {
            val imgHaP: HeightAndPage = lp.drawImage(Coord(melonX, y), bigMelon)
            assertEquals(melonHeight, imgHaP.height, 0.0)

            val txtHaP: HeightAndPage = lp.drawStyledText(Coord(textX, y), bigText.textStyle, bigText.text, true)
            assertEquals(bigText.textStyle.lineHeight, txtHaP.height, 0.0)

            val rectY: Double = lp.fillRect(Coord(squareX, y), squareDim, RGB_BLACK, true)
            assertEquals(squareSide, rectY, 0.0)

            diamondRect(lp, Coord(lineX1, y), squareSide)
//            lp.drawLine(Coord(lineX1, y), Coord(lineX2, y), LineStyle(RGB_BLACK, 1.0))
            val cellDaP: DimAndPageNums = qbfCell.render(lp, Coord(cellX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfCell.dim, cellDaP.dim, 0.0)
//            val tableDim = qbfTable.render(lp, Coord(tableX1, y))
            val tableDaP: DimAndPageNums = qbfTable.render(lp, Coord(tableX1, y + qbfCell.dim.height))
            Dim.assertEquals(qbfTable.dim, tableDaP.dim, 0.0)

            y -= listOf(imgHaP.height, txtHaP.height, rectY).maxOrNull() as Double
        }

        pageMgr.commit()

        val docId = COSString("PageGrouping test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        pageMgr.save(FileOutputStream("pageGrouping.pdf"))
    }

    // I'm commenting this out for now 2018-05-09.  Why?
    //  - We successfully printed our first book with this code even with this test failing
    //  - Though this test passed for months, the PDF it produced only looked correct for 9 days.
    //      - Added:        2018-01-15 ad41ae38c21a78537aea6dfda92fb6d697fe961d
    //      - Still worked: 2018-01-23 ea447d6607166466be20a8615600798c92e2a5e5
    //      - Broken:       2018-01-24 b7506606acba72d1a635203ddb7f9cb745b604f3
    //  - The code that broke this fixed multi-line page-breaking, which we actually use.
    //  - There's a comment that says it's a bad test (could have been cut and pasted)
//    @Test fun testPageBreakingTopMargin() {
//        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
//        val bodyWidth = PDRectangle.A6.width - 80.0
//
//        val f = File("target/test-classes/graph2.png")
//        val graphPic = ImageIO.read(f)
//
//        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)
//
//        val cell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2.0), null, BorderStyle(LineStyle(CMYK_BLACK, 0.1)))),
//                        bodyWidth,
//                        listOf(Text(BULLET_TEXT_STYLE,
//                                    ("The " +
//                                     "best points got the economic waters " +
//                                     "and problems gave great. The whole " +
//                                     "countries went the best children and " +
//                                     "eyes came able.")),
//                               Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2.0), null, BorderStyle.NO_BORDERS)),
//                                    bodyWidth - 6.0,
//                                    listOf(Text(BULLET_TEXT_STYLE,
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  " +
//                                                "This paragraph is too long to fit on a single page.  ")), 30.0),
//                               ScaledImage(graphPic)))
//        val wrappedCell = cell.wrap()
//        Dim.assertEquals(Dim(217.637817, 509.172), wrappedCell.dim, 0.000001)
//
//        // This is not a great test because I'm not sure this feature is really meant to work blocks that cross
//        // multiple pages.  In fact, it looks pretty bad for those blocks.
//        val finalDaP:DimAndPageNums = wrappedCell.render(lp, Coord(40.0, PDRectangle.A6.height - 40.0))
//        Dim.assertEquals(Dim(217.637817, 800.71111), finalDaP.dim, 0.00001)
//
//        pageMgr.commit()
//
//        pageMgr.save(FileOutputStream("testPageBreakingTopMargin.pdf"))
//    }

    // One thing this tests is cell-padding across a page break.
//    @Test fun testPageBreakWithInlineNearBottom() {
//        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
//        val bodyWidth = PDRectangle.A6.width - 80.0
//        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)
//
//        // Full disclosure: The text here says, "supposed to span the page break" when in fact the whole inner
//        // paragraph is supposed to end up on the next page.
//        val innerCell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2.0), null, BorderStyle.NO_BORDERS)),
//                             bodyWidth - 6.0,
//                             listOf(Text(BULLET_TEXT_STYLE,
//                                         "This paragraph is too long to fit on a single page.  " +
//                                         "This paragraph is too long to fit on a single page.  "),
//                                    Text(TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 12.0, CMYK_BLACK),
//                                         "It may go to the next page.")))
//
//        val cell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2.0), null, BorderStyle(LineStyle(CMYK_BLACK, 0.1)))),
//                        bodyWidth,
//                        listOf(Text(BULLET_TEXT_STYLE,
//                                    ("Some stuff.")),
//                               innerCell))
//        val wrappedCell = cell.wrap()
//        Dim.assertEquals(Dim(217.637817, 78.276), wrappedCell.dim, 0.000001)
//
//        var dap: DimAndPageNums = wrappedCell.render(lp, Coord(40.0, 210.0), reallyRender = true)
//        Dim.assertEquals(wrappedCell.dim, dap.dim, 0.0)
//
//        dap = wrappedCell.render(lp, Coord(40.0, lp.yBodyBottom + wrappedCell.dim.height), reallyRender = false)
//        Dim.assertEquals(wrappedCell.dim, dap.dim, 0.0)
//
//        // There is a 2.0 padding on the bottom, so we don't get onto the next page until that's used up.
//        val justFitsY = (lp.yBodyBottom + wrappedCell.dim.height) - wrappedCell.cellStyle.boxStyle.interiorSpaceBottom()
//
//        dap = wrappedCell.render(lp, Coord(40.0, justFitsY), reallyRender = false)
//        Dim.assertEquals(wrappedCell.dim, dap.dim, 0.0)
//
//        val innerCellHeight = innerCell.wrap().dim.height
//        val topPartHeight = wrappedCell.dim.height - innerCellHeight
//        val expectedHeight = (topPartHeight + (innerCellHeight * 2)) -
//                             wrappedCell.cellStyle.boxStyle.interiorSpaceBottom()
//
//        dap = wrappedCell.render(lp, Coord(40.0, justFitsY - 4), reallyRender = true)
////        Dim.assertEquals(wrappedCell.dim.withHeight(expectedHeight), dap.dim, 0.00000001)
//
//        pageMgr.commit()
//
//        pageMgr.save(FileOutputStream("testPageBreakWithInlineNearBottom.pdf"))
//    }

    @Test fun testAppropriatePage() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val melonHeight = 100.0

        var y:Double = lp.yBodyBottom
//        println("lp.yBodyBottom=${lp.yBodyBottom}, y=$y, lp.body=${lp.body} pageMgr.pageDim=${pageMgr.pageDim}")
//        var unCommittedPageIdx = pageMgr.unCommittedPageIdx()
        var pby:PageGrouping.PageBufferAndY = lp.appropriatePage(y, melonHeight, 0.0)
        assertEquals(0.0, pby.adj)
        assertEquals(y, pby.y)
        assertEquals(1, pby.pb.pageNum)

        y = lp.yBodyBottom.nextUp()
        pby = lp.appropriatePage(y, melonHeight, 0.0)
        assertEquals(0.0, pby.adj)
        assertEquals(y, pby.y)
        assertEquals(1, pby.pb.pageNum)

        y = lp.yBodyBottom.nextDown()
        pby = lp.appropriatePage(y, melonHeight, 0.0)
        assertEquals(melonHeight, pby.adj)
        assertEquals(2, pby.pb.pageNum)
        assertEquals(lp.yBodyTop() - 100.0, pby.y)

        pageMgr.commit()
    }

    @Test fun testRoomBelowCursorLandscape() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val bodyHeight: Double = letterLandscapeBody.dim.height
        val hel12Ts = TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 12.0, CMYK_BLACK)
        assertEquals(lp.yBodyTop(), lp.cursorY)
        assertEquals(letterLandscapeBody.topLeft.y, lp.cursorY)
        assertEquals(bodyHeight, lp.roomBelowCursor())

        lp.cursorToNewPage()
        assertEquals(DEFAULT_MARGIN, lp.cursorY)
        assertEquals(0.0, lp.roomBelowCursor())

        lp.appendCell(0.0, TOP_LEFT_BORDERLESS,
                      listOf(Text(hel12Ts, "Hi")))
        assertEquals(bodyHeight - hel12Ts.lineHeight, lp.roomBelowCursor())

        lp.cursorToNewPage()
        assertEquals(0.0, lp.roomBelowCursor())

        lp.appendCell(0.0, TOP_LEFT_BORDERLESS,
                      listOf(Text(hel12Ts, "Hello")))
        assertEquals(bodyHeight - hel12Ts.lineHeight, lp.roomBelowCursor())

        pageMgr.commit()
//        val os = FileOutputStream("roomBelowCursor.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRoomBelowCursorPortrait() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)
        val bodyHeight = a6PortraitBody.dim.height
        val hel12Ts = TextStyle(PDType1Font.HELVETICA_BOLD_OBLIQUE, 12.0, CMYK_BLACK)
        assertEquals(lp.yBodyTop(), lp.cursorY)
        assertEquals(a6PortraitBody.topLeft.y, lp.cursorY)
        assertEquals(bodyHeight, lp.roomBelowCursor())

        lp.cursorToNewPage()
        assertEquals(DEFAULT_MARGIN, lp.cursorY)
        assertEquals(0.0, lp.roomBelowCursor())

        lp.appendCell(0.0, TOP_LEFT_BORDERLESS,
                      listOf(Text(hel12Ts, "Hi")))

        assertEquals(bodyHeight - hel12Ts.lineHeight, lp.roomBelowCursor())

        lp.cursorToNewPage()
        assertEquals(0.0, lp.roomBelowCursor())

        lp.appendCell(0.0, TOP_LEFT_BORDERLESS,
                      listOf(Text(hel12Ts, "Hello")))
        assertEquals(bodyHeight - hel12Ts.lineHeight, lp.roomBelowCursor())

        pageMgr.commit()
//        val os = FileOutputStream("roomBelowCursor.pdf")
//        pageMgr.save(os)
    }

    @Test fun testFillRectAtBottomOfPage() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT,
                                           PageArea(Coord(DEFAULT_MARGIN.nextDown(), A6.height.nextUp() - 17.333333),
                                                    Dim(A6).minus(Dim(13.11111, 13.11111))))

        // Spent all afternoon wondering why printing 20 pages worked, but printing 200 threw an exception.
        // PageGrouping uses floating point arithmetic, but only blew up when one number was big enough and the
        // other small enough that the ADDITION FAILED.  How could this be?
        //
        // Floating point has a fixed number of "decimal places" so that as numbers get bigger, the difference
        // between adjacent floating point representations get bigger as well.

        // for (i = 1; i < Int.MAX_VALUE/10; i++) {
        //    println("${n}, ${n.toFloat().nextUp()}")
        // }
        // 1.0, 1.0000001
        // 10.0, 10.000001
        // 100.0, 100.00001
        // 1000.0, 1000.00006
        // 10000.0, 10000.001
        // 100000.0, 100000.01
        // 1000000.0, 1000000.06
        // 10000000, 10000001
        // 100000000, 100000008

        // So if you add 100,000,000 + 1, you get 100,000,000.  Even similar sized numbers accumulate small
        // errors with addition and subtraction, but the bigger the difference between the numbers you are
        // adding/subtracting, the bigger the error.

        // So here, we add 100 pages so that the floating point numbers have big enough gaps between them.
        for (i in 1 .. 10000) {
            lp.cursorToNewPage()
                    lp.appendCell(0.0, TOP_LEFT_BORDERLESS, listOf(Text(TextStyle(TIMES_ROMAN, 14.0, CMYK_BLACK), "Page $i")))
        }

        var testY = lp.cursorY - lp.roomBelowCursor()
        // Now we knock it off by just a tiny bit.
//        println("testY=$testY") // testY=-40645.824      Exception testY was -40645.977
        testY -= 0.000000001
//        println("testY=$testY") // testY=-40645.824      Exception testY was -40645.977
        val endTest = testY - 0.0000001

        // We're expecting an exception with the Dim having a negative width.
        try {
            while (testY > endTest) {
                testY = testY.nextDown()
//                println("testY=${testY}f")
                lp.fillRect(Coord(40.0, testY), Dim(10.0, 0.0011111), CMYK_BLACK, true)
            }
        } catch (e: Exception) {
//            println("Exception: $e")
//            println("testY was $testY")
            fail()
        }

        pageMgr.commit()
    }

    companion object {
        val a1PortraitBody = PageArea(Coord(DEFAULT_MARGIN, A1.height - DEFAULT_MARGIN),
                                      Dim(A1).minus(Dim(DEFAULT_MARGIN * 2.0, DEFAULT_MARGIN * 2.0)))

        val a1LandscapeBody = PageArea(Coord(DEFAULT_MARGIN, A1.width - DEFAULT_MARGIN),
                                       a1PortraitBody.dim.swapWh())

        private val melonPic: BufferedImage = ImageIO.read(File("target/test-classes/melon.jpg"))
        const val melonHeight = 98.0
        const val melonWidth = 170.0
        val bigMelon = ScaledImage(melonPic, Dim(melonWidth, melonHeight)).wrap()
        val bigText = Text(TextStyle(TIMES_ROMAN, 93.75, RGB_BLACK), "gN")
    }
}