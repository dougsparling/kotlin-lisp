package dev.cyberdeck.lisp

class RuntimeErr(msg: String) : RuntimeException(msg)

fun evalErr(msg: String): Nothing = throw RuntimeErr(msg)

fun eval(x: Exp, env: Environment = env()): Exp {
    return when {
        x is Literal -> x
        x == Nil -> x

        // resolve from definition
        x is Symbol -> {
            env[x] ?: evalErr("undefined: '${x.pp()}' (${env.pp()})")
        }

        // (if x (conseq) (alt))
        x is L && x.list.size == 4 && x.list[0] == Symbol("if") -> {
            val (_, test, conseq, alt) = x.list
            if (truthy(eval(test, env))) {
                eval(conseq, env)
            } else {
                eval(alt, env)
            }
        }

        // (quote (exp))
        x is L && x[0] == Symbol("quote") -> {
            if (x.size != 2) evalErr("quote can only be applied to list, but was ${x.pp()}")
            x[1]
        }

        // (require filename)
        x is L && x.size == 2 && x[0] == Symbol("require") -> {
            val (_, file) = x.list
            file as? LString ?: evalErr("require needs filename but was given ${file.pp()}")

            val exp = try {
                env.loader(file.str)
            } catch (e: SyntaxErr) {
                evalErr("syntax error in required file ${file.str}: ${e.message}")
            }
            try {
                eval(exp, env)
            } catch (e: RuntimeErr) {
                evalErr("runtime error in required file ${file.str}: ${e.message}")
            }
        }

        // (define x (exp))
        x is L && x.size == 3 && x[0] == Symbol("define") -> {
            val (_, sym, exp) = x.list
            sym as? Symbol ?: evalErr("expected define symbol but was ${sym.pp()}")
            if (env[sym] != null) evalErr("${sym.pp()} already defined")
            eval(exp, env).also { env[sym] = it }
        }

        // (lambda (a, b, c...) (exp...)
        x is L && x[0] == Symbol("lambda") && x.size == 3 -> {
            val body = x[x.size - 1]
            val paramList = x[1] as? L ?: evalErr("lambda parameter names must be in sub-list, but got ${x[1].pp()}")

            val params = paramList.list.map {
                (it as? Symbol)
                    ?: evalErr("lambda parameters must be symbols, but were: ${paramList.list.map { p -> p.pp() }}")
            }

            Proc { args ->
                if (args.size != paramList.size) evalErr("lambda of arity ${args.size} invoked with ${paramList.size} arguments")

                // this is where the magic happens: bind the parameters passed to the lambda
                // at the call site to the arguments named by the given symbols
                val binding = env.newInner(*params.zip(args.list).toTypedArray())
                eval(body, binding)
            }
        }

        // (set! x (exp))
        x is L && x.size == 3 && x[0] == Symbol("set!") -> {
            val (_, sym, exp) = x.list
            sym as? Symbol ?: evalErr("expected set! symbol but was ${sym.pp()}")
            if (env[sym] == null) evalErr("tried to set ${sym.pp()} but was not defined")
            eval(exp, env).also { env.overwrite(sym, it) }
        }

        // procedure call
        x is L && x.size > 0 -> {
            val first = eval(x[0], env)
            val proc = first as? Proc ?: evalErr("expected ${x[0].pp()} to be proc, but was ${first.pp()}")
            val args = L(x.list.drop(1).map { eval(it, env) })
            proc.proc(args)
        }

        else -> evalErr("unexpected exp: ${x.pp()}")
    }
}

fun truthy(exp: Exp) = when (exp) {
    is Bool -> exp.bool
    else -> evalErr("expected Bool, got ${exp.pp()}")
}
