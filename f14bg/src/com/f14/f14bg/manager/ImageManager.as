package com.f14.f14bg.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.manager.DefaultManagerUtil;
	import com.f14.core.util.ApplicationUtil;
	
	import flash.display.Bitmap;
	import flash.display.BitmapData;
	import flash.display.DisplayObject;
	import flash.display.Loader;
	import flash.events.Event;
	import flash.geom.Matrix;
	import flash.net.URLRequest;
	
	import mx.controls.Image;
	
	/**
	 * 图片素材管理器
	 */
	public class ImageManager
	{
		public function ImageManager()
		{
			this.init();
		}
		
		protected var bitmapCache:Array = new Array();
		protected var loadingList:Array = new Array();

		/**
		 * 初始化图片管理器
		 */
		public function init():void{
			
		}
		
		/**
		 * 装载图片资源
		 */
		public function loadImages():void{
			
		}
		
		/**
	     *  裁剪指定矩形区域并返回一个包含结果的 BitmapData 对象。
	     *
	     *  @param target 需要裁剪的显示对象。
	     *
	     *  @param width 位图图像的宽度，以像素为单位。
	     *
	     *  @param height 位图图像的高度，以像素为单位。
	     *
	     *  @param distanceX 切割矩形左上角的点到显示对象矩形左上角的点的水平距离。注意：左上角的点不一定就是注册点（0, 0）外，变形过的显示对象就是一个例外。
	     *
	     *  @param distanceY 切割矩形左上角的点到显示对象矩形左上角的点的垂直距离。注意：左上角的点不一定就是注册点（0, 0）外，变形过的显示对象就是一个例外。
	     *
	     *  @param transparent 指定裁剪后的位图图像是否支持每个像素具有不同的透明度。默认值为 true（透明）。若要创建完全透明的位图，请将 transparent 参数的值设置为 true，将 fillColor 参数的值设置为 0x00000000（或设置为 0）。将 transparent 属性设置为 false 可以略微提升呈现性能。
	     *
	     *  @param fillColor 用于填充裁剪后的位图图像区域背景的 32 位 ARGB 颜色值。默认值为 0x00000000（纯透明黑色）。
	     *
	     *  @returns 返回裁剪后的 BitmapData 对象。
	     */
		public static function cutOutRect( target:DisplayObject, distanceX:Number, distanceY:Number, width:Number, height:Number, transparent:Boolean = true, fillColor:uint = 0x00000000 ):BitmapData
		{
			var m:Matrix = target.transform.matrix;
			m.tx -= target.getBounds( target.parent ).x + distanceX;
			m.ty -= target.getBounds( target.parent ).y + distanceY;
			
			var bmpData:BitmapData = new BitmapData( width, height, transparent, fillColor );
			bmpData.draw( target, m );
			
			return bmpData;
		}
		
		/**
		 * 添加到装载列表中
		 */
		protected function addToLoadList(file:String):void{
			var info:Object = new Object();
			info.state = "loading";
			this.loadingList[file] = info;
		}
		
		/**
		 * 装载列表中的图片资源
		 */
		protected function loadList():void{
			for(var file:String in this.loadingList){
				var info:Object = this.loadingList[file];
				var url:String = ApplicationUtil.basePath + "images/" + file;
				var loader:Loader = new Loader();
				loader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoadComplete);
				info.loader = loader;
				loader.load(new URLRequest(url));
			}
		}
		
		/**
		 * 取得图片对象
		 */
		public function getImage(file:String):Image{
			return this.loadingList[file].image as Image;
		}
		
		/**
		 * 取得图片对象副本
		 */
		public function getImageInstance(file:String):Image{
			var image:Image = new Image();
			var data:BitmapData = Bitmap(this.getImage(file).content).bitmapData;
			var bitmap:Bitmap = new Bitmap(data);
			image.source = bitmap;
			return image;
		}
		
		/**
		 * 生成图片对象,默认缓存该图片对象
		 */
		protected function loadImage(file:String):void{
			var url:String = ApplicationUtil.basePath + "images/" + file;
			var loader:Loader = new Loader();
			loader.contentLoaderInfo.addEventListener(Event.COMPLETE, onLoadComplete);
			loader.load(new URLRequest(url));
		}
		
		/**
		 * 生成内嵌的图片对象
		 */
		public function loadEmbedImage(source:Class):Image{
			var image:Image = new Image();
			image.source = source;
			RoomUtil.gameModule.imageCanvas.addChild(image);
			return image;
		}
		
		/**
		 * 装载图片完成时的回调函数
		 */
		protected function onLoadComplete(evt:Event):void{
			for each(var info:Object in this.loadingList){
				//ApplicationUtil.alert(info.loader + "---" + evt.currentTarget);
				if(info.loader.contentLoaderInfo==evt.currentTarget){
					var image:Image = new Image();
					image.source = evt.currentTarget.content;
					DefaultManagerUtil.module.imageCanvas.addChild(image);
					info.image = image;
					info.state = "complete";
					break;
				}
			}
			this.checkImageLoadComplete();
			/*var image:Image = new Image();
			image.source = evt.currentTarget.content;
			DefaultManagerUtil.module.addChild(image);
			
			if(evt.currentTarget instanceof Image){
				//将图片对象在装载列表中设置为已完成
				var image:Image = evt.currentTarget as Image;
				ApplicationUtil.alert(image.id + " load complete"); 
				this.loadingCache[image.id] = "complete";
				this.checkImageLoadComplete();
			}*/
		}
		
		/**
		 * 检查图片资源是否都装载完成
		 */
		public function checkImageLoadComplete():void{
			for each(var info:Object in this.loadingList){
				if(info.state!="complete"){
					return;
				}
			}
			//如果所有的图片都装载完成,则执行回调函数
			this.onImageLoadComplete();
		}
		
		/**
		 * 图片资源全部装载完成时的回调函数
		 */
		protected function onImageLoadComplete():void{
			//ApplicationUtil.alert("all complete");
			DefaultManagerUtil.module.onImageLoadComplete();
		}
		
	}
}