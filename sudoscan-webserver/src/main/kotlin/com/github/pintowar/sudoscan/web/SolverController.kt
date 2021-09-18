package com.github.pintowar.sudoscan.web

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import mu.KLogging

@Controller("/api")
class SolverController(private val service: SudokuService) : KLogging() {

    @Post("/solve", consumes = [MediaType.APPLICATION_JSON], produces = [MediaType.TEXT_PLAIN])
    fun solve(@Body sudoku: SudokuInfo): String {
        return service.solve(sudoku)
    }

    @Get("/engine-info", produces = [MediaType.APPLICATION_JSON])
    fun engineInfo() = service.info()
}
