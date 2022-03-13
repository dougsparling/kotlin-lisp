package dev.cyberdeck.lisp

class RuntimeErr(msg: String) : RuntimeException(msg)

fun evalErr(msg: String): Nothing = throw RuntimeErr(msg)


fun eval(x: Exp, env: Environment = env()): Exp {
    return when {
        // literal value
        x is Num -> x
        // TODO: proc is a hack for tests... but bare proc is normally not possible? or maybe that's what lambda will be in the future...
        x is Proc -> x
        // resolve from definition
        x is Symbol -> {
            env.dict[x] ?: evalErr("undefined: ${x.sym}")
        }
        // (if x (conseq) (alt))
        x is L && x.list.size == 4 && x.list[0] == Symbol("if")  -> {
            val (_, test, conseq, alt) = x.list
            if (truthy(eval(test, env))) {
                eval(conseq, env)
            } else {
                eval(alt, env)
            }
        }
        // (define x 0)
        x is L && x.list.size == 3 && x.list[0] == Symbol("define") && x.list[1] is Symbol -> {
            val (_, sym, exp) = x.list
            eval(exp, env).also { env.dict[sym as Symbol] = it }
        }
        // proc call
        x is L && x.list.isNotEmpty() -> {
            val proc = eval(x.list.first(), env) as? Proc ?: evalErr("expected proc")
            val args = L(x.list.drop(1).map { eval(it, env) })
            proc.proc(args)
        }
        else -> evalErr("unexpected exp: ${x.pp()}")
    }
}

fun truthy(exp: Exp) = when (exp) {
    is Num -> when (exp.num) {
        is Int -> exp.num != 0
        is Float -> exp.num != 0.0f
        else -> evalErr("not a truthy num: ${exp.num}")
    }
    else -> evalErr("not a truthy exp: $exp")
}
