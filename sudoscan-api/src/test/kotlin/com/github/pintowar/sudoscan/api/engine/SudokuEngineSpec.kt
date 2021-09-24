package com.github.pintowar.sudoscan.api.engine

import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuFinalSolution
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class SudokuEngineSpec : StringSpec({

    val recognizer = mockk<Recognizer>()
    val solver = mockk<Solver>()
    val engine = SudokuEngine(recognizer, solver)

    val cells = "800010009050807010004090700060701020508060107010502090007040600080309040300050008".toList()
        .map { it.digitToInt() }

    val solverSol = "072403560906020304130605082409030805020904030703080406290108053605070201041206970".toList()
        .map { it.digitToInt() }

    "test solve" {
        every { recognizer.predict(any()) } returns cells
        every { solver.solve(any<List<Int>>(), false) } returns solverSol

        val result = engine.solveAndCombineSolution(sudoku)

        result.arrayWidth() shouldBe sudokuFinalSolution.arrayWidth()
        result.arrayHeight() shouldBe sudokuFinalSolution.arrayHeight()
        result.channels() shouldBe sudokuFinalSolution.channels()
    }

})