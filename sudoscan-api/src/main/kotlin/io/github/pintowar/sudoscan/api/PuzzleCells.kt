package io.github.pintowar.sudoscan.api

import io.github.pintowar.sudoscan.api.cv.Area
import io.github.pintowar.sudoscan.api.spi.Recognizer

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
    fun toMat(debug: Boolean): ImageMatrix = if (debug) cells
        .map { it.data }
        .chunked(9)
        .map { it.reduce { acc, mat -> acc.concat(mat) } }
        .reduce { acc, mat -> acc.concat(mat, false) }
    else ImageMatrix.empty(Area(9 * SudokuCell.CELL_SIZE))
}