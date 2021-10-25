package com.github.pintowar.sudoscan.api

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PuzzleSpec : StringSpec({

    "test puzzle describe" {
        val puzzle = Puzzle.unsolved(
            "549261738367498521218573649476932815892715463135846972654129387781354296923687154"
        )

        puzzle.describe(flatten = false) shouldBe """
            #5|4|9|2|6|1|7|3|8
            #3|6|7|4|9|8|5|2|1
            #2|1|8|5|7|3|6|4|9
            #4|7|6|9|3|2|8|1|5
            #8|9|2|7|1|5|4|6|3
            #1|3|5|8|4|6|9|7|2
            #6|5|4|1|2|9|3|8|7
            #7|8|1|3|5|4|2|9|6
            #9|2|3|6|8|7|1|5|4
        """.trimMargin("#")
    }
})