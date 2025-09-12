package com.github.georgib0y.crigapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.github.georgib0y.crig.ui.theme.CrigTheme
import kotlinx.coroutines.launch

private const val TAG = "UCI"

class MainActivity : ComponentActivity() {
    private val uciViewModel = UciViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                uciViewModel.uciState.collect { uciState ->
                    setContent {
                        CrigTheme {
                            UciScaffold(uciState = uciState, onGo = {
                                uciViewModel.viewModelScope.launch {
                                    uciViewModel.tryPosition()
                                }
                            }, onUpdate = { idx, str ->
                                uciViewModel.viewModelScope.launch {
                                    uciViewModel.updateMove(idx, str)
                                }
                            }, onReset = {
                                uciViewModel.viewModelScope.launch {
                                    uciViewModel.newGame()
                                }
                            }, onLogPosition = {
                                uciViewModel.viewModelScope.launch {
                                    uciViewModel.logPosition()
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UciScaffold(
    uciState: UciState,
    onGo: () -> Unit,
    onUpdate: (Int, String) -> Unit,
    onReset: () -> Unit,
    onLogPosition: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = { Text(text = "hello up here") },
        bottomBar = { Text("hello down here") },
        floatingActionButton = { GoButton(uciState, onGo) }
    ) { innerPadding ->
        MoveList(
            modifier = Modifier
                .safeContentPadding()
                .imePadding()
                .padding(innerPadding),
            uciState = uciState,
            onUpdate = onUpdate,
        )
    }
}

@Preview
@Composable
fun UciScaffoldPreview() {
    val state = UciState(moves = listOf("e2e4", "e7e5", "g1f3"))

    UciScaffold(
        uciState = state,
        onReset = { Log.d(TAG, "clicked reset") },
        onLogPosition = { Log.d(TAG, "clicked log position") },
        onGo = { Log.d(TAG, "clicked go") },
        onUpdate = { idx, str -> Log.d(TAG, "updating $str at $idx") }
    )
}

@Composable
fun GoButton(uciState: UciState, onGo: () -> Unit) {
    val modifier = Modifier
        .imePadding()
        .padding(bottom = 10.dp)
    when (uciState.status) {
        is UciStatus.Waiting -> Button(
            modifier = modifier,
            onClick = onGo,
            enabled = true
        ) { Text("GO!") }

        is UciStatus.Thinking -> Button(
            modifier = modifier,
            onClick = onGo,
            enabled = false
        ) { Text("Think") }

        else -> Button(modifier = modifier, onClick = onGo) { Text("GO?") }
    }
}

@Composable
@NonSkippableComposable
fun Move(
    idx: Int,
    str: String,
    isThinking: Boolean,
    isBadMove: Boolean,
    onUpdate: (Int, String) -> Unit
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = str,
        onValueChange = {
            onUpdate(idx, it)
        },
        isError = isBadMove,
        supportingText = { if (isBadMove) Text("Bad Move!") },
        singleLine = true,
        enabled = !isThinking,
    )
}


@Composable
fun MoveList(modifier: Modifier = Modifier, uciState: UciState, onUpdate: (Int, String) -> Unit) {
    val badIdx = when (uciState.status) {
        is UciStatus.BadMove -> uciState.status.idx
        else -> -1
    }

    val moves = uciState.moves + ""
    val thinking = uciState.status is UciStatus.Thinking


    LazyColumn(modifier = modifier) {
        itemsIndexed(moves) { idx, move ->
            Log.d(TAG, "rendering move '$move' at idx $idx")
            Move(idx, move, thinking, idx == badIdx, onUpdate)
        }
    }
}