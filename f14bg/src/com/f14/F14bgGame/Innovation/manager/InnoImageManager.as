package com.f14.F14bgGame.Innovation.manager
{
	import com.f14.F14bgGame.Innovation.InnoUtil;
	import com.f14.F14bgGame.Innovation.consts.InnoColor;
	import com.f14.F14bgGame.Innovation.consts.InnoIcon;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;
	
	public class InnoImageManager extends ImageManager
	{
		public function InnoImageManager()
		{
			super();
		}
		
		protected static var imgCards:Image;
		protected static var COLUMN_CARD:int = 10;
		public static var WIDTH_CARD:int = 350;
		public static var HEIGHT_CARD:int = 247;
		
		protected static var imgBack:Image;
		protected static var COLUMN_BACK:int = 10;
		public static var WIDTH_BACK:int = 247;
		public static var HEIGHT_BACK:int = 350;
		
		protected static var imgLabels:Image;
		protected static var COLUMN_LABEL:int = 10;
		public static var WIDTH_LABEL:int = 120;
		public static var HEIGHT_LABEL:int = 55;
		
		protected static var imgIcons:Image;
		protected static var COLUMN_ICON:int = 10;
		public static var WIDTH_ICON:int = 83;
		public static var HEIGHT_ICON:int = 83;
		
		/**
		 * 卡牌背面的序号
		 */
		public static var CARD_BACK_INDEX:int = 110;
		
		public var images:Array;
		
		/**
		 * 装载图片资源
		 */
		override public function loadImages():void{
			this.addToLoadList("cards.jpg");
			this.addToLoadList("back.jpg");
			this.addToLoadList("labels.png");
			this.addToLoadList("icons.png");
			this.loadList();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			imgCards = this.loadingList["cards.jpg"].image;
			imgBack = this.loadingList["back.jpg"].image;
			imgLabels = this.loadingList["labels.png"].image;
			imgIcons = this.loadingList["icons.png"].image;
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
		/**
		 * 取得卡牌对应的图片
		 */
		public function getCardImage(index:int):BitmapData{
			index--;
			var key:String = "CRD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_CARD * int(index/COLUMN_CARD);
				var left:int = WIDTH_CARD * int(index%COLUMN_CARD);
				bitmapCache[key] = cutOutRect(imgCards, left, top, WIDTH_CARD, HEIGHT_CARD);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得指定等级的卡牌背面
		 */
		public function getBackImage(level:int):BitmapData{
			level--;
			var key:String = "BAK"+String(level);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_BACK * int(level/COLUMN_BACK);
				var left:int = WIDTH_BACK * int(level%COLUMN_BACK);
				bitmapCache[key] = cutOutRect(imgBack, left, top, WIDTH_BACK, HEIGHT_BACK);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得颜色对应的背景图片
		 */
		public function getLabelImage(color:String):BitmapData{
			var key:String = "LAB"+color;
			var index:int = this.getLabelIndex(color);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_LABEL * int(index/COLUMN_LABEL);
				var left:int = WIDTH_LABEL * int(index%COLUMN_LABEL);
				bitmapCache[key] = cutOutRect(imgLabels, left, top, WIDTH_LABEL, HEIGHT_LABEL);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得color对应的label序号
		 */
		protected function getLabelIndex(color:String):int{
			switch(color){
				case InnoColor.RED:
					return 0;
				case InnoColor.YELLOW:
					return 1;
				case InnoColor.GREEN:
					return 2;
				case InnoColor.BLUE:
					return 3;
				case InnoColor.PURPLE:
					return 4;
			}
			return -1;
		}
		
		/**
		 * 取得旗帜对应的图片
		 */
		public function getIconImage(icon:String):BitmapData{
			var key:String = "ICO"+icon;
			var index:int = this.getIconIndex(icon);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_ICON * int(index/COLUMN_ICON);
				var left:int = WIDTH_ICON * int(index%COLUMN_ICON);
				bitmapCache[key] = cutOutRect(imgIcons, left, top, WIDTH_ICON, HEIGHT_ICON);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得color对应的label序号
		 */
		protected function getIconIndex(icon:String):int{
			switch(icon){
				case InnoIcon.CASTLE:
					return 0;
				case InnoIcon.LEAF:
					return 1;
				case InnoIcon.CROWN:
					return 2;
				case InnoIcon.LAMP:
					return 3;
				case InnoIcon.FACTORY:
					return 4;
				case InnoIcon.CLOCK:
					return 5;
				case InnoIcon.EMPTY:
					return 6;
			}
			return -1;
		}
		
		/**
		 * 取得卡牌对应的图片
		 */
		public function getCardImageById(cardId:String):BitmapData{
			var card:Object = InnoUtil.resourceManager.getObject(cardId);
			if(card!=null){
				return this.getCardImage(card.imageIndex);
			}else{
				return null
			}
		}
		
	}
}