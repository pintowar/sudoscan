package io.github.pintowar.sudoscan.api.cv

import io.github.pintowar.sudoscan.api.Puzzle
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuFinalSolution
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuPerspectiveSolution
import io.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuSolution
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlotterSpec : StringSpec({

    "test plot solution" {
        val frontal = Extractor.preProcessPhases(sudoku).frontal
        val sol = Puzzle.solved(
            "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
            "072403560906020304130605082409030805020904030703080406290108053605070201041206970"
        )
        val result = Plotter.plotSolution(frontal.frontalArea(), sol)

        result.width() shouldBe sudokuSolution.width()
        result.height() shouldBe sudokuSolution.height()
        result.channels() shouldBe sudokuSolution.channels()
    }

    "test change perspective to original size" {
        val frontal = Extractor.preProcessPhases(sudoku).frontal
        val result = Plotter.changePerspectiveToOriginalSize(frontal, sudokuSolution, sudoku.area())

        result.width() shouldBe sudokuPerspectiveSolution.width()
        result.height() shouldBe sudokuPerspectiveSolution.height()
        result.channels() shouldBe sudokuPerspectiveSolution.channels()
    }

    "test combine solution to original" {
        val result = Plotter.combineSolutionToOriginal(sudoku, sudokuPerspectiveSolution)

        result.width() shouldBe sudokuFinalSolution.width()
        result.height() shouldBe sudokuFinalSolution.height()
        result.channels() shouldBe sudokuFinalSolution.channels()
    }
})