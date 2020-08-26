
package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 public class UpdateHandler extends RemoteObject {
  public function UpdateHandler(){
	 super();
	 	this.createRemoteObject();
  }
  
  public function updateFiles():void{
  	this.asyncCall("updateFiles",arguments);

   }

 }

}
