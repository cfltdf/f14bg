package com.f14.F14bgGame.bg.component
{
	import flash.geom.Point;
	
	import mx.core.UIComponent;

	public class LinkLine extends UIComponent{  
          
        //线起点  
        private var startPoint:Point;  
        //线终点  
        private var endPoint:Point;  
        //线条颜色  
        private var lineColor:uint=0x000000;  
        //线提示语  
        private var tip:String=" ";  
        //是否要箭头  
        private var isArrow:Boolean=true;  
        //箭头大小  
        private var radius:uint=6;  
          
          
        public function LinkLine(){  
            super();  
        }  
          
        //获得线的角度  
        public function getAngle():Number{  
            var tmpx:Number=endPoint.x - startPoint.x;  
            var tmpy:Number=startPoint.y - endPoint.y;  
            var angle:Number= Math.atan2(tmpy,tmpx)*(180/Math.PI);  
            return angle;  
        }  
          
        //画线  
        public function drawLine():void{  
            this.graphics.clear();  
            this.graphics.lineStyle(2,lineColor);  
            this.graphics.moveTo(startPoint.x,startPoint.y);  
            this.graphics.lineTo(endPoint.x,endPoint.y);  
            this.toolTip=tip;  
            if(isArrow){  
                var angle:Number = this.getAngle();  
                var centerX:Number = endPoint.x - radius * Math.cos(angle*(Math.PI/180));  
                var centerY:Number = endPoint.y + radius * Math.sin(angle*(Math.PI/180));  
                var topX:Number = endPoint.x;  
                var topY:Number = endPoint.y;  
                var leftX:Number = centerX + radius * Math.cos((angle+120)*(Math.PI/180));  
                var leftY:Number = centerY - radius * Math.sin((angle+120)*(Math.PI/180));  
                var rightX:Number = centerX + radius * Math.cos((angle+240)*(Math.PI/180));  
                var rightY:Number = centerY - radius * Math.sin((angle+240)*(Math.PI/180));  
                this.graphics.beginFill(lineColor,1);  
                this.graphics.lineStyle(2,lineColor,1);  
                this.graphics.moveTo(topX,topY);  
                this.graphics.lineTo(leftX,leftY);  
                this.graphics.lineTo(centerX,centerY);  
                this.graphics.lineTo(rightX,rightY);  
                this.graphics.lineTo(topX,topY);  
                this.graphics.endFill();  
            }  
        }  
        public function removeLine():void{  
            this.graphics.clear();  
        }  
          
        public function setStartPoint(point:Point):void{  
            this.startPoint=point;  
        }   
        public function setEndPoint(point:Point):void{  
            this.endPoint=point;  
        }  
        public function setLineColor(color:uint):void{  
            this.lineColor=color;  
        }  
        public function setTip(tip:String):void{  
            this.tip=tip;  
        }  
        public function setArrow(flag:Boolean):void{  
            this.isArrow=flag;  
        }  
    }
}