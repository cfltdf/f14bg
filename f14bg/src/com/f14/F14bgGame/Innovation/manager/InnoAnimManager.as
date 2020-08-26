package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.components.DrawDeckCard;
	import com.f14.F14bgGame.Innovation.player.InnoPlayer;
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.F14bgGame.bg.manager.AnimManager;
	
	import flash.geom.Point;

	public class InnoAnimManager extends AnimManager
	{
		public function InnoAnimManager()
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
			
			var imageObject:ImageObject = this.createAnimObject(animObject);
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
				}
			}
		}
		
		/**
		 * 创建动画对象
		 */
		protected function createAnimObject(animObject:Object):ImageObject{
			if(!animObject){
				return null;
			}
			var res:ImageObject = null;
			if(animObject.object=="CARD"){
				res = InnoUtil.resourceManager.createInnoCard(animObject.id, 0.33);
				res.showTooltip = false;
			}else if(animObject.object=="CARD_BACK"){
				res = InnoUtil.resourceManager.createInnoBackCard(animObject.id);
				res.showTooltip = false;
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
				case "DRAW_DECK":{	//摸牌堆
					var level:int = param.extend;
					var drawDeck:DrawDeckCard = InnoUtil.mainBoard.drawDeckPart.getDeck(level);
					if(drawDeck!=null){
						point = this.getAnimPosition(drawDeck);
					}
				}break;
				case "PLAYER_HANDS":{	//玩家手牌
					var position:int = param.position;
					var player:InnoPlayer = InnoUtil.getPlayer(position);
					if(player!=null){
						point = this.getAnimPosition(player.playerTable.hand_cards_info);
					}
				}break;
				case "PLAYER_SCORES":{	//玩家计分区
					var position:int = param.position;
					var player:InnoPlayer = InnoUtil.getPlayer(position);
					if(player!=null){
						point = this.getAnimPosition(player.playerTable.score_cards_info);
					}
				}break;
				case "PLAYER_STACKS":{	//玩家已打出的牌堆
					var position:int = param.position;
					var color:String = param.extend;
					var player:InnoPlayer = InnoUtil.getPlayer(position);
					if(player!=null){
						point = this.getAnimPosition(player.playerTable.getCardStack(color));
					}
				}break;
				case "ACHIEVE_DECK":{	//成就牌堆
					var level:int = param.extend;
					var p:Point = InnoUtil.mainBoard.normalAchieveDeck.getDeckPosition(level);
					point = this.getAnimPositionByPoint(InnoUtil.mainBoard.normalAchieveDeck, p);
				}break;
				case "SPECIAL_ACHIEVE_DECK":{	//特殊成就牌堆
					point = this.getAnimPosition(InnoUtil.mainBoard.achieveCardsDeck);
				}break;
				case "PLAYER_ACHIEVES":{	//玩家已打出的牌堆
					var position:int = param.position;
					var player:InnoPlayer = InnoUtil.getPlayer(position);
					if(player!=null){
						point = this.getAnimPosition(player.playerTable.playerAchieve);
					}
				}break;
			}
			return point;
		}
		
	}
}