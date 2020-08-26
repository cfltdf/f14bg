package com.f14.core.util
{
	import com.f14.core.controls.tip.BaseTip;
	
	import mx.managers.PopUpManager;
	
	/**
	 * 提示框工具类
	 */
	public class TipUtil
	{
		public static var tipOffsetX:int = 0;
		public static var tipOffsetY:int = 20;
		
		public static var tip:BaseTip;
		
		/**
		 * 显示提示漂浮框,如果msg为空则不显示
		 */
		public static function showTip(msg:String = null):void{
			hideTip();
			if(msg){
				tip = new BaseTip();
				tip.msg = msg;
				PopUpManager.addPopUp(tip, ApplicationUtil.application);
				TipUtil.tip = tip;
			}
		}
		
		/**
		 * 隐藏漂浮框
		 */
		public static function hideTip():void{
			if(tip!=null){
				PopUpManager.removePopUp(tip);
				tip = null;
			}
		}
		
		/**
		 * 设置漂浮框的位置
		 */
		public static function setPosition(x:int, y:int):void{
			if(tip!=null){
				tip.x = x + tipOffsetX;
				tip.y = y + tipOffsetY;
			}
		}
	}
}