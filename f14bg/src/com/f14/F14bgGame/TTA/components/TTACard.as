package com.f14.F14bgGame.TTA.components
{
	import com.f14.F14bgGame.bg.component.ImageObject;
	import com.f14.F14bgGame.TTA.TTAUtil;
	import com.f14.F14bgGame.TTA.consts.CardType;
	import com.f14.F14bgGame.TTA.event.TTAEvent;
	import com.f14.F14bgGame.TTA.manager.TTAImageManager;
	import com.f14.F14bgGame.TTA.ui.simple.ImageString;
	
	import flash.events.Event;
	import flash.events.MouseEvent;
	
	import mx.controls.Button;
	
	public class TTACard extends ImageObject
	{
		public function TTACard(scale:Number=0.4)
		{
			super(scale);
			this.showTooltip = true;
		}
		
		protected var _blue:int;
		protected var _blueImage:ImageString;
		protected var _yellow:int;
		protected var _yellowImage:ImageString;
		protected var _red:int;
		protected var _redImage:ImageString;
		protected var _white:int;
		protected var _whiteImage:ImageString;
		
		protected var _ab:OvertimeCard;
		protected var _aImage:ImageString;
		protected var _bImage:ImageString;
		
		protected var _activable:Boolean = false;
		protected var _activeButton:Button;
		
		override protected function initImageSize():void{
			this._orgImageWidth = TTAImageManager.WIDTH_CARD;
			this._orgImageHeight = TTAImageManager.HEIGHT_CARD;
		}
		
		/**
		 * 设置卡牌是否可以使用
		 */
		public function set activable(activable:Boolean):void{
			if(this._activable!=activable){
				this._activable = activable;
				if(this._activeButton==null){
					//如果按键不存在,则创建按键
					this.createParts();
				}
				this._activeButton.visible = activable;
			}
		}
		
		/**
		 * 取得卡牌是否可以使用
		 */
		public function get activable():Boolean{
			return this._activable;
		}
		
		/**
		 * 设置蓝色标记的值
		 */
		public function set blue(value:int):void{
			this._blue = value;
			if(this._blueImage!=null){
				this._blueImage.value = String(value);
				this._blueImage.visible = (value!=0);
			}
		}
		
		public function get blue():int{
			return this._blue;
		}
		
		/**
		 * 设置黄色标记的值
		 */
		public function set yellow(value:int):void{
			this._yellow = value;
			if(this._yellowImage!=null){
				this._yellowImage.value = String(value);
				this._yellowImage.visible = (value!=0);
			}
		}
		
		public function get yellow():int{
			return this._yellow;
		}
		
		/**
		 * 设置红色标记的值
		 */
		public function set red(value:int):void{
			this._red = value;
			if(this._redImage!=null){
				this._redImage.value = String(value);
				this._redImage.visible = (value!=0);
			}
		}
		
		public function get red():int{
			return this._red;
		}
		
		/**
		 * 设置白色标记的值
		 */
		public function set white(value:int):void{
			this._white = value;
			if(this._whiteImage!=null){
				this._whiteImage.value = String(value);
				this._whiteImage.visible = (value!=0);
			}
		}
		
		public function get white():int{
			return this._white;
		}
		
		/**
		 * 装载图像
		 */
		override protected function loadObjectImage():void{
			if(this.object==null){
				this.loadImage(null);
			}else{
				this.loadImage(TTAUtil.imageManager.getCardImage(this.object.imageIndex));
				this.createTokenImage();
			}
		}
		
		/**
		 * 按照卡牌类型创建标记
		 */
		protected function createTokenImage():void{
			switch(this.object.cardType){
				case CardType.PRODUCTION: //生产
				case CardType.BUILDING: //建筑
				case CardType.UNIT: //部队
				case CardType.WONDER: //奇迹
					//为这些卡牌创建蓝色和黄色的标记
					this._blueImage = new ImageString();
					this.addChild(this._blueImage);
					this._blueImage.image.source = TTAImageManager.BLUE_IMAGE;
					this._blueImage.x = 12;
					this._blueImage.y = 65;
					this.blue = 0;
					
					this._yellowImage = new ImageString();
					this.addChild(this._yellowImage);
					this._yellowImage.image.source = TTAImageManager.YELLOW_IMAGE;
					this._yellowImage.x = 38;
					this._yellowImage.y = 65;
					this.yellow = 0;
					break;
				case CardType.GOVERMENT: //政府
					//为这些卡牌创建红色和白色的标记
					this._whiteImage = new ImageString();
					this.addChild(this._whiteImage);
					this._whiteImage.image.source = TTAImageManager.WHITE_IMAGE;
					this._whiteImage.x = 12;
					this._whiteImage.y = 65;
					this.white = 0;
					
					this._redImage = new ImageString();
					this.addChild(this._redImage);
					this._redImage.image.source = TTAImageManager.RED_IMAGE;
					this._redImage.x = 38;
					this._redImage.y = 65;
					this.red = 0;
					break;
			}
		}
		
		/**
		 * 创建卡牌的其他部件
		 */
		protected function createParts():void{
			this._activeButton = new Button();
			this._activeButton.label = "使用";
			this._activeButton.width = 60;
			this._activeButton.height = 25;
			this._activeButton.x = (this.width - 60)/2;
			this._activeButton.y = this.height - 35;
			this._activeButton.visible = false;
			this._activeButton.addEventListener(MouseEvent.CLICK, onActiveButtonClick);
			this.addChild(this._activeButton);
		}
		
		/**
		 * 使用按钮点击时触发的事件
		 */
		protected function onActiveButtonClick(event:Event):void{
			var evt:TTAEvent = new TTAEvent(TTAEvent.ACTIVE_CARD);
			evt.card = this;
			this.dispatchEvent(evt);
		}
		
		public function get ab():OvertimeCard{
			return this._ab;
		}
		
		/**
		 * 设置ab玩家的显示状态
		 */
		public function setAB(card:OvertimeCard):void{
			this._ab = card;
			if(card!=null){
				if(this._aImage==null){
					this._aImage = new ImageString();
					this.addChild(this._aImage);
					this._aImage.image.source = TTAUtil.imageManager.getPlayerTokenImage(this._ab.a);
					this._aImage.x = 7;
					this._aImage.y = 65;
					this._aImage.visible = true;
				}
				if(this._bImage==null){
					this._bImage = new ImageString();
					this.addChild(this._bImage);
					this._bImage.image.source = TTAUtil.imageManager.getPlayerTokenImage(this._ab.b);
					this._bImage.x = 42;
					this._bImage.y = 65;
					this._bImage.visible = true;
				}
			}
		}
	}
}