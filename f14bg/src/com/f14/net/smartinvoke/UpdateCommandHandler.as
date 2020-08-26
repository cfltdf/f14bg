package com.f14.net.smartinvoke
{
 import cn.smartinvoke.IServerObject;
 
 import com.f14.F14bgClient.update.UpdateUtil;
 public class UpdateCommandHandler implements IServerObject {
  public function UpdateCommandHandler(){
  }

   public function loadParam(param:Object):void{
   	UpdateUtil.module.loadParam(param);
   }
   
   public function refreshSize(currentSize:int):void{
   	UpdateUtil.module.refreshSize(currentSize);
   }
   
 }

}
