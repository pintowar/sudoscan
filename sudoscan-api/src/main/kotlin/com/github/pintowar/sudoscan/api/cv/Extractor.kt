package com.github.pintowar.sudoscan.api.cv

import com.github.pintowar.sudoscan.api.GrayMatrix
import com.github.pintowar.sudoscan.api.ImageMatrix
import com.github.pintowar.sudoscan.api.PuzzleCells
import com.github.pintowar.sudoscan.api.SudokuCell

object Extractor {

    /**
     * Splits the image into a 9x9 (81) grid. Assuming an image with a frontal perspective of a Sudoku is provided,
     * a list of bounding boxes is returned.
     *
     * @param img original image (for better function, assume an image with a frontal perspective of a Sudoku puzzle).
     * @return list of bounding boxes of every sudoku piece (cell).
     */
    internal fun splitSquares(img: ImageMatrix): List<BBox> {
        assert(img.height() == img.width())

        val numPieces = 9
        val side = img.height().toDouble() / numPieces

        return (0 until numPieces).flatMap { i ->
            (0 until numPieces).map { j ->
                BBox((j * side).toInt(), (i * side).toInt(), side.toInt(), side.toInt())
            }
        }
    }

    /**
     * Scans the image in search of any relevant data (on the sudoku context, it searches for a number in a cell).
     * The process assumes that invalid pixel is BLACK and valid pixel is white (for instance, imagine a black image
     * with a white eight in the middle).
     *
     * The scan uses a [margin] parameter that inform it the start area. This start area is a secure area with a
     * safe distance from the borders. For the sudoku context, when it scans a cell this is used to exclude its borders.
     * With the safe area informed, it marks all valid (white) data as gray.
     *
     * After the first step, it will then scan the full square (this time without the secure margin) and change all not
     * gray pixel to an invalid pixel (black) and all gray pixel to a valid (white) pixel.
     *
     * After this process it will detect the valid bounds of the final white object found.
     *
     * @param inputImg gray scale image to be scanned.
     * @param margin margin of the initial bounding box with a safe margin from the borders.
     * @return a square containing the bounds of an object (number on sudoku context).
     */
    internal fun findLargestFeature(inputImg: GrayMatrix, margin: Int): RectangleCorners {
        val bBox = BBox(margin, margin, inputImg.width() - 2 * margin, inputImg.height() - 2 * margin)
        return inputImg.clone().findLargestFeature(bBox)
    }

    /**
     * Extract sudoku cell information from an input image. For proper extraction, this must be a frontal view
     * of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @param bBox bounding box of the area to be extracted from the original image.
     * @return an object with the image extracted and additional information about the cell.
     */
    internal fun extractCell(img: GrayMatrix, bBox: BBox): SudokuCell = try {
        val cell = img.crop(bBox)
        val margin = ((cell.width() + cell.height()) / 5.0).toInt()
        val rect = findLargestFeature(cell, margin)

        val noBorder = cell.crop(rect.bBox())
        SudokuCell(noBorder)
    } catch (e: RuntimeException) {
        SudokuCell.EMPTY
    }

    /**
     * This function has the objective to find the "biggest square" in the image, crop it and changes its perspective
     * so that the view is frontal.
     *
     * This function is a pipe that "converts to grayscale" -> "pre process gray image" -> "find its corners" ->
     * "get frontal perspective". This function returns an [PreProcessPhases], that stores some images of the pipe.
     *
     * @param img original image to be processed.
     * @return original image with a frontal view.
     */
    fun preProcessPhases(img: ImageMatrix): PreProcessPhases {
        val gray = img.toGrayScale()
        val proc = gray.preProcessGrayImage()
        val corners = proc.findCorners()
        val frontal = gray.frontalPerspective(corners)

        return PreProcessPhases(gray, proc, frontal)
    }

    /**
     * Extract sudoku cells information from an input image.
     * For proper extraction, this must be a frontal view of the sudoku puzzle.
     *
     * @param img input image (for proper extraction, this must be a frontal view of the sudoku puzzle).
     * @return an object with the image extracted and additional information about the cell.
     */
    fun extractPuzzleCells(img: GrayMatrix): PuzzleCells {
        val squares = splitSquares(img)
        return PuzzleCells(squares.map { s -> extractCell(img, s) })
    }
}