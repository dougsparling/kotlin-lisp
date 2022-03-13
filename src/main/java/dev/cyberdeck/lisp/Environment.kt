package dev.cyberdeck.lisp

class Environment(
    private val dict: MutableMap<Symbol, Exp> = mutableMapOf(),
    private val outer: Environment? = null
) {
    operator fun get(symbol: Symbol): Exp? = dict[symbol] ?: outer?.get(symbol)

    operator fun set(sym: Symbol, value: Exp) {
        dict[sym] = value
    }

    fun overwrite(sym: Symbol, value: Exp) {
        if(dict.containsKey(sym)) {
            dict[sym] = value
        } else if(outer != null) {
            outer.overwrite(sym, value)
        } else {
            evalErr("couldn't find ${sym.pp()}")
        }
    }

    // convenience operators for testing
    internal operator fun get(str: String) = get(Symbol(str))
    internal operator fun set(str: String, value: Exp) = set(Symbol(str), value)

    fun newInner() = Environment(outer = this)
}

fun env(vararg bindings: Pair<String, Exp>) =
    Environment(bindings.associate { Symbol(it.first) to it.second }.toMutableMap())

fun standardEnv() = env(
    "begin" to Proc { it.list.last() }, // used mainly for side effect of evaluating expressions
    "pi" to Num(Math.PI.toFloat()),
    "*" to procNumericArity2("*", Int::times, Float::times),
    "/" to procNumericArity2("/", Int::div, Float::div),
    "+" to procNumericArity2("+", Int::plus, Float::plus),
    "-" to procNumericArity2("-", Int::minus, Float::minus)
)

private inline fun procNumericArity2(
    name: String,
    crossinline intImpl: (Int, Int) -> Int,
    crossinline floatImpl: (Float, Float) -> Float
) = procArity2<Num, Num>(name) { lhs, rhs ->
    when {
        lhs.num is Int && rhs.num is Int -> Num(intImpl(lhs.num, rhs.num))
        lhs.num is Float && rhs.num is Float -> Num(floatImpl(lhs.num, rhs.num))
        else -> evalErr("unexpected operands for *: ${lhs.pp()}, ${rhs.pp()}")
    }
}

private inline fun <reified LhsExp : Exp, reified RhsExp : Exp> procArity2(
    name: String,
    crossinline impl: (LhsExp, RhsExp) -> Exp
) = Proc {
    if (it.size != 2) {
        evalErr("$name expects 2 arguments, got ${it.pp()}")
    }

    if (it[0] !is LhsExp) {
        evalErr("$name expects first argument of type ${LhsExp::class.java.simpleName} but was ${it[0].pp()}")
    }

    if (it[1] !is RhsExp) {
        evalErr("$name expects second argument of type ${RhsExp::class.java.simpleName} but was ${it[1].pp()}")
    }

    impl(it[0] as LhsExp, it[1] as RhsExp)
}