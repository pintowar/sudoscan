package com.github.pintowar.sudoscan.core

import org.chocosolver.solver.Model

object Solver {

    fun solve(problem: String, entireSol: Boolean = true): String {
        val prob = problem.toList().map(Character::getNumericValue)
        return solve(prob, entireSol).joinToString("")
    }

    fun solve(problem: List<Int>, entireSol: Boolean = true): List<Int> {
        val prob = problem.chunked(9).map { it.toIntArray() }.toTypedArray()
        return solveWithChoco(prob, entireSol).flatMap { it.toList() }
    }

    fun solveWithChoco(problem: Array<IntArray>, entireSol: Boolean = true): Array<IntArray> {

        val gridSize = 9
        val regionSize = 3
        val model = Model("Sudoku")

        val x = model.intVarMatrix("x", gridSize, gridSize, 1, gridSize)

        (0 until gridSize).forEach { i ->
            (0 until gridSize).forEach { j ->
                if (problem[i][j] != 0) x[i][j].eq(problem[i][j]).post()
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
                    col.value - (if (entireSol) 0 else problem[i][j])
                }.toIntArray()
            }.toTypedArray()
        else
            throw IllegalArgumentException("Could not find solution for the given problem.")
    }

}