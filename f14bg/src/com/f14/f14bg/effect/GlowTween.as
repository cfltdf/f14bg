package com.f14.f14bg.effect
{
        import flash.display.InteractiveObject;
        import flash.events.Event;
        import flash.events.MouseEvent;
        import flash.filters.GlowFilter;

        public class GlowTween
        {
                private var _target:InteractiveObject;
                private var _color:uint;
                private var _toggle:Boolean;
                private var _blur:Number;

                public function GlowTween(target:InteractiveObject, color:uint=0xFFFFFF)
                {
                        _target=target;
                        _color=color;
                        _toggle=true;
                        _blur=2;
                        //target.addEventListener(MouseEvent.ROLL_OVER, startGlowHandler);
                        //target.addEventListener(MouseEvent.ROLL_OUT, stopGlowHandler);
                }

                public function remove():void
                {
                        //_target.removeEventListener(MouseEvent.ROLL_OVER, startGlowHandler);
                        //_target.removeEventListener(MouseEvent.ROLL_OUT, stopGlowHandler);
                        _target.removeEventListener(Event.ENTER_FRAME, blinkHandler);
                        _target.filters=null;
                        _target=null;
                }
                
                public function startGlow():void{
                	this.startGlowHandler();
                }
                
                public function stopGlow():void{
                	this.stopGlowHandler();
                }

                private function startGlowHandler(evt:MouseEvent=null):void
                {
                        _target.addEventListener(Event.ENTER_FRAME, blinkHandler, false, 0, true);
                }

                private function stopGlowHandler(evt:MouseEvent=null):void
                {
                        _target.removeEventListener(Event.ENTER_FRAME, blinkHandler);
                        _target.filters=null;
                }

                private function blinkHandler(evt:Event):void
                {
                        if (_blur >= 20)
                                _toggle=false;
                        else if (_blur <= 2)
                                _toggle=true;
                        _toggle ? _blur++ : _blur--;
                        var glow:GlowFilter=new GlowFilter(_color, 1, _blur, _blur, 2, 1, true);
                        _target.filters=[glow];
                }
        }
}