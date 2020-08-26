package com.f14.F14bgGame.bg.utils
{
	public class BgUtils
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
		
		public static function label2String(labels:Array):String{
			var res:String = "";
			for(var i:int=0;i<labels.length;i++){
				res += labels[i].object.object.id + ",";
			}
			return (res.length>0) ? res.substring(0, res.length-1) : res;
		}
		
		public static function label2StringEach(labels:Array):String{
			var res:String = "";
			for each(var label:Object in labels){
				if(label!=null){
					res += label.object.object.id + ",";
				}
			}
			return (res.length>0) ? res.substring(0, res.length-1) : res;
		}
		
		/**
		 * 判断obj是否在arr中
		 */
		public static function inArray(obj:Object, arr:Array):Boolean{
			for each(var o:Object in arr){
				if(o==obj){
					return true;
				}
			}
			return false;
		}
	}
}