package com.github.pintowar.sudoscan.api.engine

import com.github.benmanes.caffeine.cache.CacheLoader
import com.github.pintowar.sudoscan.api.spi.Solver
import mu.KLogging

internal class CacheableSolver(private val solver: Solver) : CacheLoader<List<Int>?, List<Int>> {

    companion object : KLogging()

    override fun load(key: List<Int>?): List<Int> {
        return if (key != null) solvePuzzle(key) else emptyList()
    }

    /**
     * Just solves the puzzle, uses a list of integers (where zero is an empty cell) to represent it's input and output
     * form.
     *
     * @param puzzle list of integers (where zero is an empty cell) to represent the puzzle.
     * @return a list of integers to represent the puzzle solution.
     */
    private fun solvePuzzle(puzzle: List<Int>): List<Int> {
        fun printableSol(prob: List<Int>) = prob.chunked(9).joinToString("\n") {
            it.joinToString("|").replace("0", " ")
        }

        val solution = solver.solve(puzzle, false)
        logger.debug { "Digital Sudoku:\n" + printableSol(puzzle) }
        logger.debug { "Solution:\n" + printableSol(solution) }
        return solution
    }
}