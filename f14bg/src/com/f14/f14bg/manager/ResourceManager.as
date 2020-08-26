package com.f14.f14bg.manager
{
	import com.adobe.serialization.json.JSON;
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.core.util.ApplicationUtil;
	
	/**
	 * 资源管理器
	 */
	public class ResourceManager
	{
		public function ResourceManager()
		{
		}
		
		protected var cache:Array = new Array();
		
		/**
		 * 添加对象
		 */
		public function putObject(obj:Object):void{
			cache[obj.id] = obj;
		}
		
		/**
		 * 按照id取得对象
		 */
		public function getObject(id:String):Object{
			return cache[id];
		}
		
		/**
		 * 按照cardNo取得对象
		 */
		public function getObjectByCardNo(cardNo:int):Object{
			for each(var obj:Object in cache){
				if(int(obj.cardNo)==cardNo){
					return obj;
				}
			}
			return null;
		}
		
		/**
		 * 装载资源
		 */
		public function load(param:Object):void{
		}
		
		/**
		 * 读取资源字符串
		 */
		public function loadResourceString():void{
			var resString:String = RoomUtil.commonHandler.loadResourceString(ApplicationUtil.gameType);
			var param:Object = JSON.decode(resString);
			this.load(param);
		}

	}
}