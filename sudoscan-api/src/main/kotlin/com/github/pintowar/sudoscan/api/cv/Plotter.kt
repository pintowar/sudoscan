package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.Puzzle
import java.awt.Color

interface Plotter<T : ImageMatrix> {

    fun plotSolution(
        image: FrontalPerspective<T>,
        solution: Puzzle,
        solutionColor: Color = Color.GREEN,
        recognizedColor: Color = Color.RED
    ): T

    fun changePerspectiveToOriginalSize(
        frontal: FrontalPerspective<T>,
        sudokuResult: T,
        originalArea: Area
    ): T

    fun combineSolutionToOriginal(original: T, solution: T): T
}