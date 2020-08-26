package com.f14.F14bgGame.bg.component
{
	/**
	 * 可选择控件的接口
	 */
	public interface Selectable
	{
		/**
		 * 设置控件是否可选
		 */
		function set selectable(selectable:Boolean):void;
		
		/**
		 * 取得控件是否可选
		 */
		function get selectable():Boolean;
	}
}