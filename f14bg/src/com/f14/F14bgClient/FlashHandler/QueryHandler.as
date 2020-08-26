
package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 public class QueryHandler extends RemoteObject {
  public function QueryHandler(){
	 super();
	 	this.createRemoteObject();
  }
  
  public function viewUser():void{
  	this.asyncCall("viewUser",arguments);

   }

 }

}
