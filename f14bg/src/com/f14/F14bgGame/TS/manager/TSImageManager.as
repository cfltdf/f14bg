package com.f14.F14bgGame.TS.manager
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;
	
	public class TSImageManager extends ImageManager
	{
		public function TSImageManager()
		{
			super();
		}
		
		protected static var imgCards:Image;
		protected static var COLUMN_CARD:int = 10;
		public static var WIDTH_CARD:int = 270;
		public static var HEIGHT_CARD:int = 395;
		
		protected static var imgLabels:Image;
		protected static var COLUMN_LABEL:int = 1;
		public static var WIDTH_LABEL:int = 120;
		public static var HEIGHT_LABEL:int = 20;
		
		protected static var imgStars:Image;
		protected static var COLUMN_STAR:int = 1;
		public static var WIDTH_STAR:int = 30;
		public static var HEIGHT_STAR:int = 29;
		
		protected static var imgFlags:Image;
		protected static var COLUMN_FLAG:int = 1;
		public static var WIDTH_FLAG:int = 150;
		public static var HEIGHT_FLAG:int = 70;
		
		/**
		 * 卡牌背面的序号
		 */
		public static var CARD_BACK_INDEX:int = 111;
		
		public var images:Array;
		
		/**
		 * 装载图片资源
		 */
		override public function loadImages():void{
			this.addToLoadList("cards.jpg");
			this.addToLoadList("labels.png");
			this.addToLoadList("stars.png");
			this.addToLoadList("flags.png");
			this.loadList();
			
			/*imgCards = loadImage("cards.jpg");
			imgLabels = loadImage("labels.png");
			imgStars = loadImage("stars.png");
			imgFlags = loadImage("flags.png");*/
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			imgCards = this.loadingList["cards.jpg"].image;
			imgLabels = this.loadingList["labels.png"].image;
			imgFlags = this.loadingList["flags.png"].image;
			imgStars = this.loadingList["stars.png"].image;
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
		 * 取得卡牌背面
		 */
		public function getBackImage():BitmapData{
			return this.getCardImage(CARD_BACK_INDEX);
		}
		
		/**
		 * 取得标签对应的图片
		 */
		public function getLabelImage(index:int):BitmapData{
			var key:String = "LAB"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_LABEL * int(index/COLUMN_LABEL);
				var left:int = WIDTH_LABEL * int(index%COLUMN_LABEL);
				bitmapCache[key] = cutOutRect(imgLabels, left, top, WIDTH_LABEL, HEIGHT_LABEL);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得星星对应的图片
		 */
		public function getStarImage(index:int):BitmapData{
			var key:String = "STR"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_STAR * int(index/COLUMN_STAR);
				var left:int = WIDTH_STAR * int(index%COLUMN_STAR);
				bitmapCache[key] = cutOutRect(imgStars, left, top, WIDTH_STAR, HEIGHT_STAR);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得旗帜对应的图片
		 */
		public function getFlagImage(index:int):BitmapData{
			var key:String = "FLG"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_FLAG * int(index/COLUMN_FLAG);
				var left:int = WIDTH_FLAG * int(index%COLUMN_FLAG);
				bitmapCache[key] = cutOutRect(imgFlags, left, top, WIDTH_FLAG, HEIGHT_FLAG);
			}
			return bitmapCache[key];
		}
	}
}