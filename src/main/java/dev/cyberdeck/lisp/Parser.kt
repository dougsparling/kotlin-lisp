package dev.cyberdeck.lisp

/*

Symbol = str              # A Scheme Symbol is implemented as a Python str
Number = (int, float)     # A Scheme Number is implemented as a Python int or float
Atom   = (Symbol, Number) # A Scheme Atom is a Symbol or Number
List   = list             # A Scheme List is implemented as a Python list
Exp    = (Atom, List)     # A Scheme expression is an Atom or List
Env    = dict             # A Scheme environment (defined below)
                          # is a mapping of {variable: value}
*/

class SyntaxErr(msg: String) : RuntimeException(msg)

//sealed interface Value

sealed class Exp //: Value
sealed class Atom : Exp()
class Proc(val proc: (L) -> Exp) : Exp()
data class Symbol(val sym: String) : Atom()
data class Num(val num: Number) : Atom()
data class Bool(val bool: Boolean) : Atom()
data class L(val list: List<Exp>) : Exp() {
    operator fun get(idx: Int): Exp = list[idx]
    val size = list.size
}

val Nil = L(emptyList())

fun listExp(vararg stuff: Exp) = L(stuff.toList())

fun parse(str: String): List<String> {
    return str.replace("(", " ( ").replace(")", " ) ").split(' ').filter { it.isNotBlank() }
}

fun readFromTokens(tokens: List<String>): Exp = readFromTokens(ArrayDeque(tokens))

private fun readFromTokens(tokens: ArrayDeque<String>): Exp {
    if (tokens.isEmpty()) syntaxError("unexpected EOF")

    return when (val token = tokens.removeFirst()) {
        "(" -> {
            val mutL = mutableListOf<Exp>()
            while (tokens.first() != ")") {
                mutL += readFromTokens(tokens)
                if (tokens.isEmpty()) syntaxError("expected ) but hit EOF")
            }
            tokens.removeFirst() // pop ")"
            L(mutL)
        }
        ")" -> syntaxError("unexpected )")
        else -> atom(token)
    }
}

private fun syntaxError(msg: String): Nothing = throw SyntaxErr(msg)

private fun atom(token: String) = when (token) {
    "true" -> Bool(true)
    "false" -> Bool(false)
    else -> token.toIntOrNull()?.let { Num(it) } ?: token.toFloatOrNull()?.let { Num(it) } ?: Symbol(token)
}

fun Exp.pp(parens: Boolean = true): String = when (this) {
    is L -> when {
        this.size == 0 -> "nil"
        parens -> "(${list.joinToString(separator = " ", transform = Exp::pp)})"
        else -> list.joinToString(separator = " ", transform = Exp::pp)
    }
    is Num -> num.toString()
    is Bool -> bool.toString()
    is Symbol -> sym
    is Proc -> "Proc"
}