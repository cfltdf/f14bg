package com.f14.F14bgGame.PuertoRico.manager
{
	import com.f14.f14bg.manager.SoundManager;
	
	import mx.core.SoundAsset;
	
	/**
	 * 声音的资源类
	 */
	public class PrSoundManager extends SoundManager
	{
		/*[Embed("./com/f14/F14bgGame/PuertoRico/sounds/startGame.mp3")]
		private static var srcStart:Class;
		[Embed("./com/f14/F14bgGame/PuertoRico/sounds/joinGame.mp3")]
		private static var srcJoin:Class;
		[Embed("./com/f14/F14bgGame/PuertoRico/sounds/leaveGame.mp3")]
		private static var srcLeave:Class;
		[Embed("./com/f14/F14bgGame/PuertoRico/sounds/playerTurn.mp3")]
		private static var srcTurn:Class;*/
		
		public function PrSoundManager(){
			super();
		}
		
		/**
		 * 初始化
		 */
		override protected function init():void{
			/*arraySound["start"] = SoundAsset(new srcStart());
			arraySound["joinGame"] = SoundAsset(new srcJoin());
			arraySound["leaveGame"] = SoundAsset(new srcLeave());
			arraySound["playerTurn"] = SoundAsset(new srcTurn());*/
		}
		
	}
}