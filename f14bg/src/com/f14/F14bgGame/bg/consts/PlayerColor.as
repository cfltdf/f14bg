package com.f14.F14bgGame.bg.consts
{
	import flash.filters.ColorMatrixFilter;
	
	public class PlayerColor
	{
		/**
		 * 玩家顺位对应的颜色代码
		 */
		public static var COLORS:Array = [
			0x0000ff, 0x00ff00, 0xff0000, 0xffff00, 0xC400FF, 0xD2D2D2
		];
		
		/**
		 * 玩家顺位对应的颜色代码
		 */
		public static var COLOR_STRING:Array = [
			"#0000FF", "#00FF00", "#FF0000", "#FFFF00", "#C400FF", "#D2D2D2"
		];
		
		/**
		 * 玩家顺位对应的滤镜
		 */
		public static var COLOR_FILTER:Array = [
			[new ColorMatrixFilter([0,0,0,0,0,  0,0,0,0,0,  1,1,1,0,0,  0,0,0,1,0])],
			[new ColorMatrixFilter([0,0,0,0,0,  1,1,1,0,0,  0,0,0,0,0,  0,0,0,1,0])],
			[new ColorMatrixFilter([1,1,1,0,0,  0,0,0,0,0,  0,0,0,0,0,  0,0,0,1,0])],
			[new ColorMatrixFilter([1,1,1,0,0,  1,1,1,0,0,  0,0,0,0,0,  0,0,0,1,0])],
			[new ColorMatrixFilter([0.5,0.5,0.5,0,0,  0,0,0,0,0,  1,1,1,0,0,  0,0,0,1,0])],
			[new ColorMatrixFilter([0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0.5,0.5,0.5,0,0,  0,0,0,1,0])]
		];

	}
}