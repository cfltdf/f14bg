package com.f14.TTA.executor;

import com.f14.TTA.TTAGameMode;
import com.f14.TTA.TTAPlayer;
import com.f14.TTA.component.param.RoundParam;
import com.f14.bg.exception.BoardGameException;


/**
 * TTA的行动处理器
 *
 * @author 吹风奈奈
 */
public abstract class TTAActionExecutor {
    protected TTAGameMode gameMode;
    protected TTAPlayer player;
    protected RoundParam param;
    protected boolean checked = false;

    /**
     * @param param
     */
    public TTAActionExecutor(RoundParam param) {
        this.gameMode = param.gameMode;
        this.player = param.player;
        this.param = param;
    }

    /**
     * @throws BoardGameException
     */
    public void check() throws BoardGameException {
        this.checked = true;
    }

    /**
     * @throws BoardGameException
     */
    abstract public void execute() throws BoardGameException;

}
