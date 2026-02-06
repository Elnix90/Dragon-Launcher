package org.elnix.dragonlauncher.common.serializables

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

object IconShapeGson {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(IconShape::class.java, IconShapeAdapter())
        .create()

    fun encode(value: IconShape): String =
        gson.toJson(value, IconShape::class.java)

    fun decode(raw: Any?, fallback: IconShape): IconShape =
        runCatching {
            val stringRaw = raw?.toString() ?: ""
            gson.fromJson(stringRaw, IconShape::class.java)
        }.getOrElse { fallback }
}



class IconShapeAdapter :
    JsonSerializer<IconShape>,
    JsonDeserializer<IconShape> {

    override fun serialize(
        src: IconShape?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        if (src == null) return JsonNull.INSTANCE

        val obj = JsonObject()

        when (src) {
            IconShape.PlatformDefault -> obj.addProperty("type", "PlatformDefault")
            IconShape.Circle -> obj.addProperty("type", "Circle")
            IconShape.Square -> obj.addProperty("type", "Square")
            IconShape.RoundedSquare -> obj.addProperty("type", "RoundedSquare")
            IconShape.Triangle -> obj.addProperty("type", "Triangle")
            IconShape.Squircle -> obj.addProperty("type", "Squircle")
            IconShape.Hexagon -> obj.addProperty("type", "Hexagon")
            IconShape.Pentagon -> obj.addProperty("type", "Pentagon")
            IconShape.Teardrop -> obj.addProperty("type", "Teardrop")
            IconShape.Pebble -> obj.addProperty("type", "Pebble")
            IconShape.EasterEgg -> obj.addProperty("type", "EasterEgg")
            IconShape.Random -> obj.addProperty("type", "Random")
            IconShape.Dragon -> obj.addProperty("type", "Dragon")
            IconShape.Rhombus -> obj.addProperty("type", "Rhombus")
            IconShape.SharpTriangle -> obj.addProperty("type", "SharpTriangle")
            IconShape.RoundedTriangle -> obj.addProperty("type", "RoundedTriangle")


            is IconShape.Custom -> {
                obj.addProperty("type", "Custom")
                obj.add("shape", context?.serialize(src.shape))
            }
        }

        return obj
    }

    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): IconShape? {
        if (json == null || !json.isJsonObject) return null

        val obj = json.asJsonObject
        val type = obj.get("type")?.asString ?: return null

        return when (type) {
            "PlatformDefault" -> IconShape.PlatformDefault
            "Circle" -> IconShape.Circle
            "Square" -> IconShape.Square
            "RoundedSquare" -> IconShape.RoundedSquare
            "Triangle" -> IconShape.Triangle
            "RoundedTriangle" -> IconShape.RoundedTriangle
            "SharpTriangle" -> IconShape.SharpTriangle
            "Rhombus" -> IconShape.Rhombus
            "Squircle" -> IconShape.Squircle
            "Hexagon" -> IconShape.Hexagon
            "Pentagon" -> IconShape.Pentagon
            "Teardrop" -> IconShape.Teardrop
            "Pebble" -> IconShape.Pebble
            "EasterEgg" -> IconShape.EasterEgg
            "Random" -> IconShape.Random
            "Dragon" -> IconShape.Dragon
            "Custom" -> {
                val shape = context?.deserialize<CustomIconShapeSerializable>(
                    obj.get("shape"),
                    CustomIconShapeSerializable::class.java
                )
                IconShape.Custom(shape!!)
            }
            else -> IconShape.Circle
        }
    }
}
