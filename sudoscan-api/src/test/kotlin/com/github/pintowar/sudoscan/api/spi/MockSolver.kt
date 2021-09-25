package com.github.pintowar.sudoscan.api.spi

class MockSolver : Solver {
    override val name = "MockSolver"

    override fun solve(puzzle: List<Int>, entireSol: Boolean) = puzzle.reversed()
}