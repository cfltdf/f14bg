package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.Eclipse.components.EclipseActionTitle;
	import com.f14.F14bgGame.Eclipse.components.EclipseShipPart;
	import com.f14.F14bgGame.Eclipse.components.EclipseTechnology;
	import com.f14.F14bgGame.Eclipse.consts.EclipsePlayerColor;
	import com.f14.F14bgGame.Eclipse.ui.simple.InfluenceDisc;
	import com.f14.F14bgGame.Eclipse.ui.simple.PopulationCube;
	import com.f14.F14bgGame.Eclipse.ui.simple.Resource;
	import com.f14.F14bgGame.Eclipse.ui.simple.Unit;
	import com.f14.F14bgGame.Eclipse.ui.simple.UnitNpc;
	import com.f14.f14bg.manager.ResourceManager;

	public class EclipseResourceManager extends ResourceManager
	{
		protected var technology:Array = new Array();
		protected var shipParts:Array = new Array();
		
		public function EclipseResourceManager()
		{
			super();
		}
		
		/**
		 * 装载资源
		 */
		override public function load(param:Object):void{
			var obj:Object;
			for each(obj in param.technology){
				this.putTechnology(obj);
			}
			for each(obj in param.shipParts){
				this.putShipPart(obj);
			}
		}
		
		/**
		 * 放入科技
		 */
		public function putTechnology(o:Object):void{
			this.technology[o.type] = o;
		}
		
		/**
		 * 取得科技
		 */
		public function getTechnology(type:String):Object{
			return this.technology[type];
		}
		
		/**
		 * 放入飞船配件
		 */
		public function putShipPart(o:Object):void{
			this.shipParts[o.cardIndex+""] = o;
		}
		
		/**
		 * 取得飞船配件
		 */
		public function getShipPart(cardIndex:int):Object{
			return this.shipParts[cardIndex+""];
		}
		
		/**
		 * 创建科技组件
		 */
		public function createTechnology(type:String):EclipseTechnology{
			var obj:Object = this.getTechnology(type);
			var card:EclipseTechnology = null;
			if(obj!=null){
				card = new EclipseTechnology();
				card.object = obj;
			}
			return card;
		}
		
		/**
		 * 创建飞船配件
		 */
		public function createShipPart(cardIndex:int):EclipseShipPart{
			var obj:Object = this.getShipPart(cardIndex);
			var card:EclipseShipPart = null;
			if(obj!=null){
				card = new EclipseShipPart();
				card.object = obj;
			}
			return card;
		}
		
		/**
		 * 创建影响力圆片
		 */
		public function createInfluenceDisc(position:int):InfluenceDisc{
			var disc:InfluenceDisc = new InfluenceDisc();
			disc.filters = EclipsePlayerColor.COLOR_FILTER[position];
			return disc;
		}
		
		/**
		 * 创建玩家小方块
		 */
		public function createPopulationCube(position:int):PopulationCube{
			var o:PopulationCube = new PopulationCube();
			o.filters = EclipsePlayerColor.COLOR_FILTER[position];
			return o;
		}
		
		/**
		 * 创建单位
		 */
		public function createUnit(object:Object):Unit{
			var o:Unit;
			if(object.owner<0){
				o = new UnitNpc();
			}else{
				o = new Unit();
				o.filters = EclipsePlayerColor.COLOR_FILTER[object.owner];
			}
			o.loadObject(object);
			return o;
		}
		
		/**
		 * 创建行动标题对象
		 */
		public function createActionTitle(type:String):EclipseActionTitle{
			var res:EclipseActionTitle = new EclipseActionTitle();
			res.actionType = type;
			//为了装载图像
			res.object = null;
			return res;
		}
		
		/**
		 * 创建资源对象
		 */
		public function createResource(resourceType:String):Resource{
			var res:Resource = new Resource();
			res.resourceType = resourceType;
			return res;
		}
		
	}
}