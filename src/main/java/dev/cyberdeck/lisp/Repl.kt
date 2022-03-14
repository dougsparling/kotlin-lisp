package dev.cyberdeck.lisp

fun main(vararg arg: String) {
    val reader = System.`in`.bufferedReader()
    val env = standardEnv()
    while (true) {
        print("> ")
        val line = reader.readLine()
        if(line.equals("quit")) return
        val parsed = parse(line)
        try {
            val exp = readFromTokens(parsed)
            val result = eval(exp, env)
            println(result.pp())
        } catch (e: SyntaxErr) {
            println("syntax error: ${e.message}")
            if(env["debug"]?.let { it is Bool && it.bool } == true) {
                println("tokens: $parsed")
            }
        } catch (e: RuntimeErr) {
            println("error: ${e.message}")
        }
    }
}