package com.f14.F14bgGame.Eclipse.player
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.consts.EclipseGameCmd;
	import com.f14.F14bgGame.bg.consts.ListenerType;
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.f14bg.consts.CmdConst;

	public class EclipsePlayerHandler extends InGameHandler
	{
		public function EclipsePlayerHandler()
		{
			super();
		}
		
		/**
		 * 执行游戏指令
		 */
		protected override function executeGameCommand(param:Object):void{
			super.executeGameCommand(param);
			var code:int = param.code;
			//设置下currentCode
			switch(code){
				case CmdConst.GAME_CODE_START_LISTEN: //玩家行动开始
					if(param.listenerType==ListenerType.INTERRUPT){
						//中断型监听器
						this.currentCode = param.validCode;
					}
					break;
			}
		}
		
		/**
		 * 处理游戏指令
		 */
		override protected function processGameCmd(param:Object):void{
			var player:EclipsePlayer = EclipseUtil.getPlayer(param.position);
			switch(param.code){
				case EclipseGameCmd.GAME_CODE_SPACEMAP_INFO:	//装载地图信息
					EclipseUtil.mainBoard.spaceMap.loadParam(param.spaceMap);
					break;
				case EclipseGameCmd.GAME_CODE_TECHNOLOGY_INFO:	//装载公共科技板块的信息
					EclipseUtil.stateManager.publicWindow.loadTechnologyInfo(param.techs);
					break;
				case EclipseGameCmd.GAME_CODE_SHIPPART_INFO:	//装载公共飞船配件的信息
					EclipseUtil.stateManager.publicWindow.loadShipPartInfo(param.shipParts);
					break;
				case EclipseGameCmd.GAME_CODE_BASEGAME_INFO:	//装载游戏基本信息
					EclipseUtil.mainBoard.roundPanel.loadRoundParam(param);
					EclipseUtil.stateManager.publicWindow.loadHexInfo(param.hexDecks);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_RACE:	//刷新玩家的种族信息
					player.loadRaceParam(param);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_BASE:	//刷新玩家的基本信息
					player.loadCostParam(param.base.costs);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_RESOURCE:	//刷新玩家的资源信息
					player.loadResourceParam(param.resource);
					//player.loadPartParam(param.resource.parts);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_TECHNOLOGY:	//刷新玩家的科技信息
					player.loadTechnologyParam(param.technology.technology);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_REPUTATIONTRACK:	//刷新玩家影响力轨道信息
					player.loadReputationTrackParam(param.reputationTrack);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_BLUEPRINT:	//刷新玩家蓝图的信息
					player.loadBlueprintParam(param.blueprint);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_STATUS:	//刷新玩家蓝图的信息
					player.loadStatusParam(param.status);
					break;
				case EclipseGameCmd.GAME_CODE_REFRESH_HEX:	//刷新地图板块信息
					EclipseUtil.mainBoard.spaceMap.refreshHex(param.hex);
					break;
				case EclipseGameCmd.GAME_CODE_PLAYER_ADD_TECHNOLOGY:	//玩家得到科技
					player.loadAddTechnologyParam(param.technology);
					break;
			}
		}
	}
}