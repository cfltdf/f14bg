package com.f14.bg.anim

import com.google.gson.annotations.SerializedName


class AnimVar(
        val anim: String? = null,
        val position: String? = null,
        val extend: String? = null,
        val id: String? = null,
        @SerializedName("object")
        val obj: String? = null
) {

    companion object {

        fun createAnimObjectVar(obj: Any, id: Any): AnimVar {
            return AnimVar(obj = obj.toString(), id = id.toString())
        }


        fun createAnimObjectVar(obj: Any, id: Any, extend: Any): AnimVar {
            return AnimVar(obj = obj.toString(), id = id.toString(), extend = extend.toString())
        }


        fun createAnimVar(anim: Any, position: Any): AnimVar {
            return AnimVar(anim = anim.toString(), position = position.toString())
        }


        fun createAnimVar(anim: Any, position: Any, extend: Any): AnimVar {
            return AnimVar(anim = anim.toString(), position = position.toString(), extend = extend.toString())
        }
    }
}
