package com.f14.net.smartinvoke
{
 import cn.smartinvoke.RemoteObject;
 public class FlashCommandHandler extends RemoteObject {
  public function FlashCommandHandler(){
	 	super();
	 	this.createRemoteObject();
  }

   public function sendCommand(roomId:int, cmdstr:String):void{
	 var retObj:Object=this.call("sendCommand",arguments);
   }
 }

}
