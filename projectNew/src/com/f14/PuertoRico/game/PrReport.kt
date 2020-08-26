package com.f14.PuertoRico.game

import com.f14.PuertoRico.component.CharacterCard
import com.f14.PuertoRico.component.PRTile
import com.f14.PuertoRico.component.PrPartPool
import com.f14.PuertoRico.consts.Ability
import com.f14.PuertoRico.consts.Character
import com.f14.PuertoRico.consts.GoodType
import com.f14.bg.report.BgReport
import com.f14.f14bgdb.util.CodeUtil

class PrReport(bg: PuertoRico) : BgReport<PRPlayer>(bg) {

    /**
     * 玩家更换板块
     * @param player
     * @param from
     * @param to
     */
    fun changeTile(player: PRPlayer, from: PRTile, to: PRTile) {
        this.action(player, "将 ${from.reportString} 换成了 ${to.reportString}")
    }

    /**
     * 玩家选择建筑
     * @param player
     * @param tile
     */
    fun chooseBuilding(player: PRPlayer, tile: PRTile) {
        this.action(player, "选择了 ${tile.reportString}")
    }

    /**
     * 玩家选择角色
     * @param player
     * @param card
     */
    fun chooseCharacter(player: PRPlayer, card: CharacterCard) {
        val message = "选择角色 ${CodeUtil.getLabel(Character.CODE_TYPE, card.character.toString())}]"
        this.action(player, message)
        this.getDoubloon(player, card.doubloon)
    }

    /**
     * 清空货船
     * @param size
     */
    fun clearShip(size: Int) {
        this.info("清空了货船($size)")
    }

    /**
     * 清空交易所
     */
    fun clearTradeHouse() {
        this.info("清空了交易所")
    }

    /**
     * 玩家建造建筑
     * @param player
     * @param tile
     * @param doubloon
     */
    fun doBuild(player: PRPlayer, tile: PRTile, doubloon: Int) {
        this.action(player, "建造了 ${tile.reportString}")
        this.getDoubloon(player, doubloon)
    }

    /**
     * 玩家分配移民
     * @param player
     */
    fun doMajor(player: PRPlayer) {
        val msg = player.tiles.cards.joinToString(",", "分配了移民: ") { if (it.colonistMax > 0) "${it.name}(${it.colonistNum})" else it.name }
        this.action(player, msg)
    }

    /**
     * 玩家直接结束行动
     * @param player
     */
    fun doPass(player: PRPlayer) {
        this.action(player, "直接结束行动")
    }

    /**
     * 玩家生产资源
     * @param player
     * @param resources
     */
    fun doProduce(player: PRPlayer, resources: PrPartPool) {
        if (resources.totalNum > 0) {
            this.action(player, "生产了 ${resources.resourceDescr}")
        } else {
            this.action(player, "没有生产任何货物")
        }
    }

    /**
     * 玩家运货

     * @param player
     * @param parts
     * @param vp
     * @param doubloon
     */
    fun doShip(player: PRPlayer, parts: PrPartPool, vp: Int, doubloon: Int) {
        var str = "运走了 ${parts.resourceDescr},得到 $vp 点VP"
        if (doubloon > 0) {
            str += " 和 $doubloon 金钱"
        }
        this.action(player, str)
    }

    /**
     * 玩家交易货物
     * @param player
     * @param goodType
     * @param doubloon
     */
    fun doTrade(player: PRPlayer, goodType: GoodType, doubloon: Int) {
        this.action(player, "卖出了 ${goodType.chinese} ,得到 $doubloon 金钱")
    }

    /**
     * 玩家得到移民
     * @param player
     * @param num
     */
    fun getColonist(player: PRPlayer, num: Int) {
        if (num > 0) {
            this.action(player, "得到 $num 个移民")
        } else if (num < 0) {
            this.action(player, "失去 ${-num} 个移民")
        }
    }

    /**
     * 玩家的建筑得到移民
     * @param player
     * @param num
     */
    fun getColonist(player: PRPlayer, tile: PRTile, num: Int) {
        if (num > 0) {
            this.action(player, "的 ${tile.reportString} 得到 $num 个移民")
        } else if (num < 0) {
            this.action(player, "的 ${tile.reportString} 失去 ${-num} 个移民")
        }
    }

    /**
     * 移民船得到移民
     * @param num
     */
    fun getColonistShip(num: Int) {
        this.info("移民船上分配了 $num 个移民")
    }

    /**
     * 玩家得到金钱
     * @param player
     * @param doubloon
     */
    fun getDoubloon(player: PRPlayer, doubloon: Int) {
        if (doubloon > 0) {
            this.action(player, "得到 $doubloon 金钱")
        } else if (doubloon < 0) {
            this.action(player, "失去 ${-doubloon} 金钱")
        }

    }

    /**
     * 玩家得到资源
     * @param player
     * @param part
     * @param factor
     */
    fun getResource(player: PRPlayer, part: PrPartPool, factor: Int) {
        if (factor > 0) {
            this.action(player, "得到 ${part.resourceDescr}")
        } else if (factor < 0) {
            this.action(player, "失去 ${part.resourceDescr}")
        }
    }

    /**
     * 玩家得到板块
     * @param player
     * @param tile
     */
    fun getTile(player: PRPlayer, tile: PRTile) {
        this.action(player, "得到了 ${tile.reportString}")
    }

    /**
     * 玩家得到VP
     * @param player
     * @param vp
     */
    fun getVP(player: PRPlayer, vp: Int) {
        if (vp > 0) {
            this.action(player, "得到 $vp 点VP")
        } else if (vp < 0) {
            this.action(player, "失去 ${-vp} 点VP")
        }
    }

    /**
     * 显示翻出的种植园信息
     * @param plantations
     */
    fun listPlantations(plantations: List<PRTile>) {
        val str = plantations.joinToString(separator = " ", prefix = "翻出种植园: ", transform = PRTile::name)
        this.info(str)
    }

    /**
     * 玩家保存资源
     * @param player
     */
    fun saveResources(player: PRPlayer) {
        val str = player.resources.resourceDescr
        if (str.isEmpty()) {
            this.action(player, "没有保存任何货物")
        } else {
            this.action(player, "保存了 $str")
        }
    }

    /**
     * 玩家使用能力
     * @param player
     */
    fun useAbility(player: PRPlayer, ability: Ability) {
        this.action(player, "使用了 [${CodeUtil.getLabel(Ability.CODE_TYPE, ability.toString())}] 的能力")
    }
}
