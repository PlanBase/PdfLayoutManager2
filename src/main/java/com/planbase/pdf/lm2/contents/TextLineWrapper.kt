package com.planbase.pdf.lm2.contents

import com.planbase.pdf.lm2.lineWrapping.ConTerm
import com.planbase.pdf.lm2.lineWrapping.ConTermNone
import com.planbase.pdf.lm2.lineWrapping.LineWrapper
import com.planbase.pdf.lm2.lineWrapping.None

/**
 * A line-wrapper for wrapping text.
 */
internal class TextLineWrapper(private val txt: Text) : LineWrapper {
    private var idx = 0

    override fun hasMore(): Boolean = idx < txt.text.length

    override fun getSomething(maxWidth: Double): ConTerm {
//            println("      TextLineWrapper.getSomething($maxWidth)")
        if (maxWidth < 0) {
            throw IllegalArgumentException("Illegal negative width: $maxWidth")
        }
        val rowIdx = tryGettingText(maxWidth, idx, txt)
//            println("rowIdx=$rowIdx")
        idx = rowIdx.idx
        return rowIdx.toContTerm()
    }

    override fun getIfFits(remainingWidth: Double): ConTermNone {
//            println("      TextLineWrapper.getIfFits($remainingWidth)")
        if (remainingWidth <= 0) {
//                return None
            throw IllegalArgumentException("remainingWidth must be > 0")
        }
        val ctri = tryGettingText(remainingWidth, idx, txt)
        val row = ctri.row
        return if (row.dim.width <= remainingWidth) {
            idx = ctri.idx
            ctri.toContTerm() as ConTermNone
        } else {
            None
        }
    }

    companion object {
        private const val CR: Char = '\n'

        /**
         * Given a maximum width, a string, and the starting index into that string,
         * return the next line-wrapping chunk that fits.  Will break at whitespace or
         * any of the [breakableChars].
         *
         * This will always return any trailing whitespeace on the last chunk of the string, but never
         * on the other chunks.  This is because the non-final chunks are at the end of a line, but the final one
         * could have something to the right of it on the same line.
         *
         * @param maxWidth the maximum width (chunk must be <= this width)
         * @param startIdx the index into the text string to start processing
         * @param txt the text string to process.
         * @return the longest chunk that fits.
         */
        internal fun tryGettingText(maxWidth: Double, startIdx: Int, txt: Text): RowIdx {
//            println("=======================\n" +
//                    "tryGettingText(maxWidth=$maxWidth, startIdx=$startIdx, txt=$txt)")
//            Exception().printStackTrace()
            if (maxWidth < 0) {
                throw IllegalArgumentException("Can't meaningfully wrap text with a negative width: $maxWidth")
            }

            // Already removed all tabs, transformed all line-terminators into "\n", and removed all runs of spaces
            // that precede line terminators.
            val row = txt.text
            if (row.length <= startIdx) {
                throw IllegalStateException("text length must be greater than startIdx")
            }

            var crIdx = row.indexOf(CR, startIdx)
            val foundCr =
                    if (crIdx < 0) {
                        crIdx = row.length
                        false
                    } else {
                        true
                    }

            val text:String = row.substring(startIdx, crIdx)
//            println("text=[$text]")

            val textLen = text.length
//            println("text=[$text] len=$textLen")
            // Knowing the average width of a character lets us guess and generally be near
            // the word where the line break will occur.  Since the font reports a narrow average,
            // (possibly due to the predominance of spaces in text) we widen it a little for a
            // better first guess.
            var idx = txt.avgCharsForWidth(maxWidth)
            if (idx > textLen) {
                idx = textLen
            }
            var substr = text.substring(0, idx)
            var strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//            println("firstGuess substr=[$substr] idx=$idx len=$strWidth")

            // We assume that most lines *do* fit understanding that if most lines don't, this will be a little
            // slower.  But then it will look terrible, so you shouldn't be doing that anyway.
            //
            // The basic algorithm is very simple.
            //
            //  - Start with a guess at the right length (above).
            //  - Find the minimum width that fits.  (Could remember if we find a breaking character)
            //  - Find the previous breaking character
            //     - Found:
            //        - Consume any white space immediately before that character
            //        - Return result
            //     - Not Found:
            //        - Search until we find one (or end of string) and return that.

            //  - Find the minimum width that fits.  Could use binary search with linear interpolation for next guess.
            // But first guess is probably close enough that we're only going to go through this loop 2-3 times
            // unless the string is "ll,,ll" (all skinny chars) or "WW@@WW" (all wide chars).
            var longestIdxThatFits: Int? = null
            while (strWidth <= maxWidth && idx < textLen) {
//                println("Finding longest that fits substr=[$substr] idx=$idx len=$strWidth")
                longestIdxThatFits = idx
                idx = longestIdxThatFits + 1
                substr = text.substring(0, idx)
                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
            }
//            println(" Maybe too long substr=[$substr] idx=$idx len=$strWidth")

            if (longestIdxThatFits == null) {
                //  Here, we're beyond the max width, so go back to maxWidth.
                while (strWidth > maxWidth && idx > 0) {
//                    println("Backing up to maxwidth...")
                    longestIdxThatFits = idx - 1
                    idx = longestIdxThatFits
                    substr = text.substring(0, idx)
                    strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//                    println("    substr=[$substr] idx=$idx len=$strWidth")
                }
                if (idx >= textLen) {
//                    println("Returning whole string 0.")
                    return RowIdx(WrappedText(txt.textStyle, substr), // strWidth),
                                  startIdx + idx + 1,
                                  foundCr = if (substr == text) {
                                      foundCr
                                  } else {
                                      false
                                  },
                                  hasMore = false)
                } else if (Character.isWhitespace(text[idx])) {
                    while (idx > 0 && Character.isWhitespace(text[idx - 1])) {
//                        println("text0[idx]=${text[idx - 1]}")
                        idx--
                    }
                    substr = text.substring(0, idx)
//                    strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//                    println("Backed up to here 0: substr=[$substr] idx=$idx len=$strWidth")
                    return RowIdx(WrappedText(txt.textStyle, substr),
                                  startIdx + idx + 1,
                                  if (substr == text) {
                                      foundCr
                                  } else {
                                      false
                                  },
                                  testHasMore(substr, text))
                }
            } else if (Character.isWhitespace(text[idx - 1])) {
//                println("Character after longest that fits is whitespace")

                if (strWidth <= maxWidth) {
                    // If we can use up the whole string, return it - whitespace or not.
//                    println("Returning text=[$text] idx=$textLen")
                    return RowIdx(WrappedText(txt.textStyle, text),
                                  startIdx + text.length,
                                  foundCr,
                                  hasMore = false)
                } else {
                    // Here we're returning the longest that fits and chopping off the whitespace.
                    // Not sure the chop is really necessary, since we must in some cases check and chop it later too.
                    idx = longestIdxThatFits
                    substr = text.substring(0, idx)
//                    println("Returning substr=[$substr] idx=$idx")
                    return RowIdx(WrappedText(txt.textStyle, substr), // strWidth),
                                  startIdx + idx + 1,
                                  if (substr == text) {
                                      foundCr
                                  } else {
                                      false
                                  },
                                  testHasMore(substr, text))
                }
            } else {
//                println("Have longestIdxThatFits and last char is not whitespace.")
                if ( (idx >= textLen) && (strWidth <= maxWidth) ) {
//                    println("Returning whole string 1.")
                    return RowIdx(WrappedText(txt.textStyle, substr), // strWidth),
                                  startIdx + idx + 1,
                                  if (substr == text) {
                                      foundCr
                                  } else {
                                      false
                                  },
                                  testHasMore(substr, text))
                }
//                println("Setting idx=$idx to longestIdxThatFits=$longestIdxThatFits")
                idx = longestIdxThatFits
                substr = text.substring(0, idx)
            }
//            strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//            println("Longest that fits: substr=[$substr] idx=$idx len=$strWidth")

            // How do we get to have a whole string here yet idx == textLen - 1?
            if (idx >= textLen) {
//                println("Returning whole string 2.")
                return RowIdx(WrappedText(txt.textStyle, substr), // strWidth),
                              startIdx + idx + 1,
                              if (substr == text) {
                                  foundCr
                              } else {
                                  false
                              },
                              testHasMore(substr, text))
            }

            while (idx > 0 && !isLineBreakable(text[idx - 1])) {
//                println("text[idx]=${text[idx - 1]}")
                idx--
            }

            if (idx <= 0) {
//                println("There is nothing line-breakable that fits.  Find shortest line-breakable that runs over...")
                idx = longestIdxThatFits!!
                while (idx < textLen && !isLineBreakable(text[idx])) {
//                    println("Longer text[idx]=${text[idx]}")
                    idx++
                }

                substr = text.substring(0, idx)
                return RowIdx(WrappedText(txt.textStyle, substr.trimEnd()), // strWidth),
                              startIdx + idx + 1,
                              if (substr == text) {
                                  foundCr
                              } else {
                                  false
                              },
                              testHasMore(substr, text))
            }

//            substr = text.substring(0, idx)
//            strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//            println("Ends with line-breakable: substr=[$substr] idx=$idx len=$strWidth")

            if (idx > -1) {
//                println("Found a line-breakable.")
                // Found a line-breakable.
                //        - Consume any white space immediately before that character
                //        - Return result
                // Find last non-whitespace character before whitespace run.
                if (idx > textLen) {
//                    println("Backing up from end of string...")
                    idx = textLen - 1
                }
                while (idx > 0 && Character.isWhitespace(text[idx - 1])) {
//                    println("text2[idx]=[${text[idx - 1]}]")
                    idx--
                }
//                substr = text.substring(0, idx)
//                strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//                println("Backed up to here: substr=[$substr] idx=$idx len=$strWidth")
            } else {
//                println("Did not find a line-breakable.")
                // Did not find a line-breakable.
                //        - Search until we find one (or end of string) and return that.
                // Find last non-breakable character
                while ( (idx < textLen) &&
                        !isLineBreakable(text[idx]) ) {
//                    println("text3[idx]=${text[idx]}")
                    idx++
                }
            }

            substr = text.substring(0, idx)
//            strWidth = txt.textStyle.stringWidthInDocUnits(substr)
//            println("substr=[$substr] idx=$idx")

            val adjIdx = if ( (idx >= textLen) ||
                              Character.isWhitespace(text[idx]) ) {
                idx + 1
            } else {
                idx // TODO: Why is this the only place we don't add +1?
            }

            return RowIdx(WrappedText(txt.textStyle, substr.trimEnd()), // strWidth),
                          startIdx + adjIdx,
                          if (substr == text) {
                              foundCr
                          } else {
                              false
                          },
                          testHasMore(substr, text))
        }

        // Once we have more experience, might want to enhance using data here:
        // http://unicode.org/reports/tr14/#BreakOpportunities
        // NEVER include arithmetic minus here, or you'll get wrapping of the sign before a number
        // Might want to add more from here: https://en.wikipedia.org/wiki/Dash
        private val breakableChars: Set<Char> =
                setOf('/',
                      '-', // Line-breakable because there exists a non-breaking hyphen '\u2011' &#8209;.
                      '\u200b', // Zero-width space
                      '\u2010', // Hyphen
                      '\u2012', // figure dash
                      '\u2013', // en-dash
                      '\u2014', // em-dash
                      '\u2015', // Horizontal bar
                      '\u2053') // Swung Dash (like a horizontally stretched tilde)

        private fun isLineBreakable(c:Char) = Character.isWhitespace(c) || breakableChars.contains(c)

        internal fun testHasMore(subStr: String, text: String): Boolean =
                (subStr != text) &&
                (subStr.length < text.length) &&
                text.substring(subStr.length).isNotBlank()
    }
}