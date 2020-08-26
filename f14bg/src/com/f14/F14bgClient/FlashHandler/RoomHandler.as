
/*******************************************************************************
* Copyright (c) 2009 SmartInvoke.
* �˴�������CodeTransform�����Զ����
* �����ͨ�� 
*  ��վ:http://smartinvoke.cn/ 
*  �ʼ�:pzxiaoxiao130130@gmail.com
*  QQ��90636900*  ��ϵ������^_^ 
*******************************************************************************/ 
package com.f14.F14bgClient.FlashHandler
{
 import cn.smartinvoke.RemoteObject;
 
 import mx.collections.ArrayCollection;
 public class RoomHandler extends RemoteObject {
  public function RoomHandler(){
	 super();
	  //���÷��񷽹���HallHandler���ͷ������
	 	this.createRemoteObject();
  }

   public function leaveRequest(roomId:int):void{
	 var retObj:Object=this.call("leaveRequest",arguments);
   }
 }

}
