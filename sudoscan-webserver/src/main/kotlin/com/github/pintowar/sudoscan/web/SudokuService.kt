package com.github.pintowar.sudoscan.web

import com.github.pintowar.sudoscan.api.engine.SudokuEngine
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import io.micronaut.context.annotation.Context
import mu.KLogging

@Context
class SudokuService : KLogging() {

    private val solver = Solver.provider()
    private val recognizer = Recognizer.provider()
    private val engine = SudokuEngine(recognizer, solver)

    fun solve(sudoku: SudokuInfo): String {
        val bytes = sudoku.decode()
        val sol = engine.solveAndCombineSolution(bytes, sudoku.awtColor(), sudoku.extension())
        return sudoku.encode(sol)
    }

    fun info(): Map<String, String> = mapOf("solver" to solver.name, "recognizer" to recognizer.name)
}