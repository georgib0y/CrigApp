package com.github.georgib0y.crigapp

import android.os.Bundle
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.github.georgib0y.crig.ui.theme.CrigTheme
import kotlinx.coroutines.launch

private const val TAG = "MainActivity"

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
                            UciScaffold(uciState, onNextMove = {
                                uciViewModel.viewModelScope.launch {
                                    uciViewModel.tryMove(it)
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
    onNextMove: (String) -> Unit,
    onReset: () -> Unit,
    onLogPosition: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = { Text(text = "hello up here") },
        bottomBar = { Text("hello down here") }) { innerPadding ->
        Column(
            modifier = Modifier
                .safeContentPadding()
                .imePadding()
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Moves(
                moves = when (uciState.status) {
                    is UciStatus.Thinking -> uciState.moves + uciState.status.moves
                    else -> uciState.moves
                }, modifier = Modifier
                    .padding(innerPadding)
                    .padding(10.dp)
                    .imePadding()
                    .fillMaxHeight(0.8f)
            )
            MoveInput(
                status = uciState.status,
                onNextMove = onNextMove,
            )
            Row {
                Button(onClick = onReset) { Text("Reset") }
                Button(onClick = onLogPosition) { Text("Log Position") }
            }
        }

    }
}

@Composable
fun Moves(moves: List<String>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        items(moves) { move -> Text(text = move) }
    }
}

@Composable
fun MoveInput(status: UciStatus, onNextMove: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row {
        TextField(
            modifier = Modifier.fillMaxWidth(0.5f),
            value = text,
            onValueChange = { text = it },
            label = { Text("Next Move") },
            isError = status is UciStatus.Error,
            supportingText = {
                when (status) {
                    is UciStatus.Error -> Text(status.toString())
                    else -> {}
                }
            })
        when (status) {
            is UciStatus.Waiting -> Button(
                onClick = {
                    onNextMove(text)
                    text = ""
                }) { Text("Next Move") }

            is UciStatus.Thinking -> Button(onClick = {}, enabled = false) { Text("Thinking") }
            is UciStatus.Error -> Button(
                onClick = {}) { Text(if (text.isEmpty()) "Error" else "Next Move") }
        }
    }
}