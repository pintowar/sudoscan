package com.github.pintowar.sudoscan.web

import com.github.pintowar.sudoscan.api.engine.SudokuEngine
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import io.micronaut.context.annotation.Context
import io.micronaut.core.annotation.Introspected
import mu.KLogging
import org.beryx.awt.color.ColorFactory
import java.awt.Color
import java.util.*

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

}

@Introspected
data class SudokuInfo(val encodedImage: String, val color: String) {
    private val template = "data:image/(.*);base64,"
    private val groups = Regex("^${template}(.*)").find(encodedImage)?.groupValues
    private val type = groups?.get(1) ?: throw IllegalArgumentException("No type declared on the encoded image.")
    private val base64Img = groups?.get(2) ?: throw IllegalArgumentException("No type declared on the encoded image.")

    fun decode(): ByteArray {
        return Base64.getDecoder().decode(base64Img)
    }

    fun encode(bytes: ByteArray): String {
        val str = Base64.getEncoder().encodeToString(bytes)
        return template.replace("(.*)", type) + str
    }

    fun extension() = when (type) {
        "jpeg", "jpg" -> ".jpg"
        else -> ".$type"
    }

    fun awtColor(): Color = ColorFactory.valueOf(color)
}