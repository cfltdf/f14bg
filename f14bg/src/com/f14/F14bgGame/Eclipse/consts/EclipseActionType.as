package com.f14.F14bgGame.Eclipse.consts
{
	public class EclipseActionType
	{
		public static var EXPLORE:String = "EXPLORE";
		public static var INFLUENCE:String = "INFLUENCE";
		public static var RESEARCH:String = "RESEARCH";
		public static var UPGRADE:String = "UPGRADE";
		public static var BUILD:String = "BUILD";
		public static var MOVE:String = "MOVE";

		public static function getActionIndex(type:String):int{
			switch(type){
				case EXPLORE:
					return 1;
				case INFLUENCE:
					return 2;
				case RESEARCH:
					return 3;
				case UPGRADE:
					return 4;
				case BUILD:
					return 5;
				case MOVE:
					return 6;
			}
			return 0;
		}
	}
}