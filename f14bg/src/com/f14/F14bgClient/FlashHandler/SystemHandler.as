
package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 public class SystemHandler extends RemoteObject {
  public function SystemHandler(){
	 super();
	 	this.createRemoteObject();
  }

   public function loadNotice():void{
	 var retObj:Object=this.call("loadNotice",arguments);

   }
   public function loadServerList():void{
	 var retObj:Object=this.call("loadServerList",arguments);

   }
   public function connectToServer(host:String,port:int):void{
	 var retObj:Object=this.call("connectToServer",arguments);

   }
	public function onLogin():void{
		this.asyncCall("onLogin",arguments);
	}
 }

}
