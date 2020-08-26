package com.f14.F14bgGame.PuertoRico.player
{
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.F14bgGame.PuertoRico.components.ImageTile;
	import com.f14.F14bgGame.PuertoRico.ui.PrPlayerBoard;

	public class PrPlayer extends Player
	{
		public function PrPlayer()
		{
			super();
		}
		
		[Bindable]
		public var vp:int;
		[Bindable]
		public var doubloon:int;
		[Bindable]
		public var colonist:int;
		[Bindable]
		public var corn:int;
		[Bindable]
		public var indigo:int;
		[Bindable]
		public var sugar:int;
		[Bindable]
		public var tobacco:int;
		[Bindable]
		public var coffee:int;
		
		public function get prPlayerBoard():PrPlayerBoard{
			return this._playerBoard as PrPlayerBoard;
		}
		
		/**
		 * 为玩家设置资源
		 */
		public function setResource(parts:Object):void{
			for(var part:String in parts){
				switch(part){
					case "vp":
						this.vp = parts[part];
						break;
					case "COLONIST":
						this.colonist = parts[part];
						break;
					case "CORN":
						this.corn = parts[part];
						break;
					case "INDIGO":
						this.indigo = parts[part];
						break;
					case "SUGAR":
						this.sugar = parts[part];
						break;
					case "TOBACCO":
						this.tobacco = parts[part];
						break;
					case "COFFEE":
						this.coffee = parts[part];
						break;
				}
			}
		}
		
		/**
		 * 玩家得到资源
		 */
		public function addResource(parts:Object):void{
			for(var part:String in parts){
				switch(part){
					case "vp":
						this.vp += parts[part];
						break;
					case "COLONIST":
						this.colonist += parts[part];
						break;
					case "CORN":
						this.corn += parts[part];
						break;
					case "INDIGO":
						this.indigo += parts[part];
						break;
					case "SUGAR":
						this.sugar += parts[part];
						break;
					case "TOBACCO":
						this.tobacco += parts[part];
						break;
					case "COFFEE":
						this.coffee += parts[part];
						break;
				}
			}
			/* this.vp += parts.vp;
			this.colonist += parts.COLONIST;
			this.corn += parts.CORN;
			this.indigo += parts.INDIGO;
			this.sugar += parts.SUGAR;
			this.tobacco += parts.TOBACCO;
			this.coffee += parts.COFFEE; */
		}
		
		/**
		 * 添加种植园板块
		 */
		public function addPlantation(obj:Object):void{
			if(this.prPlayerBoard){
				this.prPlayerBoard.playerBuildingBoard.addPlantation(obj);
			}
		}
		
		/**
		 * 移除种植园板块
		 */
		public function removePlantation(id:String):void{
			if(this.prPlayerBoard){
				this.prPlayerBoard.playerBuildingBoard.removePlantation(id);
			}
		}
		
		/**
		 * 添加建筑板块
		 */
		public function addBuilding(obj:Object):void{
			if(this.prPlayerBoard){
				this.prPlayerBoard.playerBuildingBoard.addBuilding(obj);
			}
		}
		
		/**
		 * 取得移民分配情况
		 */
		public function getColonistInfo():Object{
			var obj:Object = new Object();
			var ids:String = "";
			var nums:String = "";
			var tiles:Array = this.getAllTiles();
			for each(var tile:ImageTile in tiles){
				ids += tile.id + ",";
				nums += tile.getColonistNum() + ",";
			}
			if(ids.length>0){
				ids = ids.substring(0, ids.length-1);
				nums = nums.substring(0, nums.length-1);
			}
			obj.ids = ids;
			obj.nums = nums;
			obj.restNum = this.colonist;
			return obj;
		}
		
		/**
		 * 取得玩家所有的建筑和种植园板块
		 */
		public function getAllTiles():Array{
			var res:Array = new Array();
			var obj:Object;
			for each(obj in this.prPlayerBoard.playerBuildingBoard.canvas_building.getChildren()){
				res.push(obj);
			}
			for each(obj in this.prPlayerBoard.playerBuildingBoard.canvas_plantation.getChildren()){
				res.push(obj);
			}
			return res;
		}
		
		/**
		 * 装载玩家的移民分配情况
		 */
		public function loadColonistInfo(param:Object):void{
			this.colonist = param.restNum;
			//设置每个建筑的移民数
			for each(var obj:Object in param.details){
				var tile:ImageTile = this.getTileById(obj.id);
				if(tile!=null){
					tile.setColonistNum(obj.colonist);
				}
			}
		}
		
		/**
		 * 按照id取得对应的建筑或种植园板块
		 */
		public function getTileById(id:String):ImageTile{
			var tile:ImageTile;
			for each(tile in this.prPlayerBoard.playerBuildingBoard.canvas_building.getChildren()){
				if(tile.id==id){
					return tile;
				}
			}
			for each(tile in this.prPlayerBoard.playerBuildingBoard.canvas_plantation.getChildren()){
				if(tile.id==id){
					return tile;
				}
			}
			return null;
		}
		
		/**
		 * 设置玩家是否是总督
		 */
		public function set governor(boolean:Boolean):void{
			this.prPlayerBoard.playerInfoBoard.governor = boolean;
		}
		
		/**
		 * 设置玩家的角色
		 */
		public function set character(character:String):void{
			this.prPlayerBoard.playerInfoBoard.character = character;
		}
		
		/**
		 * 设置是否是当前回合
		 */
		public function set currentRound(boolean:Boolean):void{
			this.prPlayerBoard.playerInfoBoard.currentRound = boolean;
		}
		
		override public function clear():void{
			super.clear();
			this.vp = 0;
			this.doubloon = 0;
			this.colonist = 0;
			this.corn = 0;
			this.indigo = 0;
			this.sugar = 0;
			this.tobacco = 0;
			this.coffee = 0;
			
			if(this._playerBoard!=null){
				this._playerBoard.clear();
			}
		}
		
	}
}