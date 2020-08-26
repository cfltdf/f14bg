package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.F14bgGame.PuertoRico.PRUtil;
	import com.f14.F14bgGame.PuertoRico.player.PrPlayer;
	import com.f14.F14bgGame.bg.manager.GameManager;
	import com.f14.core.util.ApplicationUtil;

	public class PrGameManager extends GameManager
	{
		public function PrGameManager()
		{
			super();
		}
		
		/**
		 * 游戏开始时的设置
		 */
		override public function setupGame(param:Object):void{
			super.setupGame(param);
			//设置角色选择面板
			PRUtil.stateManager.chooseCharacterBoard.initCharacterCards(param.characterCards);
			//设置货船面板
			PRUtil.mainBoard.shipBoard.clear();
			for each(var obj:Object in param.ships){
				PRUtil.mainBoard.shipBoard.addShip(obj);
			}
		}
		
		/**
		 * 装载当前游戏状态
		 */
		override public function loadPlayingInfo(param:Object):void{
			var id:String;
			var str:String;
			var obj:Object;
			for each(var p:Object in param.players){
				var player:PrPlayer = PRUtil.getPlayer(p.position);
				player.doubloon = p.doubloon;
				player.vp = p.vp;
				//添加建筑板块
				str = p.buildings;
				if(str){
					var buildingIds:Array = str.split(",");
					for each(id in buildingIds){
						obj = PRUtil.resourceManager.getObject(id);
						player.prPlayerBoard.playerBuildingBoard.addBuilding(obj);
					}
				}
				//添加种植园板块
				str = p.plantations;
				if(str){
					var plantationIds:Array = str.split(",");
					for each(id in plantationIds){
						obj = PRUtil.resourceManager.getObject(id);
						player.prPlayerBoard.playerBuildingBoard.addPlantation(obj);
					}
				}
				//玩家资源
				obj = p.resources;
				player.setResource(obj);
			}
		}
		
	}
}