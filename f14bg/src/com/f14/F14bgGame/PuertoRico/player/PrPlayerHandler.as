package com.f14.F14bgGame.PuertoRico.player
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.consts.GameCmdConst;
	import com.f14.F14bgGame.PuertoRico.ui.simple.ShipTile;
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.core.util.ApplicationUtil;
	

	public class PrPlayerHandler extends InGameHandler
	{
		public function PrPlayerHandler()
		{
			super();
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			switch(param.code){
				case GameCmdConst.GAME_CODE_REFRESH_CHARACTER: //刷新角色卡
					PRUtil.stateManager.chooseCharacterBoard.refreshAll(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_PLANTATIONS: //刷新种植园
					this.refreshPlantations(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_PART: //刷新配件数量
					this.refreshParts(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_SHIP: //刷新货船
					this.refreshShips(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_BUILDING: //刷新建筑面板
					this.refreshBuildings(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_TRADEHOUSE: //刷新交易所
					this.refreshTradeHouse(param);
					break;
				case GameCmdConst.GAME_CODE_REFRESH_PLAYER_RESOURCE: //刷新玩家的资源
					this.refreshPlayerResource(param);
					break;
				case GameCmdConst.GAME_CODE_GET_SUPPLY_PART: //公共资源得到配件
					this.getSupplyParts(param);
					break;
				case GameCmdConst.GAME_CODE_GET_DOUBLOON: //玩家得到金币
					this.getDoubloon(param);
					break;
				case GameCmdConst.GAME_CODE_GET_VP: //玩家得到VP
					this.getVP(param);
					break;
				case GameCmdConst.GAME_CODE_GET_PART: //玩家得到配件
					this.getParts(param);
					break;
				case GameCmdConst.GAME_CODE_GET_PLANTATION: //玩家得到种植园板块
					this.getPlantation(param);
					break;
				case GameCmdConst.GAME_CODE_REMOVE_PLANTATION: //移除种植园板块
					this.removePlantation(param);
					break;
				case GameCmdConst.GAME_CODE_GET_BUILDING: //玩家得到建筑
					this.getBuilding(param);
					break;
				case GameCmdConst.GAME_CODE_REMOVE_BUILDING_SUPPLY: //移除建筑板块
					this.removeBuilding(param);
					break;
				case GameCmdConst.GAME_CODE_COLONIST_INFO: //刷新玩家的移民分配情况
					this.refreshColonistInfo(param);
					break;
				case GameCmdConst.GAME_CODE_GET_TRADEHOUSE: //交易所得到货物
					this.tradeHouseGetGood(param);
					break;
				case GameCmdConst.GAME_CODE_GET_SHIP: //货船得到货物
					this.shipGood(param);
					break;
				case GameCmdConst.GAME_CODE_COMMON_CONFIRM: //通用确认窗口
					PRUtil.stateManager.trigCommonConfirm(param);
					break;
				case GameCmdConst.GAME_CODE_SHOW_ACTION: //刷新玩家的行动阶段
					this.refreshPlayerAction(param);
					break;
				case GameCmdConst.GAME_CODE_CHOOSE_BUILDING: //玩家选择建筑
					PRUtil.stateManager.chooseBuildingWindow.chooseBuilding(param.userName, param.cardNo);
					break;
			}
		}
		
		/**
		 * 刷新种植园板块
		 */
		protected function refreshPlantations(param:Object):void{
			PRUtil.mainBoard.plantationBoard.clear();
			var cardIds:Array = param.cardIds.split(",");
			for each(var id:String in cardIds){
				var obj:Object = PRUtil.resourceManager.getObject(id);
				if(obj!=null){
					PRUtil.mainBoard.plantationBoard.addPlantation(obj);
				}
			}
		}
		
		/**
		 * 刷新配件数量
		 */
		protected function refreshParts(param:Object):void{
			PRUtil.mainBoard.partBoard.setParts(param.parts);
		}
		
		/**
		 * 刷新货船信息
		 */
		protected function refreshShips(param:Object):void{
			for each(var obj:Object in param.ships){
				PRUtil.mainBoard.shipBoard.updateShip(obj);
			}
		}
		
		/**
		 * 刷新建筑面板
		 */
		protected function refreshBuildings(param:Object):void{
			PRUtil.stateManager.buildingBoard.loadBuildings(param);
		}
		
		/**
		 * 刷新交易所
		 */
		protected function refreshTradeHouse(param:Object):void{
			PRUtil.mainBoard.tradeHouseBoard.clear();
			if(param.goods){
				var goods:Array = param.goods.split(",");
				for each(var good:String in goods){
					PRUtil.mainBoard.tradeHouseBoard.addGood(good);
				}
			}
		}
		
		/**
		 * 刷新玩家资源
		 */
		protected function refreshPlayerResource(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			player.setResource(param.resources);
		}
		
		/**
		 * 公共资源得到配件
		 */
		protected function getSupplyParts(param:Object):void{
			PRUtil.mainBoard.partBoard.addParts(param.parts);
		}
		
		/**
		 * 玩家得到金币
		 */
		protected function getDoubloon(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			player.doubloon += param.doubloon;
		}
		
		/**
		 * 玩家得到VP
		 */
		protected function getVP(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			player.vp += param.vp;
			PRUtil.mainBoard.partBoard.vp = param.totalVP;
		}
		
		/**
		 * 玩家得到资源
		 */
		protected function getParts(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			player.addResource(param.parts);
		}
		
		/**
		 * 玩家得到种植园
		 */
		protected function getPlantation(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			var obj:Object = PRUtil.resourceManager.getObject(param.id);
			player.addPlantation(obj);
		}
		
		/**
		 * 移除种植园,由参数position决定移除的是公共区还是玩家的种植园
		 */
		protected function removePlantation(param:Object):void{
			if(param.position<0){
				//移除公共区的种植园板块
				PRUtil.mainBoard.plantationBoard.removePlantation(param.id);
			}else{
				//移除玩家的种植园板块
				var player:PrPlayer = PRUtil.getPlayer(param.position);
				player.removePlantation(param.id);
			}
		}
		
		/**
		 * 玩家得到建筑
		 */
		protected function getBuilding(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			var obj:Object = PRUtil.resourceManager.getObject(param.id);
			player.addBuilding(obj);
		}
		
		/**
		 * 公共区移除建筑
		 */
		protected function removeBuilding(param:Object):void{
			//从公共区拿取建筑,当建筑拿光时才从公共区移除
			PRUtil.stateManager.buildingBoard.takeBuilding(param.cardNo);
		}
		
		/**
		 * 刷新玩家的移民分配情况
		 */
		protected function refreshColonistInfo(param:Object):void{
			var player:PrPlayer = PRUtil.getPlayer(param.position);
			player.loadColonistInfo(param);
		}
		
		/**
		 * 交易所得到货物
		 */
		protected function tradeHouseGetGood(param:Object):void{
			PRUtil.mainBoard.tradeHouseBoard.addGood(param.goodType);
		}
		
		/**
		 * 货船得到货物
		 */
		protected function shipGood(param:Object):void{
			var ship:ShipTile = PRUtil.mainBoard.shipBoard.getShip(param.shipSize);
			if(ship!=null){
				ship.goodType = param.goodType;
				ship.size += param.goodNum;
			}
		}
		
		/**
		 * 刷新玩家的行动信息
		 */
		protected function refreshPlayerAction(param:Object):void{
			for each(var obj:Object in param.players){
				var player:PrPlayer = PRUtil.getPlayer(obj.position);
				player.governor = obj.governor;
				player.character = obj.character;
				player.currentRound = obj.currentRound;
			}
		}
	}
}