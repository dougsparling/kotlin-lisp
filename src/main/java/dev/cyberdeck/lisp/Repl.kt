package dev.cyberdeck.lisp

fun main(vararg args: String) {
    val reader = System.`in`.bufferedReader()
    val env = standardEnv().bindArgs(args.toList())
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null || line.equals("quit") || line.equals("exit")) return
        runWithEnv(env, line)
    }
}