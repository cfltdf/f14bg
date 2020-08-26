package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.F14bgGame.bg.ui.simple.ImageTooltip;
	import com.f14.core.util.ApplicationUtil;
	
	import mx.managers.PopUpManager;
	
	/**
	 * 漂浮提示信息管理器
	 */
	public class TooltipManager
	{
		public function TooltipManager()
		{
			this.init();
		}
		
		protected var tooltip:ImageTooltip = new ImageTooltip();
		
		public function init():void{
			PopUpManager.addPopUp(tooltip, ApplicationUtil.application);
			tooltip.visible = false;
		}
		
		/**
		 * 显示tooltip
		 */
		public function showTooltip(x:int, y:int):void{
			PopUpManager.bringToFront(tooltip);
			tooltip.x = x;
			tooltip.y = y;
			tooltip.visible = true;
		}
		
		/**
		 * 隐藏tooltip
		 */
		public function hideTooltip():void{
			tooltip.visible = false;
		}

		public function getTooltipWidth():int{
			return tooltip.width;
		}
		
		public function getTooltipHeight():int{
			return tooltip.height;
		}
		
		/**
		 * 设置显示的图像对象
		 */
		public function setImage(image:ImageObject):void{
			tooltip.setImage(image);
		}

	}
}