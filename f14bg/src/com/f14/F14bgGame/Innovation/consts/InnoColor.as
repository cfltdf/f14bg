package com.f14.F14bgGame.Innovation.consts
{
	public class InnoColor
	{
		public static var RED:String = "RED";
		public static var YELLOW:String = "YELLOW";
		public static var GREEN:String = "GREEN";
		public static var BLUE:String = "BLUE";
		public static var PURPLE:String = "PURPLE";
		
		public static var COLORS:Array = [RED, YELLOW, GREEN, BLUE, PURPLE];
		public static var COLORS_DESC:Array = ["红色", "黄色", "绿色", "蓝色", "紫色"];
		
		/**
		 * 取得颜色的代码
		 */
		public static function getColorCode(colorDesc:String):String{
			for(var i:int=0;i<COLORS_DESC.length;i++){
				if(COLORS_DESC[i]==colorDesc){
					return COLORS[i];
				}
			}
			return "";
		}

	}
}