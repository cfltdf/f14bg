package com.f14.core.util
{
	import com.f14.core.consts.ConfigConst;
	import com.f14.resource.SoundResource;
	
	import mx.core.SoundAsset;
	
	/**
	 * 声音管理类
	 */
	public class SoundUtil
	{

		/**
		 * 播放声音
		 */
		public static function play(sound:String):void{
			if(ConfigConst.SOUND_ENABLED){
				var src:SoundAsset = SoundResource.getSrc(sound);
				if(src){
					src.play();
				}else{
					trace("没有找到声音资源: " + sound);
				}
			}
		}
	}
}