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
    class Thinking(val moves: List<String>) : UciStatus()
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

    fun genPosStr(startpos: Boolean, moves: List<String>): String {
        if (startpos) {
            return "position startpos"
        }
        return "position startpos moves " + moves.joinToString(separator = " ")
    }

    suspend fun logPosition() = withContext(Dispatchers.Default) {
        val isStartpos = uciState.value.moves.isEmpty()
        uciState.value.uci.logPos(genPosStr(isStartpos, uciState.value.moves))
    }

    suspend fun tryMove(moveStr: String) = withContext(Dispatchers.Default) {
        val theseMoves = moveStr.trim().lines()
        _uciState.update { curr ->
            UciState(
                uci = curr.uci,
                moves = curr.moves,
                status = UciStatus.Thinking(theseMoves)
            )
        }

        _uciState.update { curr ->
            val isStartpos = theseMoves.size == 1 && (theseMoves.first() == "startpos" || theseMoves.first().isEmpty())
            val nextMoves = curr.moves + theseMoves
            val position = genPosStr(isStartpos, nextMoves)

            try {
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