package com.github.pintowar.sudoscan.cli.factory

import com.github.pintowar.sudoscan.api.engine.SudokuEngine
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import com.github.pintowar.sudoscan.opencv.OpenCvExtractor
import com.github.pintowar.sudoscan.opencv.OpenCvPlotter
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class SudoscanComponents {

    @Singleton
    fun solver(): Solver = Solver.provider()

    @Singleton
    fun recognizer(): Recognizer = Recognizer.provider()

    @Singleton
    fun engine(recognizer: Recognizer, solver: Solver): SudokuEngine =
        SudokuEngine(recognizer, solver, OpenCvExtractor, OpenCvPlotter)
}