package com.f14.F14bgGame.PuertoRico.manager
{
	import com.adobe.serialization.json.JSON;
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.bg.manager.GameActionManager;
	
	public class PrActionManager extends GameActionManager
	{
		public function PrActionManager()
		{
			super();
		}
		
		/**
		 * 选择角色卡
		 */
		public function doChooseCharacter(cardId:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.cardId = cardId;
			sendCommand(param);
		}
		
		/**
		 * 拓荒,选择板块
		 */
		public function settle(quarry:Boolean, id:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "settle";
			param.id = id;
			param.quarry = quarry;
			sendCommand(param);
		}
		
		/**
		 * 建筑师,建造建筑
		 */
		public function build(cardNo:String, blackParam:Object):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "build";
			param.cardNo = cardNo;
			param.blackString = JSON.encode(blackParam);
			sendCommand(param);
		}
		
		/**
		 * 市长,分配移民
		 */
		public function major():void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			//取得本地玩家的移民分配情况
			var obj:Object = PRUtil.getLocalPlayer().getColonistInfo();
			param.subact = "major";
			param.ids = obj.ids;
			param.nums = obj.nums;
			param.restNum = obj.restNum;
			sendCommand(param);
		}
		
		/**
		 * 工匠,额外生产货物
		 */
		public function craftsman(resource:Object):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "produce";
			param.resourceString = JSON.encode(resource);
			sendCommand(param);
		}
		
		/**
		 * 商人,出售货物
		 */
		public function trader(goodType:String, selfTrade:Boolean):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "trade";
			param.goodType = goodType;
			param.selfTrade = selfTrade;
			sendCommand(param);
		}
		
		/**
		 * 船长,装货
		 */
		public function captain(goodType:String, shipSize:int):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "ship";
			param.goodType = goodType;
			param.shipSize = shipSize;
			sendCommand(param);
		}
		
		/**
		 * 船长阶段结束,选择保留货物
		 */
		public function captainEnd(resource:Object, goodTypeGroup:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.resourceString = JSON.encode(resource);
			param.goodTypeGroup = goodTypeGroup;
			sendCommand(param);
		}
		
		/**
		 * 选择建筑
		 */
		public function chooseBuilding(cardNo:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.cardNo = cardNo;
			sendCommand(param);
		}
		
		/**
		 * 小码头装货
		 */
		public function smallShip(resource:Object):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "ship";
			param.resourceString = JSON.encode(resource);
			param.shipSize = -1;
			sendCommand(param);
		}
	}
}