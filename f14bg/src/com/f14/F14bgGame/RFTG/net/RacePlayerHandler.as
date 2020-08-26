package com.f14.F14bgGame.RFTG.net
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.consts.CmdConst;
	import com.f14.F14bgGame.RFTG.player.RacePlayer;
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.core.util.ApplicationUtil;

	public class RacePlayerHandler extends InGameHandler
	{
		public function RacePlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var i:int;
			var player:RacePlayer;
			var act:String;
			var arr:Array;
			switch(param.code){
				case CmdConst.GAME_CODE_DRAW_CARD: //摸牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.addHandSize(param.cardNum);
						if(RaceUtil.gameManager.isLocalPlayer(player)){
							player.addCards(param.cardIds);
							//摸到的牌默认设为可选
							RaceUtil.stateManager.getHandBoard().setCardsSelectable(param.cardIds, true, false);
						}
					}
					break;
				case CmdConst.GAME_CODE_DISCARD: //弃牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.addHandSize(-param.cardNum);
						if(RaceUtil.gameManager.isLocalPlayer(player)){
							player.discardCards(param.cardIds);
						}
					}
					break;
				case CmdConst.GAME_CODE_PLAY_CARD: //从手牌中打牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.addHandSize(-param.cardNum);
						if(RaceUtil.gameManager.isLocalPlayer(player)){
							player.playCards(param.cardIds);
						}else{
							player.directPlayCards(param.cardIds);
						}
					}
					break;
				case CmdConst.GAME_CODE_DIRECT_PLAY_CARD: //直接出牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.directPlayCards(param.cardIds);
					}
					break;
				case CmdConst.GAME_CODE_ACTIVE_CARD_LIST: //可使用卡牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						if(RaceUtil.gameManager.isLocalPlayer(player)){
							//player.playerBoard.cardBoard.setActive(param.cardIds, true);
							RaceUtil.stateManager.getPlayBoard().setActiveWithType(param.cards, true);
						}
					}
					break;
				case CmdConst.GAME_CODE_DISCARD_PLAYED_CARD: //弃掉打出的牌
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.discardPlayedCards(param.cardIds);
					}
					break;
				case CmdConst.GAME_CODE_PRODUCE_GOOD: //生产货物
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.produceGoods(param.cardIds);
					}
					break;
				case CmdConst.GAME_CODE_DISCARD_GOOD: //弃掉货物
					if(param.cardNum && param.cardNum>0){
						player = RaceUtil.getPlayer(param.position);
						player.discardGoods(param.cardIds);
					}
					break;
				case CmdConst.GAME_CODE_GET_VP: //得到VP
					player = RaceUtil.getPlayer(param.position);
					player.addVP(param.vp);
					RaceUtil.gameManager.setTotalVP(param.remainvp);
					break;
				case CmdConst.GAME_CODE_REFRESH_DECK: //刷新牌堆数量
					RaceUtil.gameManager.setDeckSize(param.deckSize);
					break;
				case CmdConst.GAME_CODE_SHOW_ACTION: //显示玩家选择的行动
					player = RaceUtil.getPlayer(param.position);
					RaceUtil.gameManager.setRoundActions(param.actionTypes);
					//读取玩家选择行动的卡牌
					player.racePlayerBoard.loadActionImages(param.actionTypes);
					break;
				case CmdConst.GAME_CODE_EXPLORE: //探索选择
					//探索摸牌
					player = RaceUtil.getPlayer(param.position);
					if(RaceUtil.gameManager.isLocalPlayer(player)){
						if(param.exploreHand){
							//如果可以从手牌中选择,则允许所有手牌的选择
							RaceUtil.stateManager.getHandBoard().setAllSelectable(true);
						}
						//设置探索阶段摸到的卡牌为可选
						RaceUtil.stateManager.getHandBoard().setCardsSelectable(param.cardIds, true, true);
					}
					break;
				case CmdConst.GAME_CODE_DEVELOP: //开发阶段
					act = param.subact;
					player = RaceUtil.getPlayer(param.position);
					if(act=="choose"){
						player.controlBoard.cardBoard.setCardsLocked(param.cardIds, true);
						ApplicationUtil.alert("建造费用 " + param.cost);
					}else if(act=="discard"){
					}else if(act=="cancel"){
						player.controlBoard.cardBoard.setCardsLocked(param.cardIds, false);
					}else if(act=="pass"){
					}
					break;
				case CmdConst.GAME_CODE_SETTLE: //扩张阶段
					act = param.subact;
					player = RaceUtil.getPlayer(param.position);
					if(act=="choose"){
						player.controlBoard.cardBoard.setCardsLocked(param.cardIds, true);
						ApplicationUtil.alert("建造费用 " + param.cost);
					}else if(act=="discard"){
					}else if(act=="cancel"){
						player.controlBoard.cardBoard.setCardsLocked(param.cardIds, false);
					}else if(act=="pass"){
					}
					break;
				case CmdConst.GAME_CODE_SUPPLY_REFRESH_GOAL: //刷新公共区的目标
					RaceUtil.mainBoard.goalBoard.clear();
					if(param.goalIds){
						var goalIds:Array = param.goalIds.split(",");
						for each(var goalId:String in goalIds){
							RaceUtil.mainBoard.goalBoard.addGoal(goalId);
						}
					}
					break;
				case CmdConst.GAME_CODE_SUPPLY_GET_GOAL: //公共区得到目标
					RaceUtil.mainBoard.goalBoard.addGoal(param.goalId);
					break;
				case CmdConst.GAME_CODE_SUPPLY_LOST_GOAL: //公共区失去目标
					RaceUtil.mainBoard.goalBoard.removeGoal(param.goalId);
					break;
				case CmdConst.GAME_CODE_PLAYER_REFRESH_GOAL: //刷新玩家的目标
					player = RaceUtil.getPlayer(param.position);
					player.racePlayerBoard.playerGoalBoard.clear();
					if(param.goalIds){
						arr = param.goalIds.split(",");
						for each(goalId in arr){
							player.racePlayerBoard.playerGoalBoard.addGoal(goalId);
						}
					}
					break;
				case CmdConst.GAME_CODE_PLAYER_GET_GOAL: //玩家得到目标
					player = RaceUtil.getPlayer(param.position);
					player.racePlayerBoard.playerGoalBoard.addGoal(param.goalId);
					break;
				case CmdConst.GAME_CODE_PLAYER_LOST_GOAL: //玩家失去目标
					player = RaceUtil.getPlayer(param.position);
					player.racePlayerBoard.playerGoalBoard.removeGoal(param.goalId);
					break;
				case CmdConst.GAME_CODE_GAMBLE: //赌博
					this.doGamble(param);
					break;
			}
		}
		
		/**
		 * 赌博
		 */
		protected function doGamble(param:Object):void{
			if(!param.ending){
				RaceUtil.stateManager.showGambleWindow(param);
			}else{
				RaceUtil.stateManager.hideGambleWindow();
			}
		}
	}
	
}