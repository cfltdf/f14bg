
/*******************************************************************************
* Copyright (c) 2009 SmartInvoke.
* �˴�������CodeTransform�����Զ�����
* ������ͨ�� 
*  ��վ:http://smartinvoke.cn/ 
*  �ʼ�:pzxiaoxiao130130@gmail.com
*  QQ��90636900*  ��ϵ������^_^ 
*******************************************************************************/ 
package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 public class SystemHandler extends RemoteObject {
  public function SystemHandler(){
	 super();
	  //���÷��񷽹���SystemHandler���ͷ������
	 	this.createRemoteObject();
  }

   public function loadLocalProperties():String{
	 var retObj:Object=this.call("loadLocalProperties",arguments);
	 return retObj as String;

   }
   public function dispose():void{
	 var retObj:Object=this.call("dispose",arguments);

   }
   public function connectToServer(host:String,port:int):void{
	 var retObj:Object=this.call("connectToServer",arguments);

   }
   public function loadServerList():void{
	 var retObj:Object=this.call("loadServerList",arguments);

   }
 }

}
