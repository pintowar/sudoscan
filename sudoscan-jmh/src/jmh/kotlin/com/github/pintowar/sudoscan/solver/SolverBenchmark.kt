package com.github.pintowar.sudoscan.solver

import com.github.pintowar.sudoscan.choco.SolverChoco
import com.github.pintowar.sudoscan.ojalgo.SolverOjAlgo
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
open class SolverExecutionPlan {
    val instances = mapOf(
        "problem01" to "549001738367008001200073040000900005000705460135840070004000307780350006023080000",
        "problem02" to "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
        "problem03" to "400000070060850240000301065049078500007032008280009430120703004700010000006200009"
    )

    val solvers = mapOf(
        "choco" to SolverChoco(),
        "ojAlgo" to SolverOjAlgo()
    )

    @Param(*["problem01", "problem02", "problem03"])
    var problem = ""

    @Param(*["choco", "ojAlgo"])
    var solver = ""

    fun currentProblem() = instances.getValue(problem)

    fun currentSolver() = solvers.getValue(solver)
}

@Fork(value = 1)
@Warmup(iterations = 2)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
open class SolverBenchmark {

    @Benchmark
    fun solverBenchmark(plan: SolverExecutionPlan) {
        val solver = plan.currentSolver()
        solver.solve(plan.currentProblem())
    }
}