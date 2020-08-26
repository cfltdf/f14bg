package com.f14.F14bgGame.TS.components
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import mx.containers.Canvas;
	import mx.events.ResizeEvent;

	/**
	 * 每行放满后换到下一行
	 */
	public class FlowCanvas extends Canvas
	{
		public function FlowCanvas()
		{
			super();
			this.addEventListener(ResizeEvent.RESIZE, refreshLayout);
		}
		
		protected var _columnWidth:int = -1;
		protected var _rowHeight:int = -1;
		protected var _verticalGap:int = 0;
		protected var _horizontalGap:int = 0;
		protected var _addToFirst:Boolean = false;
		
		public function get columnWidth():int{
			return this._columnWidth;
		}
		
		public function get rowHeight():int{
			return this._rowHeight;
		}
		
		public function get verticalGap():int{
			return this._verticalGap;
		}
		
		public function set verticalGap(gap:int):void{
			this._verticalGap = gap;
		}
		
		public function get horizontalGap():int{
			return this._horizontalGap;
		}
		
		public function set horizontalGap(gap:int):void{
			this._horizontalGap = gap;
		}
		
		public function set addToFirst(addToFirst:Boolean):void{
			this._addToFirst = addToFirst;
		}
		
		public function get addToFirst():Boolean{
			return this._addToFirst;
		}
		
		override public function addChildAt(child:DisplayObject, index:int):DisplayObject{
			var res:DisplayObject = super.addChildAt(child, index);
			//按照第一个加入的对象,设置默认的列宽
			if(this._columnWidth==-1){
				this._columnWidth = child.width;
				this._rowHeight = child.height;
			}
			//重新布局
			this.refreshLayout();
			return res;
		}
		
		override public function removeChild(child:DisplayObject):DisplayObject{
			var res:DisplayObject = super.removeChild(child);
			//重新布局
			this.refreshLayout();
			return res;
		}
		
		/**
		 * 刷新布局
		 */
		public function refreshLayout(event:Event=null):void{
			var columnNum:int = Math.floor(this.width / this.columnWidth);
			//居中,计算起始位置
			var x:int = (this.width - (this.columnWidth + this.horizontalGap)*columnNum)/2
			var i:int = 0;
			for each(var obj:DisplayObject in this.getChildren()){
				obj.x = x + i%columnNum * (this.columnWidth + this.horizontalGap);
				obj.y = Math.floor(i/columnNum) * (this.rowHeight + this.verticalGap);
				i += 1;
			}
		}
		
	}
}