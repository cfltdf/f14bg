package com.f14.F14bgGame.Eclipse.player
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.components.EclipseShipPart;
	import com.f14.F14bgGame.Eclipse.consts.UnitType;
	import com.f14.F14bgGame.Eclipse.ui.EclipsePlayerBoard;
	import com.f14.F14bgGame.Eclipse.ui.EclipsePlayerInfoBoard;
	import com.f14.F14bgGame.Eclipse.ui.simple.Blueprint;
	import com.f14.F14bgGame.bg.player.Player;
	import com.f14.core.util.ApplicationUtil;

	public class EclipsePlayer extends Player
	{
		public function EclipsePlayer()
		{
			super();
		}
		
		public var race:String;
		
		[Bindable]
		public var costInterceptor:int;
		[Bindable]
		public var costCruiser:int;
		[Bindable]
		public var costDreadnought:int;
		[Bindable]
		public var costStarbase:int;
		[Bindable]
		public var costOrbital:int;
		[Bindable]
		public var costMonolith:int;
		
		[Bindable]
		public var resMoney:int;
		[Bindable]
		public var resScience:int;
		[Bindable]
		public var resMaterials:int;
		[Bindable]
		public var addMoney:int;
		[Bindable]
		public var addScience:int;
		[Bindable]
		public var addMaterials:int;
		[Bindable]
		public var popMoney:int;
		[Bindable]
		public var popScience:int;
		[Bindable]
		public var popMaterials:int;
		
		[Bindable]
		public var influenceDisc:int;
		[Bindable]
		public var maxInfluenceDisc:int;
		[Bindable]
		public var upkeep:int;
		
		[Bindable]
		public var sumMoney:String;
		[Bindable]
		public var sumScience:String;
		[Bindable]
		public var sumMaterials:String;
		[Bindable]
		public var sumInfluence:String;
		[Bindable]
		public var sumColonyShip:String;
		
		[Bindable]
		public var numSciMilitary:int;
		[Bindable]
		public var numSciGrid:int;
		[Bindable]
		public var numSciNano:int;
		[Bindable]
		public var numShipInterceptor:int;
		[Bindable]
		public var numShipCruiser:int;
		[Bindable]
		public var numShipDreadnought:int;
		[Bindable]
		public var numShipStarbase:int;
		[Bindable]
		public var numAmbassador:int;
		
		public function get eclipsePlayerBoard():EclipsePlayerBoard{
			return this.playerBoard as EclipsePlayerBoard;
		}
		
		public function get eclipsePlayerInfoBoard():EclipsePlayerInfoBoard{
			return this.playerInfoBoard as EclipsePlayerInfoBoard;
		}
		
		/**
		 * 装载玩家种族的信息
		 */
		public function loadRaceParam(param:Object):void{
			this.race = param.race;
			this.eclipsePlayerBoard.racePhoto.race = this.race;
		}
		
		/**
		 * 装载玩家建造费用的信息
		 */
		public function loadCostParam(param:Object):void{
			this.costInterceptor = param.INTERCEPTOR;
			this.costCruiser = param.CRUISER;
			this.costDreadnought = param.DREADNOUGHT;
			this.costStarbase = param.STARBASE;
			this.costOrbital = param.ORBITAL;
			this.costMonolith = param.MONOLITH;
		}
		
		/**
		 * 装载玩家资源的信息
		 */
		public function loadResourceParam(param:Object):void{
			this.resMoney = param.resources.MONEY;
			this.resScience = param.resources.SCIENCE;
			this.resMaterials = param.resources.MATERIALS;
			this.addMoney = param.properties.MONEY_ADD;
			this.addScience = param.properties.SCIENCE_ADD;
			this.addMaterials = param.properties.MATERIALS_ADD;
			this.popMoney = param.parts.MONEY;
			this.popScience = param.parts.SCIENCE;
			this.popMaterials = param.parts.MATERIALS;
			
			this.influenceDisc = param.parts.INFLUENCE_DISC;
			this.maxInfluenceDisc = param.properties.INFLUENCE_DISC;
			this.upkeep = param.properties.UPKEEP;
			
			this.numSciMilitary = param.technology.MILITARY;
			this.numSciGrid = param.technology.GRID;
			this.numSciNano = param.technology.NANO;
			this.numShipInterceptor = param.parts.INTERCEPTOR;
			this.numShipCruiser = param.parts.CRUISER;
			this.numShipDreadnought = param.parts.DREADNOUGHT;
			this.numShipStarbase = param.parts.STARBASE;
			this.numAmbassador = param.parts.AMBASSADOR;
			
			this.sumMoney = this.resMoney + "/ +" + this.addMoney;
			this.sumScience = this.resScience + "/ +" + this.addScience;
			this.sumMaterials = this.resMaterials + "/ +" + this.addMaterials;
			this.sumInfluence = this.influenceDisc + "/" + this.maxInfluenceDisc + " (" + this.upkeep + ")";
			this.sumColonyShip = param.parts.COLONY_SHIP + "/" + param.properties.COLONY_SHIP;
			
			this.eclipsePlayerBoard.track_money.num = this.popMoney;
			this.eclipsePlayerBoard.track_science.num = this.popScience;
			this.eclipsePlayerBoard.track_materials.num = this.popMaterials;
			
			//如果玩家是本地玩家,则刷新影响力轨道的
			if(EclipseUtil.gameManager.isLocalPlayer(this)){
				EclipseUtil.mainBoard.influenceTrack.num = this.influenceDisc;
			}
		}
		
		/**
		 * 装载玩家配件的信息
		 */
		public function loadPartParam(param:Object):void{
			this.eclipsePlayerBoard.track_money.num = param.MONEY;
			this.eclipsePlayerBoard.track_science.num = param.SCIENCE;
			this.eclipsePlayerBoard.track_materials.num = param.MATERIALS;
		}
		
		/**
		 * 装载玩家科技的信息
		 */
		public function loadTechnologyParam(param:Object):void{
			for each(var type:String in param){
				this.eclipsePlayerBoard.addTechnology(type);
			}
		}
		
		/**
		 * 装载玩家影响力轨道的信息
		 */
		public function loadReputationTrackParam(param:Object):void{
			this.eclipsePlayerBoard.track_reputation.clear();
			for each(var o:Object in param.squares){
				this.eclipsePlayerBoard.track_reputation.addSquare(o);
			}
		}
		
		/**
		 * 装载玩家蓝图的信息
		 */
		public function loadBlueprintParam(param:Object):void{
			var s:Array = [UnitType.INTERCEPTOR, UnitType.CRUISER, UnitType.DREADNOUGHT, UnitType.STARBASE];
			for each(var shipType:String in s){
				var o:Object = param[shipType];
				var blueprint:Blueprint = this.eclipsePlayerBoard.getBlueprint(shipType);
				if(blueprint!=null){
					blueprint.loadShipParts(o);
				}
			}
		}
		
		/**
		 * 装载蓝图变更的信息
		 */
		public function loadBlueprintChangeParam(param:Object):void{
			for each(var object:Object in param.changes){
				var shipType:String = object.blueprint.shipType;
				var blueprint:Blueprint = this.eclipsePlayerBoard.getBlueprint(shipType);
				var partIndex:int = object.partIndex;
				var positionIndex:int = object.positionIndex;
				
				var s:EclipseShipPart = EclipseUtil.resourceManager.createShipPart(partIndex);
				blueprint.addShipPart(s, positionIndex);
			}
		}
		
		/**
		 * 装载玩家的状态信息
		 */
		public function loadStatusParam(param:Object):void{
			this.eclipsePlayerInfoBoard.loadStatusParam(param);
		}
		
		/**
		 * 装载玩家得到科技的信息
		 */
		public function loadAddTechnologyParam(param:Object):void{
			this.eclipsePlayerBoard.addTechnology(param.type);
		}
	}
}