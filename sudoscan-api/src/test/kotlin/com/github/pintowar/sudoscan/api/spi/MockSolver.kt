package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Puzzle

class MockSolver : Solver {
    override val name = "MockSolver"

    override fun solve(puzzle: Puzzle) = puzzle
}