package dev.cyberdeck.lisp

import java.io.File
import kotlin.system.exitProcess

fun main(vararg arg: String) {
    if (arg.isEmpty()) {
        println("usage: <script> <script args>")
        exitProcess(1)
    }

    val file = File(arg[0])
    val env = standardEnv(file.parentFile).bindArgv(arg.drop(1)).newInner()
    try {
        runWithEnv(env, env.loader(file.name))
    } catch (e: SyntaxErr) {
        println("syntax error: ${e.message}")
        exitProcess(1)
    }
}

internal fun runWithEnv(env: Environment, prog: Exp) {
    try {
        val result = eval(prog, env)
        if (result != Nil) {
            println(result.pp())
        }
    } catch (e: RuntimeErr) {
        println("error: ${e.message}")
    }
}