package com.github.pintowar.sudoscan.viewer

import com.github.pintowar.sudoscan.api.engine.SudokuEngine
import com.github.pintowar.sudoscan.api.spi.Recognizer
import com.github.pintowar.sudoscan.api.spi.Solver
import mu.KLogging
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.*
import org.bytedeco.opencv.opencv_core.Mat
import java.awt.Color
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.WindowConstants
import kotlin.system.measureTimeMillis

/**
 * Responsible to maintain the application desktop window, grab images from a webcam, send it to the SudokuEngine
 * and plot its final image to the frame (the application desktop window).
 *
 * @property color color to plot the Sudoku solution (Default: blue).
 * @property record flag to record or not the grabbed video (Default: false).
 * @param videoPath in case of recording the video, this is the file where it must be saved (Default: /tmp/sudoku.mp4).
 */
class SudokuCamera(
    private val color: Color = Color.BLUE,
    private val record: Boolean = false,
    videoPath: String = "/tmp/sudoku.mp4"
) {

    companion object : KLogging() {
        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480
    }

    private val recognizer: Recognizer = Recognizer.provider()
    private val solver: Solver = Solver.provider()
    private val engine = SudokuEngine(recognizer, solver)
    private val grabber = OpenCVFrameGrabber(0)
    private val recorder: FFmpegFrameRecorder
    private val frame = CanvasFrame("SudoScan UI - ${recognizer.name} / ${solver.name}")
    private val fps = 10.0

    init {
        grabber.imageWidth = FRAME_WIDTH
        grabber.imageHeight = FRAME_HEIGHT

        recorder = FFmpegFrameRecorder(videoPath, FRAME_WIDTH, FRAME_HEIGHT).also {
            it.videoCodec = avcodec.AV_CODEC_ID_MPEG4
            it.format = "mp4"
            it.frameRate = fps
            it.pixelFormat = avutil.AV_PIX_FMT_YUV420P
            it.videoBitrate = (FRAME_WIDTH * FRAME_HEIGHT * fps * 10).toInt()
            it.videoQuality = 0.1
        }

        with(frame) {
            setLocationRelativeTo(null)
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isVisible = true
            addWindowListener(object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    this@SudokuCamera.dispose()
                }
            })
        }
    }

    /**
     * Function that plot the image to the application frame and saves the video.
     */
    private fun showAndRecord(solution: Mat) {
        val frm = Java2DFrameUtils.toFrame(solution)
        frame.showImage(frm)
        if (frame.isVisible && record) recorder.record(frm)
    }

    /**
     * Executes the main loop that continuously grab images from a webcam, send it to the SudokuEngine
     * and plot its final image to the frame.
     */
    fun run() {
        grabber.start()
        if (record) recorder.start()
        var isGrabbing: Boolean = !grabber.isTriggerMode
        while (frame.isVisible && isGrabbing) {
            isGrabbing = try {
                val img = Java2DFrameUtils.toMat(grabber.grab())
                if (img != null) {
                    val time = measureTimeMillis {
                        val sol = engine.solveAndCombineSolution(img, color)
                        showAndRecord(sol)
                    }
                    logger.debug { "Processing took: $time ms" }
                }
                !grabber.isTriggerMode
            } catch (e: FrameGrabber.Exception) {
                logger.warn { "Could Not grab frame (FrameGrabber)!" }
                false
            }
        }
    }

    /**
     * Function to close this the grabber and recorder.
     */
    fun dispose() {
        frame.isVisible = false
        if (record) recorder.stop()
        grabber.stop()
    }
}