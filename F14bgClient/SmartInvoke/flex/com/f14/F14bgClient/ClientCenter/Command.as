
/*******************************************************************************
* Copyright (c) 2009 SmartInvoke.
* �˴�������CodeTransform�����Զ�����
* ������ͨ�� 
*  ��վ:http://smartinvoke.cn/ 
*  �ʼ�:pzxiaoxiao130130@gmail.com
*  QQ��90636900*  ��ϵ������^_^ 
*******************************************************************************/ 
package com.f14.F14bgClient.ClientCenter
{
 import cn.smartinvoke.RemoteObject;
 public class Command extends RemoteObject {
  public function Command(){
	 super();
	  //���÷��񷽹���Command���ͷ������
	 	this.createRemoteObject();
  }

   public function getStr1():String{
	 var retObj:Object=this.call("getStr1",arguments);
	 return retObj as String;

   }
   public function getStr2():String{
	 var retObj:Object=this.call("getStr2",arguments);
	 return retObj as String;

   }
   public function setInt1(int1:int):void{
	 var retObj:Object=this.call("setInt1",arguments);

   }
   public function setStr2(str2:String):void{
	 var retObj:Object=this.call("setStr2",arguments);

   }
   public function setStr1(str1:String):void{
	 var retObj:Object=this.call("setStr1",arguments);

   }
   public function getInt1():int{
	 var retObj:Object=this.call("getInt1",arguments);
	 return Number(retObj);

   }
 }

}
