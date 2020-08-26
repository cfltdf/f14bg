package com.f14.F14bgGame.bg.manager
{
	import com.f14.F14bgClient.room.RoomUtil;
	import com.f14.F14bgGame.bg.ui.simple.AlertItem;
	import com.f14.F14bgGame.bg.ui.simple.CommonAlertItem;
	import com.f14.core.util.ApplicationUtil;
	
	import flash.filters.BlurFilter;
	
	import mx.collections.ArrayCollection;
	import mx.effects.Fade;
	import mx.effects.IEffectInstance;
	import mx.effects.Move;
	import mx.events.EffectEvent;
	
	public class AlertManager
	{
		public function AlertManager()
		{
			this.init();
		}
		
		protected var fadeEffect:Fade;
		protected var moveEffect:Move;
		protected var blurFilter:BlurFilter;
		protected var items:ArrayCollection;
		protected var moving:Boolean;
		protected var messageArray:ArrayCollection;
		
		
		protected function init():void{
			this.fadeEffect = new Fade();
			fadeEffect.duration = 3000;
			fadeEffect.alphaFrom = 1;
			fadeEffect.alphaTo = 0;
			fadeEffect.startDelay = 5000;
			
			this.moveEffect = new Move();
			moveEffect.duration = 500;
			moveEffect.yBy = -50 - 5;
			
			//该过滤器用来将文本图片化
			this.blurFilter = new BlurFilter(0,0,0);
			
			items = new ArrayCollection();
			messageArray = new ArrayCollection();
		}
		
		/**
		 * 创建新的提示信息对象
		 */
		public function createNewAlertItem():AlertItem{
			return new CommonAlertItem();
		}
		
		/**
		 * 提示信息
		 */
		public function alertMessages(messages:Array):void{
			for each(var object:Object in messages){
				this.alertMessage(object);
			}
		}
		
		/**
		 * 提示信息
		 */
		public function alertMessage(message:Object):void{
			if(message.alert){
				this.messageArray.addItem(message);
				this.checkNewMessage();
			}
		}
		
		/**
		 * 检查是否有新消息
		 */
		protected function checkNewMessage():void{
			//如果等待队列中不为空,且不在移动,则显示新消息
			if(this.messageArray.length>0 && !this.moving){
				//将所有对象上移
				this.moveUpItems();
				//创建新的提示对象
				var message:Object = this.messageArray.removeItemAt(0);
				var item:AlertItem = this.createNewAlertItem();
				item.y = 180;
				item.x = RoomUtil.application.width / 2 - item.width / 2;
				RoomUtil.application.addChild(item);
				item.message = message.message;
				item.filters = [this.blurFilter];
				item.addEventListener(EffectEvent.EFFECT_END, effectEnd);
				this.items.addItem(item);
				
				var i:IEffectInstance = this.fadeEffect.createInstance(item);
				i.startEffect();
			}
		}
		
		/**
		 * 特效结束时触发的方法
		 */
		protected function effectEnd(evt:EffectEvent):void{
			switch(evt.effectInstance.className){
				case "FadeInstance": //fade特效结束后,移除该对象
					var obj:AlertItem = evt.currentTarget as AlertItem;
					obj.removeEventListener(EffectEvent.EFFECT_END, effectEnd);
					ApplicationUtil.application.removeChild(obj);
					this.items.removeItemAt(this.items.getItemIndex(obj));
					break;
				case "MoveInstance": //move特效结束后,设置标志位
					this.moving = false;
					this.checkNewMessage();
					break;
			}
		}
		
		/**
		 * 将所有的对象上移
		 */
		protected function moveUpItems():void{
			if(this.items.length>0){
				this.moving = true;
				for each(var item:AlertItem in this.items){
					var i:IEffectInstance = moveEffect.createInstance(item);
					i.startEffect();
				}
			}
		}
	}
}