
/*******************************************************************************
* Copyright (c) 2009 SmartInvoke.
* �˴�������CodeTransform�����Զ�����
* ������ͨ�� 
*  ��վ:http://smartinvoke.cn/ 
*  �ʼ�:pzxiaoxiao130130@gmail.com
*  QQ��90636900*  ��ϵ������^_^ 
*******************************************************************************/ 
package com.f14.net.smartinvoke
{
 import cn.smartinvoke.RemoteObject;
 public class CommandHandler extends RemoteObject {
  public function CommandHandler(){
	 super();
	  //���÷��񷽹���CommandHandler���ͷ������
	 	this.createRemoteObject();
  }

   public function sendCommand(cmdstr:String):void{
	 var retObj:Object=this.call("sendCommand",arguments);

   }
   public function dispose():void{
	 var retObj:Object=this.call("dispose",arguments);

   }
 }

}
