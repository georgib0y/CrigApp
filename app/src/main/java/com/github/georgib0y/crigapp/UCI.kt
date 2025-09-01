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
            Log.d(TAG, "loaded lib dl")
            System.loadLibrary("crig")
            Log.d(TAG, "loaded lib crig")
        }
    }


    external fun initUci(): Long
    external fun uciNewGame(ptr: Long)
    external fun sendPosition(ptr: Long, posStr: String): String

    private val ptr = initUci();

    fun newGame() {
        uciNewGame(ptr)
        Log.d(TAG, "uci new gamed")
    }

    fun go(position: String): String {
        Log.d(TAG, "sending position: $position")
        return sendPosition(ptr, position)
    }

}