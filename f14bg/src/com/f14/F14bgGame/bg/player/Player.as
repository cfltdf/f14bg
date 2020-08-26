package com.f14.F14bgGame.bg.player
{
	import com.f14.F14bgGame.bg.ui.PlayerBoard;
	import com.f14.F14bgGame.bg.ui.PlayerInfoBoard;
	import com.f14.f14bg.event.BgEvent;
	
	import flash.events.EventDispatcher;
	
	public class Player extends EventDispatcher
	{
		public function Player()
		{
		}
		
		public var id:String;
		public var loginName:String;
		[Bindable]
		public var name:String;
		public var position:int;
		protected var _playerState:String;
		protected var _playingState:String;
		protected var _playerBoard:PlayerBoard;
		protected var _playerInfoBoard:PlayerInfoBoard;
		
		public function set playerState(playerState:String):void{
			this._playerState = playerState;
			var evt:BgEvent = new BgEvent(BgEvent.PLAYER_STATE_CHANGE);
			evt.param.player = this;
			evt.param.playerState = playerState;
			this.dispatchEvent(evt);
		}
		
		public function get playerState():String{
			return this._playerState;
		}
		
		public function set playingState(playingState:String):void{
			this._playingState = playingState;
			var evt:BgEvent = new BgEvent(BgEvent.PLAYING_STATE_CHANGE);
			evt.param.player = this;
			evt.param.playingState = playingState;
			this.dispatchEvent(evt);
		}
		
		public function get playingState():String{
			return this._playingState;
		}
		
		public function set playerBoard(playerBoard:PlayerBoard):void{
			if(this._playerBoard!=null){
				this._playerBoard.player = null;
			}
			this._playerBoard = playerBoard;
			if(playerBoard!=null){
				playerBoard.player = this;
			}
		}
		
		public function get playerBoard():PlayerBoard{
			return this._playerBoard;
		}
		
		public function set playerInfoBoard(playerInfoBoard:PlayerInfoBoard):void{
			if(this._playerInfoBoard!=null){
				this._playerInfoBoard.player = null;
			}
			this._playerInfoBoard = playerInfoBoard;
			if(playerInfoBoard!=null){
				playerInfoBoard.player = this;
			}
		}
		
		public function get playerInfoBoard():PlayerInfoBoard{
			return this._playerInfoBoard;
		}
		
		/**
		 * 清理玩家的游戏信息
		 */
		public function clear():void{
			this.playerState = null;
			this.playingState = null;
			
			this.playerBoard = null;
			this.playerInfoBoard = null;
			
			/* if(this._playerBoard!=null){
				this._playerBoard.player = null;
				this._playerBoard = null;
			}
			if(this._playerInfoBoard!=null){
				this._playerInfoBoard.player = null;
			} */
		}
	}
}