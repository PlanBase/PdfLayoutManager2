package com.planbase.pdf.layoutmanager.contents

import TestManualllyPdfLayoutMgr.Companion.RGB_LIGHT_GREEN
import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.attributes.Align
import com.planbase.pdf.layoutmanager.attributes.BorderStyle
import com.planbase.pdf.layoutmanager.attributes.BoxStyle
import com.planbase.pdf.layoutmanager.attributes.CellStyle
import com.planbase.pdf.layoutmanager.attributes.Padding
import com.planbase.pdf.layoutmanager.attributes.TextStyle
import com.planbase.pdf.layoutmanager.lineWrapping.MultiLineWrapped
import com.planbase.pdf.layoutmanager.utils.rgbBlack
import com.planbase.pdf.layoutmanager.utils.Dim
import com.planbase.pdf.layoutmanager.utils.Coord
import junit.framework.TestCase.assertTrue
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test
import kotlin.test.assertEquals

class WrappedCellTest {

    @Test fun testBasics() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        wrappedCell.render(lp, upperLeft)
//        val xyOff : Coord = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        // TODO: This is not right.  Cell should report it's lower-right-hand corner, no?
//        val xyOff2 : Coord = wrappedCell.render(lp, upperLeft.plusXMinusY(xyOff))
//        println("xyOff2=" + xyOff2)

//        assertEquals(upperLeft.plusXMinusY(Dim(wrappedCell.dim.width, wrappedCell.lineHeight)), xyOff)

        lp.commit()
//        val os = FileOutputStream("test3.pdf")
//        pageMgr.save(os)
    }

    @Test fun testMultiLine() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()
        val cellWidth = 300f
        val hello = Text(textStyle, "Hello\nThere\nWorld!")
        val cell = Cell(CellStyle(Align.BOTTOM_CENTER, boxStyle),
                        cellWidth, listOf(hello), null)
//        println(cell)
        val wrappedCell: WrappedCell = cell.wrap()
//        println(wrappedCell)

        kotlin.test.assertEquals((textStyle.lineHeight() * 3) + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        wrappedCell.render(lp, upperLeft)
        lp.commit()

//        val os = FileOutputStream("test4.pdf")
//        pageMgr.save(os)
    }

    @Test fun testRightAlign() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(PDRectangle.LETTER))
        val lp = pageMgr.startPageGrouping()

        val cellWidth = 200f
        val hello = Text(textStyle, "Hello")
        val cell = Cell(CellStyle(Align.TOP_RIGHT, boxStyle),
                        cellWidth, listOf(hello), null)

        val wrappedCell =
                WrappedCell(Dim(cellWidth,
                                  textStyle.lineHeight() + boxStyle.topBottomInteriorSp()),
                            CellStyle(Align.TOP_RIGHT, boxStyle),
                            listOf(MultiLineWrapped(hello.maxWidth(),
                                                    textStyle.ascent(),
                                                    textStyle.descent() + textStyle.leading(),
                                                    mutableListOf(Text.WrappedText(textStyle,
                                                                                   hello.text,
                                                                                   Dim(hello.maxWidth(),
                                                                                       textStyle.lineHeight()),
                                                                                   hello)))))
//        val wrappedCell = cell.wrap()
//        println("cell.wrap()=${cell.wrap()}")

        kotlin.test.assertEquals(textStyle.lineHeight() + cell.cellStyle.boxStyle.topBottomInteriorSp(),
                                 wrappedCell.lineHeight)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val upperLeft = Coord(100f, 500f)

        kotlin.test.assertEquals(cellWidth,
                                 wrappedCell.dim.width)

        val dim: Dim = wrappedCell.render(lp, upperLeft)
//        println("upperLeft=" + upperLeft)
//        println("xyOff=" + xyOff)

        // TODO: Enable!
        assertEquals(cellWidth, dim.width)

        // TODO: This is not right.  Cell should report it's lower-righ-hand corner, no?
//        val xyOff2 : Coord = wrappedCell.render(lp, upperLeft.plusXMinusY(xyOff))
//        println("xyOff2=" + xyOff2)

        assertTrue(Dim.within(0.00002f, wrappedCell.dim, dim))

        lp.commit()

//        // We're just going to write to a file.
//        // Commit it to the output stream!
//        val os = FileOutputStream("wrappedCellRight.pdf")
//        pageMgr.save(os)
    }

    companion object {
        val boxStyle = BoxStyle(Padding(2f), RGB_LIGHT_GREEN, BorderStyle(rgbBlack))
        private val textStyle = TextStyle(PDType1Font.HELVETICA, 9.5f, rgbBlack)
    }
}