package com.github.pintowar.sudoscan.api

import com.github.pintowar.sudoscan.api.cv.Area
import com.github.pintowar.sudoscan.api.cv.concat
import com.github.pintowar.sudoscan.api.cv.zeros
import com.github.pintowar.sudoscan.api.spi.Recognizer

/**
 * Represents a set of sudoku cells
 */
class PuzzleCells(private val cells: List<SudokuCell>) {

    /**
     * Converts all puzzle cells into an unsolved puzzle with the help of a [Recognizer].
     *
     * @param recognizer used on the conversion.
     * @return an unsolved [Puzzle].
     */
    fun toUnsolvedPuzzle(recognizer: Recognizer): Puzzle.Unsolved {
        val digits = recognizer.reliablePredict(cells)
        return Puzzle.Unsolved(digits)
    }

    /**
     * Converts all puzzle cells into a merged image in case of debug. Or else just return an empty image.
     *
     * @param debug debug condition.
     * @return merged image
     */
    fun toMat(debug: Boolean) = if (debug) cells
        .map { it.data }
        .chunked(9)
        .map { it.reduce { acc, mat -> acc.concat(mat) } }
        .reduce { acc, mat -> acc.concat(mat, false) }
    else zeros(Area(9 * SudokuCell.CELL_SIZE))
}