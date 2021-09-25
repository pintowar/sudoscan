package com.github.pintowar.sudoscan.choco

import com.github.pintowar.sudoscan.api.spi.Solver
import org.chocosolver.solver.Model

/**
 * Solver implementation that uses Choco Solver (CSP Solver) to solve a sudoku problem.
 */
class SolverChoco : Solver {

    override val name: String = "Choco Solver"

    override fun solve(puzzle: List<Int>, entireSol: Boolean): List<Int> {
        val prob = puzzle.chunked(9).map { it.toIntArray() }.toTypedArray()
        return solveWithChoco(prob, entireSol).flatMap { it.toList() }
    }

    /**
     * @param puzzle 2d array representation of the sudoku puzzle. Zero (0) represents an empty cell.
     * @param entireSol the solution must be the entire problem or just the initial empty cells.
     * @return 2d array representation of the sudoku solution.
     */
    private fun solveWithChoco(puzzle: Array<IntArray>, entireSol: Boolean = true): Array<IntArray> {
        val gridSize = 9
        val regionSize = 3
        val model = Model("Sudoku")

        val x = model.intVarMatrix("x", gridSize, gridSize, 1, gridSize)

        (0 until gridSize).forEach { i ->
            (0 until gridSize).forEach { j ->
                if (puzzle[i][j] != 0) x[i][j].eq(puzzle[i][j]).post()
            }
        }

        (0 until gridSize).forEach { i ->
            val row = x[i]
            model.allDifferent(*row).post()
        }

        (0 until gridSize).forEach { j ->
            val col = (0 until gridSize).map { i -> x[i][j] }.toTypedArray()
            model.allDifferent(*col).post()
        }

        (0 until gridSize step regionSize).forEach { rowIdx ->
            (0 until gridSize step regionSize).forEach { colIdx ->
                val block = (0 until regionSize).flatMap { i ->
                    (colIdx until (colIdx + regionSize)).map { j -> x[rowIdx + i][j] }
                }.toTypedArray()
                model.allDifferent(*block).post()
            }
        }

        val solution = model.solver.findSolution()
        if (solution != null)
            return x.mapIndexed { i, row ->
                row.mapIndexed { j, col ->
                    col.value - (if (entireSol) 0 else puzzle[i][j])
                }.toIntArray()
            }.toTypedArray()
        else
            throw IllegalArgumentException("Could not find solution for the given problem.")
    }
}