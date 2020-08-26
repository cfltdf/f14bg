package com.f14.F14bgGame.bg.component
{
	import mx.containers.HBox;

	/**
	 * 可选择的hbox容器
	 */
	public class SelectableHbox extends HBox implements Selectable
	{
		public function SelectableHbox()
		{
			super();
		}
		
		protected var _selectable:Boolean;
		
		public function set selectable(selectable:Boolean):void
		{
			this._selectable = selectable;
			//如果子控件为selectable控件,则设置其可选状态
			for each(var obj:Object in this.getChildren()){
				if(obj is Selectable){
					obj.selectable = this.selectable;
				}
			}
		}
		
		public function get selectable():Boolean
		{
			return this._selectable;
		}
		
	}
}