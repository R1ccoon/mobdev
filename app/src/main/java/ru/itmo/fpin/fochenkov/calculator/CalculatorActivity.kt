package ru.itmo.fpin.fochenkov.calculator

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.itmo.fpin.fochenkov.calculator.ui.theme.CalculatorTheme

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        CalculatorScreen(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    val session = rememberCalculatorSession()
    val configuration = LocalConfiguration.current
    val isWideMode = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isWideMode) {
        WideCalculatorLayout(
            currentExpression = session.expression,
            onRemoveLast = session::eraseLastSymbol,
            onTokenSelected = session::appendToken,
            onResultRequested = session::calculate,
            modifier = modifier
        )
    } else {
        CompactCalculatorLayout(
            currentExpression = session.expression,
            onRemoveLast = session::eraseLastSymbol,
            onTokenSelected = session::appendToken,
            onResultRequested = session::calculate,
            modifier = modifier
        )
    }
}

@Stable
class CalculatorSession(private val evaluator: ExpressionInterpreter) {
    var expression by mutableStateOf("0")
        private set

    fun appendToken(token: String) {
        if (expression == "Infinity" || expression == "NaN" || expression == "0") {
            expression = token
            return
        }

        if (token.isOperator() && expression.lastOrNull()?.let { it.toString().isOperator() } == true) {
            expression = expression.dropLast(1) + token
            return
        }

        expression += token
    }

    fun eraseLastSymbol() {
        if (expression == "Infinity" || expression == "NaN") {
            expression = "0"
            return
        }

        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
        }

        if (expression.isEmpty()) {
            expression = "0"
        }

        if (expression.endsWith(".") || expression.lastOrNull()?.let { it.toString().isOperator() } == true) {
            expression = expression.dropLast(1)
        }

        if (expression.isEmpty()) {
            expression = "0"
        }
    }

    fun calculate() {
        expression = evaluator.solve(expression).toString()
    }
}

@Composable
fun rememberCalculatorSession(): CalculatorSession {
    val evaluator = remember { ExpressionInterpreter() }
    return remember(evaluator) { CalculatorSession(evaluator) }
}

@Composable
fun CompactCalculatorLayout(
    currentExpression: String,
    onRemoveLast: () -> Unit,
    onTokenSelected: (String) -> Unit,
    onResultRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {
        CalculatorDisplayBar(
            value = currentExpression,
            onClearPressed = onRemoveLast,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        CalculatorPad(
            onTokenSelected = onTokenSelected,
            onResultRequested = onResultRequested
        )
    }
}

@Composable
fun WideCalculatorLayout(
    currentExpression: String,
    onRemoveLast: () -> Unit,
    onTokenSelected: (String) -> Unit,
    onResultRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        CalculatorDisplayBar(
            value = currentExpression,
            onClearPressed = onRemoveLast,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        CalculatorPad(
            onTokenSelected = onTokenSelected,
            onResultRequested = onResultRequested,
            modifier = Modifier
                .weight(1.4f)
                .fillMaxHeight(),
            outerPadding = 0.dp,
            gap = 6.dp
        )
    }
}

@Composable
fun CalculatorDisplayBar(
    value: String,
    onClearPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressionText(
            text = value,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = onClearPressed) {
            Text(text = "C", fontSize = 24.sp)
        }
    }
}

@Composable
fun CalculatorPad(
    onTokenSelected: (String) -> Unit,
    onResultRequested: () -> Unit,
    modifier: Modifier = Modifier,
    outerPadding: Dp = 16.dp,
    gap: Dp = 8.dp
) {
    val keys = listOf(
        "7", "8", "9", "/",
        "4", "5", "6", "*",
        "1", "2", "3", "-",
        "0", ".", "=", "+"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier.padding(outerPadding),
        verticalArrangement = Arrangement.spacedBy(gap),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        items(keys.size) { index ->
            val key = keys[index]
            Button(
                onClick = {
                    if (key == "=") {
                        onResultRequested()
                    } else {
                        onTokenSelected(key)
                    }
                },
                shape = CircleShape,
                modifier = Modifier.aspectRatio(1f)
            ) {
                Text(text = key, fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun ExpressionText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 32.sp,
        modifier = modifier.padding(16.dp),
        textAlign = TextAlign.End
    )
}

private fun String.isOperator(): Boolean {
    return this == "+" || this == "-" || this == "*" || this == "/"
}