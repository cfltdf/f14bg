package com.f14.F14bgGame.RFTG.manager
{
	import com.f14.f14bg.manager.SoundManager;
	
	import mx.core.SoundAsset;
	
	/**
	 * 声音的资源类
	 */
	public class RaceSoundManager extends SoundManager
	{
		/*[Embed("./com/f14/F14bgGame/RFTG/sounds/start.mp3")]
		private static var srcStart:Class;
		[Embed("./com/f14/F14bgGame/RFTG/sounds/join.mp3")]
		private static var srcJoin:Class;
		[Embed("./com/f14/F14bgGame/RFTG/sounds/leave.mp3")]
		private static var srcLeave:Class;*/
		
		public function RaceSoundManager(){
			super();
		}
		
		/**
		 * 初始化
		 */
		override protected function init():void{
			/*arraySound["start"] = SoundAsset(new srcStart());
			arraySound["joinGame"] = SoundAsset(new srcJoin());
			arraySound["leaveGame"] = SoundAsset(new srcLeave());*/
		}
		
	}
}