package com.f14.F14bgGame.Tichu.manager
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;

	public class TichuImageManager extends ImageManager
	{
		public function TichuImageManager()
		{
			super();
		}
		
		protected static var imgCards:Image;
		protected static var COLUMN_CARD:int = 13;
		public static var WIDTH_CARD:int = 84;
		public static var HEIGHT_CARD:int = 128;
		public static var WIDTH_CARD_REAL:int = WIDTH_CARD * 1;
		public static var HEIGHT_CARD_REAL:int = HEIGHT_CARD * 1;
		
		/**
		 * 卡牌背面的序号
		 */
		public static var CARD_BACK_INDEX:int = 56;
		
		/**
		 * 初始化图片
		 */
		override public function loadImages():void{
			//imgCards = loadImage("cards.jpg");
			this.addToLoadList("cards.jpg");
			this.loadList();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			imgCards = this.loadingList["cards.jpg"].image;
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
		public function getBackImage():BitmapData{
			return this.getCardImage(CARD_BACK_INDEX);
		}
		
	}
}