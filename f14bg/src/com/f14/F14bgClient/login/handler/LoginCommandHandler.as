package com.f14.F14bgClient.login.handler
{
	import com.f14.F14bgClient.login.LoginUtil;
	import com.f14.core.util.ApplicationUtil;
	import com.f14.f14bg.consts.CmdConst;
	import com.f14.f14bg.manager.TooltipManager;
	import com.f14.f14bg.net.CommandHandler;

	public class LoginCommandHandler extends CommandHandler
	{
		public function LoginCommandHandler(){
			super();
		}
		
		/**
		 * 处理系统指令
		 */
		override protected function processSystemCommand(param:Object):void{
			var i:int;
			switch(param.code){
				case CmdConst.SYSTEM_CODE_CONNECT: //连接服务器
					TooltipManager.hideLoadingTip();
					//显示输入账号的界面
					LoginUtil.module.setShowState("ACCOUNT");
					break;
				case CmdConst.SYSTEM_CODE_LOGIN: //登录
					TooltipManager.hideLoadingTip();
					LoginUtil.module.systemHandler.onLogin();
					//读取系统代码
					//CodeDetailManager.initCodes();
					//显示大厅界面
					//StateManager.toHallBoard();
					//创建本地用户
					//ApplicationUtil.localUser = new User();
					//ApplicationUtil.localUser.id = param.id;
					//ApplicationUtil.localUser.name = param.name;
					//if(param.reconnect){
						//如果是重新连接的,则弹出重新连接提示框
					//	StateManager.showReconnectBoard();
					//}
					break;
				case CmdConst.SYSTEM_CODE_USER_REGIST: //用户注册
					TooltipManager.hideLoadingTip();
					ApplicationUtil.alert("用户注册成功!");
					LoginUtil.stateManager.hideRegistWindow();
					break;
				case CmdConst.SYSTEM_CODE_CODE_DETAIL: //读取系统代码
					//CodeDetailManager.loadCodes(param.codes);
					break;
				default: //其他指令不予执行
					break;
			}
		}
		
	}
}