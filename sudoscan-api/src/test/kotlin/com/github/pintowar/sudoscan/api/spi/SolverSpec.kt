package com.github.pintowar.sudoscan.api.spi

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeInstanceOf

class SolverSpec : StringSpec({

    "test solver" {
        Solver.provider().shouldBeInstanceOf<MockSolver>()
    }
})