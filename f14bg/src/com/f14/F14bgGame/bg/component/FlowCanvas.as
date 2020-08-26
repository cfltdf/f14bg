package com.f14.F14bgGame.bg.component
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
		protected var _columnScale:int = 1;
		
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
			var columnNum:int = Math.floor(this.width / this.columnWidth) * this._columnScale;
			var i:int = 0;
			for each(var obj:DisplayObject in this.getChildren()){
				obj.x = i%columnNum * (this.columnWidth + this.horizontalGap);
				obj.y = Math.floor(i/columnNum) * (this.rowHeight + this.verticalGap);
				i += 1;
			}
		}
		
		/**
		 * 按比例缩放容器,并保持原显示大小
		 */
		public function setScale(s:Number):void{
			this._columnScale *= s;
			this.width = this.width * s;
			this.height = this.height * s;
			this.scaleX *= 1 / s;
			this.scaleY *= 1 / s;
			this.refreshLayout();
		}
		
	}
}