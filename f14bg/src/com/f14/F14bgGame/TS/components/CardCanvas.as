package com.f14.F14bgGame.TS.components
{
	import flash.display.DisplayObject;
	import flash.events.Event;
	
	import mx.containers.Canvas;
	import mx.events.ResizeEvent;

	/**
	 * 专门用来放置卡牌的Canvas,会自动缩进超出长度的卡牌
	 */
	public class CardCanvas extends Canvas
	{
		public function CardCanvas()
		{
			super();
			this.addEventListener(ResizeEvent.RESIZE, refreshLayout);
		}
		
		protected var _columnWidth:int = -1;
		protected var _gap:int = 0;
		
		public function get columnWidth():int{
			return this._columnWidth;
		}
		
		public function get gap():int{
			return this._gap;
		}
		
		public function set gap(gap:int):void{
			this._gap = gap;
		}
		
		override public function addChildAt(child:DisplayObject, index:int):DisplayObject{
			var res:DisplayObject = super.addChildAt(child, index);
			//按照第一个加入的对象,设置默认的列宽
			if(this._columnWidth==-1){
				this._columnWidth = child.width;
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
			var size:int = this.getChildren().length;
			var totalWidth:int = size*(this.columnWidth+gap);
			if(totalWidth>this.width){
				//如果超出长度,则需要缩进
				//总之最后一张牌都是可以完全显示的,所以需要缩进的是前N张
				var width:int = this.width - this.columnWidth - gap;
				//计算每张牌所占的实际宽度
				var sw:int = width / (size-1);
				//进行排列
				var i:int = 0;
				for each(var obj:DisplayObject in this.getChildren()){
					obj.x = i * sw;
					i += 1;
				}
			}else{
				//否则就排列,并且居中
				var offset:int = (this.width - totalWidth)/2;
				var i:int = 0;
				for each(var obj:DisplayObject in this.getChildren()){
					obj.x = offset + i * (this.columnWidth + gap);
					i += 1;
				}
			}
		}
		
	}
}