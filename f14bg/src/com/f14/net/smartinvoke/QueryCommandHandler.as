package com.f14.net.smartinvoke
{
 import cn.smartinvoke.IServerObject;
 
 import com.adobe.serialization.json.JSON;
 import com.f14.F14bgClient.query.QueryUtil;
 public class QueryCommandHandler implements IServerObject {
  public function QueryCommandHandler(){
  }

   public function loadUserParam(paramString:String):void{
   	var param:Object = JSON.decode(paramString);
   	QueryUtil.module.loadParam(param);
   }
   
 }

}
