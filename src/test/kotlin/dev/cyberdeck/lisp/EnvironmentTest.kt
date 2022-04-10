package dev.cyberdeck.lisp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.exhaustive

class EnvironmentTest : ShouldSpec({
    val stdEnv = standardEnv()

    context("nested environment") {
        should("find outer symbols") {
            val inner = env("a" to Num(1)).newInner()
            inner["a"].shouldNotBeNull()
        }

        should("allow shadowing of outer symbols") {
            val outer = env("a" to Num(1))
            val inner = outer.newInner(Symbol("a") to Num(2))

            inner["a"].shouldBe(Num(2))
            outer["a"].shouldBe(Num(1))
        }
    }

    context("std environment") {
        should("multiply longs") {
            checkAll<Long, Long> { l, r ->
                eval(listExp(Symbol("*"), Num(l), Num(r)), stdEnv).shouldBe(Num(l * r))
            }
        }

        should("divide longs") {
            checkAll<Long, Long> { l, r ->
                if (r != 0L) {
                    eval(listExp(Symbol("/"), Num(l), Num(r)), stdEnv).shouldBe(Num(l / r))
                }
            }
        }

        should("add longs") {
            checkAll<Long, Long> { l, r ->
                eval(listExp(Symbol("+"), Num(l), Num(r)), stdEnv).shouldBe(Num(l + r))
            }
        }

        should("subtract longs") {
            checkAll<Long, Long> { l, r ->
                eval(listExp(Symbol("-"), Num(l), Num(r)), stdEnv).shouldBe(Num(l - r))
            }
        }

        should("multiply floats") {
            checkAll<Float, Float> { l, r ->
                eval(listExp(Symbol("*"), Num(l), Num(r)), stdEnv).shouldBe(Num(l * r))
            }
        }

        should("divide floats") {
            checkAll<Float, Float> { l, r ->
                if (r != 0.0f) {
                    eval(listExp(Symbol("/"), Num(l), Num(r)), stdEnv).shouldBe(Num(l / r))
                }
            }
        }

        should("add floats") {
            checkAll<Float, Float> { l, r ->
                eval(listExp(Symbol("+"), Num(l), Num(r)), stdEnv).shouldBe(Num(l + r))
            }
        }

        should("subtract floats") {
            checkAll<Float, Float> { l, r ->
                eval(listExp(Symbol("-"), Num(l), Num(r)), stdEnv).shouldBe(Num(l - r))
            }
        }

        should("fail with mixed numeric types") {
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
    }

    context("require") {
        should("define into current scope") {
            val reqTest = listExp(Symbol("require"), LString("test"))
            val env = Environment(loader = {
                listExp(Symbol("define"), Symbol("a"), Num(1))
            })
            eval(reqTest, env = env)
            env["a"].shouldBe(Num(1))
        }

        should("propagate syntax errors") {
            shouldThrow<RuntimeErr> {
                val reqTest = listExp(Symbol("require"), LString("test"))
                eval(reqTest, env = Environment(loader = {throw SyntaxErr("oops")}))
            }
        }
    }
})