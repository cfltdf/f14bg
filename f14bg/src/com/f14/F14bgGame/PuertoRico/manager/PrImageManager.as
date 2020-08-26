package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.f14bg.manager.ImageManager;
	
	import flash.display.BitmapData;
	
	import mx.controls.Image;
	
	/**
	 * 图片管理器
	 */
	public class PrImageManager extends ImageManager
	{
		public function PrImageManager()
		{
			super();
		}
		
		//[Embed(source="images/all_building.jpg")]
		//private static var IMG_BUILDING:Class;
		private static var imgBuilding:Image;
		public static var WIDTH_BUILDING:int = 211;
		public static var HEIGHT_BUILDING:int = 123;
		public static var COLUMN_BUILDING:int = 7;
		
		//[Embed(source="images/all_plantation.jpg")]
		//private static var IMG_PLANTATION:Class;
		private static var imgPlantation:Image;
		public static var WIDTH_PLANTATION:int = 132;
		public static var HEIGHT_PLANTATION:int = 132;
		
		//[Embed(source="images/all_card.jpg")]
		//private static var IMG_CARD:Class;
		private static var imgCard:Image;
		public static var WIDTH_CARD:int = 280;
		public static var HEIGHT_CARD:int = 460;
		public static var COLUMN_CARD:int = 4;
		
		[Embed(source="./com/f14/F14bgGame/PuertoRico/images/all_part.png")]
		private static var IMG_PART:Class;
		private static var imgPart:Image;
		public static var WIDTH_PART:int = 20;
		public static var HEIGHT_PART:int = 20;
		//public static var COLUMN_PART:int = 4;
		
		/**
		 * 初始化图片
		 */
		override public function loadImages():void{
			//imgBuilding = loadImage("all_building.jpg");
			//imgPlantation = loadImage("all_plantation.jpg");
			//imgCard = loadImage("all_card.jpg");
			//不知道为啥最后那个透明图像总是显示不出来,只能通过嵌入后显示
			imgPart = loadEmbedImage(IMG_PART);
			
			this.addToLoadList("all_building.jpg");
			this.addToLoadList("all_plantation.jpg");
			this.addToLoadList("all_card.jpg");
			this.addToLoadList("disabled.png");
			this.loadList();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		override protected function onImageLoadComplete():void{
			imgBuilding = this.loadingList["all_building.jpg"].image;
			imgPlantation = this.loadingList["all_plantation.jpg"].image;
			imgCard = this.loadingList["all_card.jpg"].image;
			
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
		/**
		 * 取得种植园对应的图片
		 */
		public function getPlantationImage(index:int):BitmapData{
			var key:String = "PLT"+String(index);
			if(bitmapCache[key]==null){
				var top:int = 0;
				var left:int = WIDTH_PLANTATION * index;
				bitmapCache[key] = cutOutRect(imgPlantation, left, top, WIDTH_PLANTATION, HEIGHT_PLANTATION);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得卡牌对应的图片
		 */
		public function getCardImage(index:int):BitmapData{
			var key:String = "CRD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_CARD * int(index/COLUMN_CARD);
				var left:int = WIDTH_CARD * int(index%COLUMN_CARD);
				bitmapCache[key] = cutOutRect(imgCard, left, top, WIDTH_CARD, HEIGHT_CARD);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得大建筑对应的图片
		 */
		public function getBigBuildingImage(index:int):BitmapData{
			var key:String = "BBD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = 0;
				var left:int = WIDTH_BUILDING * int(index%COLUMN_BUILDING);
				bitmapCache[key] = cutOutRect(imgBuilding, left, top, WIDTH_BUILDING, HEIGHT_BUILDING*2);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得普通建筑对应的图片
		 */
		public function getBuildingImage(index:int):BitmapData{
			var key:String = "SBD"+String(index);
			if(bitmapCache[key]==null){
				var top:int = HEIGHT_BUILDING*2 + HEIGHT_BUILDING * int(index/COLUMN_BUILDING);
				var left:int = WIDTH_BUILDING * int(index%COLUMN_BUILDING);
				bitmapCache[key] = cutOutRect(imgBuilding, left, top, WIDTH_BUILDING, HEIGHT_BUILDING);
			}
			return bitmapCache[key];
		}
		
		/**
		 * 取得配件对应的图片
		 */
		public function getPartImage(index:int):BitmapData{
			var key:String = "PRT"+String(index);
			if(bitmapCache[key]==null){
				var top:int = 0;
				var left:int = WIDTH_PART * int(index);
				bitmapCache[key] = cutOutRect(imgPart, left, top, WIDTH_PART, HEIGHT_PART);
			}
			return bitmapCache[key];
		}
		
	}
}