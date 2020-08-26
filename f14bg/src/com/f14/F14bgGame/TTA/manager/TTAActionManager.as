package com.f14.F14bgGame.TTA.manager
{
	import com.f14.F14bgGame.bg.handler.InGameHandler;
	import com.f14.F14bgGame.bg.manager.GameActionManager;
	import com.f14.F14bgGame.TTA.consts.TTAGameCmd;
	import com.f14.F14bgGame.TTA.player.TTAPlayerHandler;

	public class TTAActionManager extends GameActionManager
	{
		public function TTAActionManager()
		{
			super();
		}
		
		/**
		 * 玩家拿取内政牌
		 */
		public function takeCard(cardId:String):void{
			var code:int = handler.currentCode;
			var param:Object = createGameCommand(code);
			param.subact = "ACTION_TAKE_CARD";
			param.cardId = cardId;
			sendCommand(param);
		}
		
		/**
		 * 玩家打出手牌
		 */
		public function playCard(cardId:String, param:Object = null):void{
			if(param==null){
				param = {};
			}
			param.cardId = cardId;
			this.sendCurrentCommand("ACTION_PLAY_CARD", param);
		}
		
		/**
		 * 发送当前阶段的指令
		 */
		/*public function sendCurrentCommand(subact:String, param:Object = null):void{
			var code:int = handler.currentCode;
			var p:Object = createGameCommand(code);
			p.subact = subact;
			if(param!=null){
				for(var key:String in param){
					p[key] = param[key];
				}
			}
			this.sendCommand(p);
		}*/
		
		/**
		 * 玩家增加人口
		 */
		public function increasePopulation():void{
			this.sendCurrentCommand("ACTION_POPULATION");
		}
		
		/**
		 * 发送建筑的请求
		 */
		public function requestBuild():void{
			this.sendCurrentCommand("REQUEST_BUILD");
		}
		
		/**
		 * 发送建筑升级的请求
		 */
		public function requestUpgrade():void{
			this.sendCurrentCommand("REQUEST_UPGRADE");
		}
		
		/**
		 * 发送摧毁建筑的请求
		 */
		public function requestDestory():void{
			this.sendCurrentCommand("REQUEST_DESTORY");
		}
		
		/**
		 * 发送摧毁建筑的请求
		 */
		public function changeGoverment(param:Object):void{
			this.sendCurrentCommand("ACTION_CHANGE_GOVERMENT", param);
		}
		
		/**
		 * 发送建造奇迹(多步骤)的请求
		 */
		public function buildWonderStep(param:Object):void{
			this.sendCurrentCommand("ACTION_BUILD", param);
		}
		
		/**
		 * 结束政治行动阶段
		 */
		public function passPolitical():void{
			this.sendCurrentCommand("POLITICAL_PASS");
		}
		
		/**
		 * 体面退出游戏
		 */
		public function resign():void{
			this.sendCurrentCommand("RESIGN");
		}
		
		/**
		 * 结束政治行动阶段
		 */
		public function discardMilitary(cardIds:String):void{
			var p:Object = createGameCommand(TTAGameCmd.GAME_CODE_DISCARD_MILITARY);
			p.cardIds = cardIds;
			p.stepCode = "POLITICAL_DISCARD";
			this.sendCommand(p);
		}
		
		/**
		 * 使用卡牌的能力
		 */
		public function activeCard(cardId:String):void{
			var param:Object = {};
			param.cardId = cardId;
			this.sendCurrentCommand("ACTION_ACTIVE_CARD", param);
		}
		
		/**
		 * 发送中止条约的请求
		 */
		public function requestBreakPact():void{
			this.sendCurrentCommand("REQUEST_BREAK_PACT");
		}
		
	}
}