/* hider */
(function (window){
	
    var FWDRLHider = function(screenToTest, hideDelay){
    	
    	var self = this;
    	var prototype = FWDRLHider.prototype;
   
    	this.screenToTest = screenToTest;
    	this.hideDelay = hideDelay;
    	this.globalX = 0;
    	this.globalY = 0;
	
		this.currentTime;
    	this.checkIntervalId_int;
    	
    	this.hideCompleteId_to;
    	
    	this.hasInitialTestEvents_bl = false;
    	this.addSecondTestEvents_bl = false;
    	this.dispatchOnceShow_bl = true;
    	this.dispatchOnceHide_bl = false;
    	this.isStopped_bl = true;
    	this.isHidden_bl = false;
    	this.isMobile_bl = FWDRLUtils.isMobile;
    	this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
    	
		self.init = function(){};
	
		self.start = function(){
			self.currentTime = new Date().getTime();
			clearInterval(self.checkIntervalId_int);
			self.checkIntervalId_int = setInterval(self.update, 100);
			self.addMouseOrTouchCheck();
			self.isStopped_bl = false;
		};
		
		self.stop = function(){
			clearInterval(self.checkIntervalId_int);
			self.isStopped_bl = true;
			self.removeMouseOrTouchCheck();
			self.removeMouseOrTouchCheck2();
		};
		
		self.addMouseOrTouchCheck = function(){	
			if(self.hasInitialTestEvents_bl) return;
			self.hasInitialTestEvents_bl = true;
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.screenToTest.screen.addEventListener("MSPointerDown", self.onMouseOrTouchUpdate);
					self.screenToTest.screen.addEventListener("MSPointerMove", self.onMouseOrTouchUpdate);
				}else{
					self.screenToTest.screen.addEventListener("touchstart", self.onMouseOrTouchUpdate);
				}
			}else if(window.addEventListener){
				window.addEventListener("mousemove", self.onMouseOrTouchUpdate);
			}else if(document.attachEvent){
				document.attachEvent("onmousemove", self.onMouseOrTouchUpdate);
			}
		};
		
		self.removeMouseOrTouchCheck = function(){	
			if(!self.hasInitialTestEvents_bl) return;
			self.hasInitialTestEvents_bl = false;
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.screenToTest.screen.removeEventListener("MSPointerDown", self.onMouseOrTouchUpdate);
					self.screenToTest.screen.removeEventListener("MSPointerMove", self.onMouseOrTouchUpdate);
				}else{
					self.screenToTest.screen.removeEventListener("touchstart", self.onMouseOrTouchUpdate);
				}
			}else if(window.removeEventListener){
				window.removeEventListener("mousemove", self.onMouseOrTouchUpdate);
			}else if(document.detachEvent){
				document.detachEvent("onmousemove", self.onMouseOrTouchUpdate);
			}
		};
		
		self.addMouseOrTouchCheck2 = function(){	
			if(self.addSecondTestEvents_bl) return;
			self.addSecondTestEvents_bl = true;
			if(self.screenToTest.screen.addEventListener){
				self.screenToTest.screen.addEventListener("mousemove", self.secondTestMoveDummy);
			}else if(self.screenToTest.screen.attachEvent){
				self.screenToTest.screen.attachEvent("onmousemove", self.secondTestMoveDummy);
			}
		};
		
		self.removeMouseOrTouchCheck2 = function(){	
			if(!self.addSecondTestEvents_bl) return;
			self.addSecondTestEvents_bl = false;
			if(self.screenToTest.screen.removeEventListener){
				self.screenToTest.screen.removeEventListener("mousemove", self.secondTestMoveDummy);
			}else if(self.screenToTest.screen.detachEvent){
				self.screenToTest.screen.detachEvent("onmousemove", self.secondTestMoveDummy);
			}
		};
		
		this.secondTestMoveDummy = function(){
			self.removeMouseOrTouchCheck2();
			self.addMouseOrTouchCheck();
		};
		
		self.onMouseOrTouchUpdate = function(e){
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);	
			
			if(self.globalX != viewportMouseCoordinates.screenX
			   && self.globalY != viewportMouseCoordinates.screenY){
				self.currentTime = new Date().getTime();
			}
			
			self.globalX = viewportMouseCoordinates.screenX;
			self.globalY = viewportMouseCoordinates.screenY;
			
			if(!self.isMobile_bl){
				if(!FWDRLUtils.hitTest(self.screenToTest.screen, self.globalX, self.globalY)){
					self.removeMouseOrTouchCheck();
					self.addMouseOrTouchCheck2();
				}
			}
		};
	
		self.update = function(e){
			if(new Date().getTime() > self.currentTime + self.hideDelay){
				if(self.dispatchOnceShow_bl){	
					self.dispatchOnceHide_bl = true;
					self.dispatchOnceShow_bl = false;
					self.isHidden_bl = true;
					self.dispatchEvent(FWDRLHider.HIDE);
					clearTimeout(self.hideCompleteId_to);
					self.hideCompleteId_to = setTimeout(function(){
						self.dispatchEvent(FWDRLHider.HIDE_COMPLETE);
					}, 1000);
				}
			}else{
				if(self.dispatchOnceHide_bl){
					clearTimeout(self.hideCompleteId_to);
					self.dispatchOnceHide_bl = false;
					self.dispatchOnceShow_bl = true;
					self.isHidden_bl = false;
					self.dispatchEvent(FWDRLHider.SHOW);
				}
			}
		};

		self.reset = function(){
			self.isHidden_bl = false;
			clearTimeout(self.hideCompleteId_to);
			self.currentTime = new Date().getTime();
			self.dispatchEvent(FWDRLHider.SHOW);
		};
		
		
		self.init();
     };
     
	 FWDRLHider.HIDE = "hide";
	 FWDRLHider.SHOW = "show";
	 FWDRLHider.HIDE_COMPLETE = "hideComplete";
	 
	 FWDRLHider.setPrototype = function(){
		 FWDRLHider.prototype = new FWDRLEventDispatcher();
	 };
	 

	 window.FWDRLHider = FWDRLHider;
}(window));