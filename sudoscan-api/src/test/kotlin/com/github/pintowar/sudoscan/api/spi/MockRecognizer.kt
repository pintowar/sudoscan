package com.github.pintowar.sudoscan.api.spi

import com.github.pintowar.sudoscan.api.SudokuCell

class MockRecognizer : Recognizer {
    override val name = "Mock"
    override fun predict(cells: List<SudokuCell>) = cells.indices.toList()
}