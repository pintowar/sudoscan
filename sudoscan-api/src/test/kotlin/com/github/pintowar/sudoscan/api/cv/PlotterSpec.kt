package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.Puzzle
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudoku
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuFinalSolution
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuPerspectiveSolution
import com.github.pintowar.sudoscan.api.cv.CvSpecHelpers.sudokuSolution
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class PlotterSpec : StringSpec({

    "test plot solution" {
        val cropped = Extractor.cropImage(sudoku)
        val sol = Puzzle.solved(
            "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
            "072403560906020304130605082409030805020904030703080406290108053605070201041206970"
        )
        val result = Plotter.plotSolution(cropped, sol)

        result.arrayWidth() shouldBe sudokuSolution.arrayWidth()
        result.arrayHeight() shouldBe sudokuSolution.arrayHeight()
        result.channels() shouldBe sudokuSolution.channels()
    }

    "test change perspective to original size" {
        val cropped = Extractor.cropImage(sudoku)
        val result = Plotter.changePerspectiveToOriginalSize(cropped, sudokuSolution, sudoku.area())

        result.arrayWidth() shouldBe sudokuPerspectiveSolution.arrayWidth()
        result.arrayHeight() shouldBe sudokuPerspectiveSolution.arrayHeight()
        result.channels() shouldBe sudokuPerspectiveSolution.channels()
    }

    "test combine solution to original" {
        val result = Plotter.combineSolutionToOriginal(sudoku, sudokuPerspectiveSolution)

        result.arrayWidth() shouldBe sudokuFinalSolution.arrayWidth()
        result.arrayHeight() shouldBe sudokuFinalSolution.arrayHeight()
        result.channels() shouldBe sudokuFinalSolution.channels()
    }
})