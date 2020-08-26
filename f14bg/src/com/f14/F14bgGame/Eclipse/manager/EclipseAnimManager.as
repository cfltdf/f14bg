package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.player.EclipsePlayer;
	import com.f14.F14bgGame.Eclipse.ui.part.EclipseHex;
	import com.f14.F14bgGame.Eclipse.ui.simple.Planet;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	
	import flash.geom.Point;
	
	import mx.core.UIComponent;

	public class EclipseAnimManager extends AnimManager
	{
		public function EclipseAnimManager()
		{
			super();
		}
		
		/**
		 * 处理动画效果的指令
		 */
		override public function processAnimCommand(param:Object):void{
			var animType:String = param.animType;
			var from:Object = param.from;
			var too:Object = param.to;
			var animObject:Object = param.animObject;
			
			var imageObject:UIComponent = this.createAnimObject(animObject);
			if(imageObject!=null){
				//取得起点和终点
				var fp:Point = this.convertPosition(from);
				var tp:Point = this.convertPosition(too);
				if(fp && tp){
					switch(animType){
						case "REVEAL":
							this.reveal(imageObject, fp, tp);
							break;
						default:
							this.move(imageObject, fp, tp);
							break;
					}
				}else if(fp){	//如果没有终点,则判断如下
					switch(animType){
						case "REVEAL_FADEOUT":
							this.revealAndFadeOut(imageObject, fp);
							break;
					}
				}else{
					//如果起点终点都没有,则只会是直接显示在屏幕中
					switch(animType){
						case "SHOW_FADEOUT":
							this.showAndFadeOut(imageObject);
							break;
					}
				}
			}
		}

		/**
		 * 创建动画对象
		 */
		protected function createAnimObject(animObject:Object):UIComponent{
			if(!animObject){
				return null;
			}
			var res:UIComponent = null;
			var obj:Object;
			if(animObject.object=="CARD"){
				//res = InnoUtil.resourceManager.createInnoCard(animObject.id, 0.33);
				//res.showTooltip = false;
			}else if(animObject.object=="CARD_BACK"){
				//res = InnoUtil.resourceManager.createInnoBackCard(animObject.id);
				//res.showTooltip = false;
			}else if(animObject.object=="TITLE"){
				res = EclipseUtil.resourceManager.createActionTitle(animObject.id);
			}else if(animObject.object=="CUBE"){
				res = EclipseUtil.resourceManager.createPopulationCube(animObject.id);
			}else if(animObject.object=="UNIT"){
				obj = {"owner":animObject.id, "unitType":animObject.extend};
				res = EclipseUtil.resourceManager.createUnit(obj);
			}else if(animObject.object=="PLANET"){
				obj = {"owner":animObject.id, "resourceType":animObject.extend};
				var planet:Planet = new Planet();
				planet.setPlanetObject(obj);
				EclipseUtil.animManager.animCanvas.addChild(planet);
				planet.drawComponent();
				res = planet;
			}else if(animObject.object=="DISC"){
				res = EclipseUtil.resourceManager.createInfluenceDisc(animObject.id);
			}else if(animObject.object=="TECHNOLOGY"){
				res = EclipseUtil.resourceManager.createTechnology(animObject.id);
			}else if(animObject.object=="SHIP_PART"){
				res = EclipseUtil.resourceManager.createShipPart(animObject.id);
			}
			return res;
		}
		
		/**
		 * 将参数转换成位置
		 */
		protected function convertPosition(param:Object):Point{
			if(!param){
				return null;
			}
			var point:Point = null;
			switch(param.anim){
				case "PLAYER_BOARD":{	//玩家面板
					var position:int = param.position;
					var player:EclipsePlayer = EclipseUtil.getPlayer(position);
					if(player!=null){
						point = this.getAnimPosition(player.playerInfoBoard);
					}
				}break;
				case "HEX":{	//板块
					var p:String = param.extend;
					var ps:Array = p.split("_");
					var pt:Point = new Point(ps[0], ps[1]);
					var hex:EclipseHex = EclipseUtil.mainBoard.spaceMap.getHex(pt);
					if(hex!=null){
						point = this.getAnimPosition(hex);
					}
				}break;
				case "PUBLIC_BOARD":{	//公共板块
					point = this.getAnimPosition(EclipseUtil.mainBoard.roundPanel.supplyButton);
				}break;
			}
			return point;
		}
		
	}
}