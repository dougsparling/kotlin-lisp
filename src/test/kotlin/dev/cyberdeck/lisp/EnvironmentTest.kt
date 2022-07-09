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

        should("convert base 10 strings to longs") {
            val res = eval(listExp(Symbol("atoi"), LString("42")))
            res.shouldBe(Num(42))
        }

        should("convert base 16 strings to longs") {
            val res = eval(listExp(Symbol("atoi"), LString("FF"), Num(16)))
            res.shouldBe(Num(255))
        }

        context("sorted") {
            should("use natural long order") {
                val res = eval(listExp(Symbol("sorted"), listExp(Symbol("quote"), listExp(Num(3), Num(2), Num(1)))))
                res.shouldBe(listExp(Num(1), Num(2), Num(3)))
            }

            should("use natural float order") {
                val res =
                    eval(listExp(Symbol("sorted"), listExp(Symbol("quote"), listExp(Num(3.0f), Num(2.0f), Num(1.0f)))))
                res.shouldBe(listExp(Num(1.0f), Num(2.0f), Num(3.0f)))
            }

            should("use natural string order") {
                val res = eval(
                    listExp(
                        Symbol("sorted"),
                        listExp(Symbol("quote"), listExp(LString("c"), LString("b"), LString("a")))
                    )
                )
                res.shouldBe(listExp(LString("a"), LString("b"), LString("c")))
            }

            should("compare lists by element") {
                val res = eval(
                    listExp(
                        Symbol("sorted"),
                        listExp(
                            Symbol("quote"),
                            listExp(
                                listExp(Num(2), LString("b")),
                                listExp(Num(2), LString("a")),
                                listExp(Num(1), LString("d")),
                                listExp(Num(1), LString("c"))
                            )
                        )
                    )
                )

                res.shouldBe(
                    listExp(
                        listExp(Num(1), LString("c")),
                        listExp(Num(1), LString("d")),
                        listExp(Num(2), LString("a")),
                        listExp(Num(2), LString("b"))
                    )
                )
            }

            should("fail to compare mixed types") {
                shouldThrow<RuntimeErr> {
                    eval(
                        listExp(
                            Symbol("sorted"),
                            listExp(
                                Symbol("quote"),
                                listExp(
                                    listExp(Num(2), Num(1)),
                                    listExp(LString("a"), LString("b"))
                                )
                            )
                        )
                    )
                }
            }

            should("fail to compare mismatched lists") {
                shouldThrow<RuntimeErr> {
                    eval(
                        listExp(
                            Symbol("sorted"),
                            listExp(
                                Symbol("quote"),
                                listExp(
                                    listExp(Num(2), Num(1)),
                                    listExp(Num(1)),
                                )
                            )
                        )
                    )
                }
            }

            should("fail to compare incomparables") {
                shouldThrow<RuntimeErr> {
                    eval(
                        listExp(
                            Symbol("sorted"),
                            listExp(
                                Symbol("quote"),
                                listExp(
                                    Symbol("a"),
                                    Symbol("b")
                                )
                            )
                        )
                    )
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
                eval(reqTest, env = Environment(loader = { throw SyntaxErr("oops") }))
            }
        }
    }
})