package com.github.pintowar.sudoscan.viewer

import com.github.pintowar.sudoscan.SudokuSolver
import com.github.pintowar.sudoscan.core.OpenCvWrapper
import com.github.pintowar.sudoscan.core.Plotter.combineSolutionToOriginal
import mu.KLogging
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.WindowConstants
import kotlin.system.measureTimeMillis

class SudokuCamera(val record: Boolean = false, videoPath: String = "/tmp/sudoku.mp4") {

    companion object : KLogging() {
        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480
    }

    private val grabber = OpenCVFrameGrabber(0)
    private val recorder: FFmpegFrameRecorder
    private val frame = CanvasFrame("SudoScan UI")
    private val solver = SudokuSolver()
    private var isOpen = true

    init {
        grabber.imageWidth = FRAME_WIDTH
        grabber.imageHeight = FRAME_HEIGHT

        recorder = FFmpegFrameRecorder(videoPath, FRAME_WIDTH, FRAME_HEIGHT)
        recorder.videoCodec = avcodec.AV_CODEC_ID_MPEG4
        recorder.format = "mp4"
//        recorder.frameRate = 30.0
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
//        recorder.videoBitrate = 10 * 1024 * 1024

        with(frame) {
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isVisible = true
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    isOpen = false
                }
            })
        }
    }

    fun showAndRecord(img: Mat, sol: Mat?) {
        val frm = solutionToFrame(img, sol)!!
        frame.showImage(frm)
        if (record) recorder.record(frm)
    }

    fun run() {
        isOpen = true
        grabber.start()
        if (record) recorder.start()
        var sol: Mat? = null

        var frm = grabber.grab()
        while (isOpen && frm != null) {
            val time = measureTimeMillis {
                frm = grabber.grab()
                val img = Java2DFrameUtils.toMat(frm)
                showAndRecord(img, sol)
                sol = solver.solve(img)
//                showAndRecord(solutionToImage(img, sol))
            }
            logger.info("Processing took: $time ms")
        }

    }

    fun solutionToFrame(img: Mat, sol: Mat?) =
            OpenCvWrapper.toFrame(if (sol != null) combineSolutionToOriginal(img, sol) else img)

    fun dispose() {
        logger.debug("Stopping!!!")
        if (record) recorder.stop()
        grabber.stop()
        logger.debug("Stopped.")
    }

}

fun main() {
    val cam = SudokuCamera(true)
    val mainThread = Thread.currentThread()
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            cam.dispose()
            mainThread.join()
        }
    })

    cam.run()
}