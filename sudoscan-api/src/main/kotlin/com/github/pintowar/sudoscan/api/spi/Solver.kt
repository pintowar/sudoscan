package com.github.pintowar.sudoscan.api.spi

import java.util.*

interface Solver {

    companion object {
        fun provider(): Solver {
            val loader = ServiceLoader.load(Solver::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Solver found in classpath.")
        }
    }

    fun solve(problem: String, entireSol: Boolean = true): String {
        val prob = problem.toList().map(Character::getNumericValue)
        return solve(prob, entireSol).joinToString("")
    }

    val name: String

    fun solve(problem: List<Int>, entireSol: Boolean = true): List<Int>
}