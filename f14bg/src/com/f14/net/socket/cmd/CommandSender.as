package com.f14.core.net.socket.cmd
{
	import flash.net.Socket;
	import flash.utils.ByteArray;
	
	public class CommandSender
	{
		public var socket:Socket;
		
		public function CommandSender(socket:Socket)
		{
			this.socket = socket;
		}
	
		/**
		 * 发送指令
		 * 
		 * @param cmd
		 * @throws IOException
		 */
		public function sendCommand(cmd:ByteCommand):void{
			this.socket.writeBytes(this.toByte(cmd));
			this.socket.flush();
		}
		
		/**
		 * 将指令转换成byte数组
		 */
		public function toByte(cmd:ByteCommand):ByteArray{
			var bb:ByteArray = new ByteArray();
			bb.writeShort(cmd.head);
			bb.writeShort(cmd.flag);
			bb.writeInt(cmd.size);
			bb.writeBytes(cmd.contentBytes);
			bb.writeShort(cmd.tail);
			return bb;
		}
	}
}