package com.f14.TTA.component

import com.f14.TTA.consts.ChooserType
import com.f14.TTA.consts.CivilizationProperty
import com.google.gson.annotations.SerializedName

/**
 * 文明选择器,按照设定的条件取得文明

 * @author F14eagle
 */
class Chooser {
    var type: ChooserType? = null
    var num = 0
    var byProperty: CivilizationProperty? = null
    @SerializedName("weakest")
    var isWeakest: Boolean = false

}