package com.f14.net.smartinvoke
{
	import cn.smartinvoke.IServerObject;
	
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.manager.TooltipManager;

	public class ClientCmdHandler implements IServerObject
	{
		public function ClientCmdHandler()
		{
		}
		
		/**
		 * 提示错误信息
		 */
		public function onError(msg:String):void{
			TooltipManager.hideLoadingTip();
			ApplicationUtil.alert(msg);
		}
		
		/**
		 * 收到指令
		 */
		public function onCommand(cmdstr:String):void{
			try{
				ApplicationUtil.onCommand(cmdstr);
			}catch(e:Error){
				//将错误信息输出到调试窗口
				ApplicationUtil.debugManager.printError(e, cmdstr);
			}
		}
		
		/**
		 * 完成客户端连接时触发的事件
		 */
		public function onConnection():void{
			//ApplicationUtil.application.onConnection();
		}
		
		/**
		 * 装载子模块
		 */
		public function loadModule(path:String):void{
			ApplicationUtil.loadModule(path);
		}
		
		/**
		 * 设置房间id
		 */
		public function setRoomInfo(param:Object):void {
			ApplicationUtil.roomId = param.roomId;
			ApplicationUtil.gameType = param.gameType;
			ApplicationUtil.basePath = param.basePath;
		}
		
		/**
		 * 显示读取进度条
		 */
		public function showTooltips(message:String, timeout:Number):void{
			TooltipManager.showLoadingTip(message, timeout);
		}
		
		/**
		 * 隐藏读取进度条
		 */
		public function hideTooltips():void{
			TooltipManager.hideLoadingTip();
		}

	}
}