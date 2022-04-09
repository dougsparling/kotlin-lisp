package dev.cyberdeck.lisp

import java.io.File
import kotlin.system.exitProcess

fun main(vararg arg: String) {
    if (arg.isEmpty()) {
        println("usage: <script> <script args>")
        exitProcess(1)
    }

    val prog = File(arg[0]).readLines(charset("UTF-8")).joinToString(separator = " ")
    val env = standardEnv().bindArgs(arg.drop(1)).newInner()
    runWithEnv(env, prog)
}

internal fun runWithEnv(env: Environment, prog: String) {
    val parsed = parse(prog)
    try {
        val exp = readFromTokens(parsed)
        val result = eval(exp, env)
        if (result != Nil) {
            println(result.pp())
        }
    } catch (e: SyntaxErr) {
        println("syntax error: ${e.message}")
        if (env["debug"]?.let { it is Bool && it.bool } == true) {
            println("tokens: $parsed")
        }
    } catch (e: RuntimeErr) {
        println("error: ${e.message}")
    }
}