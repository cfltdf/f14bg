
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
 import java.util.HashMap;
 public class ClientShell extends RemoteObject {
  public function ClientShell(){
	 super();
	  //���÷��񷽹���ClientShell���ͷ������
	 	this.createRemoteObject();
  }

   public function getBytes():Array{
	 var retObj:Object=this.call("getBytes",arguments);
	 return retObj as Array;

   }
   public function getCommand():HashMap{
	 var retObj:Object=this.call("getCommand",arguments);
	 return retObj as HashMap;

   }
   public function dispose():void{
	 var retObj:Object=this.call("dispose",arguments);

   }
   public function setState(state:String):String{
	 var retObj:Object=this.call("setState",arguments);
	 return retObj as String;

   }
   public function getString():String{
	 var retObj:Object=this.call("getString",arguments);
	 return retObj as String;

   }
 }

}
