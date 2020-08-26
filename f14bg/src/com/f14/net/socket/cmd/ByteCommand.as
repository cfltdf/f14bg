package com.f14.core.net.socket.cmd
{
	import flash.utils.ByteArray;
	
	public class ByteCommand
	{
		public static var BYTE_HEAD:int = 0xf14f;
		public static var BYTE_TAIL:int = 0xf41f;
		
		public var head:int;
		public var flag:int;
		public var zip:int;
		public var size:int;
		public var contentBytes:ByteArray = new ByteArray();
		public var tail:int;
		
		protected var toStr:String;
		protected var content:String;
		
		public function ByteCommand(){
		}
		
		public function setContent(content:String):void{
			this.contentBytes = new ByteArray();
			this.contentBytes.writeUTFBytes(content);
			this.size = this.contentBytes.length;
		}
		
		/**
		 * 取得内容信息的字符串
		 * 
		 * @return
		 */
		public function getContent():String{
			if(this.content==null){
				this.content = contentBytes.toString();
			}
			return this.content;
		}

		public function toString():String {
			if(toStr==null){
				if(this.zip==0){
					toStr = head + " | " + flag + " | " + zip + " | " + size + " | " + getContent() + " | " + tail;
				}else{
					toStr = head + " | " + flag + " | " + zip + " | " + size + " | " + getContent().substr(0, 300) + " ...} | " + tail;
				}
			}
			return toStr;
		}

	}
}