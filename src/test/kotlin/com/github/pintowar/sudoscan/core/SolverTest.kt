package com.github.pintowar.sudoscan.core

import kotlin.test.Test
import kotlin.test.assertEquals

internal class SolverTest {

    @Test
    fun solve() {
        listOf(listOf(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "549261738367498521218573649476932815892715463135846972654129387781354296923687154"))
                .forEach { (given, exp) ->
                    val sol = Solver.solve(given)
                    assertEquals(exp, sol)
                }

        listOf(listOf(
                "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
                "000260000000490520018500609476032810892010003000006902650129080001004290900607154"))
                .forEach { (given, exp) ->
                    val sol = Solver.solve(given, false)
                    assertEquals(exp, sol)
                }
    }
}