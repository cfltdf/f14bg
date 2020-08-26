package com.f14.F14bgGame.RFTG.utils
{
	public class RaceUtils
	{
		
		public static function card2String(cards:Array):String{
			var res:String = "";
			for(var i:int=0;i<cards.length;i++){
				res += cards[i].id + ",";
			}
			return (res.length>0) ? res.substring(0, res.length-1) : res;
		}

		public static function array2String(arr:Array):String{
			var res:String = "";
			for(var i:int=0;i<arr.length;i++){
				res += arr[i] + ",";
			}
			return (res.length>0) ? res.substring(0, res.length-1) : res;
		}
	}
}