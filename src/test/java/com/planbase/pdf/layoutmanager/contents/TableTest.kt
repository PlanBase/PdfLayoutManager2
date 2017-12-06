package com.planbase.pdf.layoutmanager.contents

import com.planbase.pdf.layoutmanager.PdfLayoutMgr
import com.planbase.pdf.layoutmanager.pages.SinglePage
import com.planbase.pdf.layoutmanager.utils.Dimensions
import junit.framework.Assert.assertEquals
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.junit.Test

class TableTest {
    @Test fun testSingleCell() {
        val table:Table = TableBuilder(mutableListOf(twoHundred))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(hello))
                .buildRow()
                .buildPart()
                .buildTable()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals(textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dimensions.height)

        assertEquals(twoHundred, wrappedTable.dimensions.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(twoHundred, ret.width)
        // TODO: Can we adjust the calculations to have less errors?
        assertEquals(textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)

        // TODO: Make rendered section of all items below.
    }

    @Test fun testSingleCellWrapped() {
        val table:Table = TableBuilder(mutableListOf(helloHelloWidth))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(helloHello))
                .buildRow()
                .buildPart()
                .buildTable()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals((textStyle.lineHeight() * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dimensions.height)

        assertEquals(helloHelloWidth,
                     wrappedTable.dimensions.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(helloHelloWidth, ret.width)
        // TODO: Can we adjust the calculations to have less errors?
        assertEquals((textStyle.lineHeight() * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
    }

    @Test fun testTwoCells() {
        val table:Table = TableBuilder(mutableListOf(twoHundred, twoHundred))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(hello))
                .cell(cellStyle, listOf(hello))
                .buildRow()
                .buildPart()
                .buildTable()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals(textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dimensions.height)

        assertEquals(twoHundred + twoHundred, wrappedTable.dimensions.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(twoHundred + twoHundred, ret.width)
        // TODO: Can we adjust the calculations to have less errors?
        assertEquals(textStyle.lineHeight() + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
    }

    @Test fun testTwoCellsWrapped() {
        val table:Table = TableBuilder(mutableListOf(helloHelloWidth, helloHelloWidth))
                .partBuilder()
                .rowBuilder()
                .cell(cellStyle, listOf(helloHello))
                .cell(cellStyle, listOf(helloHello))
                .buildRow()
                .buildPart()
                .buildTable()

        val wrappedTable:Table.WrappedTable = table.wrap()

        assertEquals((textStyle.lineHeight() * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     wrappedTable.dimensions.height)

        assertEquals(helloHelloWidth + helloHelloWidth, wrappedTable.dimensions.width)

        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dimensions(PDRectangle.LETTER))
        val lp = pageMgr.logicalPageStart()
        val page: SinglePage = pageMgr.page(0)

        val ret = wrappedTable.render(page, lp.bodyTopLeft())
        assertEquals(helloHelloWidth + helloHelloWidth, ret.width)
        // TODO: Can we adjust the calculations to have less errors?
        assertEquals((textStyle.lineHeight() * 2) + cellStyle.boxStyle.topBottomInteriorSp(),
                     ret.height, 0.00003f)
    }
}