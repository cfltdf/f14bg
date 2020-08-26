package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;
	
	public class EclipseImageManager extends ImageManager
	{
		public function EclipseImageManager()
		{
			super();
		}
		
		protected static var science:Image;
		protected static var COLUMN_SCIENCE:int = 8;
		public static var WIDTH_SCIENCE:int = 181;
		public static var HEIGHT_SCIENCE:int = 179;
		
		protected static var gear:Image;
		protected static var COLUMN_GEAR:int = 9;
		public static var WIDTH_GEAR:int = 162;
		public static var HEIGHT_GEAR:int = 161;
		
		protected static var actionTitle:Image;
		protected static var COLUMN_TITLE:int = 3;
		public static var WIDTH_TITLE:int = 300;
		public static var HEIGHT_TITLE:int = 100;
		
		public var images:Array;
		
		/**
		 * 装载图片资源
		 */
		override public function loadImages():void{
			this.addToLoadList("science.png");
			this.addToLoadList("gear.png");
			this.addToLoadList("actionTitle.png");
			this.loadList();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			science = this.loadingList["science.png"].image;
			gear = this.loadingList["gear.png"].image;
			actionTitle = this.loadingList["actionTitle.png"].image;
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
		/**
		 * 取得科技对应的图片
		 */
		public function getScienceImage(index:int):BitmapData{
			index--;
			var key:String = "SCI"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_SCIENCE * int(index/COLUMN_SCIENCE);
				var left:int = WIDTH_SCIENCE * int(index%COLUMN_SCIENCE);
				bitmapCache[key] = cutOutRect(science, left, top, WIDTH_SCIENCE, HEIGHT_SCIENCE);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得科技对应的图片
		 */
		public function getGearImage(index:int):BitmapData{
			index--;
			var key:String = "GER"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_GEAR * int(index/COLUMN_GEAR);
				var left:int = WIDTH_GEAR * int(index%COLUMN_GEAR);
				bitmapCache[key] = cutOutRect(gear, left, top, WIDTH_GEAR, HEIGHT_GEAR);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得行动标题对应的图片
		 */
		public function getActionTitleImage(index:int):BitmapData{
			index--;
			var key:String = "TIL"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_TITLE * int(index/COLUMN_TITLE);
				var left:int = WIDTH_TITLE * int(index%COLUMN_TITLE);
				bitmapCache[key] = cutOutRect(actionTitle, left, top, WIDTH_TITLE, HEIGHT_TITLE);
			}
			return bitmapCache[key];
		}
		
	}
}