package dev.cyberdeck.lisp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.floats.shouldBeBetween
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

fun run(prog: String) = Result.runCatching {
    val tokens = tokenize(prog)
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

    "head returns the first element" {
        run("(begin (head (quote (hello))))").shouldBeSuccess {
            it.shouldBe(Symbol("hello"))
        }
    }

    "tail returns the rest of the list" {
        run("(begin (tail (quote (hello from the evaluator))))").shouldBeSuccess {
            it.shouldBe(L(listOf(Symbol("from"), Symbol("the"), Symbol("evaluator"))))
        }
    }

    "cons creates a new list" {
        run("(begin (cons 1 (quote (2 3))))").shouldBeSuccess {
            it.shouldBe(L(listOf(Num(1), Num(2), Num(3))))
        }
    }
})