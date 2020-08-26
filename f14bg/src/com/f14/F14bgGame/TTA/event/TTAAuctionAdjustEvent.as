package com.f14.F14bgGame.TTA.event
{
	import com.f14.F14bgGame.TTA.components.TTACard;
	
	/**
	 * TTA拍卖部队数量调整的事件
	 */
	public class TTAAuctionAdjustEvent extends TTAEvent
	{
		public function TTAAuctionAdjustEvent()
		{
			super(TTAEvent.AUCTION_ADJUST);
		}
		
		public var num:int;
		
	}
}