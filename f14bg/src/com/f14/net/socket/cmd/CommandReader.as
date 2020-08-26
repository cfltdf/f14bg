package com.f14.core.net.socket.cmd
{
	import flash.net.Socket;
	import flash.utils.ByteArray;
	
	public class CommandReader
	{
		public function CommandReader()
		{
		}
		
		protected static var bytes:ByteArray = new ByteArray();
		//protected static var bsize:int = 1024;
		protected static var cmdlist:Array = new Array();
		protected static var step:int = 0;
		protected static var cmd:ByteCommand;
		protected static var len:int = 0;
		protected static var _isReading:Boolean = false;
		
		/**
		 * 判断是否正在读取指令中
		 */
		public static function isReading():Boolean{
			return _isReading;
		}
			
		/**
		 * 读取指令,如果返回null,则表示读取指令失败
		 * 
		 * 指令格式为:2位头信息+2位标志+4位长度信息+指令内容+2位结束信息
		 * 
		 * @param is
		 * @return
		 * @throws IOException
		 */
		public static function readCommands(socket:Socket):Array{
			var i:uint = 0;
			while((i = socket.bytesAvailable)){
				switch(step){
					case 0:
						//trace("读取新指令...");
						if(i>=2){
							cmd = new ByteCommand();
							cmd.head = socket.readUnsignedShort();
							if(cmd.head!=ByteCommand.BYTE_HEAD){
								//trace("头信息非法，切断连接！");
								socket.close();
								return null;
							}
							step++;
						}else{
							return null;
						}
						break;
					case 1:
						if(i>=2){
							cmd.flag = socket.readUnsignedShort();
							step++;
						}else{
							return null;
						}
						break;
					case 2:
						if(i>=2){
							cmd.zip = socket.readUnsignedShort();
							step++;
						}else{
							return null;
						}
						break;	
					case 3:
						if(i>=4){
							cmd.size = socket.readUnsignedInt();
							len = cmd.size;
							step++;
						}else{
							return null;
						}
						break;
					case 4:
						if(len>0){
							var s:int = (i>len)?len:i;
							len -= s;
							socket.readBytes(bytes, 0, s);
							cmd.contentBytes.writeBytes(bytes, 0, s);
						}else{
							return null;
						}
						if(len==0){
							step++;
						}
						//socket.readBytes(cmd.contentBytes, 0, cmd.size);
						break;
					case 5:
						if(i>=2){
							cmd.tail = socket.readUnsignedShort();
							if(cmd.tail!=ByteCommand.BYTE_TAIL){
								//trace("结束信息非法，切断连接！");
								socket.close();
								return null;
							}
							//trace("指令读取成功,存放到返回结果中.");
							step = 0;
							//判断数据是否经过压缩,如果经过压缩,则需要解压
							if(cmd.zip==1){
								cmd.contentBytes.uncompress();
							}
							cmdlist.push(cmd);
						}else{
							return null;
						}
						break;
					default:
						return null;
				}
			}
			var res:Array = new Array();
			while(cmdlist.length>0){
				res.push(cmdlist.shift());
			}
			return res;
		}
	}
}