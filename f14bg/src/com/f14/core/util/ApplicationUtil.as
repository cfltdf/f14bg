package com.f14.core.util
{
	import com.adobe.serialization.json.JSON;
	import com.f14.F14bgClient.FlashHandler.CommonHandler;
	import com.f14.core.controls.F14application;
	import com.f14.f14bg.manager.DebugManager;
	import com.f14.f14bg.net.CommandHandler;
	import com.f14.f14bg.player.User;
	import com.f14.net.smartinvoke.FlashCommandHandler;
	
	import flash.events.Event;
	
	import mx.controls.Alert;
	
	public class ApplicationUtil
	{
		public static var application:F14application;
		public static var localUser:User;
		public static var commandHandler:CommandHandler;
		public static var flashCommandHandler:FlashCommandHandler;
		public static var roomId:int = 0;
		public static var gameType:String = "";
		public static var basePath:String = "";
		public static var version:Version = null;
		
		public static var commonHandler:CommonHandler;
		public static var debugManager:DebugManager;
		
		/**
		 * 初始化
		 */
		public static function init():void{
			flashCommandHandler = new FlashCommandHandler();
			//ApplicationUtil.application = application;
			//ActionManager.init();
			//playerHandler = new PlayerHandler();
			commonHandler = new CommonHandler();
		}
		
		/**
		 * 初始化调试管理模块
		 */
		public static function initDebugManager():void{
			//初始化调试管理模块
			debugManager = new DebugManager();
		}
		
		/**
		 * 提示信息
		 */
		public static function alert(object:Object):void{
			if(object==null){
				Alert.show(null);
			}else{
				Alert.show(object.toString());
			}
		}
		
		/**
		 * 发送指令对象
		 */
		public static function sendCommandParam(param:Object):void{
			sendCommand(JSON.encode(param));
		}
		
		/**
		 * 发送指令字符串
		 */
		public static function sendCommand(cmdstr:String):void{
			flashCommandHandler.sendCommand(roomId, cmdstr);
		}
		
		/**
		 * 收到指令字符串
		 */
		public static function onCommand(cmdstr:String):void{
			var param:Object = JSON.decode(cmdstr);
			commandHandler.processCommand(param);
		}
		
		/**
		 * 装载swf文件
		 */
		public static function loadModule(url:String):void{
			application.loader.loadModule(url);
		}
		
		/**
		 * 装载本地参数
		 */
		public static function loadLocalProperties():Object{
			var localProp:Object = new Object();
			var str:String = ApplicationUtil.commonHandler.loadLocalProperties();
			//读取属性成功时,设置默认值
			if(str){
				localProp = JSON.decode(str);
			}
			return localProp;
		}
		
		/**
		 * 动态创建方法
		 */
		public static function createClickFunction(f:Function, ... arg):Function{
			return function(e:Event):void{
				f.apply(null, [e].concat(arg));
			};
		}
		
	}
}