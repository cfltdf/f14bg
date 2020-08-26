package com.f14.core.util
{
	import com.f14.resource.ImageResource;
	
	import mx.managers.CursorManager;
	import mx.managers.CursorManagerPriority;
	
	/**
	 * 鼠标指针辅助类
	 */
	public class CursorUtil
	{
		private static var cursorID:Number;
		
		/**
         * change cursor.
         * @param type
         * @param x
         * @param y
         */               
        public static function changeCursor(type:Class, x:Number, y:Number):void{
            if(cursorID >= 0){
            	CursorManager.removeCursor(cursorID);
            }
            if(type != null){
            	cursorID = CursorManager.setCursor(type, CursorManagerPriority.MEDIUM, x, y);
            }
        }
        
        /**
        * 显示手型目标鼠标
        */
        public static function showHandCursor():void{
        	changeCursor(ImageResource.getSrc("defaultAvatar"), 0, 0);
        }
        
        /**
        * 重置鼠标形状
        */
        public static function resetCursor():void{
        	changeCursor(null, 0, 0);
        }
	}
}