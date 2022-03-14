package dev.cyberdeck.lisp

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.inspectors.forExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Exhaustive
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.cartesian
import io.kotest.property.exhaustive.exhaustive
import io.kotest.property.exhaustive.merge

class ParserTest: StringSpec({
    val symbols = listOf("abc", "x", "x1", " a", " b").exhaustive()
    val atoms = symbols.merge(listOf("-1", "1", "1.0", "-1.0").exhaustive())
    val lists = Exhaustive.cartesian(atoms, atoms) { a, b -> listOf(a, b) }

    "parse token" {
        checkAll(symbols) { s ->
            val result = parse(s)
            result.forAll {
                it.shouldNotContain(" ")
                it.shouldNotBeEmpty()
            }

            result.forExactly(1) { it.shouldBe(s.trim()) }
        }
    }

    "parse with parens" {
        checkAll(symbols) { s ->
            val result = parse("($s)")
            result.forAll {
                it.shouldNotContain(" ")
                it.shouldNotBeEmpty()
            }

            result.forExactly(1) { it.shouldBe(s.trim()) }
        }
    }

    "parse with unbalanced parens" {
        checkAll(symbols) { s ->
            shouldThrow<SyntaxErr> {
                readFromTokens(parse("($s $s ($s ($s $s))")) // no closing paren
            }
        }
    }

    "readFromTokens fails if empty" {
        shouldThrow<SyntaxErr> {
            readFromTokens(emptyList())
        }
    }

    "readFromTokens returns int" {
        checkAll<Int> { i ->
            readFromTokens(listOf(i.toString())).shouldBe(Num(i))
        }
    }

    "readFromTokens returns float" {
        checkAll<Float> { f ->
            readFromTokens(listOf(f.toString())).shouldBe(Num(f))
        }
    }

    "readFromTokens returns boolean" {
        checkAll<Boolean> { b ->
            readFromTokens(listOf(b.toString())).shouldBe(Bool(b))
        }
    }

    "readFromTokens returns symbol" {
        checkAll(symbols) { s ->
            readFromTokens(listOf(s)).shouldBe(Symbol(s))
        }
    }

    "readFromTokens fails on unmatched closing paren" {
        shouldThrow<SyntaxErr> {
            readFromTokens(listOf(")"))
        }
    }

    "readFromTokens returns list of atoms" {
        checkAll(lists) { list ->
            readFromTokens(listOf("(") + list + ")")
                .shouldBeInstanceOf<L>()
                .list.shouldHaveSize(list.size)
                .forAll { it.shouldBeInstanceOf<Atom>() }
        }
    }
})