package com.f14.F14bgGame.TTA.player
{
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.F14bgGame.TTA.TTAUtil;
	import com.f14.F14bgGame.TTA.consts.TTAGameCmd;

	public class TTAPlayerHandler extends InGameHandler
	{
		public function TTAPlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:TTAPlayer = TTAUtil.getPlayer(param.position);
			switch(param.code){
				case TTAGameCmd.GAME_CODE_BASE_INFO: //装载游戏基本信息
					TTAUtil.mainBoard.gameInfo.loadGameInfo(param);
					break;
				case TTAGameCmd.GAME_CODE_CHANGE_STEP: //玩家刷新回合中的阶段
					if(TTAUtil.gameManager.isLocalParam(param)){
						TTAUtil.getLocalPlayer().step = param.currentStep;
					}
					break;
				case TTAGameCmd.GAME_CODE_CARD_ROW: //刷新卡牌序列
					TTAUtil.mainBoard.cardRow.loadCards(param);
					break;
				case TTAGameCmd.GAME_CODE_ADD_CARD: //玩家面板打出卡牌
					player.addCards(param.cardIds);
					break;
				case TTAGameCmd.GAME_CODE_REMOVE_CARD: //玩家面板失去卡牌
					player.removeCards(param.cardIds);
					break;
				case TTAGameCmd.GAME_CODE_CIVILIZATION_INFO: //刷新玩家文明属性
					player.loadProperty(param.property);
					break;
				case TTAGameCmd.GAME_CODE_CARD_TOKEN: //刷新玩家卡牌标志物数量
					player.loadCardToken(param.cards);
					break;
				case TTAGameCmd.GAME_CODE_BOARD_TOKEN: //刷新玩家面板的标志物
					player.loadBoardToken(param);
					break;
				case TTAGameCmd.GAME_CODE_ADD_HAND: //玩家得到手牌
					this.playerAddHand(player, param);
					break;
				case TTAGameCmd.GAME_CODE_REMOVE_HAND: //玩家失去手牌
					this.playerRemoveHand(player, param);
					break;
				case TTAGameCmd.GAME_CODE_REMOVE_CARDROW: //卡牌序列移除牌
					TTAUtil.mainBoard.cardRow.removeCard(param.cardId);
					break;
				case TTAGameCmd.GAME_CODE_GET_WONDER: //玩家得到奇迹
					player.setUncompleteWonder(param.cardId);
					break;
				case TTAGameCmd.GAME_CODE_WONDER_COMPLETE: //玩家奇迹建造完成
					player.completeWonder();
					break;
				case TTAGameCmd.GAME_CODE_ACTION_REQUEST: //玩家请求行动界面
					//只有本地玩家才会处理该请求
					//if(TTAUtil.isLocalPlayer(player)){
					TTAUtil.stateManager.showRequestWindow(param);
					//}
					break;
				case TTAGameCmd.GAME_CODE_REQUEST_END: //玩家请求行动界面结束
					//只有本地玩家才会处理该请求
					//if(TTAUtil.isLocalPlayer(player)){
					TTAUtil.stateManager.hideRequestWindow();
					//}
					break;
				case TTAGameCmd.GAME_CODE_BONUS_CARD: //刷新玩家的奖励牌堆
					TTAUtil.mainBoard.button_bonusCard.enabled = true;
					TTAUtil.stateManager.bonusCardWindow.addCards(param.cardIds);
					break;
				case TTAGameCmd.GAME_CODE_AUCTION_TERRITORY: //拍卖殖民地相关行动
				case TTAGameCmd.GAME_CODE_WAR: //侵略/战争和殖民地使用相同的方法
					this.territoryAction(param);
					break;
				case TTAGameCmd.GAME_CODE_ACTIVABLE_CARD: //刷新可激活的卡牌
					if(TTAUtil.gameManager.isLocalParam(param)){
						TTAUtil.getLocalPlayer().ttaPlayerBoard.initActivableCards(param.activableCardIds);
						TTAUtil.getLocalPlayer().ttaPlayerBoard.refreshInputState();
					}
					break;
				case TTAGameCmd.GAME_CODE_OVERTIME_CARD: //设置持续效果卡牌的信息
					TTAUtil.overtimeCardManager.addCards(param.cards);
					break;
				case TTAGameCmd.GAME_CODE_PACT: //条约
					this.pactAction(param);
					break;
			}
		}
		
		/**
		 * 玩家得到手牌
		 */
		protected function playerAddHand(player:TTAPlayer, param:Object):void{
			player.civilHands += param.civilNum;
			player.militaryHands += param.militaryNum;
			if(TTAUtil.gameManager.isLocalPlayer(player)){
				//如果是本地玩家,则添加卡牌到手牌
				player.addHands(param);
			}
		}
		
		/**
		 * 玩家失去手牌
		 */
		protected function playerRemoveHand(player:TTAPlayer, param:Object):void{
			player.civilHands -= param.civilNum;
			player.militaryHands -= param.militaryNum;
			if(TTAUtil.gameManager.isLocalPlayer(player)){
				//如果是本地玩家,则从手牌移除卡牌
				player.removeHands(param);
			}
		}
		
		/**
		 * 拍卖殖民地阶段的相关行动
		 */
		protected function territoryAction(param:Object):void{
			switch(param.subact){
				case "loadParam": //装载拍卖信息的参数
					//装载参数并显示殖民地拍卖的窗口
					TTAUtil.stateManager.territoryWindow.loadParam(param);
					TTAUtil.stateManager.territoryWindow.show(false);
					break;
				case "auctionValue": //刷新拍卖总数
					TTAUtil.stateManager.territoryWindow.loadTotalValue(param);
					break;
				case "auctionParam": //装载玩家拍卖的出价信息
					TTAUtil.stateManager.territoryWindow.loadAuctionParam(param);
					break;
			}
		}
		
		/**
		 * 签订条约阶段的相关行动
		 */
		protected function pactAction(param:Object):void{
			switch(param.subact){
				case "loadPactSide": //装载条约签订双方的信息
					TTAUtil.stateManager.pactWindow.loadPactSide(param);
					break;
			}
		}
		
	}
}