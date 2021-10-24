package com.github.pintowar.sudoscan.choco

import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.Puzzle
import com.github.pintowar.sudoscan.api.spi.Solver
import org.chocosolver.solver.Model

/**
 * Solver implementation that uses Choco Solver (CSP Solver) to solve a sudoku problem.
 */
class SolverChoco : Solver {

    override val name: String = "Choco Solver (Minimum digits: ${minimumValidDigits()})"

    override fun minimumValidDigits(): Int = 20

    /**
     * @param puzzle 2d array representation of the sudoku puzzle.
     * @return 2d array representation of the sudoku solution.
     */
    override fun solveWithSolver(puzzle: Puzzle): Puzzle {
        val model = Model("Sudoku")

        val x = model.intVarMatrix("x", puzzle.gridSize, puzzle.gridSize, 1, puzzle.gridSize)

        (0 until puzzle.gridSize).forEach { i ->
            (0 until puzzle.gridSize).forEach { j ->
                if (puzzle[i, j] is Digit.Valid) x[i][j].eq(puzzle[i, j].value).post()
            }
        }

        (0 until puzzle.gridSize).forEach { i ->
            val row = x[i]
            model.allDifferent(*row).post()
        }

        (0 until puzzle.gridSize).forEach { j ->
            val col = (0 until puzzle.gridSize).map { i -> x[i][j] }.toTypedArray()
            model.allDifferent(*col).post()
        }

        (0 until puzzle.gridSize step puzzle.regionSize).forEach { rowIdx ->
            (0 until puzzle.gridSize step puzzle.regionSize).forEach { colIdx ->
                val block = (0 until puzzle.regionSize).flatMap { i ->
                    (colIdx until (colIdx + puzzle.regionSize)).map { j -> x[rowIdx + i][j] }
                }.toTypedArray()
                model.allDifferent(*block).post()
            }
        }

        val solution = model.solver.findSolution()
        return if (solution != null)
            Puzzle.Solved(
                x.flatMapIndexed { i, row ->
                    row.mapIndexed { j, col ->
                        val cell = puzzle[i, j]
                        if (cell is Digit.Valid) cell else Digit.Found(col.value)
                    }
                }
            )
        else
            puzzle
    }
}