package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;
	
	/**
	 * 图片管理器
	 */
	public class RaceImageManager extends ImageManager
	{
		public function RaceImageManager()
		{
			super();
		}
		
		//[Embed(source="images/layout.jpg")]
		//private static var IMG_1:Class;
		private static var img1:Image;
		//[Embed(source="images/layout2.jpg")]
		//private static var IMG_3:Class;
		private static var img3:Image;
		
		//[Embed(source="images/goals.jpg")]
		//private static var IMG_2:Class;
		private static var img2:Image;
		
		//[Embed(source="images/manual1.jpg")]
		//public static var manual1:Class;
		//[Embed(source="images/manual2.jpg")]
		//public static var manual2:Class;
		
		public static var WIDTH:int = 251;
		public static var HEIGHT:int = 350;
		public static var COLUMN:int = 10;
		public static var DEFAULT_SCALE:Number = 0.25;
		public static var IMAGE_INDEX_BACK:int = 115;
		public static var IMAGE_INDEX_ACTION:int = 120;
		
		public static var WIDTH_GOAL:int = 128;
		public static var HEIGHT_GOAL:int = 128;
		public static var COLUMN_GOAL:int = 4;
		
		/**
		 * 初始化图片
		 */
		override public function loadImages():void{
			this.addToLoadList("layout.jpg");
			this.addToLoadList("goals.jpg");
			this.addToLoadList("layout2.jpg");
			this.loadList();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			img1 = this.loadingList["layout.jpg"].image;
			img2 = this.loadingList["goals.jpg"].image;
			img3 = this.loadingList["layout2.jpg"].image;
			
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
		/**
		 * 取得卡牌对应的图片
		 */
		public function getCardImage(index:int):BitmapData{
			var key:String = "CRD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = 0;
				var left:int = 0;
				if(index<=200){
					top = HEIGHT * int((index-1)/COLUMN);
					left = WIDTH * int((index-1)%COLUMN);
					bitmapCache[key] = cutOutRect(img1, left, top, WIDTH, HEIGHT);
				}else{
					top = HEIGHT * int((index-201)/COLUMN);
					left = WIDTH * int((index-201)%COLUMN);
					bitmapCache[key] = cutOutRect(img3, left, top, WIDTH, HEIGHT);
				}
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得卡牌的背面
		 */
		public function getCardBack():BitmapData{
			return getCardImage(IMAGE_INDEX_BACK);
		}
		
		/**
		 * 取得目标对应的图片
		 */
		public function getGoalImage(index:int):BitmapData{
			var key:String = "GOL"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_GOAL * int(index/COLUMN_GOAL);
				var left:int = WIDTH_GOAL * int(index%COLUMN_GOAL);
				bitmapCache[key] = cutOutRect(img2, left, top, WIDTH_GOAL, HEIGHT_GOAL);
			}
			return bitmapCache[key];
		}
	}
}