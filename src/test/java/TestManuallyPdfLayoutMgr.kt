import com.planbase.pdf.lm2.PdfLayoutMgr
import com.planbase.pdf.lm2.PdfLayoutMgr.Companion.DEFAULT_MARGIN
import com.planbase.pdf.lm2.attributes.*
import com.planbase.pdf.lm2.attributes.Align.*
import com.planbase.pdf.lm2.attributes.LineStyle.Companion.NO_LINE
import com.planbase.pdf.lm2.attributes.Orientation.*
import com.planbase.pdf.lm2.contents.Cell
import com.planbase.pdf.lm2.contents.ScaledImage
import com.planbase.pdf.lm2.contents.Table
import com.planbase.pdf.lm2.contents.Text
import com.planbase.pdf.lm2.pages.SinglePage
import com.planbase.pdf.lm2.utils.Coord
import com.planbase.pdf.lm2.utils.Dim
import com.planbase.pdf.lm2.utils.RGB_BLACK
import com.planbase.pdf.lm2.utils.RGB_WHITE
import junit.framework.TestCase.assertEquals
import org.apache.pdfbox.cos.COSArray
import org.apache.pdfbox.cos.COSString
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB
import org.apache.pdfbox.util.Charsets
import java.io.File
import java.io.FileOutputStream
import java.lang.IllegalArgumentException
import javax.imageio.ImageIO
import kotlin.test.Test

class TestManuallyPdfLayoutMgr {

    /**
     * Note that [com.planbase.pdf.lm2.contents.TextLineWrapperTest.ohSayCanYouSee]
     * needs to work for the line breaking
     * of the Star Spangled Banner to come out right.
     */
    @Test fun testPdf() {
        // Nothing happens without a PdfLayoutMgr.
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))

        // One inch is 72 document units.  36 is about a half-inch - enough margin to satisfy most
        // printers. A typical monitor has 72 dots per inch, so you can think of these as pixels
        // even though they aren't.  Things can be aligned right, center, top, or anywhere within
        // a "pixel".
        val pMargin:Double = PdfLayoutMgr.DOC_UNITS_PER_INCH / 2.0

        // A PageGrouping is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        var lp = pageMgr.startPageGrouping(LANDSCAPE, letterLandscapeBody)

        // Set up some useful constants for later.
        val tableWidth = lp.pageWidth() - 2.0 * pMargin
        val pageRMargin = pMargin + tableWidth
        val colWidth = tableWidth / 4.0
        val colWidths = doubleArrayOf(colWidth + 10.0, colWidth + 10, colWidth + 10, colWidth - 30)
        val textCellPadding = Padding(2.0)

        // Set up some useful styles for later
        val heading = TextStyle(PDType1Font.HELVETICA_BOLD, 9.5, RGB_WHITE)
        val headingCell = CellStyle(BOTTOM_CENTER,
                                    BoxStyle(textCellPadding, RGB_BLUE,
                                             BorderStyle(NO_LINE, LineStyle(RGB_WHITE),
                                                         NO_LINE, LineStyle(RGB_BLUE))))
        val headingCellR = CellStyle(BOTTOM_CENTER,
                                     BoxStyle(textCellPadding, RGB_BLACK,
                                              BorderStyle(NO_LINE, LineStyle(RGB_BLACK),
                                                          NO_LINE, LineStyle(RGB_WHITE))))

        val regular = TextStyle(PDType1Font.HELVETICA, 9.5, RGB_BLACK)
        val regularCell = CellStyle(TOP_LEFT_JUSTIFY,
                                    BoxStyle(textCellPadding, null,
                                             BorderStyle(NO_LINE, LineStyle(RGB_BLACK),
                                                         LineStyle(RGB_BLACK), LineStyle(RGB_BLACK))))

        // Let's draw three tables on our first landscape-style page grouping.

        // Draw the first table with lots of extra room to show off the vertical and horizontal
        // alignment.
        var tB = Table()
        tB.addCellWidths(listOf(120.0, 120.0, 120.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT, null, 10.0))
                .cellStyle(CellStyle(BOTTOM_CENTER, BoxStyle(Padding(2.0),
                                                             RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("First", "Second", "Third").endRow()
                .cellStyle(CellStyle(MIDDLE_CENTER, BoxStyle(Padding(2.0),
                                                             RGB_LIGHT_GREEN,
                                                             BorderStyle(RGB_DARK_GRAY))))
                .minRowHeight(120.0)
                .textStyle(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK))
                .startRow()
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .endRow()
                .startRow()
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .endRow()
                .startRow()
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .endRow()
        val xya: DimAndPageNums = tB.wrap()
                .render(lp, lp.body.topLeft)

        assertEquals(Dim(360.0, 375.0), xya.dim)
        assertEquals(1, pageMgr.numPages())

        // The second table uses the x and y offsets from the previous table to position it to the
        // right of the first.
        tB = Table()
        tB.addCellWidths(listOf(100.0, 100.0, 100.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT, null, 10.0))
                .cellStyle(CellStyle(BOTTOM_CENTER,
                                     BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("January", "February", "March").endRow()
                .cellStyle(CellStyle(MIDDLE_CENTER,
                                     BoxStyle(Padding(2.0), RGB_LIGHT_GREEN,
                                              BorderStyle(RGB_DARK_GRAY))))
                .minRowHeight(100.0)
                .textStyle(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK))
                .startRow()
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .endRow()
                .startRow()
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .endRow()
                .startRow()
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .endRow()
        val xyb: DimAndPageNums = tB.wrap()
                .render(lp, lp.body.topLeft.plusX(xya.dim.width + 10))

        assertEquals(Dim(300.0, 315.0), xyb.dim)
        assertEquals(1, pageMgr.numPages())

        // The third table uses the x and y offsets from the previous tables to position it to the
        // right of the first and below the second.  Negative Y is down.  This third table showcases
        // the way cells extend vertically (but not horizontally) to fit the text you put in them.
        tB = Table()
        tB.addCellWidths(listOf(100.0, 100.0, 100.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT, null, 10.0))
                .cellStyle(CellStyle(BOTTOM_CENTER,
                                     BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("Uno", "Dos", "Tres").endRow()
                .cellStyle(CellStyle(MIDDLE_CENTER,
                                     BoxStyle(Padding(2.0), RGB_LIGHT_GREEN,
                                              BorderStyle(RGB_DARK_GRAY))))
                .textStyle(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK))
                .startRow().align(BOTTOM_RIGHT).addTextCells("Line 1")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two")
                .align(BOTTOM_LEFT)
                .addTextCells("Line 1\n" +
                              "Line two\n" +
                              "[Line three is long enough to wrap]")
                .endRow()
                .startRow().align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                             "Line two")
                .align(MIDDLE_CENTER).addTextCells("")
                .align(MIDDLE_LEFT).addTextCells("Line 1").endRow()
                .startRow().align(TOP_RIGHT).addTextCells("L1")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two")
                .align(TOP_LEFT).addTextCells("Line 1").endRow()
                .wrap()
                .render(lp, Coord(lp.body.topLeft.x + xya.dim.width + 10, lp.yBodyTop() - xyb.dim.height - 10))

        pageMgr.commit()
        assertEquals(1, pageMgr.numPages())

        // Let's do a portrait page now.  I just copied this from the previous page.
        lp = pageMgr.startPageGrouping(PORTRAIT, letterPortraitBody)
        tB = Table()
        tB.addCellWidths(listOf(120.0, 120.0, 120.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0, RGB_YELLOW_BRIGHT, null, 10.0))
                .cellStyle(CellStyle(BOTTOM_CENTER,
                                     BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("First", "Second", "Third").endRow()
                .cellStyle(CellStyle(MIDDLE_CENTER,
                                     BoxStyle(Padding(2.0), RGB_LIGHT_GREEN,
                                              BorderStyle(RGB_DARK_GRAY))))
                .minRowHeight(120.0)
                .textStyle(TextStyle(PDType1Font.COURIER, 12.0, RGB_BLACK))
                .startRow()
                .align(TOP_LEFT).addTextCells("Line 1\n" +
                                              "Line two\n" +
                                              "Line three")
                .align(TOP_CENTER).addTextCells("Line 1\n" +
                                                "Line two\n" +
                                                "Line three")
                .align(TOP_RIGHT).addTextCells("Line 1\n" +
                                               "Line two\n" +
                                               "Line three")
                .endRow()
                .startRow()
                .align(MIDDLE_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(MIDDLE_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(MIDDLE_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .endRow()
                .startRow()
                .align(BOTTOM_LEFT).addTextCells("Line 1\n" +
                                                 "Line two\n" +
                                                 "Line three")
                .align(BOTTOM_CENTER).addTextCells("Line 1\n" +
                                                   "Line two\n" +
                                                   "Line three")
                .align(BOTTOM_RIGHT).addTextCells("Line 1\n" +
                                                  "Line two\n" +
                                                  "Line three")
                .endRow()
        val xyc: DimAndPageNums = tB
                .wrap()
                .render(lp, lp.body.topLeft.withX(0.0))
        assertEquals(2, pageMgr.numPages())

        // This was very hastily added to this test to prove that font loading works (it does).
        val fontFile = File("target/test-classes/EmilysCandy-Regular.ttf")
        val liberationFont: PDType0Font = pageMgr.loadTrueTypeFont(fontFile)
        Cell(CellStyle(MIDDLE_CENTER,
                       BoxStyle(Padding(2.0), RGB_LIGHT_GREEN, BorderStyle(RGB_DARK_GRAY))),
             170.0,
             listOf(Text(TextStyle(liberationFont, 12.0, RGB_BLACK), "Hello Emily's Candy Font!")))
                .wrap()
                .render(lp, lp.body.topLeft.minusY(xyc.dim.height).withX(xyc.dim.width))

        tB = Table()
        tB.addCellWidths(listOf(100.0))
                .textStyle(TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12.0,
                                     RGB_YELLOW_BRIGHT))
                .cellStyle(CellStyle(MIDDLE_CENTER,
                                     BoxStyle(Padding(2.0), RGB_BLUE_GREEN, BorderStyle(RGB_BLACK))))
                .startRow().addTextCells("Lower-Right").endRow()
        // Where's the lower-right-hand corner?  Put a cell there.
        tB.wrap()
                .render(lp, Coord(lp.body.dim.width - 100,
                                  lp.yBodyBottom + 15 + pMargin))

        pageMgr.commit()
        assertEquals(2, pageMgr.numPages())

        // More landscape pages
        val pageHeadTextStyle = TextStyle(PDType1Font.HELVETICA, 7.0, RGB_BLACK)
        val pageHeadCellStyle = CellStyle(TOP_CENTER, BoxStyle.NO_PAD_NO_BORDER)
        lp = pageMgr.startPageGrouping(LANDSCAPE,
                                       letterLandscapeBody,
                                       { pageNum, pb->
                                           val cell = Cell(pageHeadCellStyle, tableWidth,
                                                           listOf(Text(pageHeadTextStyle,
                                                                       "Test Logical Page Three" +
                                                                       " (physical page $pageNum)")))

                                           cell.wrap().render(pb, Coord(pMargin, LETTER.width - 27.0))
                                           0.0 // reverts to regular page offset.
                                       })

        // We're going to reset and reuse this y variable.
//        var y = lp.yBodyTop()

        val f = File("target/test-classes/melon.jpg")
        val melonPic = ImageIO.read(f)

        tB = Table(colWidths.toMutableList(), headingCell, heading)
        tB.startRow()
                .cell(headingCell, listOf(Text(heading, "Stuff")))
                .cell(headingCellR, listOf(Text(heading, "US English")))
                .cell(headingCellR, listOf(Text(heading, "Finnish")))
                .cell(headingCellR, listOf(Text(heading, "German")))
                .endRow()
                .startRow()
                .cell(regularCell,
                      listOf(Text(regular,
                                  "This used to have Russian and Chinese text. " +
                                  "The Russian was transliterated and the Chinese was turned into bullets. " +
                                  "PDFBox 2.x, allows you to load fonts to show these characters, but throws" +
                                  " an exception if the character is not in the chosen font. " +
                                  "See how liberationFont is loaded in this test or see:\n" +
                                  "https://pdfbox.apache.org/1.8/cookbook/\n" +
                                  "workingwithfonts.html\n" +
                                  "\n\n" +
                                  "here\n" +
                                  "are\n" +
                                  "more lines\n" +
                                  //                                   "Россия – священная наша держава,\n" +
                                  //                                   "Россия – любимая наша страна.\n" +
                                  //                                   "Могучая воля, великая слава –\n" +
                                  //                                   "Твоё достоянье на все времена!\n" +
                                  //                                   null,
                                  //                                   "Chorus:\n" +
                                  //                                   null,
                                  //                                   "Славься, Отечество наше свободное, Братских народов союз\n" +
                                  //                                   " вековой, Предками данная мудрость народная! Славься, страна!\n" +
                                  //                                   " Мы гордимся тобой!\n" +
                                  //                                   null,
                                  //                                   "От южных морей до полярного края Раскинулись наши леса и\n" +
                                  //                                   " поля. Одна ты на свете! Одна ты такая – Хранимая Богом \n" +
                                  //                                   "родная земля!\n" +
                                  //                                   null,
                                  //                                   "Chorus:\n" +
                                  //                                   null,
                                  //                                   "Широкий простор для мечты и для жизни\n" +
                                  //                                   "Грядущие нам открывают года.\n" +
                                  //                                   "Нам силу даёт наша верность Отчизне.\n" +
                                  //                                   "Так было, так есть и так будет всегда!\n" +
                                  //                                   null,
                                  //                                   "Chorus\n" +
                                  //                                   null,
                                  //                                   null,
                                  //                                   null,
                                  //                                   "Chinese will not print.  The substitution character is a\n" +
                                  //                                   " bullet, so below should be lots of bullets.\n" +
                                  //                                   null,
                                  //                                   "起來！不願做奴隸的人們！ \n" +
                                  //                                   "把我們的血肉，築成我們新的長城！ \n" +
                                  //                                   "中華民族到了最危險的時候， \n" +
                                  //                                   "每個人被迫著發出最後的吼聲。 \n" +
                                  //                                   "起來！起來！起來！ \n" +
                                  //                                   "我們萬眾一心， \n" +
                                  //                                   "冒著敵人的炮火，前進！ \n" +
                                  //                                   "冒著敵人的炮火，前進！ \n" +
                                  //                                   "前進！前進！進！\n" +
                                  "\n\n" +
                                  "\n\n" +
                                  "\n\n" +
                                  "\n\n" +
                                  "\n\n" +
                                  "\n\n" +
                                  "Here is a picture with the default and other sizes.  Though" +
                                  " it shows up several times, the image data is only attached" +
                                  " to the file once and reused."),
                             ScaledImage(melonPic),
                             ScaledImage(melonPic, Dim(50.0, 50.0)),
                             Text(regular, " Melon "),
                             ScaledImage(melonPic, Dim(50.0, 50.0)),
                             Text(regular, " Yum!"),
                             ScaledImage(melonPic, Dim(170.0, 100.0)),
                             Text(regular, "Watermelon!")))
                .cell(regularCell,
                      listOf(Text(textStyle = regular,
                                  initialText = "O say can you see by the dawn's early light,\n" +
                                                "What so proudly we hailed at the twilight's last gleaming,\n" +
                                                "Whose broad stripes and bright stars\n" +
                                                "through the perilous fight,\n" +
                                                "O'er the ramparts we watched, were so gallantly streaming?\n" +
                                                "And the rockets' red glare, the bombs bursting in air,\n" +
                                                "Gave proof through the night that our flag was still there;\n" +
                                                "O say does that star-spangled banner yet wave,\n" +
                                                "O'er the land of the free and the home of the brave?\n" +
                                                "\n" +
                                                "On the shore dimly seen through the mists of the deep,\n" +
                                                "Where the foe's haughty host in dread silence reposes,\n" +
                                                "What is that which the breeze, o'er the towering steep,\n" +
                                                "As it fitfully blows, half conceals, half discloses?\n" +
                                                "Now it catches the gleam of the morning's first beam,\n" +
                                                "In full glory reflected now shines in the stream:\n" +
                                                "'Tis the star-spangled banner, O! long may it wave\n" +
                                                "O'er the land of the free and the home of the brave.\n" +
                                                "\n" +
                                                "And where is that band who so vauntingly swore\n" +
                                                "That the havoc of war and the battle's confusion,\n" +
                                                "A home and a country, should leave us no more?\n" +
                                                "Their blood has washed out their foul footsteps' pollution.\n" +
                                                "No refuge could save the hireling and slave\n" +
                                                "From the terror of flight, or the gloom of the grave:\n" +
                                                "And the star-spangled banner in triumph doth wave,\n" +
                                                "O'er the land of the free and the home of the brave.\n" +
                                                "\n" +
                                                "O thus be it ever, when freemen shall stand \n" +
                                                "Between their loved home and the war's desolation. \n" +
                                                "Blest with vict'ry and peace, may the Heav'n rescued land \n" +
                                                "Praise the Power that hath made and preserved us a nation! \n" +
                                                "Then conquer we must, when our cause it is just, \n" +
                                                "And this be our motto: \"In God is our trust.\" \n" +
                                                "And the star-spangled banner in triumph shall wave \n" +
                                                "O'er the land of the free and the home of the brave!\n" +
                                                "\n" +
                                                "more \n" +
                                                "lines \n" +
                                                "to \n" +
                                                "test")))
                .cell(regularCell,
                      listOf(Text(regular,
                                  "Maamme\n" +
                                  "\n" +
                                  "Monument to the Vårt Land poem in Helsinki. \n" +
                                  "Oi maamme, Suomi, synnyinmaa, \n" +
                                  "soi, sana kultainen! \n" +
                                  "Ei laaksoa, ei kukkulaa, \n" +
                                  "ei vettä, rantaa rakkaampaa \n" +
                                  "kuin kotimaa tää pohjoinen, \n" +
                                  "maa kallis isien. \n" +
                                  "Sun kukoistukses kuorestaan \n" +
                                  "kerrankin puhkeaa; \n" +
                                  "viel' lempemme saa nousemaan \n" +
                                  "sun toivos, riemus loistossaan, \n" +
                                  "ja kerran laulus, synnyinmaa \n" +
                                  "korkeemman kaiun saa.\n" +
                                  "\n" +
                                  "Vårt land\n" +
                                  "\n" +
                                  "(the original, by Johan Ludvig Runeberg) \n" +
                                  "Vårt land, vårt land, vårt fosterland, \n" +
                                  "ljud högt, o dyra ord! \n" +
                                  "Ej lyfts en höjd mot himlens rand, \n" +
                                  "ej sänks en dal, ej sköljs en strand, \n" +
                                  "mer älskad än vår bygd i nord, \n" +
                                  "än våra fäders jord! \n" +
                                  "Din blomning, sluten än i knopp, \n" +
                                  "Skall mogna ur sitt tvång; \n" +
                                  "Se, ur vår kärlek skall gå opp \n" +
                                  "Ditt ljus, din glans, din fröjd, ditt hopp. \n" +
                                  "Och högre klinga skall en gång \n" +
                                  "Vår fosterländska sång.\n\n")))
                .cell(regularCell,
                      listOf(Text(regular,
                                  "Einigkeit und Recht und Freiheit \n" +
                                  "Für das deutsche Vaterland! \n" +
                                  "Danach lasst uns alle streben \n" +
                                  "Brüderlich mit Herz und Hand! \n" +
                                  "Einigkeit und Recht und Freiheit \n" +
                                  "Sind des Glückes Unterpfand;\n" +
                                  "Blüh' im Glanze dieses Glückes, \n" +
                                  "  Blühe, deutsches Vaterland!")))
                .endRow()
                .startRow()
                .cell(regularCell, listOf(Text(regular, "Another row of cells")))
                .cell(regularCell, listOf(Text(regular, "On the second page")))
                .cell(regularCell, listOf(Text(regular, "Just like any other page")))
                .cell(regularCell, listOf(Text(regular, "That's it!")))
                .endRow()
        tB.wrap()
                .render(lp, lp.body.topLeft)
        pageMgr.commit()
        assertEquals(4, pageMgr.numPages())

        val lineStyle = LineStyle(RGB_BLACK, 1.0)

        lp = pageMgr.startPageGrouping(LANDSCAPE,
                                       letterLandscapeBody,
                                       { pageNum, pb->
                                           val cell = Cell(pageHeadCellStyle, tableWidth,
                                                           listOf(Text(pageHeadTextStyle,
                                                                       "Test Logical Page Four" +
                                                                       " (physical page $pageNum)")))
                                           cell.wrap().render(pb, Coord(pMargin, LETTER.width - 27.0))
                                           0.0 // reverts to regular page offset.
                                       })

        // Make a big 3-page X in a box.  Notice that we code it as though it's on one page, and the
        // API adds two more pages as needed.  This is a great test for how geometric shapes break
        // across pages.

        val topLeft = Coord(pMargin, lp.yBodyTop())
        val topRight = Coord(pageRMargin, lp.yBodyTop())
        val bottomRight = Coord(pageRMargin, -lp.yBodyTop())
        val bottomLeft = Coord(pMargin, -lp.yBodyTop())

        // top lne
        lp.drawLine(topLeft, topRight, lineStyle)
        // right line
        lp.drawLine(topRight, bottomRight, lineStyle)
        // bottom line
        lp.drawLine(bottomRight, bottomLeft, lineStyle)
        // left line
        lp.drawLine(bottomLeft, topLeft, lineStyle)

        // 3-page-long X
        lp.drawLine(topLeft, bottomRight, lineStyle)
        // Note reversed params
        lp.drawLine(bottomLeft, topRight, lineStyle)

        // middle line
        lp.drawLine(Coord(pMargin, 0.0), Coord(pageRMargin, 0.0), lineStyle)
        pageMgr.commit()
        assertEquals(7, pageMgr.numPages())

        // All done - write it out!

        // In a web application, this could be:
        //
        // httpServletResponse.setContentType("application/pdf") // your server may do this for you.
        // os = httpServletResponse.getOutputStream()            // you probably have to do this
        //
        // Also, in a web app, you probably want name your action something.pdf and put
        // target="_blank" on your link to the PDF download action.

        // If we don't set the file identifiers, PDFBox will set them for us.  We only care because we're checking
        // this into git so that when something breaks, we can easily compare with a known-good file to see how big
        // a change it is.
        val docId = COSString("PdfLayoutMgr2 Test/Sample PDF".toByteArray(Charsets.ISO_8859_1))
        pageMgr.setFileIdentifiers(docId, docId)

        val docIdCosArray: COSArray = pageMgr.getFileIdentifiers()!!
        assertEquals(2, docIdCosArray.size())
        assertEquals(docId, docIdCosArray.get(0))
        assertEquals(docId, docIdCosArray.get(1))

        // We're just going to write to a file.
        pageMgr.save(FileOutputStream("test.pdf"))
    }


    @Test(expected = IllegalArgumentException::class)
    fun testInsertPageAtEx01() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        pageMgr.insertPageAt(SinglePage(1, pageMgr, null, letterPortraitBody), -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testInsertPageAtEx02() {
        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
        pageMgr.insertPageAt(SinglePage(1, pageMgr, null, letterPortraitBody), 999)
    }

    // This will take a little longer to figure out...
//    @Test(expected = IllegalStateException::class)
//    fun testInsertPageAtEx03() {
//        val pageMgr = PdfLayoutMgr(PDDeviceRGB.INSTANCE, Dim(LETTER))
//        pageMgr.insertPageAt(SinglePage(1, pageMgr, null, letterPortraitBody), 0)
//    }

    companion object {
        internal val RGB_BLUE = PDColor(floatArrayOf(0.2f, 0.2f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_BLUE_GREEN = PDColor(floatArrayOf(0.2f, 0.4f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_DARK_GRAY = PDColor(floatArrayOf(0.2f, 0.2f, 0.2f), PDDeviceRGB.INSTANCE)
        internal val RGB_LIGHT_GREEN = PDColor(floatArrayOf(0.8f, 1f, 0.8f), PDDeviceRGB.INSTANCE)
        internal val RGB_LIGHT_BLUE = PDColor(floatArrayOf(0.8f, 1f, 1f), PDDeviceRGB.INSTANCE)
        internal val RGB_YELLOW_BRIGHT = PDColor(floatArrayOf(1f, 1f, 0f), PDDeviceRGB.INSTANCE)

        val letterPortraitBody = PageArea(Coord(DEFAULT_MARGIN, PDRectangle.LETTER.height - DEFAULT_MARGIN),
                                          Dim(PDRectangle.LETTER).minus(Dim(DEFAULT_MARGIN * 2.0, DEFAULT_MARGIN * 2.0)))

        val letterLandscapeBody = PageArea(Coord(DEFAULT_MARGIN, PDRectangle.LETTER.width - DEFAULT_MARGIN),
                                           letterPortraitBody.dim.swapWh())
    }
}
