package dev.cyberdeck.lisp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.shouldBeBetween
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.types.shouldBeInstanceOf

fun run(prog: String) = Result.runCatching {
    val tokens = parse(prog)
    val ast = readFromTokens(tokens)
    val env = standardEnv()
    eval(ast, env)
}

class IntegrationTest : StringSpec({
    "eval should work on complex expressions" {
        run("(begin (define r 10.0) (* pi (* r r)))").shouldBeSuccess {
            it.shouldBeInstanceOf<Num>().num.shouldBeInstanceOf<Float>().shouldBeBetween(314.1592f, 314.1593f, 0.0f)
        }
    }
})