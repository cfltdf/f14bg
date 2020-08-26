package com.f14.F14bgClient.login.manager
{
	import com.adobe.serialization.json.JSON;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.ActionManager;
	import com.f14.f14bg.manager.TooltipManager;
	
	public class LoginActionManager extends ActionManager
	{
		public function LoginActionManager(){
			super();
		}
		
		/**
		 * 连接服务器
		 */
		public function connectToServer(host:String, port:String):void{
			TooltipManager.showLoadingTip("连接服务器...", 60000);
			//var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_CONNECT);
			//sendCommand(param);
			//连接服务器
			/*if(handler!=null){
				closeConnect();
			}
			handler = new PlayerHandler(host, int(port));
			handler.connect();*/
		}
		
		/**
		 * 登录服务器
		 */
		public function doLogin(loginName:String, password:String):void{
			TooltipManager.showLoadingTip("登录服务器...", 60000);
			//发送登录指令
			var param:Object = createSystemCommand(CmdConst.SYSTEM_CODE_LOGIN);
			param.loginName = loginName;
			param.password = password;
			//param.clientInfo = ApplicationUtil.CLIENT_INFO;
			sendCommand(param);
		}
		
		/**
		 * 关闭连接
		 */
		public function closeConnect():void{
			/*try{
				if(handler.socket!=null){
					handler.socket.close();
					handler = null;
				}
			}catch(e:Error){
				ApplicationUtil.debug(e.message);
			}*/
		}
		
		/**
		 * 注册用户
		 */
		public function registUser(user:Object):void{
			TooltipManager.showLoadingTip("注册中...");
			var code:int = CmdConst.SYSTEM_CODE_USER_REGIST;
			var param:Object = createSystemCommand(code);
			for(var p:String in user){
				param[p] = user[p];
			}
			sendCommand(param);
		}
		
		/**
		 * 装载系统代码
		 */
		public function loadCodeDetail():void{
			var code:int = CmdConst.SYSTEM_CODE_CODE_DETAIL;
			var param:Object = createSystemCommand(code);
			sendCommand(param);
		}
		
	}
}