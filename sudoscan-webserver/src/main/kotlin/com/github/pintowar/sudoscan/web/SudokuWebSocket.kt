package com.github.pintowar.sudoscan.web

import io.micronaut.websocket.WebSocketBroadcaster
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.ServerWebSocket
import mu.KLogging

@ServerWebSocket("/ws/sudoku")
class SudokuWebSocket(private val broadcaster: WebSocketBroadcaster, private val service: SudokuService) : KLogging() {

    @OnMessage
    fun onMessage(message: SudokuInfo, session: WebSocketSession) {
        val sol = service.solve(message)
        broadcaster.broadcastSync(sol) { it.id == session.id }
    }
}