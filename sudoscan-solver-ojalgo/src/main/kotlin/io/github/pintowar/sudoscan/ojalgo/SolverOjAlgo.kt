package io.github.pintowar.sudoscan.ojalgo

import io.github.pintowar.sudoscan.api.Digit
import io.github.pintowar.sudoscan.api.Puzzle
import io.github.pintowar.sudoscan.api.spi.Solver
import org.ojalgo.optimisation.ExpressionsBasedModel

/**
 * Solver implementation that uses OjAlgo (MIP Solver) to solve a sudoku problem.
 */
class SolverOjAlgo : Solver {

    override val name: String = "OjAlgo Solver (Minimum digits: ${minimumValidDigits()})"

    override fun minimumValidDigits(): Int = 25

    /**
     * @param puzzle 2d array representation of the sudoku puzzle.
     * @return 2d array representation of the sudoku solution.
     */
    override fun solveWithSolver(puzzle: Puzzle): Puzzle {
        val model = ExpressionsBasedModel()

        val x = (0 until puzzle.gridSize).map { i ->
            (0 until puzzle.gridSize).map { j ->
                (0 until puzzle.gridSize).map { k ->
                    model.addVariable("x[$i][$j][$k]").binary()
                }
            }
        }

        // Initialize variables in case of known (defined) values.
        (0 until puzzle.gridSize).forEach { i ->
            (0 until puzzle.gridSize).forEach { j ->
                if (puzzle[i, j].value != 0) {
                    model.addExpression().set(x[i][j][puzzle[i, j].value - 1], 1).level(1)
                }
            }
        }

        // Initialize variables in case of known (defined) values.
        // All bins of a cell must have sum equals to 1
        (0 until puzzle.gridSize).forEach { i ->
            (0 until puzzle.gridSize).forEach { j ->
                (0 until puzzle.gridSize).fold(model.addExpression()) { exp, k ->
                    exp.set(x[i][j][k], 1)
                }.level(1)
            }
        }

        (0 until puzzle.gridSize).forEach { k ->
            // AllDifferent on rows.
            (0 until puzzle.gridSize).forEach { i ->
                (0 until puzzle.gridSize).fold(model.addExpression()) { exp, j ->
                    exp.set(x[i][j][k], 1)
                }.level(1)
            }

            // AllDifferent on columns.
            (0 until puzzle.gridSize).forEach { j ->
                (0 until puzzle.gridSize).fold(model.addExpression()) { exp, i ->
                    exp.set(x[i][j][k], 1)
                }.level(1)
            }

            // AllDifferent on regions.
            (0 until puzzle.gridSize step puzzle.regionSize).forEach { rowIdx ->
                (0 until puzzle.gridSize step puzzle.regionSize).forEach { colIdx ->
                    val region = (0 until puzzle.regionSize).flatMap { i ->
                        (colIdx until (colIdx + puzzle.regionSize)).map { j -> x[rowIdx + i][j][k] }
                    }
                    region.fold(model.addExpression()) { exp, v -> exp.set(v, 1) }.level(1)
                }
            }
        }

        val result = model.minimise()

        return if (result.state.isSuccess)
            Puzzle.Solved(
                x.flatMapIndexed { i, row ->
                    row.mapIndexed { j, col ->
                        val cell = puzzle[i, j]
                        if (cell is Digit.Valid) cell else Digit.Found(col.indexOfFirst { it.value.toInt() == 1 } + 1)
                    }
                }
            )
        else
            puzzle
    }
}