fun exec(vararg args: String): String =
    ProcessBuilder(*args).start()
        .inputStream
        .reader()
        .readText()
        .trim()
