package ru.itmo.fpin.fochenkov.calculator

class ExpressionInterpreter {
    fun solve(expression: String): Double {
        val values = expression
            .split("+", "-", "*", "/")
            .map { it.toDouble() }

        val operation = expression.firstOrNull { symbol ->
            symbol == '+' || symbol == '-' || symbol == '*' || symbol == '/'
        }

        return when (operation) {
            '+' -> values[0] + values[1]
            '-' -> values[0] - values[1]
            '*' -> values[0] * values[1]
            '/' -> values[0] / values[1]
            else -> values[0]
        }
    }
}