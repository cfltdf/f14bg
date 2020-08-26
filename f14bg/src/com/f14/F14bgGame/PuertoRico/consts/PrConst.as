package com.f14.F14bgGame.PuertoRico.consts
{
	public class PrConst
	{
		private static var characters:Array = new Array();
		characters["SETTLE"] = "拓荒者";
		characters["MAYOR"] = "市长";
		characters["BUILDER"] = "建筑师";
		characters["CRAFTSMAN"] = "工匠";
		characters["TRADER"] = "商人";
		characters["CAPTAIN"] = "船长";
		characters["PROSPECTOR"] = "淘金者";
		
		/**
		 * 取得角色代码对应的角色名称
		 */
		public static function getCharacterName(character:String):String{
			return characters[character];
		}
	}
}