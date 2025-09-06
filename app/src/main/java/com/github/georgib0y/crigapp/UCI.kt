package com.github.georgib0y.crigapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UCI";

class UciException(message: String) : Exception(message)

// ULong assumes 64bit
class UCI {
    companion object {
        init {
            System.loadLibrary("c")
            Log.d(TAG, "loaded lib c")
            System.loadLibrary("dl")
            System.loadLibrary("log")
            Log.d(TAG, "loaded lib log")
            System.loadLibrary("crig")
            Log.d(TAG, "loaded lib crig")
        }
    }


    private external fun initUci(): Long
    private external fun uciNewGame(ptr: Long)
    private external fun sendPosition(ptr: Long, posStr: String): String
    private external fun logUciPosition(ptr: Long, posStr: String)

    private val ptr = initUci();

    fun newGame() {
        uciNewGame(ptr)
        Log.d(TAG, "uci new gamed")
    }

    fun go(position: String): String {
        Log.d(TAG, "sending position: $position")
        return sendPosition(ptr, position)
    }

    fun logPos(position: String) {
        Log.d(TAG, "asking zig to log uci state")
        logUciPosition(ptr, position)
    }
}