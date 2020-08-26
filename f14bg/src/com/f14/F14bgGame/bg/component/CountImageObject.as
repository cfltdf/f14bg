package com.f14.F14bgGame.bg.component
{
	import mx.controls.Text;
	
	public class CountImageObject extends ImageObject
	{
		public function CountImageObject(scale:Number=1)
		{
			super(scale);
			this.initCounter();
		}
		
		protected var counterText:Text;
		protected var _num:int;
		
		/**
		 * 初始化计数部件
		 */
		protected function initCounter():void{
			this.counterText = new Text();
			this.counterText.width = 60;
			this.counterText.height = 20;
			this.counterText.setStyle("fontWeight", "bold");
			this.counterText.setStyle("textAlign", "right");
			this.counterText.setStyle("fontSize", "14");
			this.counterText.selectable = false;
			this.counterText.x = this.width - this.counterText.width;
			this.counterText.y = this.height - this.counterText.height;
			this.addChild(this.counterText);
			this._colorEffect = true;
			this.refreshCounterText();
		}
		
		public function set num(num:int):void{
			this._num = num;
			this.refreshCounterText();
		}
		
		public function get num():int{
			return this._num;
		}
		
		/**
		 * 刷新显示的计数值
		 */
		public function refreshCounterText():void{
			var text:String = "x " + this.num;
			this.counterText.text = text;
			if(this.num==0){
				//等于0时显示为黑白效果
				this.setColorEffect(false);
			}else{
				this.setColorEffect(true);
			}
		}
		
	}
}