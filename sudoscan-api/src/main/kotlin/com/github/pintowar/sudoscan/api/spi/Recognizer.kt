package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.SudokuCell
import com.github.pintowar.sudoscan.api.spi.Recognizer.Companion.provider
import java.util.*

/**
 * This interface represents the main contract (function and properties) of a Recognizer.
 * Recognizers are responsible to classify a number found on an image.
 *
 * An implementation of this Recognizer can be found using the [provider] function. This function will load an
 * implementation of this interface (found on classpath) via SPI.
 */
interface Recognizer {

    companion object {

        private val properties = Thread.currentThread().contextClassLoader.let { cl ->
            Properties().also {
                cl.getResourceAsStream("sudoscan-recognizer.properties").let { res -> it.load(res) }
            }
        }

        /**
         * This function will load an implementation of this interface (found on classpath) via SPI.
         *
         * @return Recognizer implementation found in the classpath.
         */
        fun provider(): Recognizer {
            val loader = ServiceLoader.load(Recognizer::class.java)
            return loader.single()
        }
    }

    /**
     * Recognizers are usually separated modules that use a Machine Learning model to recognize numbers from images.
     * These models are provided by an external and can be accessed by an url. This url is provided by the property
     * "sudoscan.recognizer.model.url" and by default, can be found on the sudoscan-recognizer.properties file.
     *
     * This function restores the url configured on this property file.
     *
     * @return url there model can be downloaded.
     */
    fun modelUrl(): String = "sudoscan.recognizer.model.url".let { urlProperty ->
        System.getProperty(urlProperty) ?: properties.getProperty(urlProperty)!!
    }

    /**
     * Predict the number provided by a list images of sudoku cells. If the confidence/probability of the model
     * prediction is lower than 0.8, it will return an unknown digit.
     *
     * @param cells list of sudoku cells to have a number recognized.
     * @return sequence of digits with the number found on each sudoku cell. If an empty sudoku cell is provided or
     * the confidence of the model prediction is lower than 0.8, an unknown digit is returned.
     */
    fun reliablePredict(cells: List<SudokuCell>) = predict(cells).map {
        when (it) {
            is Digit.Valid -> if (it.confidence >= 0.8) it else Digit.Unknown
            else -> it
        }
    }.toList()

    /**
     * Name of Recognizer implementation.
     */
    val name: String

    /**
     * Predict the number provided by a list images of sudoku cells.
     *
     * @param cells list of sudoku cells to have a number recognized.
     * @return sequence of digits with the number found on each sudoku cell. If an empty sudoku cell is provided,
     * an unknown digit is returned.
     */
    fun predict(cells: List<SudokuCell>): Sequence<Digit>
}