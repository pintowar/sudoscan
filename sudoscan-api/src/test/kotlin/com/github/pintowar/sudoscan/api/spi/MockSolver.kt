package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Puzzle

class MockSolver : Solver {
    override val name = "MockSolver"

    override fun solveWithSolver(puzzle: Puzzle) = puzzle
}