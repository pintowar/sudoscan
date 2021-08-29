package com.github.pintowar.sudoscan.viewer

import com.github.pintowar.sudoscan.core.OpenCvWrapper
import com.github.pintowar.sudoscan.core.Plotter.combineSolutionToOriginal
import com.github.pintowar.sudoscan.core.solver.SudokuSolver
import com.github.pintowar.sudoscan.nd4j.RecognizerNd4j
import mu.KLogging
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.CanvasFrame
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Java2DFrameUtils
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.Color
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.WindowConstants
import kotlin.system.measureTimeMillis

class SudokuCamera(private val color: Color = Color.BLUE,
                   private val record: Boolean = false,
                   videoPath: String = "/tmp/sudoku.mp4") {

    companion object : KLogging() {
        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480
    }

    private val grabber = OpenCVFrameGrabber(0)
    private val recorder: FFmpegFrameRecorder
    private val frame = CanvasFrame("SudoScan UI")
    private val solver = SudokuSolver(RecognizerNd4j())
    private val fps = 10.0

    init {
        grabber.imageWidth = FRAME_WIDTH
        grabber.imageHeight = FRAME_HEIGHT

        recorder = FFmpegFrameRecorder(videoPath, FRAME_WIDTH, FRAME_HEIGHT)
        recorder.videoCodec = avcodec.AV_CODEC_ID_MPEG4
        recorder.format = "mp4"
        recorder.frameRate = fps
        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P
        recorder.videoBitrate = (FRAME_WIDTH * FRAME_HEIGHT * fps * 10).toInt()
        recorder.videoQuality = 0.1

        with(frame) {
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isVisible = true
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    dispose()
                }
            })
        }
    }

    fun showAndRecord(img: Mat, sol: Mat?) {
        val frm = solutionToFrame(img, sol)!!
        frame.showImage(frm)
        if (frame.isVisible && record) recorder.record(frm)
    }

    fun run() {
        grabber.start()
        if (record) recorder.start()

        while (frame.isVisible) {
            val img = Java2DFrameUtils.toMat(grabber.grab())
            if (img != null) {
                val time = measureTimeMillis {
                    val sol = solver.solve(img, color)
                    showAndRecord(img, sol)
                }
                logger.debug("Processing took: $time ms")
            }
        }
    }

    fun solutionToFrame(img: Mat, sol: Mat?) =
            OpenCvWrapper.toFrame(if (sol != null) combineSolutionToOriginal(img, sol) else img)

    fun dispose() {
        frame.isVisible = false
        if (record) recorder.stop()
        grabber.stop()
    }

}
