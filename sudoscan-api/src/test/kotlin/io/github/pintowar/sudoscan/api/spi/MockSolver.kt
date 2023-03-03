package io.github.pintowar.sudoscan.api.spi

import io.github.pintowar.sudoscan.api.Puzzle

class MockSolver : Solver {
    override val name = "MockSolver"

    override fun solveWithSolver(puzzle: Puzzle) = puzzle
}