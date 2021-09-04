package com.github.pintowar.sudoscan.core.spi

import com.github.pintowar.sudoscan.core.Digit
import java.util.*

interface Recognizer {

    companion object {
        fun provider(): Recognizer {
            val loader = ServiceLoader.load(Recognizer::class.java)
            val it = loader.iterator()
            return if (it.hasNext()) it.next() else throw ClassNotFoundException("No Recognizer found in classpath.")
        }
    }

    fun predict(digits: List<Digit>): List<Int>

}