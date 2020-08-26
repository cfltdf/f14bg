package com.f14.TTA.component

import com.f14.TTA.consts.CivilizationProperty
import com.f14.bg.component.PropertyCounter
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter


/**
 * TTA用的属性容器
 * @author F14eagle
 */
@JsonAdapter(TTAProperty.TTAPropertyAdapter::class)
class TTAProperty : PropertyCounter<CivilizationProperty>() {
    @Throws(CloneNotSupportedException::class)
    override fun clone() = super.clone() as TTAProperty

    class TTAPropertyAdapter : TypeAdapter<TTAProperty>() {
        override fun read(reader: JsonReader): TTAProperty {
            val res = TTAProperty()
            try {
                reader.beginObject()
                while (reader.hasNext()) {
                    val key = reader.nextName()
                    val value = reader.nextInt()
                    res.addProperty(CivilizationProperty.valueOf(key), value)
                }
                reader.endObject()
            } catch (e: Exception) {
            }
            return res
        }

        override fun write(writer: JsonWriter, value: TTAProperty) {
            writer.beginObject()
            value.allProperties.forEach { (k, v) ->
                writer.name(k.toString()).value(v)
            }
            writer.endObject()
        }
    }
}
