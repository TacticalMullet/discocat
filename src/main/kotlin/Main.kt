import java.io.BufferedReader
import java.io.InputStreamReader


fun main() {
    while (true) {
        try {
            DiscordLogcat().discoCat()
        } catch (t: Throwable) {
            println("ERROR: $t")
        }
    }
}

// local configuration
const val LOGCAT_CMD = "-s 127.0.0.1:5557 logcat"
const val CLEAR_CMD = "logcat -c"

const val PREFIX = "DISCORD_69_69_420" // prefix the messages you want to grep for with this

val HEADERS = mapOf("Accept" to "application/json", "Content-Type" to "application/json")

class DiscordLogcat(var adbPath: String = "", var webhook: String = "") {
    init {
        adbPath = this::class.java.getResource("adb.properties").readText() // location of adb.exe
        webhook = this::class.java.getResource("discord.webhook").readText() // discord webhook url
    }

    fun discoCat() {
        Runtime.getRuntime().exec("$adbPath $CLEAR_CMD") // remove old msgs

        val proc = Runtime
            .getRuntime()
            .exec("$adbPath $LOGCAT_CMD")

        val inputStreamReader = BufferedReader(InputStreamReader(proc.inputStream))

        while (true) {
            inputStreamReader
                .readLine()
                .takeIf { it.contains(PREFIX) }
                ?.sanitize()
                ?.also { msg ->  println(msg) }
                ?.post()
                ?.also { response -> println(response) }
        }
    }

    fun String.post() = khttp.post(webhook, data = mapOf("content" to this), headers = HEADERS)
}

private fun String.sanitize() =
    String(this.toByteArray(), Charsets.US_ASCII)
        .replace("����", " ") // this replaces some weird unicode space
        .replaceBefore(PREFIX, "")
        .replace(PREFIX, "")
