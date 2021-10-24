package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Puzzle
import com.github.pintowar.sudoscan.api.spi.Recognizer.Companion.provider
import com.github.pintowar.sudoscan.api.spi.Solver.Companion.provider
import java.util.*

/**
 * This interface represents the main contract (function and properties) of a Solver.
 * Solver are responsible to find the solution of a sudoku puzzle.
 *
 * An implementation of this Solver can be found using the [provider] function. This function will load an
 * implementation of this interface (found on classpath) via SPI.
 */
interface Solver {

    companion object {
        /**
         * This function will load an implementation of this interface (found on classpath) via SPI.
         *
         * @return Solver implementation found in the classpath.
         */
        fun provider(): Solver {
            val loader = ServiceLoader.load(Solver::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Solver found in classpath.")
        }
    }

    /**
     * @param puzzle flatten sudoku puzzle encoded as String. Empty cells are represented as zero (0).
     * @param onlyFound the solution description must contain only digits found by solver.
     * @return flatten sudoku solution as a string.
     */
    fun solve(puzzle: String, onlyFound: Boolean = false): String {
        return solve(Puzzle.unsolved(puzzle)).describe(onlyFound = onlyFound)
    }

    /**
     * Name of Solver implementation.
     */
    val name: String

    /**
     * The minimum valid digits a solver can handle.
     */
    fun minimumValidDigits(): Int = 23

    /**
     * Solves a sudoku puzzle.
     *
     * @param puzzle flatten sudoku puzzle encoded as a list of integers. Empty cells are represented as zero (0).
     * @return flatten sudoku solution as a list of integers.
     */
    fun solve(puzzle: Puzzle): Puzzle {
        return if (puzzle.numValidDigits >= minimumValidDigits()) solveWithSolver(puzzle) else puzzle
    }

    fun solveWithSolver(puzzle: Puzzle): Puzzle
}