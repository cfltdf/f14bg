package com.f14.F14bgGame.PuertoRico.components
{
	import com.f14.F14bgGame.bg.component.ImageObject;

	public class ImageTile extends ImageObject
	{
		public static var IMAGE_WIDTH:int = 45;
		public static var IMAGE_HEIGHT:int = 45;
		
		public function ImageTile(scale:Number=1)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		protected var _colonists:Array = new Array();
		
		override protected function initImageSize():void{
			this._orgImageWidth = IMAGE_WIDTH;
			this._orgImageHeight = IMAGE_HEIGHT;
		}
		
		public function get colonists():Array{
			return this._colonists;
		}
		
		/**
		 * 创建移民组件
		 */
		public function createColonists():void{
			this.removeAllColonists();
			if(object!=null && object.colonistMax>0){
				var c:ColonistPart;
				for(var i:int=0;i<object.colonistMax;i++){
					c = new ColonistPart();
					c.x = i*(PartTile.IMAGE_WIDTH + 0) + 3;
					c.y = this.height - PartTile.IMAGE_HEIGHT - 3;
					c.selected = false;
					this.addChild(c);
					this._colonists.push(c);
				}
			}
		}
		
		/**
		 * 移除所有的移民组件
		 */
		public function removeAllColonists():void{
			for each(var c:PartTile in this._colonists){
				this.removeChild(c);
			}
			this._colonists = new Array();
		}
		
		/**
		 * 取得选中的移民数
		 */
		public function getColonistNum():int{
			var num:int = 0;
			for each(var c:PartTile in this._colonists){
				if(c.selected){
					num++;
				}
			}
			return num;
		}
		
		/**
		 * 设置选中的移民数
		 */
		public function setColonistNum(num:int):void{
			for(var i:int=0;i<this._colonists.length;i++){
				if(i<num){
					this._colonists[i].selected = true;
				}else{
					this._colonists[i].selected = false;
				}
			}
		}
		
	}
}