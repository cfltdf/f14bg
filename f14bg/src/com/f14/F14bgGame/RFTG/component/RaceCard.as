package com.f14.F14bgGame.RFTG.component
{
	import com.f14.F14bgGame.bg.component.Card;

	public class RaceCard extends Card
	{
		public function RaceCard()
		{
			super();
		}
		
		public var enName:String;
		public var qty:int = 0;
		public var cost:int = 0;
		public var vp:int = 0;
		public var startWorld:int = -1;
		public var startHand:int = -1;
		public var military:int = 0;
		public var type:String;
		public var worldType:String;
		public var productionType:String;
		public var goodType:String;
		public var good:Boolean;
		public var activeType:String;
		
		public function toString():String{
			var str:String = "";
			str += "id: " + id + "\n"
				+ "name: " + name + "\n"
				+ "enName: " + enName + "\n"
				+ "cost: " + cost + "\n"
				+ "vp: " + vp + "\n"
				+ "military: " + military + "\n"
				+ "type: " + type + "\n"
				+ "good: " + good + "\n"
				+ "worldType: " + worldType + "\n"
				+ "productionType: " + productionType + "\n"
				+ "goodType: " + goodType + "\n"
				+ "descr: " + descr;
			return str;
		}
		
	}
}