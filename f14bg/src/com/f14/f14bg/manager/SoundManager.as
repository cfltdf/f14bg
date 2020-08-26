package com.f14.f14bg.manager
{
	import com.f14.core.util.ApplicationUtil;
	
	import flash.events.IOErrorEvent;
	import flash.media.SoundChannel;
	import flash.net.URLRequest;
	
	import mx.core.SoundAsset;
	
	/**
	 * 声效管理器
	 */
	public class SoundManager
	{
		public function SoundManager()
		{
			this.init();
		}
		
		protected var channel:SoundChannel;
		
		/**
		 * 存放所有资源引用的数组
		 */
		protected var arraySound:Array = new Array();
		
		/**
		 * 初始化
		 */
		protected function init():void{
		}
		
		/**
		 * 取得资源
		 */
		public function getSrc(sound:String):Object{
			if(arraySound[sound]==null){
				arraySound[sound] = this.loadSound(sound);
			}
			return arraySound[sound];
		}
		
		/**
		 * 播放声音
		 */
		public function play(sound:String):void{
			var object:Object = this.getSrc(sound);
			if(object!=null && object.sound!=null){
				channel = object.sound.play();
			}//else{
				//trace("没有找到声音资源: ",sound);
			//}
		}
		
		/**
		 * 生成图片对象,默认缓存该图片对象
		 */
		protected function loadSound(sound:String):Object{
			var obj:Object = new Object();
			var sa:SoundAsset = new SoundAsset();
			var url:URLRequest = new URLRequest(this.getSoundPath(sound));
			sa.addEventListener(IOErrorEvent.IO_ERROR, onIOError);
			sa.load(url);
			obj.sound = sa;
			return obj;
		}
		
		/**
		 * 取得声音文件的路径
		 */
		protected function getSoundPath(sound:String):String{
			return ApplicationUtil.basePath + "sounds/" + sound + ".mp3";
		}
		
		/**
		 * 当装载声音文件发送错误时触发的方法
		 */
		protected function onIOError(event:IOErrorEvent):void{
			var sound:String = event.text.substring(event.text.lastIndexOf("sounds/")+7, event.text.lastIndexOf(".mp3"));
			if(this.arraySound[sound]!=null){
				this.arraySound[sound].sound = null;
			}
		}

	}
}