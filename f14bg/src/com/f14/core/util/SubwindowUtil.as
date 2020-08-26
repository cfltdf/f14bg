package com.f14.core.util
{
	import com.f14.core.controls.window.BaseWindow;
	
	import flash.display.DisplayObject;
	
	import mx.collections.ArrayCollection;
	import mx.managers.PopUpManager;
	
	/**
	 * 子窗口辅助类
	 */
	public class SubwindowUtil
	{
		private static var _windows:ArrayCollection = new ArrayCollection();
		
		/**
		 * 显示子窗口
		 */
		public static function show(window:BaseWindow, parent:DisplayObject, modal:Boolean = false):void{
			PopUpManager.addPopUp(window, parent, modal);
			if(window.neverOpened){
				PopUpManager.centerPopUp(window);
				window.neverOpened = false;
			}
			_windows.addItem(window);
		}
		
		/**
		 * 隐藏子窗口
		 */
		public static function hide(window:BaseWindow):void{
			PopUpManager.removePopUp(window);
			var i:int = _windows.getItemIndex(window);
			if(i>=0){
				_windows.removeItemAt(i);
			}
		}
		
		/**
		 * 隐藏所有子窗口
		 */
		public static function hideAll():void{
			for(var i:int=_windows.length-1;i>=0;i--){
				_windows[i].hide();
			}
		}
		
		/**
		 * 将子窗口显示到最前面
		 */
		public static function front(window:BaseWindow):void{
			PopUpManager.bringToFront(window);
		}
	}
}