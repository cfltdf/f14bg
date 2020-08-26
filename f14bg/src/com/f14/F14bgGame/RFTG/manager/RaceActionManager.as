package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.RFTG.RaceUtil;
	import com.f14.F14bgGame.RFTG.consts.ActiveType;
	import com.f14.F14bgGame.RFTG.consts.CmdConst;
	import com.f14.F14bgGame.RFTG.utils.RaceUtils;
	import com.f14.F14bgGame.bg.manager.GameActionManager;
	
	public class RaceActionManager extends GameActionManager
	{
		public function RaceActionManager()
		{
			super();
		}
		
		/**
		 * 执行弃牌动作
		 * 
		 * @param 默认则取当前收到的代码
		 */
		public function doDiscard(code:int=0):void{
			//如果发送代码为0,则取当前收到的代码
			if(code==0){
				code = handler.currentCode;
			}
			var param:Object = createGameCommand(code);
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedCards();
			param.cardIds = RaceUtils.card2String(cards);
			param.subact = "discard";
			sendCommand(param);
		}
		
		/**
		 * 选择行动
		 * 
		 * @param Array<String>
		 */
		public function doChooseAction(actions:Array):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.actionCode = RaceUtils.array2String(actions);
			sendCommand(param);
		}
		
		/**
		 * 选择开发的设施
		 */
		public function doDevelop():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "choose";
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedCards();
			param.cardIds = RaceUtils.card2String(cards);
			sendCommand(param);
		}
		
		/**
		 * 选择扩张的星球
		 */
		public function doSettle():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "choose";
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedCards();
			param.cardIds = RaceUtils.card2String(cards);
			sendCommand(param);
		}
		
		/**
		 * 选择交易的货物
		 */
		public function doTrade():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "trade";
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedGoods();
			param.cardIds = RaceUtils.card2String(cards);
			sendCommand(param);
		}
		
		/**
		 * 选择生产的星球
		 */
		public function doProduce():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "produce";
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedPlayedCards();
			param.cardIds = RaceUtils.card2String(cards);
			sendCommand(param);
		}
		
		/**
		 * 激活卡牌的能力
		 */
		public function activeCard(cardId:String, activeType:String=null):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "active";
			param.cardId = cardId;
			//按照激活类型得到选择的卡牌列表
			var cards:Array = null;
			switch(activeType){
				case ActiveType.TARGET_GOOD:
					cards = RaceUtil.getLocalPlayer().getSelectedGoods();
					break;
				case ActiveType.TARGET_HAND_CARD:
					cards = RaceUtil.getLocalPlayer().getSelectedCards();
					break;
				case ActiveType.TARGET_PLAYED_CARD:
					cards = RaceUtil.getLocalPlayer().getSelectedPlayedCards();
					break;
			}
			//如果选择的卡牌不为空则传到服务器端
			if(cards!=null && cards.length>0){
				param.cardIds = RaceUtils.card2String(cards);
			}
			sendCommand(param);
		}
		
		/**
		 * 使用卡牌的能力
		 */
		public function useCard(cardId:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "use";
			param.cardId = cardId;
			sendCommand(param);
		}
		
		/**
		 * 选择起始牌组
		 */
		public function selectStartingWorld(startWorldIds:String, handIds:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.startWorldIds = startWorldIds;
			param.handIds = handIds;
			sendCommand(param);
		}
		
		/**
		 * 激活赌博的能力
		 */
		public function doGamble():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "gamble";
			var cards:Array = RaceUtil.getLocalPlayer().getSelectedCards();
			param.cardIds = RaceUtils.card2String(cards);
			sendCommand(param);
		}
		
		/**
		 * 执行赌博,如果选择了pass,则不需要输入cardIds
		 */
		public function gamble(stepCode:String, pass:Boolean, cardIds:String=null):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.stepCode = stepCode;
			if(pass){
				param.subact = "pass";
			}else{
				param.subact = "gamble";
				param.cardIds = cardIds;
			}
			sendCommand(param);
		}
	}
}