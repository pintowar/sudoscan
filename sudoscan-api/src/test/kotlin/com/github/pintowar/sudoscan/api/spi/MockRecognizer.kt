package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.Digit
import com.github.pintowar.sudoscan.api.SudokuCell

class MockRecognizer : Recognizer {
    override val name = "Mock"
    override fun predict(cells: List<SudokuCell>) = cells.indices.asSequence().map { Digit.Valid(it, 1.0) }
}