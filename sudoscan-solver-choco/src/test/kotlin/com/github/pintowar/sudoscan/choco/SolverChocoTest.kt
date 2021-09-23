package com.github.pintowar.sudoscan.choco

import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.shouldBe

class SolverChocoTest : StringSpec({

    val solver = SolverChoco()

    "solution must present all numbers" {
        forAll(
            row(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "549261738367498521218573649476932815892715463135846972654129387781354296923687154"
            ),
            row(
                "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
                "872413569956827314134695782469731825528964137713582496297148653685379241341256978"
            )
        ) { input: String, output: String ->
            solver.solve(input) shouldBe output
        }
    }

    "solution must present only unknown numbers" {
        forAll(
            row(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "000260000000490520018500609476032810892010003000006902650129080001004290900607154"
            ),
            row(
                "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
                "072403560906020304130605082409030805020904030703080406290108053605070201041206970"
            )
        ) { input: String, output: String ->
            solver.solve(input, false) shouldBe output
        }
    }
})