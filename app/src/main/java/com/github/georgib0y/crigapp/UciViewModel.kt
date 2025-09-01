package com.github.georgib0y.crigapp

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

sealed class UciStatus {
    class Waiting() : UciStatus()
    class Thinking() : UciStatus()
    data class Error(val err: Throwable): UciStatus()
}
data class UciState(
    val uci: UCI = UCI(),
    val moves: List<String> = emptyList(),
    val status: UciStatus = UciStatus.Waiting(),
)

class UciViewModel : ViewModel() {
    private val _uciState: MutableStateFlow<UciState> = MutableStateFlow(UciState())
    val uciState: StateFlow<UciState> = _uciState.asStateFlow()

    fun newGame() {
        _uciState.update { curr ->
            curr.uci.newGame()
            UciState(
                uci = curr.uci,
                moves = emptyList(),
                status = UciStatus.Waiting()
            )
        }
    }

    suspend fun tryMove(move: String) = withContext(Dispatchers.Default) {
        _uciState.update { curr ->
            UciState(
                uci = curr.uci,
                moves = curr.moves,
                status = UciStatus.Thinking()
            )
        }

        _uciState.update { curr ->
            val isStartpos = (move == "startpos" || move.isEmpty())

            val nextMoves = curr.moves + move
            try {
                val position = if (isStartpos && curr.moves.isEmpty()) {
                    "position startpos"
                } else if (move.length < 4 || move.length > 5) {
                    throw UciException("invalid move: $move")
                } else {
                    "position startpos moves " + nextMoves.joinToString(separator =  " ")
                }

                val bm = curr.uci.go(position)

                return@update UciState(
                    uci = curr.uci,
                    moves = if (isStartpos) listOf(bm) else nextMoves + bm,
                    status = UciStatus.Waiting()
                )
            } catch (err: Throwable) {
                return@update UciState(
                    curr.uci,
                    curr.moves,
                    status = UciStatus.Error(err))
            }
        }
    }
}