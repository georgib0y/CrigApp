package com.github.georgib0y.crigapp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "UCI";

class UciException(message: String) : Exception(message)
class BadPositionException(val idx: Int) : Exception("bad position at move idx $idx")

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

    private external fun validatePosition(posStr: String): Int
    private external fun searchPosition(ptr: Long, posStr: String): String
    private external fun logUciPosition(ptr: Long, posStr: String)

    private val ptr = initUci();

    fun newGame() {
        uciNewGame(ptr)
        Log.d(TAG, "uci new gamed")
    }

    fun validate(position: String) {
        Log.d(TAG, "validating position: '$position'")
        val res = validatePosition(position)
        if (res != -1) {
            Log.e(TAG, "bad move at idx $res")
            throw BadPositionException(res)
        }
    }

    fun search(position: String): String {
        Log.d(TAG, "searching position: $position")
        return searchPosition(ptr, position)
    }

    fun logPos(position: String) {
        Log.d(TAG, "asking zig to log uci state")
        logUciPosition(ptr, position)
    }
}