package com.f14.F14bgGame.Tichu.player
{
	import com.f14.F14bgGame.Tichu.TichuUtil;
	import com.f14.F14bgGame.Tichu.consts.TichuGameCmd;
	import com.f14.F14bgGame.bg.handler.InGameHandler;

	public class TichuPlayerHandler extends InGameHandler
	{
		public function TichuPlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:TichuPlayer = TichuUtil.getPlayer(param.position);
			switch(param.code){
				case TichuGameCmd.GAME_CODE_BASE_INFO: //装载游戏基本信息
					TichuUtil.mainBoard.gameInfoBoard.loadGameInfo(param);
					break;
				case TichuGameCmd.GAME_CODE_PLAYER_HAND: //刷新玩家手牌
					player.loadHands(param);
					break;
				case TichuGameCmd.GAME_CODE_PLAYER_INFO: //刷新玩家信息
					player.loadPlayerInfo(param);
					break;
				case TichuGameCmd.GAME_CODE_PLAYER_PLAY_CARD: //刷新玩家的出牌区
					player.loadPlayerPlayedCard(param);
					break;
				case TichuGameCmd.GAME_CODE_PLAYER_BUTTON: //刷新玩家的按键
					player.loadPlayerButton(param);
					break;
				case TichuGameCmd.GAME_CODE_ROUND_RESULT: //回合结算
					TichuUtil.stateManager.roundResultWindow.loadResultParam(param);
					break;
				case TichuGameCmd.GAME_CODE_CONFIRM_EXCHANGE: //确认换牌
					//装载确认换牌的参数
					TichuUtil.stateManager.confirmExchangeWindow.loadExchangeParam(param);
					break;
			}
		}
		
		/**
		 * 处理简单指令的方法
		 */
		override protected function processSimpleCmd(param:Object):void{
			var subact:String = param.subact;
			if(subact){
				//发声!!
				TichuUtil.soundManager.play(subact);
			}
		}
		
	}
}