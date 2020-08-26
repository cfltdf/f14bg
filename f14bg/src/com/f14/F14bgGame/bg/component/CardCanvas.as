package com.f14.F14bgGame.bg.component
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
		protected var _autoSort:Boolean = false;
		
		public function get columnWidth():int{
			return this._columnWidth;
		}
		
		public function set columnWidth(columnWidth:int):void{
			this._columnWidth = columnWidth;
		}
		
		public function get gap():int{
			return this._gap;
		}
		
		public function set gap(gap:int):void{
			this._gap = gap;
		}
		
		public function get autoSort():Boolean{
			return this._autoSort;
		}
		
		public function set autoSort(autoSort:Boolean):void{
			this._autoSort = autoSort;
		}
		
		override public function addChildAt(child:DisplayObject, index:int):DisplayObject{
			if(this.autoSort){
				//如果需要排序,则先检查实际插入的位置
				index = this.getSortedIndex(child);
			}
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
				i = 0;
				for each(var obj:DisplayObject in this.getChildren()){
					obj.x = offset + i * (this.columnWidth + gap);
					i += 1;
				}
			}
		}
		
		/**
		 * 移动对象
		 */
		public function moveChild(child:DisplayObject, offset:int):DisplayObject{
			var i:int = this.getChildIndex(child);
			if(i>=0){
				var t:int = i + offset;
				if(t<0){
					t = 0;
				}
				if(t>this.getChildren().length){
					t = this.getChildren().length;
				}
				return this.addChildAt(child, t);
			}else{
				return child;
			}
		}
		
		/**
		 * 取得排序后的index
		 */
		protected function getSortedIndex(object:Object):int{
			var children:Array = this.getChildren();
			if(children.length==0){
				return 0;
			}else{
				var i:int = 0;
				for each(var o2:Object in children){
					if(this.compareChild(object, o2)<0){
						return i;
					}
					i++;
				}
				return i;
			}
		}
		
		/**
		 * 比较排序对象的大小,默认用cardId排序
		 */
		protected function compareChild(o1:Object, o2:Object):int{
			//应该传入的都是ImageObject对象,需要取id比较
			var i1:int = o1.object.object!=null?o1.object.object.id:o1.object.id;
			var i2:int = o2.object.object!=null?o2.object.object.id:o2.object.id;
			if(i1>i2){
				return 1;
			}else if(i1<i2){
				return -1;
			}else{
				return 0;
			}
		}
		
	}
}