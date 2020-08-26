package com.f14.F14bgGame.Innovation.player
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.consts.InnoGameCmd;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChooseScoreWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoChooseSpecificCardWindow;
	import com.f14.F14bgGame.Innovation.ui.window.InnoCustom081Window;
	import com.f14.F14bgGame.bg.handler.InGameHandler;

	public class InnoPlayerHandler extends InGameHandler
	{
		public function InnoPlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:InnoPlayer = InnoUtil.getPlayer(param.position);
			switch(param.code){
				case InnoGameCmd.GAME_CODE_DRAW_DECK_INFO: //装载摸牌堆信息
					InnoUtil.mainBoard.loadDrawDeckParam(param);
					break;
				case InnoGameCmd.GAME_CODE_ACHIEVE_INFO:	//装载成就牌堆信息
					InnoUtil.mainBoard.loadAchieveCardDeckParam(param);
					break;
				case InnoGameCmd.GAME_CODE_ADD_HANDS: //玩家得到手牌
					player.addHands(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_HANDS: //玩家失去手牌
					player.removeHands(param);
					break;
				case InnoGameCmd.GAME_CODE_ADD_SCORES: //玩家得到计分区的牌
					player.addScores(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_SCORES: //玩家失去计分区的牌
					player.removeScores(param);
					break;
				case InnoGameCmd.GAME_CODE_CARD_STACK: //刷新玩家已打出牌堆的信息
					player.refreshCardStack(param);
					break;
				case InnoGameCmd.GAME_CODE_SCORE_INFO:	//刷新玩家得分信息
					player.refreshScoreInfo(param);
					break;
				case InnoGameCmd.GAME_CODE_REFRESH_AP:	//刷新行动点数
					player.refreshAp(param);
					break;
				case InnoGameCmd.GAME_CODE_ICON_INFO:	//刷新玩家的所有图标
					player.refreshTotalIcons(param);
					break;
				case InnoGameCmd.GAME_CODE_ADD_ACHIEVE:	//玩家得到成就
					player.addAchieves(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_SPECIFIC_CARD:	//移除待选的卡牌
					this.removeSpecificCard(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_CHOOSE_SCORE_CARD:	//移除待选的计分牌
					this.removeScoreCard(param);
					break;
				case InnoGameCmd.GAME_CODE_ADD_SCORES:	//玩家得到分数
					player.refreshScoreInfo(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_SCORES:	//玩家失去分数
					player.refreshScoreInfo(param);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_ACHIEVE_CARD:	//版图失去成就牌
					InnoUtil.mainBoard.normalAchieveDeck.removeCard(param.level);
					break;
				case InnoGameCmd.GAME_CODE_REMOVE_SPECIAL_ACHIEVE_CARD:	//版图失去特殊成就牌
					InnoUtil.mainBoard.achieveCardsDeck.removeCard(param.cardId);
					break;
			}
		}
		
		/**
		 * 移除待选的卡牌
		 */
		protected function removeSpecificCard(param:Object):void{
			if(InnoUtil.stateManager.currentConfirmWindow!=null
				&& InnoUtil.stateManager.currentConfirmWindow instanceof InnoChooseSpecificCardWindow){
				var window:InnoChooseSpecificCardWindow = InnoUtil.stateManager.currentConfirmWindow as InnoChooseSpecificCardWindow;
				window.removeCards(param.cardIds);
			}
		}
		
		/**
		 * 移除待选的计分牌
		 */
		protected function removeScoreCard(param:Object):void{
			if(InnoUtil.stateManager.currentConfirmWindow!=null){
				if(InnoUtil.stateManager.currentConfirmWindow instanceof InnoChooseScoreWindow){
					var window:InnoChooseScoreWindow = InnoUtil.stateManager.currentConfirmWindow as InnoChooseScoreWindow;
					window.removeCards(param.cardIds);
				}else if(InnoUtil.stateManager.currentConfirmWindow instanceof InnoCustom081Window){
					var win2:InnoCustom081Window = InnoUtil.stateManager.currentConfirmWindow as InnoCustom081Window;
					win2.removeCards(param.cardIds);
				}
			}
		}
		
	}
}