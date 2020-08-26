package com.f14.F14bgGame.Eclipse.manager
{
	import com.f14.F14bgGame.Eclipse.EclipseUtil;
	import com.f14.F14bgGame.Eclipse.consts.EclipseGameCmd;
	import com.f14.F14bgGame.Eclipse.consts.EclipseInputState;
	import com.f14.F14bgGame.bg.consts.InputState;
	import com.f14.F14bgGame.bg.utils.BgUtils;
	
	public class EclipseUIManager
	{
		/**
		 * 所有输入部件
		 */
		protected var parts:Array = ["DEFAULT", "BUTTON", "SPACE_MAP", "PLANET", "TECHNOLOGY", "BLUEPRINT"];
		protected var inputState:String;
			
		public function EclipseUIManager()
		{
		}
		
		/**
		 * 设置提示信息
		 */
		public function setTipMessage(param:Object):void{
			if(param.msg){
				EclipseUtil.mainBoard.messageBoard.msg = param.msg;
			}else{
				EclipseUtil.mainBoard.messageBoard.msg = null;
			}
		}

		/**
		 * 设置界面输入的状态,该游戏未用到setInputState的方法,用的是这个方法
		 */
		public function setInputStateWithParam(code:int, param:Object):void{
			this.setTipMessage(param);	
			
			var state:String = InputState.DEFAULT;
			switch(code){
				case EclipseGameCmd.GAME_CODE_ROUND:
					state = EclipseInputState.ROUND_ACTION;
					break;
			}
			EclipseUtil.mainBoard.buttonPanel.setInputState(state, param);
			this.setInputState(state, param);
		}
		
		/**
		 * 设置界面输入的状态,用于中断监听器但是非弹出窗口的情况
		 */
		public function setInterruptStateWithParam(code:int, param:Object):void{
			this.setTipMessage(param);
			
			var state:String = InputState.DEFAULT;
			var buttonState:String = InputState.DEFAULT;
			switch(code){
				case EclipseGameCmd.GAME_CODE_ACTION_EXPLORE:	//探索
					switch(param.subPhase){	//判断探索阶段中子阶段的状态
						case 0:	//选择板块位置
							state = EclipseInputState.ACTION_EXPLORE;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 1:	//选择如何放置板块
							state = EclipseInputState.ACTION_EXPLORE_1;
							buttonState = EclipseInputState.ACTION_EXPLORE_1;
							break;
						case 2:	//放置板块完成后,选择是否放置影响力
							state = EclipseInputState.ACTION_EXPLORE_2;
							buttonState = EclipseInputState.ACTION_EXPLORE_2;
							break;
						case 3:	//放置完影响力后的自由行动
							state = EclipseInputState.FREE_ACTION;
							buttonState = EclipseInputState.FREE_ACTION;
							break;
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_INFLUENCE:	//扩张
					switch(param.subPhase){	//判断探索阶段中子阶段的状态
						case 0:	//选择板块位置
							state = EclipseInputState.ACTION_INFLUENCE;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 3:	//选择影响力圆片所在板块的位置
							state = EclipseInputState.ACTION_INFLUENCE;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 5:	//选择自由行动
							state = EclipseInputState.ACTION_INFLUENCE_5;
							buttonState = EclipseInputState.ACTION_INFLUENCE_5;
							break;
						/*case 2:	//放置板块完成后,选择是否放置影响力
							state = EclipseInputState.ACTION_EXPLORE_2;
							buttonState = EclipseInputState.ACTION_EXPLORE_2;
							break;
						case 3:	//放置完影响力后的自由行动
							state = EclipseInputState.FREE_ACTION;
							buttonState = EclipseInputState.FREE_ACTION;
							break;*/
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_BUILD:	//建造
					switch(param.subPhase){	//判断建造阶段中子阶段的状态
						case 0:	//选择板块位置
							state = EclipseInputState.ACTION_BUILD;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 1:	//选择建造何种单位
							state = EclipseInputState.ACTION_BUILD_1;
							buttonState = EclipseInputState.ACTION_BUILD_1;
							break;
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_MOVE:	//移动
					switch(param.subPhase){	//判断移动阶段中子阶段的状态
						case 0:	//选择板块位置
							state = EclipseInputState.ACTION_MOVE;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 1:	//选择移动哪种单位
							state = EclipseInputState.ACTION_MOVE_1;
							buttonState = EclipseInputState.ACTION_MOVE_1;
							break;
						case 2:	//选择移动去哪
							state = EclipseInputState.ACTION_MOVE_2;
							buttonState = EclipseInputState.CANCEL_ACTION;
							break;
						case 3:	//确认移动
							state = EclipseInputState.ACTION_MOVE_3;
							buttonState = EclipseInputState.ACTION_MOVE_3;
							break;
					}
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_RESEARCH:	//科研
					state = EclipseInputState.ACTION_RESEARCH;
					buttonState = EclipseInputState.ACTION_RESEARCH;
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_UPGRADE:	//升级
					state = EclipseInputState.ACTION_UPGRADE;
					buttonState = EclipseInputState.ACTION_UPGRADE;
					break;
				case EclipseGameCmd.GAME_CODE_ACTION_COLONY:	//殖民
					state = EclipseInputState.ACTION_COLONY;
					buttonState = EclipseInputState.CONFIRM_ACTION;
					break;
			}
			EclipseUtil.mainBoard.buttonPanel.setInputState(buttonState, param);
			this.setInputState(state, param);
		}
		
		/**
		 * 设置输入状态
		 */
		public function setInputState(inputState:String, param:Object):void{
			this.inputState = inputState;
			var enableParts:Array = new Array();
			var ps:Object = {};
			switch(inputState){
				case InputState.ENABLE_ALL: //允许所有
				case InputState.DEFAULT: //默认状态
					break;
				case InputState.DISABLE_ALL: //不允许所有
					break;
				case EclipseInputState.ACTION_EXPLORE:	//探索
					enableParts.push("SPACE_MAP");
					break;
				case EclipseInputState.ACTION_INFLUENCE:	//扩张
					enableParts.push("SPACE_MAP");
					break;
				case EclipseInputState.ACTION_BUILD:	//建造
					enableParts.push("SPACE_MAP");
					break;
				case EclipseInputState.ACTION_MOVE:	//移动
				case EclipseInputState.ACTION_MOVE_2:	//选择移动去哪
					enableParts.push("SPACE_MAP");
					break;
				case EclipseInputState.ACTION_RESEARCH:	//科研
					enableParts.push("TECHNOLOGY");
					break;
				case EclipseInputState.ACTION_UPGRADE:	//升级
					enableParts.push("BLUEPRINT");
					break;
				case EclipseInputState.ACTION_COLONY:	//殖民
					enableParts.push("PLANET");
					break;
			}
			this.setEnableParts(enableParts, ps);
		}
		
		/**
		 * 设置数组中的部件为可选
		 */
		protected function setEnableParts(enableParts:Array, param:Object):void{
			for each(var part:String in parts){
				this.setPartInputable(part, BgUtils.inArray(part, enableParts), param);
			}
		}
		
		/**
		 * 设置各个输入部件是否输入的情况
		 */
		protected function setPartInputable(part:String, inputable:Boolean, param:Object):void{
			switch(part){
				case "DEFAULT": //默认部件
					//this.workerPool.selectable = inputable;
					break;
				case "BUTTON": //按键
					//this.buttonPart.hbox_normal.visible = inputable;
					break;
				case "SPACE_MAP": //地图
					EclipseUtil.mainBoard.spaceMap.selectable = inputable;
					break;
				case "TECHNOLOGY": //科研
					EclipseUtil.stateManager.publicWindow.techSelectable = inputable;
					break;
				case "PLANET":	//星球
					EclipseUtil.mainBoard.spaceMap.planetSelectable = inputable;
					break;
				case "BLUEPRINT":	//蓝图
					EclipseUtil.getLocalPlayer().eclipsePlayerBoard.blueprintSelectable = inputable;
					break;
			}
		}
	}
}