package com.f14.F14bgGame.bg.manager
{
	import flash.geom.Point;
	
	import mx.containers.Canvas;
	import mx.core.UIComponent;
	import mx.effects.Effect;
	import mx.effects.Fade;
	import mx.effects.Move;
	import mx.effects.Zoom;
	import mx.events.EffectEvent;
	
	public class AnimManager
	{
		public function AnimManager()
		{
			this.init();
		}

		public var lastInputState:String;
				
		private var afterToo:Point = null;
		private var afterFadeOut:Boolean = false;
		private var origWidth:int = 0;
		private var origHeight:int = 0;
		
		private var _isPlaying:Boolean= false;
		private var _animCanvas:Canvas;
		
		protected var _move:Move = new Move();
		protected var _zoom:Zoom = new Zoom();
		protected var _fadeOut:Fade = new Fade();
		
		protected function init():void{
			this.moveEffect.duration = 500;
			this.moveEffect.addEventListener(EffectEvent.EFFECT_END, onAnimOver);
			
			this.zoomEffect.duration = 450;
			
			this.fadeOutEffect.duration = 350;
			this.fadeOutEffect.alphaFrom = 1;
			this.fadeOutEffect.alphaTo = 0;
			this.fadeOutEffect.addEventListener(EffectEvent.EFFECT_END, onAnimOver);
		}
		
		/**
		 * 取得动画效果所在的画布容器
		 */
		public function get animCanvas():Canvas{
			return this._animCanvas;
		}
		
		/**
		 * 设置动画效果所在的画布容器
		 */
		public function set animCanvas(animCanvas:Canvas):void{
			this._animCanvas = animCanvas;
		}
		
		/**
		 * 取得移动效果对象
		 */
		protected function get moveEffect():Move{
			return this._move;
		}
		
		/**
		 * 取得缩放效果对象
		 */
		protected function get zoomEffect():Zoom{
			return this._zoom;
		}
		
		/**
		 * 取得淡出效果对象
		 */
		protected function get fadeOutEffect():Fade{
			return this._fadeOut;
		}
		
		
		/**
		 * 判断是否正在播放动画效果
		 */
		public function get isPlaying():Boolean{
			return this._isPlaying;
		}
		
		/**
		 * 设置是否正在播放动画效果
		 */
		public function set isPlaying(isPlaying:Boolean):void{
			this._isPlaying = isPlaying;
			if(DefaultManagerUtil.gameManager.localPlayer!=null){
				//存在当前玩家才检查输入状态
				if(isPlaying){
					//如果开始动画,则禁止输入
					if(this.lastInputState==null){
						this.lastInputState = DefaultManagerUtil.gameManager.localPlayer.playerBoard.inputState;
					}
					DefaultManagerUtil.module.disableAllInput();
				}else{
					//如果动画执行完成,则刷新当前玩家的界面输入状态
					DefaultManagerUtil.gameManager.localPlayer.playerBoard.inputState = this.lastInputState;
					this.lastInputState = null;
				}
			}
		}
		
		/**
		 * 设置初始尺寸
		 */
		protected function setOrigSize(target:UIComponent):void{
			if(target==null){
				this.origHeight = 0;
				this.origWidth = 0;
			}else{
				this.origWidth = target.width;
				this.origHeight = target.height;
			}
		}
		
		/**
		 * 处理动画效果的指令
		 */
		public function processAnimCommand(param:Object):void{
			
		}
		
		/**
		 * 执行移动效果
		 */
		protected function move(target:UIComponent, from:Point, too:Point, setOrigSize:Boolean=true, scaleFrom:Number=1, scaleToo:Number=1, delay:Number=0):void{
			this.isPlaying = true;
			if(setOrigSize){
				this.setOrigSize(target);
			}
			this.animCanvas.addChild(target);
			moveEffect.target = target;
			moveEffect.xFrom = from.x-this.origWidth*scaleFrom/2;
			moveEffect.yFrom = from.y-this.origHeight*scaleFrom/2;
			moveEffect.xTo = too.x-this.origWidth*scaleToo/2;
			moveEffect.yTo = too.y-this.origHeight*scaleToo/2;
			moveEffect.startDelay = delay;
			this.zoom(target, scaleFrom, scaleToo, delay);
			moveEffect.play();
		}
		
		/**
		 * 执行缩放效果
		 */
		protected function zoom(target:UIComponent, from:Number, too:Number, delay:Number=0):void{
			if(from!=too){
				//只有在前后大小不一样时才执行缩放效果
				zoomEffect.target = target;
				zoomEffect.zoomHeightFrom = from;
				zoomEffect.zoomWidthFrom = from;
				zoomEffect.zoomHeightTo = too;
				zoomEffect.zoomWidthTo = too;
				zoomEffect.startDelay = delay;
				zoomEffect.play();
			}
		}
		
		/**
		 * 执行淡出效果
		 */
		protected function fadeOut(target:UIComponent, delay:Number=0):void{
			fadeOutEffect.target = target;
			fadeOutEffect.startDelay = delay;
			fadeOutEffect.play();
		}
		
		/**
		 * 执行展示效果
		 */
		protected function reveal(target:UIComponent, from:Point, too:Point):void{
			var cp:Point = this.getAnimPosition(this.animCanvas);
			this.afterToo = too;
			this.move(target, from, cp, true, 1, 3);
		}
		
		/**
		 * 执行展示效果
		 */
		protected function revealAndFadeOut(target:UIComponent, from:Point):void{
			var cp:Point = this.getAnimPosition(this.animCanvas);
			this.afterFadeOut = true;
			this.move(target, from, cp, true, 1, 3);
		}
		
		/**
		 * 执行显示并淡出效果
		 */
		protected function showAndFadeOut(target:UIComponent):void{
			var cp:Point = this.getAnimPosition(this.animCanvas);
			this.afterFadeOut = true;
			this.move(target, cp, cp, true, 1, 1);
		}
		
		/**
		 * 动画结束时,触发的事件
		 */
		protected function onAnimOver(event:EffectEvent):void{
			var effect:Effect = Effect(event.currentTarget);
			var target:UIComponent = UIComponent(effect.target);
			if(this.afterToo!=null){
				//如果存在后续节点,则继续执行动画效果
				var cp:Point = this.getAnimPosition(this.animCanvas);
				this.move(target, cp, afterToo, false, 3, 1, 500);
				this.afterToo = null;
			}else if(this.afterFadeOut){
				//如果需要淡出,则执行淡出效果
				this.fadeOut(target, 500);
				this.afterFadeOut = false;
			}else{
				this.animCanvas.removeChild(target);
				//检查是否存在需要执行的命令
				this.isPlaying = false;
				DefaultManagerUtil.module.gameCommandHandler.isProcessing = false;
				DefaultManagerUtil.module.gameCommandHandler.checkProcessGameCommand();
			}
		}
		
		/**
		 * 取得目标的中点在动画面板中的实际位置
		 */
		protected function getAnimPosition(target:UIComponent):Point{
			var point:Point = new Point()
			point.x = target.width/2;
			point.y = target.height/2;
			point = target.localToGlobal(point);
			return this.animCanvas.globalToLocal(point);
		}
		
		/**
		 * 取得目标中的点在动画面板中的实际位置
		 */
		protected function getAnimPositionByPoint(target:UIComponent, p:Point):Point{
			var point:Point = target.localToGlobal(p);
			return this.animCanvas.globalToLocal(point);
		}

	}
}