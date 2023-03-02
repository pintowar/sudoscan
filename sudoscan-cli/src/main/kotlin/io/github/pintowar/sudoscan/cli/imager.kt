package io.github.pintowar.sudoscan.api

import io.github.pintowar.sudoscan.api.cv.Extractor
import io.github.pintowar.sudoscan.api.cv.Plotter
import java.awt.Color
import java.awt.Dimension
import java.io.ByteArrayInputStream
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

fun main() {
    val matrix = File("/home/thiago/Projects/code/sudoscan/sudoscan-api/src/testFixtures/resources/imgs/sudoku01.jpg")
        .inputStream()
        .readAllBytes()
        .let(io.github.pintowar.sudoscan.api.ImageMatrix::fromBytes)

    val phases = Extractor.preProcessPhases(matrix)

    val sol = Puzzle.solved(
        "800010009050807010004090700060701020508060107010502090007040600080309040300050008",
        "072403560906020304130605082409030805020904030703080406290108053605070201041206970"
    )
    val result = Plotter.plotSolution(phases.frontal.frontalArea(), sol, Color.BLUE, Color.WHITE)
    val solution = Plotter.changePerspectiveToOriginalSize(phases.frontal, result, matrix.area())
    val bytes = Plotter.combineSolutionToOriginal(matrix, solution).toBytes("jpg")
//    val m = phases.frontal.perspectiveMatrix()
//    val solution = result.warpPerspective(m, matrix.area()).revertColors()
//
//    val bytes = solution.bitwiseAnd(matrix).toBytes("jpg")
    val img = ByteArrayInputStream(bytes).let { ImageIO.read(it) }

    with(JFrame("Test")) {
        size = Dimension(img.width, img.height)
        contentPane.add(JLabel().also { it.icon = ImageIcon(img) })

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        pack()
        isVisible = true
    }
}

//
// import com.github.pintowar.sudoscan.api.cv.Extractor
// import com.github.pintowar.sudoscan.api.cv.Plotter
// import com.github.pintowar.sudoscan.api.cv.area
// import org.bytedeco.opencv.global.opencv_imgcodecs
// import java.awt.Color
//
// fun main() {
//    val path = "/home/thiago/Projects/code/sudoscan/sudoscan-pages/src/jbake/assets/imgs/sudoku01.jpg"
//    val sudoku01 = opencv_imgcodecs.imread(path, opencv_imgcodecs.IMREAD_COLOR)
//
//    val prePhases = Extractor.preProcessPhases(sudoku01)
//    val sudoku02 = prePhases.preProcessedGrayImage
//    val cropped = prePhases.frontal
//
//    val sudoku03 = Extractor.preProcessGrayImage(cropped.img, false)
//    val sudoku04 = Extractor.removeGrid(sudoku03)
//
//    val puzzleCells = Extractor.extractPuzzleCells(sudoku04)
//    val sudoku05 = puzzleCells.toMat(true)
//
//    val solution = Puzzle.solved(
//        "000010000000400070000706008502000004600200000800900056080003000200600700309840010",
//        "427508369968032501135090420070361890094085137013074200706120945041059083050007602"
//    )
//
//    val sudoku06 = Plotter.plotSolution(cropped, solution, Color.BLUE, Color(255, 255, 255, 0))
//    val sudoku07 = Plotter.changePerspectiveToOriginalSize(cropped, sudoku06, sudoku01.area())
//    val sudoku08 = Plotter.combineSolutionToOriginal(sudoku01, sudoku07)
//
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku01.jpg", sudoku01)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku02.jpg", sudoku02)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku03.jpg", sudoku03)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku04.jpg", sudoku04)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku05.jpg", sudoku05)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku06.jpg", sudoku06)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku07.jpg", sudoku07)
//    opencv_imgcodecs.imwrite("/tmp/sudoku/sudoku08.jpg", sudoku08)
// }