package com.github.pintowar.sudoscan.api

sealed class Puzzle(protected open val cells: List<Digit>) {

    companion object {

        /**
         * Creates an Unsolved Puzzle from a given String. Every '0' found in the String will be converted to an
         * Unknown Digit.
         *
         * @param cells string with encoded sudoku.
         */
        fun unsolved(cells: String) =
            Unsolved(cells.map { if (it != '0') Digit.Valid(it.digitToInt(), 1.0) else Digit.Unknown })

        /**
         * Creates a Solved Puzzle from given Strings. A valid Solved Puzzle is composed by Valid and Found Digits.
         *
         * @param valids string with encoded valid digits. Zeros ('0') are Found Digits.
         * @param founds string with encoded found digits. Zeros ('0') are Valid Digits.
         */
        fun solved(valids: String, founds: String) =
            Solved(
                valids.indices.map { idx ->
                    if (valids[idx] != '0')
                        Digit.Valid(valids[idx].digitToInt(), 1.0)
                    else
                        Digit.Found(founds[idx].digitToInt())
                }
            )
    }

    val gridSize = 9
    val regionSize = 3
    val numValidDigits by lazy { cells.count { it is Digit.Valid } }

    /**
     * Returns a cell value of a 2d representation.
     *
     * @param i sudoku row
     * @param j sudoku col
     * @return cell value
     */
    operator fun get(i: Int, j: Int): Digit = cells[i * gridSize + j]

    /**
     * Returns a String representing Sudoku state.
     *
     * @param flatten if true, the output String must be a 1d array. Otherwise, a 2d representation will be returned.
     * @param onlyFound if true, only found digits (found by a solver) will be present. Otherwise, all digits
     * will be present.
     *
     * @return the String representation.
     */
    fun describe(flatten: Boolean = true, onlyFound: Boolean = false): String {
        fun format(digit: Digit) = when (digit) {
            is Digit.Unknown -> "0"
            is Digit.Found -> "${digit.value}"
            else -> if (onlyFound) "0" else "${digit.value}"
        }

        return if (flatten)
            cells.joinToString("") { format(it) }
        else
            cells.toList().chunked(gridSize).joinToString("\n") { it.joinToString("|") { dig -> format(dig) } }
    }

    class Unsolved(override val cells: List<Digit>) : Puzzle(cells)

    class Solved(override val cells: List<Digit>) : Puzzle(cells)
}