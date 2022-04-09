package dev.cyberdeck.lisp

fun main(vararg args: String) {
    val reader = System.`in`.bufferedReader()
    val env = standardEnv().bindArgv(args.toList())
    while (true) {
        print("> ")
        val line = reader.readLine()
        if (line == null || line.equals("quit") || line.equals("exit")) return
        try {
            runWithEnv(env, readFromTokens(parse(line)))
        } catch (e: SyntaxErr) {
            println("syntax error: ${e.message}")
        }
    }
}