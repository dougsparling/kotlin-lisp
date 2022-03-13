package dev.cyberdeck.lisp

fun main(vararg arg: String) {
    val reader = System.`in`.bufferedReader()
    val env = standardEnv()
    while (true) {
        print("> ")
        val line = reader.readLine()
        if(line.equals("quit")) return
        try {
            val parsed = parse(line)
            val exp = readFromTokens(parsed)
            val result = eval(exp, env)
            println(result.pp())
        } catch (e: SyntaxErr) {
            println("syntax error: ${e.message}")
        } catch (e: RuntimeErr) {
            println("error: ${e.message}")
        }
    }
}