package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.player.RacePlayer;
	import com.f14.F14bgGame.bg.manager.GameManager;
	
	public class RaceGameManager extends GameManager
	{
		public function RaceGameManager()
		{
			super();
		}
		
		/**
		 * 允许的行动数
		 */
		public static var ACTION_NUM:int = 1;
		
		/**
		 * 设置总VP数
		 */
		public function setTotalVP(totalVP:int):void{
			RaceUtil.mainBoard.gameInfoBoard.totalVP = totalVP;
		}
		
		/**
		 * 设置牌堆数量
		 */
		public function setDeckSize(deckSize:int):void{
			RaceUtil.mainBoard.gameInfoBoard.deckSize = deckSize;
		}
		
		/**
		 * 设置选择回合的行动
		 */
		public function setRoundActions(actionTypes:String):void{
			RaceUtil.mainBoard.gameInfoBoard.setActionSelected(actionTypes);
		}
		
		/**
		 * 清除选择的回合行动
		 */
		public function clearRoundActions():void{
			RaceUtil.mainBoard.gameInfoBoard.clearSelection();
		}
		
		/**
		 * 设置游戏的基本信息
		 */
		override public function setupGame(param:Object):void{
			setTotalVP(param.totalVP);
			setDeckSize(param.deckSize);
			ACTION_NUM = param.actionNum;
			//if(RaceUtil.getLocalPlayer()!=null){
				//初始化本地玩家行动选择板块
				RaceUtil.stateManager.chooseActionBoard.initActionCard();
			//}
			//初始化所有玩家的板块
			for each(var player:RacePlayer in players){
				if(player!=null){
					player.racePlayerBoard.initImageAction();
				}
			}
		}
		
	}
}