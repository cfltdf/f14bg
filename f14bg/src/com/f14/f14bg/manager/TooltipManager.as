package com.f14.f14bg.manager
{
	import com.f14.core.controls.tip.LoadingTip;
	import com.f14.core.util.ApplicationUtil;
	
	import flash.utils.setTimeout;
	
	public class TooltipManager
	{
		public function TooltipManager()
		{
		}
		
		private static var loadingTip:LoadingTip = new LoadingTip();
		
		public static function init():void{
		}
		
		/**
		 * 显示读取远程数据进度条
		 */
		public static function showLoadingTip(msg:String=null, timeout:int=10000):void{
			hideLoadingTip();
			loadingTip.showLoadingTip(ApplicationUtil.application, msg);
			if(timeout>0){
				setTimeout(hideLoadingTip, timeout);
			}
		}
		
		/**
		 * 隐藏读取远程数据进度条
		 */
		public static function hideLoadingTip():void{
			loadingTip.hideLoadingTip();
		}
	}
}