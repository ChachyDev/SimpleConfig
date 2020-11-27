package club.chachy.simpleconfig

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.lang.reflect.Field

private val nonPrettyPrinted = Gson()

private val prettyPrintedGson = GsonBuilder()
    .setPrettyPrinting()
    .create()

class Config(private val file: File, private val `class`: Any, isPrettyPrinted: Boolean = true) {
    private val gson: Gson = if (isPrettyPrinted) prettyPrintedGson else nonPrettyPrinted

    private var obj = JsonObject()

    private val configClass = `class`::class.java

    fun load() {
        if (!file.exists()) error("The file given does not exist.")
        val text = file.readText()
        try {
            if (text.isNotEmpty()) {
                obj = JsonParser.parseString(text).asJsonObject
                configClass.declaredFields.filter { it.isAnnotationPresent(ConfigOption::class.java) }.forEach {
                    if (obj.has(it.name)) {
                        it.isAccessible = true
                        it.set(`class`, gson.fromJson(obj.get(it.name), it.type))
                    }
                }
            }
        } catch (e: Exception) {
            println("An error occurred trying to load the config")
        }
    }

    fun save() {
        configClass.declaredFields.filter { it.isAnnotationPresent(ConfigOption::class.java) }.forEach {
            save(it)
        }
        file.writeText(gson.toJson(obj))
    }


    private fun save(field: Field) {
        try {
            field.isAccessible = true
            obj.add(field.name, gson.toJsonTree(field.get(`class`), field.type))
        } catch (e: Exception) {
            e.printStackTrace()
            println("An error occurred when trying to save ${field.name}")
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ConfigOption