package com.planbase.pdf.layoutmanager.contents

import TestManual2.Companion.CMYK_PALE_PEACH
import TestManual2.Companion.a6PortraitBody
import TestManualllyPdfLayoutMgr.Companion.RGB_BLUE_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import TestManualllyPdfLayoutMgr.Companion.RGB_YELLOW_BRIGHT
import TestManualllyPdfLayoutMgr.Companion.letterLandscapeBody
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.LANDSCAPE
import com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.DimAndPageNums
import com.planbase.pdf.layoutmanager.attributes.LineStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.utils.CMYK_BLACK
import com.planbase.pdf.layoutmanager.utils.Coord
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.RGB_BLACK
import junit.framework.TestCase
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.PDType1Font.TIMES_ROMAN
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import java.io.FileOutputStream
import kotlin.test.assertTrue

class WrappedCellTest {

    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val cellWidth = 200.0
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello))
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedCell.render(lp, upperLeft)
        Dim.assertEquals(wrappedCell.dim, dimAndPageNums.dim, 0.00002)

        pageMgr.commit()
    }

    @Test fun testMultiLine() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val cellWidth = 300.0
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello))
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        wrappedCell.render(lp, upperLeft)
        pageMgr.commit()

//        val os = FileOutputStream("test4.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRightAlign() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        val cellWidth = 200.0
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.TOP_RIGHT, boxStyle),
                        cellWidth, listOf(hello))

        val wrappedCell =
                WrappedCell(Dim(cellWidth,
                                  textStyle.lineHeight + boxStyle.topBottomInteriorSp()),
                            CellStyle(Align.TOP_RIGHT, boxStyle),
                            listOf({
                                       val mlw = MultiLineWrapped()
                                       mlw.width = hello.maxWidth()
                                       mlw.ascent = textStyle.ascent
                                       mlw.lineHeight = textStyle.lineHeight
                                       mlw.append(
                                               WrappedText(
                                                       textStyle,
                                                       hello.text,
                                                       hello.maxWidth()
                                               )
                                       )
                                       mlw
                                   }.invoke()), 0.0)
//        val wrappedCell = cell.wrap()
//        println("cell.wrap()=${cell.wrap()}")

        kotlin.test.assertEquals(textStyle.lineHeight + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.dim.height)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100.0, 500.0)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        assertEquals(cellWidth, dimAndPageNums.dim.width)

        Dim.assertEquals(wrappedCell.dim, dimAndPageNums.dim, 0.00002)

        pageMgr.commit()

//        // We're just going to write to a file.
//        // Commit it to the output stream!
//        val os = FileOutputStream("wrappedCellRight.pdf")
//        pageMgr.save(os)
    }

    // There was only one significant line changed when I added this test without any comments.
    // Looks like I had assumed pageBreakingTopMargin wanted the text baseline which was above the curent y-value,
    // but it actually needs the bottom of the text area which is below the current y-value.
    //
    // This comment is based on:
    // git diff 13d097b86807ff458191a01633e1d507abcf3fc3 e2958def12f99beb699fc7546f5f7f0024b22df7
    // In class WrappedCell:
    // - val adjY = lp.pageBreakingTopMargin(y + line.descentAndLeading, line.lineHeight) + line.lineHeight
    // + val adjY = lp.pageBreakingTopMargin(y - line.lineHeight, line.lineHeight) + line.lineHeight
    @Test fun testCellHeightBug() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val textStyle = TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT)
        val cellStyle = CellStyle(Align.BOTTOM_CENTER, BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK)))

        val tB = Table()
                .addCellWidths(listOf(120.0))
                .textStyle(textStyle)
                .partBuilder()
                .cellStyle(cellStyle)
                .rowBuilder().addTextCells("First").buildRow()
                .buildPart()
        val wrappedTable = tB.wrap()

        TestCase.assertEquals(textStyle.lineHeight + cellStyle.boxStyle.topBottomInteriorSp(),
                              wrappedTable.dim.height)

        TestCase.assertEquals(120.0, wrappedTable.dim.width)

        val dimAndPageNums: DimAndPageNums = wrappedTable.render(lp, lp.body.topLeft)

        Dim.assertEquals(wrappedTable.dim, dimAndPageNums.dim, 0.00003)

        pageMgr.commit()
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }


    val regular7p5 = TextStyle(TIMES_ROMAN, 7.5, CMYK_BLACK) // ascent=5.1225  lineHeight=8.37
    val italic7p5 = TextStyle(PDType1Font.TIMES_BOLD_ITALIC, 7.0, CMYK_BLACK, 12.0) // ascent=4.781   lineHeight=12

    // The "shorter" (smaller line-height) font has the bigger ascent.  Ascent difference = 0.3415.
    // Text is aligned to the baseline, so the ascent diff of the first font is added to the line-height of the
    // second (which has a larger descentAndLeading) yielding a combined line-height of 12.3415.
    //
    // The following text is line-broken like:
    //
    // Men often hate each other because they fear each other;
    // they fear each other because they don't know each other;
    // they don't know each other because they can not
    // communicate; they can not communicate [because they
    // are separated.]
    //
    // The text in the square brackets is italics.  Here is a diagram of the critical part of this test -
    // The 2nd to last line in the transition from "communicate " to "because" in a different font:
    //
    //          -  -  -  -  -  P A G E   B R E A K  -  -  -  -
    //          ^                                         ^
    //          |                                         |
    //          |                                         |
    //          |Correct                                  |Rendered
    //          |Adjustment=                              |Height=
    //          |9.89                                     |21.5485
    //          |                                         |
    //          |                                         |
    //          |                                         |
    //          v                                         |
    // ------------------------+________________________  |
    //             |           |    /                     |
    //            -+-          |   /                      |
    //   ,-.  ,-.. |   ,-.     |  /,-.  ,-.   ,-.  ,--..  |
    //  (    (   | |  (---     | /   ) (---' (    (   /   |
    // __`-'__`-''_`-_ `--_____|/`--'__`--____'-___'-/__  | /__Aligned_on_baseline__
    //                         |                          | \
    //                         |                          |
    // ________________________|                          |
    //                         |                          |
    //                         |                          |
    //                         |                          |
    //                         |________________________  v
    //
    // This means that line 1 has line-height: 8.37 (total: 25.11)
    //                 Line 2 has line-height: 12.3415
    //                 Line 3 has line-height: 12.0
    //                           Grand total = 49.4515
    //
    // Actual quote:
    //     "Men often hate each other because they fear each other;
    //      they fear each other because they don't know each other;
    //      they don't know each other because they can not communicate;
    //      they can not communicate because they are separated."
    //            - MLK 1962
    //
    val theyAreSeparatedCell = Cell(CellStyle(
            Align.TOP_LEFT,
            BoxStyle(Padding.NO_PADDING, CMYK_PALE_PEACH,
                     BorderStyle(LineStyle(CMYK_BLACK, 0.00000001)))),
                                    170.0,
                                    listOf(Text(regular7p5,
                                                "they don't know each other because they can not communicate;" +
                                                " they can not communicate "),
                                           Text(italic7p5, "because they are separated.")))

    @Test fun testCellWithoutPageBreak() {
        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        val wrappedCell: WrappedCell = theyAreSeparatedCell.wrap()
        Dim.assertEquals(Dim(170.0, 32.7115), wrappedCell.dim, 0.0000001)

        // Rendered away from the page break, the dimensions are unchanged.
        val ret1:DimAndPageNums = lp.add(Coord(0.0, 183.26), wrappedCell)
        Dim.assertEquals(Dim(170.0, 32.7115), ret1.dim, 0.0000001)
        assertEquals(150.5485, lp.cursorY, 0.000001)

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("testCellWithoutPageBreak.pdf"))
    }

    @Test fun testCellAcrossPageBreak() {
        // The bold-italic text showed on the wrong page because the last line wasn't being dealt with as a unit.
        // A total line height is now calculated for the entire MultiLineWrapped when later inline text has a surprising
        // default lineHeight.  This test maybe belongs in MultiLineWrapped, but better here than nowhere.
        val pageMgr = PdfLayoutMgr(PDDeviceCMYK.INSTANCE, Dim(PDRectangle.A6))
        val lp = pageMgr.startPageGrouping(PORTRAIT, a6PortraitBody)

        // Actual quote:
        //     "Men often hate each other because they fear each other;
        //      they fear each other because they don't know each other;
        //      they don't know each other because they can not communicate;
        //      they can not communicate because they are separated."
        //            - MLK 1962
        //
        val theyAreSeparatedCell2 = Cell(CellStyle(
                Align.TOP_LEFT,
                BoxStyle(Padding.NO_PADDING, CMYK_PALE_PEACH,
                         BorderStyle(LineStyle(CMYK_BLACK, 0.00000001)))),
                                        170.0,
                                        listOf(Text(regular7p5,
                                                    "they fear each other because they don't know each other;" +
                                                    " they don't know each other because they can not communicate;" +
                                                    " they can not communicate "),
                                               Text(italic7p5, "because they are separated.")))


        val wrappedCell: WrappedCell = theyAreSeparatedCell2.wrap()
        Dim.assertEquals(Dim(170.0, 41.0815), wrappedCell.dim, 0.0000001)

        // Rendered across the page break, it's bigger.
        // Update 2018-05-08: I think it should be 9.89 bigger, but it's only 9.207 bigger.
        // The difference is 0.683 which is twice 0.3415 which is the difference between the heights
        // of the ascents of the different text parts.
        val ret2:DimAndPageNums = lp.add(Coord(0.0, 64.63), wrappedCell)

        Dim.assertEquals(Dim(170.0, 51.9715), ret2.dim, 0.0000001)

        assertEquals(12.6585, lp.cursorY, 0.00000001)

        pageMgr.commit()

        val docId = COSString("Cell Across Page Break Test PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)
        pageMgr.save(FileOutputStream("testCellAcrossPageBreak.pdf"))
    }

    // TODO: The above two tests may become obsolete once MultiLineWrappedTest.testPageBreakingDiffAscentDescent() passes!

    //    // A widow is the final line of text in a paragraph all alone at the top of the next page.
    @Test fun testWidowPrevention() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)
        val textStyle = TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT)
        val cellStyle = CellStyle(Align.TOP_LEFT, BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK)))

        // Make a five-line paragraph from copyright-exempt material.
        val text = "Just then her head struck against the roof of the hall: in fact she was now more than nine" +
                   " feet high, and she at once took up the little golden key and hurried off to the garden door."

        val cell = Cell(cellStyle, 320.0, listOf(Text(textStyle, text)))
        val wrappedCell = cell.wrap()

        // Verify that it's 5 lines (if it's not, there's no point in testing more).
        assertEquals((textStyle.lineHeight * 5.0) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedCell.dim.height, 0.0)

        // Just for fun, check the width, again to blow up early if something bigger is wrong...
        assertEquals(320.0, wrappedCell.dim.width)

        // At top of page, the rendered height should be the same as the wrapped height because nothing needs extra
        // space to cross the page margin.
        var dimAndPageNums: DimAndPageNums = wrappedCell.render(lp, lp.body.topLeft)

        Dim.assertEquals(wrappedCell.dim, dimAndPageNums.dim, 0.0)

        // Half-a-line above the bottom of the page, we don't want one line to go to the next page, we want two!
        // Set up the cursor to make this happen:
        lp.cursorY = lp.yBodyBottom + (textStyle.lineHeight * 4.5)

        // If you look at the resulting PDF, the blue box at the bottom of the first page should have more than one
        // line of blank blue background at the bottom - this is the desired widow-prevention in action!
        // It might be polite to adjust the background color, but not today.
        dimAndPageNums = lp.append(wrappedCell)

        // Really need to use doubles!
        assertEquals(lp.yBodyBottom - (textStyle.lineHeight * 2.0), lp.cursorY, 0.00000001)

        // Test that our wrappedCell size should be exceeded by more than one line.  This is because not one line,
        // but two should be pushed to the next page so that the one line isn't a lonely widow.
        assertTrue(dimAndPageNums.dim.height > (wrappedCell.dim.height + textStyle.lineHeight))

        // On to the next page for one more test...
        lp.cursorToNewPage()

        // Here, we'll page break so that each page naturally gets at least 2 lines of text so there won't be any
        // widows.  We'll expect the page-break adjustment to be less than one line of text.

        // 2.5 lines above the bottom of the page, we can put 2 of our 5 lines on the next page just fine.
        // If you look at the blue box at the bottom of the second page in the resulting PDF, it should have less than
        // one line of text height of extra blue at the bottom like normal.
        lp.cursorY = lp.cursorY - (lp.roomBelowCursor() - (textStyle.lineHeight * 3.5))
        dimAndPageNums = lp.append(wrappedCell)

        // Test that our wrappedCell size should be exceeded by LESS than one line.  This is because there's no danger
        // of a widow.
        assertTrue(dimAndPageNums.dim.height < (wrappedCell.dim.height + textStyle.lineHeight))

        pageMgr.commit()

//        pageMgr.save(FileOutputStream("test3.pdf"))
    }

    @Test fun testTooLongWordCellWrapping() {
        // This just tests that we don't go into an infinite loop (it used to).
        val cell = Cell(CellStyle(Align.TOP_LEFT, BoxStyle.NO_PAD_NO_BORDER),
                        100.0,
                        listOf(Text(TextStyle(TIMES_ROMAN, 8.0, CMYK_BLACK, 9.0),
                                    "www.c.ymcdn.com/sites/value-eng.site-ym.com/resource/resmgr/Standards_Documents/vmstd.pdf")))
        cell.wrap()
    }

    companion object {
        val boxStyle = BoxStyle(Padding(2.0), RGB_LIGHT_GREEN, BorderStyle(RGB_BLACK))
        private val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5, RGB_BLACK)
    }
}