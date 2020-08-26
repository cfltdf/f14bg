package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 
 import com.f14.f14bg.player.User;
 
 import flash.utils.ByteArray;
 
 import mx.collections.ArrayCollection;
 public class CommonHandler extends RemoteObject {
  public function CommonHandler(){
	 super();
	 	this.createRemoteObject();
  }

   public function getLocalUser():User{
	 var retObj:Object=this.call("getLocalUser",arguments);
	 return retObj as User;

   }
   
   public function loadFile(gameType:String, file:String):ByteArray{
	 var retObj:Object=this.call("loadFile",arguments);
	 return retObj as ByteArray;

   }
   
   public function loadResourceString(gameType:String):String{
   	var retObj:Object=this.call("loadResourceString",arguments);
	 return retObj as String;
   }
   
   public function getLocalProperty(key:String):String{
   	var retObj:Object=this.call("getLocalProperty",arguments);
	 return retObj as String;
   }
   
    public function loadLocalProperties():String{
	 var retObj:Object=this.call("loadLocalProperties",arguments);
	 return retObj as String;
   }
   
   public function saveLocalProperty(key:String, value:String):void{
   	this.call("saveLocalProperty",arguments);
   }
   
   public function saveLocalProperties(param:Array):void{
   	this.call("saveLocalProperties",arguments);
   }
   
   public function closeConnection():void{
   	this.call("closeConnection",arguments);
   }
   
   public function getVersionInfo(gameType:String):Object{
   	var retObj:Object = this.call("getVersionInfo",arguments);
   	return retObj;
   }
   
   public function getCodes(codeType:String):ArrayCollection{
	 var retObj:Object=this.call("getCodes",arguments);
	 return retObj as ArrayCollection;
   }
   
   public function getCodeLabel(codeType:String, codeValue:String):String{
	 var retObj:Object=this.call("getCodeLabel",arguments);
	 return retObj as String;
   }
   
   public function getConfigValue(key:String):String{
	 var retObj:Object=this.call("getConfigValue",arguments);
	 return retObj as String;
   }
   
   public function viewUser(userId:String):void{
	 this.asyncCall("viewUser",arguments);
   }
   
 }

}
