package io.github.pintowar.sudoscan.api.spi

import io.github.pintowar.sudoscan.api.Digit
import io.github.pintowar.sudoscan.api.SudokuCell

class MockRecognizer : Recognizer {
    override val name = "Mock"
    override fun predict(cells: List<SudokuCell>): Sequence<Digit> =
        cells.indices.asSequence().map { Digit.Valid(it, 1.0) }
}