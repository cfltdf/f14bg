package com.f14.f14bg.components
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.MouseEvent;
	
	import mx.collections.ArrayCollection;
	
	public class ComponentOption
	{
		protected var _components:ArrayCollection = new ArrayCollection();
		protected var _selectedComponent:Object;
		protected var _multiSelection:Boolean = false;
		protected var _trigOnSelected:Boolean = false;
		
		public function ComponentOption(multiSelection:Boolean = false, trigOnSelected:Boolean = false)
		{
			this.multiSelection = multiSelection;
			this.trigOnSelected = trigOnSelected;
		}
		
		/**
		 * 设置组件是否允许多选
		 */
		public function set multiSelection(multiSelection:Boolean):void{
			this._multiSelection = multiSelection;
		}
		
		public function get multiSelection():Boolean{
			return this._multiSelection;
		}
		
		/**
		 * 设置是否在选择组件时就触发事件
		 */
		public function set trigOnSelected(trigOnSelected:Boolean):void{
			this._trigOnSelected = trigOnSelected;
		}
		
		public function get trigOnSelected():Boolean{
			return this._trigOnSelected;
		}
		
		public function addComponent(obj:EventDispatcher):void{
			this._components.addItem(obj);
			obj.addEventListener(MouseEvent.CLICK, onComponentClick);
		}
		
		public function removeComponent(obj:EventDispatcher):void{
			this.clearSelection();
			//移除监听函数
			obj.removeEventListener(MouseEvent.CLICK, onComponentClick);
			this._components.removeItemAt(this._components.getItemIndex(obj));
		}
		
		/**
		 * 移除所有组内的组件
		 */
		public function removeAllComponent():void{
			this.clearSelection();
			//移除所有监听函数
			for each(var obj:EventDispatcher in this._components.toArray()){
				obj.removeEventListener(MouseEvent.CLICK, onComponentClick);
			}
			this._components.removeAll();
		}
		
		public function get selectedComponent():Object{
			return this._selectedComponent;
		}
		
		public function set selectedComponent(selectedComponent:Object):void{
			if(this._components.contains(selectedComponent)){
				//只有当组件在该控件中,才会进行设置
				if(this._selectedComponent!=null){
					_selectedComponent.selected = false;
				}
				_selectedComponent = selectedComponent;
				if(this._selectedComponent!=null){
					_selectedComponent.selected = true;
				}
			}
		}
		
		protected function onComponentClick(evt:Event):void{
			if(evt.currentTarget.selectable){
				//如果不是在选择时触发事件,则显示选中组件时的效果
				if(!this._trigOnSelected){
					if(this.multiSelection){
						//多选
						evt.currentTarget.selected = !evt.currentTarget.selected;
					}else{
						//单选
						if(this._selectedComponent!=null){
							this._selectedComponent.selected = false;
						}
						this._selectedComponent = evt.currentTarget;
						this._selectedComponent.selected = true;
					}
				}
			}
		}
		
		/**
		 * 设置所有组件是否可选
		 */
		public function setAllComponentsSelectable(selectable:Boolean):void{
			this.clearSelection();
			for each(var obj:Object in this._components.toArray()){
				obj.selectable = selectable;
			}
		}
		
		/**
		 * 清除选择的组件
		 */
		public function clearSelection():void{
			if(this._selectedComponent!=null){
				this._selectedComponent.selected = false;
				this._selectedComponent = null;
			}
			for each(var obj:Object in this._components.toArray()){
				obj.selected = false;
			}
		}
		
		/**
		 * 取得所有选中的组件
		 */
		public function getSelection():Array{
			var res:Array = new Array();
			for each(var obj:Object in this._components.toArray()){
				if(obj.selected){
					res.push(obj);
				}
			}
			return res;
		}

	}
}