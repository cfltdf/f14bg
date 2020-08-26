package com.f14.TTA.component

import com.f14.TTA.consts.Token
import com.f14.bg.component.PartPool
import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * Created by 吹风奈奈 on 2017/7/24.
 */
@JsonAdapter(TTAPartPool.TTAPartPoolAdapter::class)
class TTAPartPool : PartPool() {

    class TTAPartPoolAdapter : TypeAdapter<TTAPartPool>() {
        override fun read(reader: JsonReader): TTAPartPool {
            val res = TTAPartPool()
            reader.beginObject()
            while (reader.hasNext()) {
                val key = reader.nextName()
                val value = reader.nextInt()
                res.setPart(Token.valueOf(key), value)
            }
            reader.endObject()
            return res
        }

        override fun write(writer: JsonWriter, value: TTAPartPool) {
            writer.beginObject()
            value.allPartsNumber.forEach { (k, v) ->
                writer.name(k.toString()).value(v)
            }
            writer.endObject()
        }
    }

    override fun clone() = super.clone() as TTAPartPool
}