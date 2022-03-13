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

class SyntaxError(msg: String) : RuntimeException(msg)

//sealed interface Value

sealed class Exp //: Value
abstract class Atom : Exp()
data class Symbol(val sym: String) : Atom()
data class Num(val num: Number) : Atom()
data class L(val list: List<Exp>): Exp()

fun listExp(vararg stuff: Exp) = L(stuff.toList())

// TODO: built-in env type, maybe it should be above Exp?
class Proc(val proc: (List<Exp>) -> Exp): Exp()

fun parse(str: String): List<String> {
    return str.replace("(", " ( ").replace(")", " ) ").split(' ').filter { it.isNotBlank() }
}

fun readFromTokens(tokens: List<String>): Exp = readFromTokens(ArrayDeque(tokens))

private fun readFromTokens(tokens: ArrayDeque<String>): Exp {
    if (tokens.isEmpty()) syntaxError("unexpected EOF")

    return when (val token = tokens.removeFirst()) {
        "(" -> {
            val mutL = mutableListOf<Exp>()
            while(tokens.first() != ")") {
                mutL += readFromTokens(tokens)
            }
            tokens.removeFirst() // pop )
            L(mutL)
        }
        ")" -> syntaxError("unexpected )")
        else -> atom(token)
    }
}

private fun syntaxError(msg: String): Nothing = throw SyntaxError(msg)

private fun atom(token: String) =
    token.toIntOrNull()?.let { Num(it) } ?: token.toFloatOrNull()?.let { Num(it) } ?: Symbol(token)
