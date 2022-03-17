import khttp.extensions.fileLike
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


fun main() {
    DiscordLogcat().discoCat()
}

// local configuration
const val LOGCAT_CMD = "-s 192.168.1.24:5555 logcat"
const val CLEAR_CMD = "-s 192.168.1.24:555 logcat -c"
//const val LOGCAT_CMD = "logcat"
//const val CLEAR_CMD = "logcat -c"

const val PREFIX = "DISCORD_69_69_420" // prefix the messages you want to grep for with this
const val PROG_PREFIX = "DISCORD_69_69_PROG" // prefix the messages you want to grep for with this
const val SPAM_PREFIX = "DISCORD_69_69_SPAM" // prefix the messages you want to grep for with this

val JSON_HEADERS = mapOf("Accept" to "application/json", "Content-Type" to "application/json")
val FILE_HEADERS = mapOf("Content-Type" to "multipart/form-data")

class DiscordLogcat(
    var adbPath: String = "",
    var webhook: String = "",
    var webhookProg: String = "",
    var webhookSpam: String = "",
    var imgGen: String = "",
) {
    init {
        adbPath = this::class.java.getResource("adb.properties").readText() // location of adb.exe
        webhook = this::class.java.getResource("discord.webhook").readText() // discord webhook url
        webhookProg = this::class.java.getResource("discord.webhook.prog").readText() // discord webhook url
        webhookSpam = this::class.java.getResource("discord.webhook.spam").readText() // discord webhook url
        imgGen = this::class.java.getResource("img.gen").readText() // dank image generator
    }

    fun getImage(payload: JSONObject): File? = try {
        val img = khttp.post(imgGen, json = payload, headers = JSON_HEADERS)
        val base64Img = img.jsonObject.getString("body")
            .replace("data:image/png;base64,", "")
            .replace("\"", "")

        File("temp.png").apply { writeBytes(Base64.getDecoder().decode(base64Img)) }
    } catch (t: Throwable) {
        println("Failed to get image ${t.stackTraceToString()}")
        null
    }

    fun discoCat() {
        Runtime.getRuntime().exec("$adbPath $CLEAR_CMD") // remove old msgs

        val proc = Runtime
            .getRuntime()
            .exec("$adbPath $LOGCAT_CMD")

        val inputStreamReader = BufferedReader(InputStreamReader(proc.inputStream))

        while (true) {
            try {
                inputStreamReader
                    .readLine()
                    .also {
                        when {
                            it.contains(PREFIX) -> {
                                it.sanitize()
                                    .also { println("$PREFIX $it") }
                                    .postToWebhook()
                            }
                            it.contains(PROG_PREFIX) -> {
                                it.sanitize()
                                    .also { println("$PREFIX $it") }
                                    .let { getImage(JSONObject(it)) }
                                    ?.postToWebhook(webhookProg)
                            }
                            it.contains(SPAM_PREFIX) -> {
                                it.sanitize()
                                    .also { println("$PREFIX $it") }
                                    .postToWebhook(webhookSpam)
                            }
                            else -> {
                            }
                        }
                    }
            } catch (t: Throwable) {
                println("ERROR: $t, ${t.stackTraceToString()}")
            }
        }
    }

    fun String.postToWebhook(wh: String = webhook) = try {
        khttp.post(wh, data = mapOf("content" to this), headers = JSON_HEADERS)
    } catch (t: Throwable) {
        println("webhook post failed ${t.stackTraceToString()}")
    }

    fun File.postToWebhook(wh: String = webhook) = try {
        khttp.post(wh, files = listOf(this.fileLike()), headers = FILE_HEADERS)
    } catch (t: Throwable) {
        println("webhook post failed ${t.stackTraceToString()}")
    }
}

private fun String.sanitize() =
    String(this.toByteArray(), Charsets.US_ASCII)
        .replace("����", " ") // this replaces some weird unicode space
        .replaceBefore(PREFIX, "")
        .replace(PREFIX, "")
        .replaceBefore(PROG_PREFIX, "")
        .replace(PROG_PREFIX, "")
        .replaceBefore(SPAM_PREFIX, "")
        .replace(SPAM_PREFIX, "")




