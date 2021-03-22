package club.chachy.simpleconfig

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.lang.reflect.Field

class Config(private val file: File, private val `class`: Any, isPrettyPrinted: Boolean = true) {
    private val gson: Gson = if (isPrettyPrinted) Gson() else GsonBuilder().setPrettyPrinting().create()

    private val parser = JsonParser()

    private var obj = JsonObject()

    private val configClass = `class`::class.java

    private val fields = mutableListOf<Field>()

    fun load() {
        if (!file.exists()) {
            file.createNewFile()
            file.writeText(gson.toJson(obj))
        }
        val text = file.readText()
        try {
            if (text.isNotEmpty()) {
                obj = parser.parse(text).asJsonObject
                for (i in configClass.declaredFields.size - 1 downTo 0) {
                    val field = configClass.declaredFields[i]
                    if (field.isAnnotationPresent(ConfigOption::class.java)) {
                        if (obj.has(field.name)) {
                            field.isAccessible = true
                            field.set(`class`, gson.fromJson(obj.get(field.name), field.type))
                            fields.add(field)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("An error occurred trying to load the config")
        }
    }

    fun loadField(field: Field, checkAnnotation: Boolean = true) {
        val passed = if (checkAnnotation) field.isAnnotationPresent(ConfigOption::class.java) else true
        if (passed) {
            if (obj.has(field.name)) {
                field.isAccessible = true
                field.set(`class`, gson.fromJson(obj.get(field.name), field.type))
                fields.add(field)
            }
        }
    }

    fun addField(field: Field) = loadField(field, false)

    fun save() {
        for (i in fields.size - 1 downTo 0) {
            val field = fields[i]
            save(field)
        }
        file.writeText(gson.toJson(obj))
    }


    private fun save(field: Field) {
        try {
            field.isAccessible = true
            obj.add(field.name, gson.toJsonTree(field.get(`class`), field.type))
        } catch (e: Exception) {
            println("An error occurred when trying to save ${field.name}")
        }
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ConfigOption