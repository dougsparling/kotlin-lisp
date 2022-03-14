package dev.cyberdeck.lisp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.merge

class EvaluatorTest: StringSpec({
    val symbols = listOf("abc", "x", "x1", " a", " b").exhaustive()
    val truthy = listOf(Bool(true)).exhaustive()
    val falsey = listOf(Bool(false)).exhaustive()

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
            env[Symbol(symbol)].shouldBe(Num(1))
        }
    }

    "eval re-definition fails" {
        checkAll(symbols) { symbol ->
            val env = env(symbol to Num(1))

            val recorder = Recorder()

            shouldThrow<RuntimeErr> {
                eval(listExp(Symbol("define"), Symbol(symbol), recorder.exp), env)
            }

            // definition unchanged
            env[Symbol(symbol)].shouldBe(Num(1))

            // expression wasn't evaluated (TODO is this desireable?)
            recorder.wasCalled().shouldBeFalse()
        }
    }

    "eval set! sets previously defined value" {
        checkAll(symbols) { symbol ->
            val env = env(symbol to Num(1))
            eval(listExp(Symbol("set!"), Symbol(symbol), Num(2)), env)
            env[Symbol(symbol)].shouldBe(Num(2))
        }
    }

    "eval set! sets value in correct scope" {
        checkAll(symbols) { symbol ->
            val top = env(symbol to Num(1))
            val bottom = top.newInner().newInner()
            eval(listExp(Symbol("set!"), Symbol(symbol), Num(2)), top)
            top[symbol].shouldBe(Num(2))
            bottom[symbol].shouldBe(Num(2))
        }
    }

    "eval lambda returns invocable proc" {
        checkAll(symbols) { symbol ->
            val res = eval(listExp(Symbol("lambda"), listExp(Symbol(symbol)), Num(123)))
            val lambdaRes = res.shouldBeInstanceOf<Proc>().proc(listExp(Num(42)))
            lambdaRes.shouldBe(Num(123))
        }
    }

    "eval lambda parameter names shadow env" {
        checkAll(symbols) { symbol ->
            val outer = env(symbol to Num(1))

            // identity function whose parameter names shadows definition in outer scope
            val res = eval(listExp(Symbol("lambda"), listExp(Symbol(symbol)), Symbol(symbol)), outer.newInner())
            val lambdaRes = res.shouldBeInstanceOf<Proc>().proc(listExp(Num(2)))
            lambdaRes.shouldBe(Num(2))
        }
    }

    "eval quote returns literal exp" {
        checkAll(symbols) { symbol ->
            val env = env()
            val res = eval(listExp(Symbol("quote"), listExp(Symbol(symbol), Num(1), Num(2))), env)
            res.shouldBe(listExp(Symbol(symbol), Num(1), Num(2)))
        }
    }

    "eval if fails unless boolean" {
        shouldThrow<RuntimeErr> {
            eval(listExp(Symbol("if"), Num(1), Num(2), Num(3)))
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