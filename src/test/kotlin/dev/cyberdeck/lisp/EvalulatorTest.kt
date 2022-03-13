package dev.cyberdeck.lisp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.merge

class EvalulatorTest: StringSpec({
    val symbols = listOf("abc", "x", "x1", " a", " b").exhaustive()
    val truthy = listOf(Num(1), Num(-1), Num(-1.0f), Num(+1.0f)).exhaustive()
    val falsey = listOf(Num(0), Num(0.0f)).exhaustive()

    "eval symbol gives value" {
        eval(Symbol("x"), env("x" to Num(42)))
    }

    "eval number gives number" {
        eval(Num(1)).shouldBe(Num(1))
    }

    "eval if gives conseq when true" {
        checkAll(truthy) { chk ->
            eval(listExp(Symbol("if"), chk, Num(2), Num(3))).shouldBe(Num(2))
        }
    }

    "eval if gives alt when false" {
        checkAll(falsey) { chk ->
            eval(listExp(Symbol("if"), chk, Num(2), Num(3))).shouldBe(Num(3))
        }
    }

    "eval if only eval conseq when true" {
        val conseqRec = Recorder()
        val altRec = Recorder()
        checkAll(truthy) { chk ->
            eval(listExp(Symbol("if"), chk, conseqRec.exp, altRec.exp))

            conseqRec.wasCalled().shouldBe(true)
            altRec.wasCalled().shouldBe(false)
        }
    }

    "eval if only eval alt when false" {
        val conseqRec = Recorder()
        val altRec = Recorder()
        checkAll(falsey) { chk ->
            eval(listExp(Symbol("if"), chk, conseqRec.exp, altRec.exp))

            conseqRec.wasCalled().shouldBe(false)
            altRec.wasCalled().shouldBe(true)
        }
    }

    "eval define sets value in env" {
        checkAll(symbols) { symbol ->
            val env = env()
            eval(listExp(Symbol("define"), Symbol(symbol), Num(1)), env)
            env.dict.shouldContain(Symbol(symbol), Num(1))
        }
    }

    "eval if fails unless truthy" {
        shouldThrow<RuntimeErr> {
            eval(listExp(Symbol("if"), Symbol("wat"), Num(2), Num(3)))
        }
    }

    "eval if fails when if exp is missing clause" {
        shouldThrow<RuntimeErr> {
            eval(listExp(Symbol("if"), Symbol("wat"), Num(2)))
        }
    }

    "eval if fails when if exp has too many clauses" {
        checkAll(truthy.merge(falsey)) { chk ->
            shouldThrow<RuntimeErr> {
                eval(listExp(Symbol("if"), chk, Num(2), Num(3), Num(4)))
            }
        }
    }

    "eval missing symbol fails" {
        shouldThrow<RuntimeErr> {
            eval(Symbol("x"))
        }
    }
})

class Recorder(private val retVal: Exp = Num(0)) {
    val calls = mutableListOf<List<Exp>>()

    val exp = listExp(Proc { args ->
        calls.add(args.list)
        retVal
    })

    fun wasCalled() = calls.isNotEmpty()
}