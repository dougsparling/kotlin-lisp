package dev.cyberdeck.lisp

class Environment(val dict: MutableMap<Symbol, Exp>)

fun env(vararg bindings: Pair<String, Exp>) = Environment(bindings.associate { Symbol(it.first) to it.second }.toMutableMap() )

fun standardEnv() = env(
    "begin" to Proc { it.last() }, // used mainly for side effect of evaluating expressions
    "pi" to Num(Math.PI.toFloat()),
    "*" to Proc {
        when (it.size) {
            2 -> {
                val lhs = it.first()
                val rhs = it.last()

                when {
                    lhs is Num && lhs.num is Int && rhs is Num && rhs.num is Int -> Num(lhs.num * rhs.num)
                    lhs is Num && lhs.num is Float && rhs is Num && rhs.num is Float -> Num(lhs.num * rhs.num)
                    else -> evalErr("unexpected operands for *: $lhs, $rhs")
                }
            }
            else -> evalErr("* expects two arguments, got $it")
        }
    }
)