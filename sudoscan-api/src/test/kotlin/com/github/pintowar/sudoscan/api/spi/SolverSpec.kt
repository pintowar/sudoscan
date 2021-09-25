package com.github.pintowar.sudoscan.api.spi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class SolverSpec : StringSpec({

    "test solver" {
        Solver.provider().shouldBeInstanceOf<MockSolver>()
    }

    "test solve" {
        val solution = MockSolver().solve("12345")
        solution shouldBe "54321"
    }
})