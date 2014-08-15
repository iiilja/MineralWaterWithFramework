/* FWDRLComplexButton */
(function (){
var FWDRLComplexButton = function(
			n1Img, 
			s1Path, 
			n2Img, 
			s2Path, 
			disptachMainEvent_bl
		){
		
		var self = this;
		var prototype = FWDRLComplexButton.prototype;
		
		this.n1Img = n1Img;
		this.s1Path_str = s1Path;
		this.n2Img = n2Img;
		this.s2Path_str = s2Path;
	
		this.buttonsHolder_do;
		this.firstButton_do;
		this.n1_do;
		this.s1_do;
		this.secondButton_do;
		this.n2_do;
		this.s2_do;
		
		this.buttonWidth = self.n1Img.width;
		this.buttonHeight = self.n1Img.height;
		
		this.isSelectedState_bl = false;
		this.currentState = 1;
		this.isDisabled_bl = false;
		this.isMaximized_bl = false;
		this.disptachMainEvent_bl = disptachMainEvent_bl;
		this.isDisabled_bl = false;
		this.isHoverDisabled_bl = false;
		this.isMobile_bl = FWDRLUtils.isMobile;
		this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
		this.allowToCreateSecondButton_bl = !self.isMobile_bl || self.hasPointerEvent_bl;
		
		//##########################################//
		/* initialize self */
		//##########################################//
		self.init = function(){
			self.setButtonMode(true);
			self.setWidth(self.buttonWidth);
			self.setHeight(self.buttonHeight);
			self.setupMainContainers();
			self.secondButton_do.setX(-50);
		};
		
		//##########################################//
		/* setup main containers */
		//##########################################//
		self.setupMainContainers = function(){
			
			self.buttonsHolder_do = new FWDRLDisplayObject("div");
			self.buttonsHolder_do.setOverflow("visible");
			
			self.firstButton_do = new FWDRLDisplayObject("div");
			self.addChild(self.firstButton_do);
			self.n1_do = new FWDRLDisplayObject("img");	
			self.n1_do.setScreen(self.n1Img);
			self.firstButton_do.addChild(self.n1_do);
			if(self.allowToCreateSecondButton_bl){
				self.s1_do = new FWDRLDisplayObject("img");
				var img1 = new Image();
				img1.src = self.s1Path_str;
				self.s1_do.setScreen(img1);
				self.s1_do.setWidth(self.buttonWidth);
				self.s1_do.setHeight(self.buttonHeight);
				self.s1_do.setAlpha(0);
				self.firstButton_do.addChild(self.s1_do);
			}
			self.firstButton_do.setWidth(self.buttonWidth);
			self.firstButton_do.setHeight(self.buttonHeight);
			
			self.secondButton_do = new FWDRLDisplayObject("div");
			self.addChild(self.secondButton_do);
			self.n2_do = new FWDRLDisplayObject("img");	
			self.n2_do.setScreen(self.n2Img);
			self.secondButton_do.addChild(self.n2_do);
			
			if(self.allowToCreateSecondButton_bl){
				self.s2_do = new FWDRLDisplayObject("img");
				var img2 = new Image();
				img2.src = self.s2Path_str;
				self.s2_do.setScreen(img2);
				self.s2_do.setWidth(self.buttonWidth);
				self.s2_do.setHeight(self.buttonHeight);
				self.s2_do.setAlpha(0);
				self.secondButton_do.addChild(self.s2_do);
			}
			
			self.secondButton_do.setWidth(self.buttonWidth);
			self.secondButton_do.setHeight(self.buttonHeight);
			
			self.buttonsHolder_do.addChild(self.secondButton_do);
			self.buttonsHolder_do.addChild(self.firstButton_do);
			self.addChild(self.buttonsHolder_do);
			
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.screen.addEventListener("MSPointerDown", self.onMouseUp);
					self.screen.addEventListener("MSPointerOver", self.onMouseOver);
					self.screen.addEventListener("MSPointerOut", self.onMouseOut);
				}else{
					self.screen.addEventListener("toustart", self.onDown);
					self.screen.addEventListener("touchend", self.onMouseUp);
				}
			}else if(self.screen.addEventListener){	
				self.screen.addEventListener("mouseover", self.onMouseOver);
				self.screen.addEventListener("mouseout", self.onMouseOut);
				self.screen.addEventListener("mouseup", self.onMouseUp);
			}else if(self.screen.attachEvent){
				self.screen.attachEvent("onmouseover", self.onMouseOver);
				self.screen.attachEvent("onmouseout", self.onMouseOut);
				self.screen.attachEvent("onmousedown", self.onMouseUp);
			}
		};
		
		self.onMouseOver = function(e, animate){
			self.dispatchEvent(FWDRLComplexButton.SHOW_TOOLTIP, {e:e});
			if(self.isDisabled_bl || self.isSelectedState_bl || self.isHoverDisabled_bl) return;
			if(!e.pointerType || e.pointerType == "mouse"){
				self.dispatchEvent(FWDRLComplexButton.MOUSE_OVER, {e:e});
				self.setSelectedState(true);
			}
		};
			
		self.onMouseOut = function(e){
			if(self.isDisabled_bl || !self.isSelectedState_bl || self.isHoverDisabled_bl) return;
			if(!e.pointerType || e.pointerType == "mouse"){
				self.setNormalState();
				self.dispatchEvent(FWDRLComplexButton.MOUSE_OUT);
			}
		};
		
		self.onDown = function(e){
			if(e.preventDefault) e.preventDefault();
		};
	
		self.onMouseUp = function(e){
			
			if(self.isDisabled_bl || e.button == 2) return;
			if(e.preventDefault) e.preventDefault();
			if(!self.isMobile_bl) self.onMouseOver(e, false);
		
			//if(self.hasPointerEvent_bl) self.setNormalState();
			if(self.disptachMainEvent_bl) self.dispatchEvent(FWDRLComplexButton.MOUSE_UP, {e:e});
		};
		
		//##############################//
		/* toggle button */
		//#############################//
		self.toggleButton = function(){
			if(self.currentState == 1){
				self.firstButton_do.setX(-50);
				self.secondButton_do.setX(0);
				self.currentState = 0;
				self.dispatchEvent(FWDRLComplexButton.FIRST_BUTTON_CLICK);
			}else{
				self.firstButton_do.setX(-50);
				self.secondButton_do.setX(0);
				self.currentState = 1;
				self.dispatchEvent(FWDRLComplexButton.SECOND_BUTTON_CLICK);
			}
		};
		
		//##############################//
		/* set second buttons state */
		//##############################//
		self.setButtonState = function(state){
			if(state == 1){
				self.firstButton_do.setX(0);
				self.secondButton_do.setX(-50);
				self.currentState = 1; 
			}else{
				self.firstButton_do.setX(-50);
				self.secondButton_do.setX(0);
				self.currentState = 0; 
			}
		};
		
		//###############################//
		/* set normal state */
		//################################//
		this.setNormalState = function(){
			if(self.isMobile_bl && !self.hasPointerEvent_bl) return;
			self.isSelectedState_bl = false;
			FWDRLTweenMax.killTweensOf(self.s1_do);
			FWDRLTweenMax.killTweensOf(self.s2_do);
			FWDRLTweenMax.to(self.s1_do, .5, {alpha:0, ease:Expo.easeOut});	
			FWDRLTweenMax.to(self.s2_do, .5, {alpha:0, ease:Expo.easeOut});
		};
		
		this.setSelectedState = function(animate){
			self.isSelectedState_bl = true;
			FWDRLTweenMax.killTweensOf(self.s1_do);
			FWDRLTweenMax.killTweensOf(self.s2_do);
			FWDRLTweenMax.to(self.s1_do, .5, {alpha:1, delay:.1, ease:Expo.easeOut});
			FWDRLTweenMax.to(self.s2_do, .5, {alpha:1, delay:.1, ease:Expo.easeOut});
		};
		
		
		//###################################//
		/* Enable disable */
		//###################################//
		this.disable = function(){
			self.isDisabled_bl = true;
			self.setButtonMode(false);
			FWDRLTweenMax.to(self, .6, {alpha:.4});
		};
		
		this.enable = function(){
			self.isDisabled_bl = false;
			self.setButtonMode(true);
			FWDRLTweenMax.to(self, .6, {alpha:1});
		};
		
		this.disableHover = function(){
			self.isHoverDisabled_bl = true;
			self.setSelectedState();
		};
		
		this.enableHover = function(){
			self.isHoverDisabled_bl = false;
			//self.setSelectedState();
		};
		
		self.init();
	};
	
	/* set prototype */
	FWDRLComplexButton.setPrototype = function(){
		FWDRLComplexButton.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLComplexButton.FIRST_BUTTON_CLICK = "onFirstClick";
	FWDRLComplexButton.SECOND_BUTTON_CLICK = "secondButtonOnClick";
	FWDRLComplexButton.SHOW_TOOLTIP = "showToolTip";
	FWDRLComplexButton.MOUSE_OVER = "onMouseOver";
	FWDRLComplexButton.MOUSE_OUT = "onMouseOut";
	FWDRLComplexButton.MOUSE_UP = "onMouseUp";
	FWDRLComplexButton.CLICK = "onClick";
	
	FWDRLComplexButton.prototype = null;
	window.FWDRLComplexButton = FWDRLComplexButton;
}(window));