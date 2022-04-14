package dev.cyberdeck.lisp

import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

typealias Loader = (String) -> Exp

class Environment(
    val loader: Loader = { str -> outer?.loader?.invoke(str) ?: evalErr("no loader") },
    private val dict: MutableMap<Symbol, Exp> = mutableMapOf(),
    private val outer: Environment? = null,
) {
    operator fun get(symbol: Symbol): Exp? = dict[symbol] ?: outer?.get(symbol)

    operator fun set(sym: Symbol, value: Exp) {
        dict[sym] = value
    }

    fun overwrite(sym: Symbol, value: Exp) {
        if (dict.containsKey(sym)) {
            dict[sym] = value
        } else if (outer != null) {
            outer.overwrite(sym, value)
        } else {
            evalErr("couldn't find ${sym.pp()}")
        }
    }

    // convenience operators for testing
    internal operator fun get(str: String) = get(Symbol(str))
    internal operator fun set(str: String, value: Exp) = set(Symbol(str), value)

    fun newInner(vararg bindings: Pair<Symbol, Exp>) =
        Environment(outer = this, dict = bindings.associate { it }.toMutableMap())

    fun withBindings(vararg bindings: Pair<String, Exp>) = apply {
        bindings.forEach { (sym, exp) -> set(sym, exp) }
    }

    fun bindArgv(arg: List<String>) = apply { set("argv", L(arg.map(::LString))) }

    fun pp() = "env={"+ dict.map { "${it.key.sym}:${it.value.pp(trunc = true)}" }.joinToString() + "}"

}

fun env(vararg bindings: Pair<String, Exp>) = standardEnv().withBindings(*bindings)

fun standardEnv(root: File = Paths.get(".").toFile()) = Environment(loader = filesystemLoader(root)).withBindings(
    // used mainly for side effect of evaluating expressions
    "begin" to Proc { it.list.last() },

    // lists
    "nil" to Nil,
    "head" to procArity1<L>("head") { it.list.first() }, // car is a silly name
    "tail" to procArity1<L>("tail") { L(it.list.drop(1)) }, // car is a silly name
    "cons" to procArity2<Exp, L>("cons") { l, r -> L(listOf(l) + r.list) },
    "list" to Proc { it },
    "sorted" to procArity1<L>("sorted") { l -> L(l.list.sortedWith(naturalComparator)) },

    // strings
    "atoi" to procArity1<LString>("atoi") { Num(it.str.toLong()) },
    "splitp" to procArity2<LString, LString>("splitp") { s, delim -> L(s.str.split(Regex(delim.str)).map(::LString)) },
    "chars" to procArity1<LString>("chars") { s -> L(s.str.map { LString("$it") }) },
    "charAt" to procArity2<LString, Num>("charAt") { s, idx -> LString(""+s.str[idx.num.toInt()]) },
    "length" to procArity1<LString>("length") { s -> Num(s.str.length) },
    "substr" to procArity3<LString, Num, Num>("substr") { s, i, j -> LString(s.str.substring(i.num.toInt(), j.num.toInt())) },
    "matches" to procArity2<LString, LString>("matches") { s, r -> Bool(r.str.toRegex().matches(s.str)) },

    // boolean logic
    "eq" to procArity2<Exp, Exp>("eq") { l, r -> Bool(l == r) },
    "and" to procArity2<Bool, Bool>("and") { l, r -> Bool(l.bool && r.bool) },
    "or" to procArity2<Bool, Bool>("or") { l, r -> Bool(l.bool || r.bool) },
    "not" to procArity1<Bool>("or") { Bool(!it.bool) },

    // mathematical operators
    ">=" to procNumericArity2("gte", { l, r -> Bool(l >= r) }, { l, r -> Bool(l >= r) }),
    ">" to procNumericArity2("gte", { l, r -> Bool(l > r) }, { l, r -> Bool(l > r) }),
    "<=" to procNumericArity2("gte", { l, r -> Bool(l <= r) }, { l, r -> Bool(l <= r) }),
    "<" to procNumericArity2("gte", { l, r -> Bool(l < r) }, { l, r -> Bool(l < r) }),
    "*" to procNumericArity2("*", { l, r -> Num(l.times(r)) }, { l, r -> Num(l.times(r)) }),
    "/" to procNumericArity2("/", { l, r -> Num(l.div(r)) }, { l, r -> Num(l.div(r)) }),
    "+" to procNumericArity2("+", { l, r -> Num(l.plus(r)) }, { l, r -> Num(l.plus(r)) }),
    "-" to procNumericArity2("-", { l, r -> Num(l.minus(r)) }, { l, r -> Num(l.minus(r)) }),
    "%" to procNumericArity2("-", { l, r -> Num(l.mod(r)) }, { l, r -> Num(l.mod(r)) }),

    // constants
    "pi" to Num(Math.PI.toFloat()),

    // I/O
    "println" to Proc { args -> println(args.pp(parens = false)); Nil },
    "print" to Proc { args -> print(args.pp(parens = false)); Nil },
    "readfile" to procArity1<LString>("readfile") { L(File(it.str).readLines().map(::LString)) }
)

fun filesystemLoader(requireRoot: File): Loader = { file: String ->
    try {
        val sanitized = File(requireRoot, file).readLines(charset("UTF-8")).joinToString(separator = " ")
        readFromTokens(tokenize(sanitized))
    } catch (e: FileNotFoundException) {
        evalErr("failed to load $file (root: $requireRoot)")
    }
}

private inline fun <Res : Exp> procNumericArity2(
    name: String,
    crossinline longImpl: (Long, Long) -> Res,
    crossinline floatImpl: (Float, Float) -> Res
) = procArity2<Num, Num>(name) { lhs, rhs ->
    when {
        lhs.num is Long && rhs.num is Long -> longImpl(lhs.num, rhs.num)
        lhs.num is Float && rhs.num is Float -> floatImpl(lhs.num, rhs.num)
        else -> evalErr("incompatible operands for $name: ${lhs.pp()}, ${rhs.pp()}")
    }
}

private inline fun <reified Exp1 : Exp> procArity1(
    name: String,
    crossinline impl: (Exp1) -> Exp
) = Proc {
    if (it.size != 1) {
        evalErr("$name expects 1 argument, got ${it.pp()}")
    }

    if (it[0] !is Exp1) {
        evalErr("$name expects first argument of type ${Exp1::class.java.simpleName} but was ${it[0].pp()}")
    }

    impl(it[0] as Exp1)
}

private inline fun <reified Exp1 : Exp, reified Exp2 : Exp> procArity2(
    name: String,
    crossinline impl: (Exp1, Exp2) -> Exp
) = Proc {
    if (it.size != 2) {
        evalErr("$name expects 2 arguments, got ${it.pp()}")
    }

    if (it[0] !is Exp1) {
        evalErr("$name expects first argument of type ${Exp1::class.java.simpleName} but was ${it[0].pp()}")
    }

    if (it[1] !is Exp2) {
        evalErr("$name expects second argument of type ${Exp2::class.java.simpleName} but was ${it[1].pp()}")
    }

    impl(it[0] as Exp1, it[1] as Exp2)
}


private inline fun <reified Exp1 : Exp, reified Exp2 : Exp, reified Exp3 : Exp> procArity3(
    name: String,
    crossinline impl: (Exp1, Exp2, Exp3) -> Exp
) = Proc {
    if (it.size != 3) {
        evalErr("$name expects 2 arguments, got ${it.pp()}")
    }

    if (it[0] !is Exp1) {
        evalErr("$name expects first argument of type ${Exp1::class.java.simpleName} but was ${it[0].pp()}")
    }

    if (it[1] !is Exp2) {
        evalErr("$name expects second argument of type ${Exp2::class.java.simpleName} but was ${it[1].pp()}")
    }

    if (it[2] !is Exp3) {
        evalErr("$name expects third argument of type ${Exp3::class.java.simpleName} but was ${it[2].pp()}")
    }

    impl(it[0] as Exp1, it[1] as Exp2, it[2] as Exp3)
}