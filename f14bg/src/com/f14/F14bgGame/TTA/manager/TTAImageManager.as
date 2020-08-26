package com.f14.F14bgGame.TTA.manager
{
	import com.f14.F14bgGame.TTA.consts.CardType;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;

	public class TTAImageManager extends ImageManager
	{
		public function TTAImageManager()
		{
			super();
		}
		
		[Embed(source="./com/f14/F14bgGame/TTA/images/blue.png")]
		public static var BLUE_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/yellow.png")]
		public static var YELLOW_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/white.png")]
		public static var WHITE_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/red.png")]
		public static var RED_IMAGE:Class;
		
		[Embed(source="./com/f14/F14bgGame/TTA/images/tokenp1.png")]
		public static var P1_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/tokenp2.png")]
		public static var P2_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/tokenp3.png")]
		public static var P3_IMAGE:Class;
		[Embed(source="./com/f14/F14bgGame/TTA/images/tokenp4.png")]
		public static var P4_IMAGE:Class;
		
		//[Embed(source="./com/f14/F14bgGame/TTA/images/cards1.jpg")]
		//protected static var IMG_CARDS:Class;
		protected static var imgCards:Image;
		protected static var COLUMN_CARD:int = 10;
		public static var WIDTH_CARD:int = 180;
		public static var HEIGHT_CARD:int = 270;
		/**
		 * 背景图片对应的index
		 */
		protected static var backImage:Array = [
			{"CIVIL":33, "MILITARY":34},
			{"CIVIL":104, "MILITARY":105},
			{"CIVIL":248, "MILITARY":249},
			{"CIVIL":250, "MILITARY":251}
		];
		
		//[Embed(source="./com/f14/F14bgGame/TTA/images/ttalabel.png")]
		//protected static var IMG_LABELS:Class;
		protected static var imgLabels:Image;
		protected static var COLUMN_LABEL:int = 1;
		public static var WIDTH_LABEL:int = 105;
		public static var HEIGHT_LABEL:int = 20;
		
		/**
		 * 初始化图片
		 */
		override public function loadImages():void{
			this.addToLoadList("cards1.jpg");
			this.addToLoadList("ttalabel.png");
			this.loadList();
			
			//imgCards = loadImage("cards1.jpg");
			//imgLabels = loadImage("ttalabel.png");
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			imgCards = this.loadingList["cards1.jpg"].image;
			imgLabels = this.loadingList["ttalabel.png"].image;
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
		/**
		 * 取得卡牌对应的图片
		 */
		public function getCardImage(index:int):BitmapData{
			var key:String = "CRD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_CARD * int(index/COLUMN_CARD);
				var left:int = WIDTH_CARD * int(index%COLUMN_CARD);
				bitmapCache[key] = cutOutRect(imgCards, left, top, WIDTH_CARD, HEIGHT_CARD);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得卡牌背面
		 */
		public function getBackImage(level:int, type:String):BitmapData{
			var i:int = backImage[level][type];
			return this.getCardImage(i);
		}
		
		/**
		 * 取得标签的背景图
		 */
		public function getLabelImage(cardType:String):BitmapData{
			var index:int = this.getLabelIndex(cardType);
			var key:String = "LAB"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_LABEL * int(index/COLUMN_LABEL);
				var left:int = WIDTH_LABEL * int(index%COLUMN_LABEL);
				bitmapCache[key] = cutOutRect(imgLabels, left, top, WIDTH_LABEL, HEIGHT_LABEL);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 按照cardType取得对应index
		 */
		protected function getLabelIndex(cardType:String):int{
			switch(cardType){
				case CardType.GOVERMENT:
					return 0;
				case CardType.LEADER:
					return 1;
				case CardType.WONDER:
					return 2;
				case CardType.PRODUCTION:
					return 3;
				case CardType.BUILDING:
					return 4;
				case CardType.UNIT:
					return 5;
				case CardType.SPECIAL:
					return 6;
				case CardType.EVENT:
					return 7;
				case CardType.TACTICS:
					return 8;
				case CardType.PACT:
					return 9;
				case CardType.WAR:
					return 10;
			}
			return 0;
		}
		
		/**
		 * 按照玩家位置取得对应的标志物
		 */
		public function getPlayerTokenImage(position:int):Class{
			switch(position){
				case 0:
					return P1_IMAGE;
				case 1:
					return P2_IMAGE;
				case 2:
					return P3_IMAGE;
				case 3:
					return P4_IMAGE;
				default:
					return null;
			}
		}
	}
}