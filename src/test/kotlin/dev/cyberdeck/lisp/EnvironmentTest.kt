package dev.cyberdeck.lisp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive

class EnvironmentTest: StringSpec({
    val stdEnv = standardEnv()

    "std should multiply ints" {
        checkAll<Int, Int> { l, r ->
            eval(listExp(Symbol("*"), Num(l), Num(r)), stdEnv).shouldBe(Num(l * r))
        }
    }

    "std should divide ints" {
        checkAll<Int, Int> { l, r ->
            if(r != 0) {
                eval(listExp(Symbol("/"), Num(l), Num(r)), stdEnv).shouldBe(Num(l / r))
            }
        }
    }

    "std should add ints" {
        checkAll<Int, Int> { l, r ->
            eval(listExp(Symbol("+"), Num(l), Num(r)), stdEnv).shouldBe(Num(l + r))
        }
    }

    "std should subtract ints" {
        checkAll<Int, Int> { l, r ->
            eval(listExp(Symbol("-"), Num(l), Num(r)), stdEnv).shouldBe(Num(l - r))
        }
    }

    "std should multiply floats" {
        checkAll<Float, Float> { l, r ->
            eval(listExp(Symbol("*"), Num(l), Num(r)), stdEnv).shouldBe(Num(l * r))
        }
    }

    "std should divide floats" {
        checkAll<Float, Float> { l, r ->
            if(r != 0.0f) {
                eval(listExp(Symbol("/"), Num(l), Num(r)), stdEnv).shouldBe(Num(l / r))
            }
        }
    }

    "std should add floats" {
        checkAll<Float, Float> { l, r ->
            eval(listExp(Symbol("+"), Num(l), Num(r)), stdEnv).shouldBe(Num(l + r))
        }
    }

    "std should subtract floats" {
        checkAll<Float, Float> { l, r ->
            eval(listExp(Symbol("-"), Num(l), Num(r)), stdEnv).shouldBe(Num(l - r))
        }
    }

    "std should fail with mixed numeric types" {
        val operators = listOf("-", "*", "/", "+").map { Symbol(it) }.exhaustive()

        checkAll<Float, Int> { l, r ->
            checkAll(operators) { op ->
                shouldThrow<RuntimeErr> {
                    eval(listExp(op, Num(l), Num(r)), stdEnv)
                }
            }
        }

        checkAll<Int, Float> { l, r ->
            checkAll(operators) { op ->
                shouldThrow<RuntimeErr> {
                    eval(listExp(op, Num(l), Num(r)), stdEnv)
                }
            }
        }
    }
})