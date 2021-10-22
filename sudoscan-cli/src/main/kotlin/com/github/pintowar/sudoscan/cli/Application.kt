package com.github.pintowar.sudoscan.cli

import com.github.pintowar.sudoscan.api.engine.SudokuEngine
import io.micronaut.configuration.picocli.PicocliRunner
import io.micronaut.context.BeanContext
import jakarta.inject.Inject
import org.beryx.awt.color.ColorFactory
import picocli.CommandLine.*
import java.awt.Color
import java.io.File

@Command(name = "sudoscan-cli", version = ["CLI Version 0.9"], mixinStandardHelpOptions = true)
class Application : Runnable {

    internal class ColorConverter : ITypeConverter<Color> {
        override fun convert(value: String): Color =
            if ("none" == value.lowercase()) ColorFactory.web("white", 0.0) else ColorFactory.valueOf(value)
    }

    @Inject
    lateinit var beanContext: BeanContext

    @Option(
        names = ["-s", "--solution"],
        description = ["Solution color (An AWT color name or NONE to result solution numbers)"],
        defaultValue = "BLUE", converter = [ColorConverter::class]
    )
    lateinit var solutionColor: Color

    @Option(
        names = ["-p", "--predicted"],
        description = ["Recognized color (An AWT color name or NONE to result recognized numbers)"],
        defaultValue = "NONE", converter = [ColorConverter::class]
    )
    lateinit var recognizedColor: Color

    @Option(
        names = ["-r", "--record"], description = ["In case the solution must be recorded on a video file"],
        defaultValue = "false"
    )
    var record: Boolean = false

    @Option(names = ["-d", "--debug"], description = ["Debug mode"], defaultValue = "false")
    var debug: Boolean = false

    @Option(
        names = ["-f", "--file"],
        description = [
            "File path to record the solution with solution " +
                "(in case -r is provided). Must be a mp4 extension. Ex.: /my_folder/solution.mp4"
        ]
    )
    var file: File = File("${System.getProperty("java.io.tmpdir")}${System.getProperty("file.separator")}sudoku.mp4")

    override fun run() {
        val engine = beanContext.getBean(SudokuEngine::class.java)
        val camera = SudokuCamera(engine, solutionColor, recognizedColor, debug, record, file.absolutePath)
        camera.startCapture()
    }
}

fun main(args: Array<String>) {
    PicocliRunner.run(Application::class.java, *args)
}