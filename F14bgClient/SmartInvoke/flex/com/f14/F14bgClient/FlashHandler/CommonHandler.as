
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
 import com.f14.F14bgClient.User;
 public class CommonHandler extends RemoteObject {
  public function CommonHandler(){
	 super();
	  //���÷��񷽹���CommonHandler���ͷ������
	 	this.createRemoteObject();
  }

   public function dispose():void{
	 var retObj:Object=this.call("dispose",arguments);

   }
   public function getLocalUser():User{
	 var retObj:Object=this.call("getLocalUser",arguments);
	 return retObj as User;

   }
 }

}
