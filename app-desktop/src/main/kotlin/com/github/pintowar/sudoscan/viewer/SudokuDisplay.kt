package com.github.pintowar.sudoscan.viewer

import com.github.pintowar.sudoscan.SudokuSolver
import java.awt.FlowLayout
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

class SudokuDisplay {

    private val solver = SudokuSolver()
    private val frame = JFrame()

    init {
        frame.contentPane.layout = FlowLayout()
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isVisible = true
    }

    private fun imageInputStream(resource: String): InputStream? {
        val cl = Thread.currentThread().contextClassLoader
        return cl.getResourceAsStream(resource)
    }

    fun showImages(path: String) {
        val original = ImageIO.read(imageInputStream(path))
        showImages(original)
    }

    fun showImages(original: BufferedImage) {
        val transformed = solver.solve(original)
        frame.contentPane.removeAll()
        frame.contentPane.add(JLabel(ImageIcon(original)))
        frame.contentPane.add(JLabel(ImageIcon(transformed)))
        frame.pack()
    }
}

fun main() {
    SudokuDisplay().showImages("sudoku01.jpg")
}