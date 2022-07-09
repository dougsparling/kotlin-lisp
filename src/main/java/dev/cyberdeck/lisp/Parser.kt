package dev.cyberdeck.lisp

class SyntaxErr(msg: String) : RuntimeException(msg)

sealed class Exp
sealed class Atom : Exp()
sealed class Literal : Atom()
class Proc(val proc: (L) -> Exp) : Literal()
data class Symbol(val sym: String) : Atom()
data class Num internal constructor(val num: Number) : Literal() {
    constructor(long: Long): this(long as Number)
    constructor(float: Float): this(float as Number)
    constructor(int: Int): this(int.toLong() as Number)
}
data class Bool(val bool: Boolean) : Literal()
data class LString(val str: String) : Literal()
data class L(val list: List<Exp>) : Exp() {
    operator fun get(idx: Int): Exp = list[idx]
    fun getOrElse(idx: Int, default: Exp) = if(list.size > idx) list[idx] else default
    val size = list.size
}

val Nil = L(emptyList())

fun listExp(vararg stuff: Exp) = L(stuff.toList())

fun tokenize(str: String): List<String> {
    val trimmed = str.trimStart()
    return if (trimmed.isEmpty()) {
        listOf()
    } else if(trimmed.startsWith('"')) {
        val next = trimmed.indexOf('"', 1)
        if(next == -1) syntaxError("unbalanced string starting at ${trimmed.take(10)}...")
        val token = trimmed.substring(0, next + 1)
        listOf(token) + tokenize(trimmed.substring(next + 1))
    } else if (trimmed.startsWith("(") || trimmed.startsWith(")")) {
        listOf(trimmed.substring(0, 1)) + tokenize(trimmed.substring(1))
    } else {
        val endOfToken = trimmed.indexOfAny(charArrayOf(' ', ')', '('), 1)
        if (endOfToken == -1) {
            // EOF
            return listOf(trimmed)
        }
        listOf(trimmed.substring(0, endOfToken)) + tokenize(trimmed.substring(endOfToken))
    }
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
    else -> {
        if(token.startsWith('"') && token.endsWith('"')) {
            LString(token.substring(1, token.length - 1))
        } else {
            token.toLongOrNull()?.let { Num(it) } ?: token.toFloatOrNull()?.let { Num(it) } ?: Symbol(token)
        }
    }
}

val naturalComparator = object : Comparator<Exp> {
    override fun compare(o1: Exp, o2: Exp): Int = when {
        o1 is Num && o2 is Num && o1.num is Long && o2.num is Long -> o1.num.compareTo(o2.num)
        o1 is Num && o2 is Num && o1.num is Float && o2.num is Float -> o1.num.compareTo(o2.num)
        o1 is LString && o2 is LString -> o1.str.compareTo(o2.str)
        o1 is L && o2 is L && o1.size == o2.size -> o1.list.zip(o2.list).fold(0) { cmp, (l, r) ->
            if(cmp == 0) compare(l, r) else cmp
        }
        else -> evalErr("incomparable elements in list: ${o1.pp()}, ${o2.pp()}")
    }
}

fun Exp.pp(parens: Boolean = true, trunc: Boolean = false): String = when (this) {
    is L -> {
        val itemsStr = if(trunc && size > 3) {
            list.take(3).joinToString(separator = " ", transform = {it.pp(trunc = true)}) + " ..."
        } else {
            list.joinToString(separator = " ", transform = Exp::pp)
        }
        when {
            this == Nil -> "nil"
            parens -> "($itemsStr)"
            else -> itemsStr
        }
    }
    is Num -> num.toString()
    is Bool -> bool.toString()
    is Symbol -> sym
    is LString -> "\"$str\""
    is Proc -> "Proc"
}