package dev.cyberdeck.lisp

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.checkAll

class EnvironmentTest: StringSpec({
    val stdEnv = standardEnv()

    "std * should multiply ints" {
        checkAll<Int, Int> { l, r ->
            eval(listExp(Symbol("*"), Num(l), Num(r)), stdEnv).shouldBe(Num(l * r))
        }
    }
})