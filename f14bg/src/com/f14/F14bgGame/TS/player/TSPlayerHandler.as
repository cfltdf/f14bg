package com.f14.F14bgGame.TS.player
{
	import com.f14.F14bgGame.TS.TSUtil;
	import com.f14.F14bgGame.TS.consts.TSGameCmd;
	import com.f14.F14bgGame.TS.consts.TSInputState;
	import com.f14.F14bgGame.bg.handler.InGameHandler;

	public class TSPlayerHandler extends InGameHandler
	{
		public function TSPlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.code){
				case TSGameCmd.GAME_CODE_BASE_INFO: //装载游戏基本信息
					TSUtil.mainBoard.loadGameParam(param);
					break;
				case TSGameCmd.GAME_CODE_COUNTRY_INFO: //刷新国家信息
					TSUtil.mainBoard.loadCountriesParam(param);
					break;
				case TSGameCmd.GAME_CODE_DECK_INFO: //刷新牌组信息
					TSUtil.mainBoard.loadDeckInfo(param);
					break;
				case TSGameCmd.GAME_CODE_PLAYER_INFO: //刷新玩家的信息
					player.loadPlayerParam(param);
					break;
				case TSGameCmd.GAME_CODE_ADD_HANDS: //玩家得到手牌
					player.addHands(param);
					break;
				case TSGameCmd.GAME_CODE_REMOVE_HANDS: //玩家失去手牌
					player.removeHands(param);
					break;
				case TSGameCmd.GAME_CODE_CHINA_CARD: //刷新中国牌信息
					TSUtil.mainBoard.loadChinaCardParam(param);
					break;
				case TSGameCmd.GAME_CODE_ROUND: //玩家回合行动相关方法
					this.playerRoundAction(param);
					break;
				case TSGameCmd.GAME_CODE_ADJUST_INFLUENCE: //调整影响力的相关方法
					this.adjustInfluence(param);
					break;
				case TSGameCmd.GAME_CODE_ADD_INFLUENCE: //使用OP放置影响力的相关方法
					this.addInfluence(param);
					break;
				case TSGameCmd.GAME_CODE_COUP: //政变相关方法
					this.coup(param);
					break;
				case TSGameCmd.GAME_CODE_REALIGNMENT: //调整阵营相关方法
					this.realignment(param);
					break;
				case TSGameCmd.GAME_CODE_COUNTRY_ACTION: //选择国家进行行动的相关方法
					this.countryAction(param);
					break;
				case TSGameCmd.GAME_CODE_HEAD_LINE: //头条相关方法
					this.headLine(param);
					break;
				case TSGameCmd.GAME_CODE_ADD_ACTIVED_CARD: //添加生效卡牌
					TSUtil.mainBoard.activedCardList.addActivedCards(param.target, param.cardIds);
					break;
				case TSGameCmd.GAME_CODE_REMOVE_ACTIVED_CARD: //移除生效卡牌
					TSUtil.mainBoard.activedCardList.removeActivedCards(param.cardIds);
					break;
				case TSGameCmd.GAME_CODE_CARD_ACTION: //选择卡牌的相关方法
					//this.cardAction(param);
					break;
				case TSGameCmd.GAME_CODE_CHOICE: //选择行动的相关方法
					this.choice(param);
					break;
				case TSGameCmd.GAME_CODE_VIEW_HAND: //查看手牌
					this.viewHand(param);
					break;
				case TSGameCmd.GAME_CODE_ACTION_RECORD: //行动记录
					TSUtil.mainBoard.actionRecordList.loadParam(param);
					break;
				case TSGameCmd.GAME_CODE_ADD_DISCARD: //添加弃牌
					TSUtil.stateManager.deckWindow.loadDiscardParam(param);
					break;
				case TSGameCmd.GAME_CODE_TRASH_CARD: //卡牌移出游戏
					TSUtil.stateManager.deckWindow.loadTrashParam(param);
					break;
				case TSGameCmd.GAME_CODE_REMOVE_DISCARD: //移除弃牌
					TSUtil.stateManager.deckWindow.removeDiscards(param);
					break;
			}
		}
		
		/**
		 * 玩家回合行动的相关方法
		 */
		protected function playerRoundAction(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "button": //装载按键信息
					player.tsPlayerBoard.inputState = TSInputState.ACTION_ROUND;
					player.tsPlayerBoard.buttonPart.loadParam(param);
					break;
			}
		}
		
		/**
		 * 调整影响力的相关方法
		 */
		protected function adjustInfluence(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "influenceParam": //装载已选影响力参数
					TSUtil.stateManager.influenceWindow.loadInfluenceParam(param);
					break;
			}
		}
		
		/**
		 * 使用OP放置影响力的相关方法
		 */
		protected function addInfluence(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "influenceParam": //装载已选影响力参数
					TSUtil.stateManager.addInfluenceWindow.loadInfluenceParam(param);
					break;
			}
		}
		
		/**
		 * 政变的相关方法
		 */
		protected function coup(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "coupParam": //装载选中政变目标的参数
					TSUtil.stateManager.coupWindow.loadCoupParam(param);
					break;
			}
		}
		
		/**
		 * 调整阵营的相关方法
		 */
		protected function realignment(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "realignmentParam": //装载选中国家的参数
					TSUtil.stateManager.realignmentWindow.loadRealignmentParam(param);
					break;
			}
		}
		
		/**
		 * 选择国家进行行动的相关方法
		 */
		protected function countryAction(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "countryParam": //装载国家列表参数
					TSUtil.stateManager.countryWindow.loadCountryParam(param);
					break;
			}
		}
		
		/**
		 * 选择国家进行行动的相关方法
		 */
		protected function headLine(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "headLineParam": //装载国家列表参数
					TSUtil.stateManager.healLineWindow.loadStatusParam(param);
					break;
				case "inputState": //设置输入状态
					if(param.selecting){
						player.tsPlayerBoard.inputState = TSInputState.ACTION_HEADLINE;
					}else{
						TSUtil.mainBoard.disableAllInput();
					}
					break;
			}
		}
		
		/**
		 * 选择行动的相关方法
		 */
		protected function choice(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "choiceParam": //装载国家列表参数
					TSUtil.stateManager.choiceWindow.loadChoiceParam(param);
					break;
			}
		}
		
		/**
		 * 查看手牌的相关方法
		 */
		protected function viewHand(param:Object):void{
			var player:TSPlayer = TSUtil.getPlayer(param.position);
			switch(param.subact){
				case "handParam": //装载查看手牌的参数
					TSUtil.stateManager.viewHandWindow.loadHandParam(param);
					break;
			}
		}
	}
}