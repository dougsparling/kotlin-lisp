package dev.cyberdeck.lisp

import java.io.File

class Environment(
    private val dict: MutableMap<Symbol, Exp> = mutableMapOf(),
    private val outer: Environment? = null
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
}

fun env(vararg bindings: Pair<String, Exp>) =
    Environment(bindings.associate { Symbol(it.first) to it.second }.toMutableMap())

fun standardEnv() = env(
    "begin" to Proc { it.list.last() }, // used mainly for side effect of evaluating expressions

    "pi" to Num(Math.PI.toFloat()),

    // lists
    "nil" to Nil,
    "head" to procArity1<L>("head") { it.list.first() }, // car is a silly name
    "tail" to procArity1<L>("tail") { L(it.list.drop(1)) }, // car is a silly name

    ">=" to procNumericArity2("gte", { l, r -> Bool(l >= r) }, { l, r -> Bool(l >= r) }),
    ">" to procNumericArity2("gte", { l, r -> Bool(l > r) }, { l, r -> Bool(l > r) }),
    "<=" to procNumericArity2("gte", { l, r -> Bool(l <= r) }, { l, r -> Bool(l <= r) }),
    "<" to procNumericArity2("gte", { l, r -> Bool(l < r) }, { l, r -> Bool(l < r) }),

    "*" to procNumericArity2("*", { l, r -> Num(l.times(r)) }, { l, r -> Num(l.times(r)) }),
    "/" to procNumericArity2("/", { l, r -> Num(l.div(r)) }, { l, r -> Num(l.div(r)) }),
    "+" to procNumericArity2("+", { l, r -> Num(l.plus(r)) }, { l, r -> Num(l.plus(r)) }),
    "-" to procNumericArity2("-", { l, r -> Num(l.minus(r)) }, { l, r -> Num(l.minus(r)) }),

    "println" to Proc { args -> println(args.pp(parens = false)); Nil },
    "print" to Proc { args -> print(args.pp(parens = false)); Nil },

    "readfile" to procArity1<LString>("readfile") { filename ->
        L(File(filename.str).readLines().map(::LString))
    }
)

private inline fun <Res : Exp> procNumericArity2(
    name: String,
    crossinline intImpl: (Int, Int) -> Res,
    crossinline floatImpl: (Float, Float) -> Res
) = procArity2<Num, Num>(name) { lhs, rhs ->
    when {
        lhs.num is Int && rhs.num is Int -> intImpl(lhs.num, rhs.num)
        lhs.num is Float && rhs.num is Float -> floatImpl(lhs.num, rhs.num)
        else -> evalErr("incompatible operands for *: ${lhs.pp()}, ${rhs.pp()}")
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

private inline fun <reified Exp1 : Exp, reified Rxp2 : Exp> procArity2(
    name: String,
    crossinline impl: (Exp1, Rxp2) -> Exp
) = Proc {
    if (it.size != 2) {
        evalErr("$name expects 2 arguments, got ${it.pp()}")
    }

    if (it[0] !is Exp1) {
        evalErr("$name expects first argument of type ${Exp1::class.java.simpleName} but was ${it[0].pp()}")
    }

    if (it[1] !is Rxp2) {
        evalErr("$name expects second argument of type ${Rxp2::class.java.simpleName} but was ${it[1].pp()}")
    }

    impl(it[0] as Exp1, it[1] as Rxp2)
}