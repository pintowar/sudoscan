package com.github.pintowar.sudoscan.choco

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class SolverChocoTest: StringSpec({

    val solver = SolverChoco()

    "solution must present all numbers" {
        forAll(
            row(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "549261738367498521218573649476932815892715463135846972654129387781354296923687154")
        ) { input: String, output: String ->
            solver.solve(input) shouldBe output
        }
    }

    "solution must present only unknown numbers" {
        forAll(
            row(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "000260000000490520018500609476032810892010003000006902650129080001004290900607154")
        ) { input: String, output: String ->
            solver.solve(input, false) shouldBe output
        }
    }
})