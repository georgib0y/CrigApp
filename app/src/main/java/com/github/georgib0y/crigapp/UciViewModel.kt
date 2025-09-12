package com.github.georgib0y.crigapp

import android.util.Log
import androidx.compose.runtime.currentComposer
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

private const val TAG = "UCI"

sealed class UciStatus {
    class Waiting() : UciStatus()
    class Thinking() : UciStatus()
    data class Error(val err: Throwable): UciStatus()
    data class BadMove(val idx: Int): UciStatus()
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

    fun updateMove(idx: Int, newMove: String) {
        _uciState.update { curr ->
            Log.d(TAG, "updating move")
            var newMoves: List<String>;
            if (idx >= curr.moves.size) {
                Log.d(TAG, "new move idx $idx outside of range")
                newMoves = curr.moves + newMove.trim()
            } else {
                newMoves = curr.moves.mapIndexed { i, m ->
                    Log.d(TAG, "new move idx $idx within range")
                    if (idx == i) newMove else m.trim()
                }
            }

            // filter out any blanks
            newMoves = newMoves.filterNot { it.isBlank() }

            UciState(
                uci = curr.uci,
                moves = newMoves,
                status = curr.status
            )
        }
    }

    suspend fun tryPosition() = withContext(Dispatchers.Default) {
        _uciState.update { curr ->
            UciState(
                uci = curr.uci,
                moves = curr.moves,
                status = UciStatus.Thinking()
            )
        }

        _uciState.update { curr ->
            val inStartpos = curr.moves.all{ m -> m.isBlank() }
            val posStr = genPosStr(inStartpos, curr.moves)
            try {
                curr.uci.validate(posStr)
            } catch (e: BadPositionException) {
                Log.e(TAG, "got bad position at idx ${e.idx}")
                return@update UciState(
                    uci = curr.uci,
                    moves = curr.moves,
                    status = UciStatus.BadMove(e.idx)
                )
            } catch (e: Throwable) {
                Log.e(TAG, "encountered error validating moves $e")
                return@update UciState(
                    uci = curr.uci,
                    moves = curr.moves,
                    status = UciStatus.Error(e)
                )
            }

            try {
                val bm = curr.uci.search(posStr)
                return@update UciState(
                    uci = curr.uci,
                    moves = if (inStartpos) listOf(bm) else curr.moves + bm,
                    status = UciStatus.Waiting()
                )
            } catch (e: Throwable) {
                Log.e(TAG, "encountered error searching $e")
                return@update UciState(
                    uci = curr.uci,
                    moves = curr.moves,
                    status = UciStatus.Error(e)
                )
            }
        }
    }

    fun genPosStr(startpos: Boolean, moves: List<String>): String {
        if (startpos) {
            return "position startpos"
        }
        return "position startpos moves " + moves.filterNot { it.isBlank() }.joinToString(separator = " ")
    }

    suspend fun logPosition() = withContext(Dispatchers.Default) {
        val isStartpos = uciState.value.moves.isEmpty()
        uciState.value.uci.logPos(genPosStr(isStartpos, uciState.value.moves))
    }
}