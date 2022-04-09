package dev.cyberdeck.lisp

fun main(vararg arg: String) {
    val reader = System.`in`.bufferedReader()
    val env = standardEnv()
    env["argv"] = L(arg.map(::LString))
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null || line.equals("quit") || line.equals("exit")) return
        runWithEnv(env, line)
    }
}