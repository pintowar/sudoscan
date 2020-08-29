package com.github.pintowar.sudoscan.viewer

import com.github.pintowar.sudoscan.SudokuSolver
import com.github.pintowar.sudoscan.core.OpenCvWrapper
import mu.KLogging
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.videoio.VideoCapture
import org.opencv.videoio.VideoWriter
import org.opencv.videoio.Videoio
import java.awt.FlowLayout
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.WindowConstants

class SudokuCamera {

    companion object : KLogging() {
        init {
            OpenCV.loadShared()
        }
    }

    private val capture = VideoCapture()
    private val frame = JFrame()
    private val solver = SudokuSolver()

    private val frameSize = Size(640.0, 480.0)

    private val writer = VideoWriter("/tmp/sudoku.avi", VideoWriter.fourcc('X', 'V', 'I', 'D'),
            capture.get(Videoio.CAP_PROP_FPS), frameSize, true)

    init {
        frame.contentPane.layout = FlowLayout()
        frame.setLocationRelativeTo(null)
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.isVisible = true
    }

    fun showImages(img: BufferedImage) {
        frame.contentPane.removeAll()
        frame.contentPane.add(JLabel(ImageIcon(img)))
        frame.pack()
    }

    fun destroy() {
        if (capture.isOpened) {
            capture.release()
            writer.release()
            OpenCvWrapper.destroy()
        }
    }

    fun run() {
        destroy()
        capture.open(0)

        while (capture.isOpened) {
            val image = Mat()
            logger.info("Capturing!!")
            capture.read(image)
            val img = OpenCvWrapper.cvtColor(image, Imgproc.COLOR_BGR2RGB)
            val sol = solver.solve(img)

            val back = OpenCvWrapper.toImage(sol)
            showImages(back)
            writer.write(sol)
        }

        destroy()
    }

}

fun main() {
    val cam = SudokuCamera()

    val mainThread = Thread.currentThread()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            println("Finishing!!")
            cam.destroy()
            mainThread.join()
        }
    })

    cam.run()
}