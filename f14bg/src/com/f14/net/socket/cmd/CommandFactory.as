package com.f14.core.net.socket.cmd
{
	import flash.utils.ByteArray;
	
	public class CommandFactory
	{
	
		public function CommandFactory()
		{
		}

		public static function createCommand(flag:int, content:String):ByteCommand
		{
			var cmd:ByteCommand = new ByteCommand();
			cmd.head = ByteCommand.BYTE_HEAD;
			cmd.flag = flag;
			cmd.tail = ByteCommand.BYTE_TAIL;
			cmd.setContent(content);
			return cmd;
		}
		
	}
}