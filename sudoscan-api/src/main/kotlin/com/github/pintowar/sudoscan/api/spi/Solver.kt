package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.spi.Recognizer.Companion.provider
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
         */
        fun provider(): Solver {
            val loader = ServiceLoader.load(Solver::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Solver found in classpath.")
        }
    }

    fun solve(problem: String, entireSol: Boolean = true): String {
        val prob = problem.toList().map(Character::getNumericValue)
        return solve(prob, entireSol).joinToString("")
    }

    /**
     * Name of Solver implementation.
     */
    val name: String

    /**
     * Solves a sudoku puzzle.
     *
     * @param puzzle flatten sudoku puzzle provided as a list of integers. Empty cells are represented as zero (0).
     * @param entireSol the solution must be the entire problem or just the initial empty cells.
     * @return flatten sudoku solution as a list of integers.
     */
    fun solve(puzzle: List<Int>, entireSol: Boolean = true): List<Int>
}