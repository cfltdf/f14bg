package com.f14.bg.report;

import com.f14.bg.BoardGame;
import com.f14.bg.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带有缓存的战报记录
 *
 * @author F14eagle
 */
public class BgCacheReport<P extends Player> extends BgReport<P> {
    /**
     * 所有玩家的战报缓存记录
     */

    protected Map<P, List<MessageObject>> messageCache = new HashMap<>();

    public BgCacheReport(BoardGame<P, ?, ?, ?> bg) {
        super(bg);
    }

    /**
     * 玩家执行动作,并输出该玩家所有缓存的信息
     */
    @Override
    public void action(P player, String message) {
        StringBuilder sbPublic = new StringBuilder(message);
        StringBuilder sbPrivate = null;
        String sep = message.length() > 0 ? "," : "";
        for (MessageObject mo : this.getCacheMessages(player)) {
            if (mo.param != null && mo.param.containsKey("private")) {
                if (sbPrivate == null) {
                    sbPrivate = new StringBuilder(sbPublic);
                }
                sbPublic.append(sep).append(mo.message);
                sbPrivate.append(sep).append(mo.param.get("private"));
            } else {
                sbPublic.append(sep).append(mo.message);
                if (sbPrivate != null) {
                    sbPrivate.append(sep).append(mo.message);
                }
            }
            sep = ",";
        }
        this.clearPlayerCache(player);
        if (sbPrivate != null) {
            super.action(player, sbPublic.toString(), sbPrivate.toString());
        } else {
            super.action(player, sbPublic.toString());
        }
    }

    /**
     * 玩家执行动作,并输出该玩家所有缓存的信息
     */
    @Override
    public void action(P player, String msgPublic, String msgPrivate) {
        StringBuilder sbPublic = new StringBuilder(msgPublic);
        StringBuilder sbPrivate = new StringBuilder(msgPrivate);
        String sep = msgPublic.length() > 0 ? "," : "";
        for (MessageObject mo : this.getCacheMessages(player)) {
            if (mo.param != null && mo.param.containsKey("private")) {
                sbPublic.append(sep).append(mo.message);
                sbPrivate.append(sep).append(mo.param.get("private"));
            } else {
                sbPublic.append(sep).append(mo.message);
                sbPrivate.append(sep).append(mo.message);
            }
            sep = ",";
        }
        this.clearPlayerCache(player);
        super.action(player, sbPublic.toString(), sbPrivate.toString());
    }

    /**
     * 添加玩家行动到缓存中
     *
     * @param player
     * @param message
     */
    public void addAction(P player, String message) {
        MessageObject mo = new MessageObject(null, message, null, false);
        this.getCacheMessages(player).add(mo);
    }

    /**
     * 添加玩家行动到缓存中
     *
     * @param player
     */
    public void addAction(P player, String msgPublic, String msgPrivate) {
        Map<String, Object> param = new HashMap<>();
        param.put("private", msgPrivate);
        MessageObject mo = new MessageObject(null, msgPublic, param, false);
        this.getCacheMessages(player).add(mo);
    }

    /**
     * 清空玩家的缓存信息
     *
     * @param player
     */
    protected void clearPlayerCache(P player) {
        this.getCacheMessages(player).clear();
    }

    /**
     * 取得玩家的所有缓存信息
     *
     * @param player
     * @return
     */
    protected List<MessageObject> getCacheMessages(P player) {
        return this.messageCache.computeIfAbsent(player, k -> new ArrayList<>());
    }

    /**
     * 添加玩家行动到缓存中
     *
     * @param player
     * @param message
     */
    public void insertAction(P player, String message) {
        MessageObject mo = new MessageObject(null, message, null, false);
        this.getCacheMessages(player).add(0, mo);
    }

    /**
     * 添加玩家行动到缓存中
     *
     * @param player
     */
    public void insertAction(P player, String msgPublic, String msgPrivate) {
        Map<String, Object> param = new HashMap<>();
        param.put("private", msgPrivate);
        MessageObject mo = new MessageObject(null, msgPublic, param, false);
        this.getCacheMessages(player).add(0, mo);
    }

    /**
     * 输出玩家当前缓存内容(如果没有缓存内容则不输出)
     *
     * @param player
     */
    public void printCache(P player) {
        if (!this.getCacheMessages(player).isEmpty()) {
            this.action(player, "");
        }
    }

}
