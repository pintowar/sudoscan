package io.github.pintowar.sudoscan.cli

import io.github.pintowar.sudoscan.api.engine.SudokuEngine
import mu.KLogging
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.*
import java.awt.Color
import java.awt.Dimension
import java.awt.image.BufferedImage
import javax.swing.WindowConstants
import kotlin.system.measureTimeMillis

/**
 * Responsible to maintain the application desktop window, grab images from a webcam, send it to the SudokuEngine
 * and plot its final image to the frame (the application desktop window).
 *
 * @param engine the Sudoku engine to read the problem and generate the solution.
 * @param solutionColor the color of solution digits to be plotted on solution (Default: blue).
 * @param recognizedColor the color of recognized digits to be plotted on solution (Default: red).
 * @property debug flag to activate debug mode (Default: false).
 * @property record flag to record or not the grabbed video (Default: false).
 * @param videoPath in case of recording the video, this is the file where it must be saved (Default: /tmp/sudoku.mp4).
 */
class SudokuCamera(
    private val engine: SudokuEngine,
    private val solutionColor: Color = Color.BLUE,
    private val recognizedColor: Color = Color.RED,
    private val debug: Boolean = false,
    private val record: Boolean = false,
    videoPath: String = "/tmp/sudoku.mp4"
) {

    companion object : KLogging() {
        const val FRAME_WIDTH = 640
        const val FRAME_HEIGHT = 480
    }

    private val grabber = OpenCVFrameGrabber(0)
    private val recorder: FFmpegFrameRecorder
    private val frame = CanvasFrame("SudoScan UI - ${engine.components()}")
    private val fps = 10.0
    private val debugScale = if (!debug) 1.0 else 1.5
    private val frmWidth = (FRAME_WIDTH * debugScale).toInt()
    private val frmHeight = (FRAME_HEIGHT * debugScale).toInt()

    init {
        grabber.imageWidth = FRAME_WIDTH
        grabber.imageHeight = FRAME_HEIGHT

        recorder = FFmpegFrameRecorder(videoPath, frmWidth, frmHeight).also {
            it.videoCodec = avcodec.AV_CODEC_ID_MPEG4
            it.format = "mp4"
            it.frameRate = fps
            it.pixelFormat = avutil.AV_PIX_FMT_YUV420P
            it.videoBitrate = (frmWidth * frmHeight * fps * 10).toInt()
            it.videoQuality = 0.1
        }

        with(frame) {
            defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            isVisible = true

            size = Dimension(frmWidth, frmHeight)
            setLocationRelativeTo(null)
        }
    }

    /**
     * Function that plot the image to the application frame and saves the video.
     */
    private fun showAndRecord(solution: BufferedImage) {
        frame.showImage(solution)
        if (frame.isVisible && record) recorder.record(Java2DFrameUtils.toFrame(solution))
    }

    /**
     * Executes the main loop that continuously grab images from a webcam, send it to the SudokuEngine
     * and plot its final image to the frame.
     */
    fun startCapture() {
        grabber.start()
        if (record) recorder.start()
        var isGrabbing: Boolean = !grabber.isTriggerMode
        while (frame.isVisible && isGrabbing) {
            isGrabbing = try {
                val img = Java2DFrameUtils.toBufferedImage(grabber.grab())
                if (img != null) {
                    val time = measureTimeMillis {
                        val sol =
                            engine.solveAndCombineSolution(img, solutionColor, recognizedColor, debugScale = debugScale)
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
        stopCapture()
    }

    /**
     * Function to stop the grabber and recorder.
     */
    private fun stopCapture() {
        frame.isVisible = false
        if (record) recorder.stop()
        grabber.stop()
    }
}