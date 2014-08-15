/* Image manager */
(function (window){
	
	var FWDRLDescriptionWindow = function(
			parent,
			descriptionAnimationType,
			descriptionWindowPosition,
			margins,
			backgroundColor_str,
			backgroundOpacity
			){
		
		var self = this;
		var prototype = FWDRLDescriptionWindow.prototype;
		
		this.main_do;
		this.text_do;
		this.bk_do;
		
		this.descriptionAnimationType_str = descriptionAnimationType;
		this.backgroundColor_str = backgroundColor_str;
		this.position_str = descriptionWindowPosition;
		
		this.backgroundOpacity = backgroundOpacity;
		this.margins = margins;
		this.finalW = 0;
		this.finalH = 0;
		this.finalY = 0;

		this.resizeWithDelayId_to;
		
		this.isShowedFirstTime_bl = false;
		this.isShowed_bl = false;
		this.isHiddenDone_bl = true;
		
		self.init = function(){
			//self.setBkColor("#00FF00");
			self.setupMainContainers();
		};
		
		//#####################################//
		/* setup main containers */
		//####################################//
		self.setupMainContainers = function(){
			
			self.main_do = new FWDRLDisplayObject("div");
			self.main_do.getStyle().width = "100%";
			self.main_do.getStyle().height = "100%";
			self.main_do.setBackfaceVisibility();
			if(!self.isMobile_bl && FWDRLUtils.isChrome){
				self.main_do.hasTransform3d_bl =  false;
				self.main_do.hasTransform2d_bl =  false;
			}
			
			self.text_do = new FWDRLDisplayObject("div");
			self.text_do.getStyle().fontSmoothing = "antialiased";
			self.text_do.getStyle().webkitFontSmoothing = "antialiased";
			self.text_do.getStyle().textRendering = "optimizeLegibility";
			self.text_do.getStyle().width = "100%";
			self.text_do.setBackfaceVisibility();
			self.text_do.hasTransform3d_bl =  false;
			self.text_do.hasTransform2d_bl =  false;
			
			self.bk_do = new FWDRLDisplayObject("div");
			self.bk_do.setResizableSizeAfterParent();
			self.bk_do.setBkColor(self.backgroundColor_str);
			self.bk_do.setAlpha(self.backgroundOpacity);
			self.bk_do.setBackfaceVisibility();
			if(!self.isMobile_bl && FWDRLUtils.isChrome){
				self.bk_do.hasTransform3d_bl =  false;
				self.bk_do.hasTransform2d_bl =  false;
			}
			
			self.main_do.addChild(self.bk_do);
			self.main_do.addChild(self.text_do);
			self.addChild(self.main_do);
		};
		
		//#####################################//
		/* set text */
		//####################################//
		self.setText = function(pText){
			self.text_do.setInnerHTML(pText);
			self.resizeAndPosition();
		};
		
		self.resizeAndPosition = function(finalW, overwrite){
			if(finalW) self.finalW = finalW;
			self.finalH = self.text_do.getHeight();
			self.setFinalSize();
			clearTimeout(self.resizeWithDelayId_to);
			self.resizeWithDelayId_to = setTimeout(self.setFinalSize, 50);
			
		};
		
		self.setFinalSize = function(){
			self.finalH = self.text_do.getHeight();
			
			if(self.position_str == "top"){
				self.finalY = self.margins;
			}else{
				self.finalY = parent.mainItemHolder_do.h - self.finalH - self.margins;
			}
			
		
			self.setX(self.margins);
			self.setY(self.finalY);
			self.setWidth(self.finalW);
			self.main_do.setHeight(self.finalH);
			self.setHeight(self.finalH);
		};
		
		//#####################################//
		/* hide / show */
		//####################################//
		self.hide = function(animate, overwrite, isShowedFirstTime){
			if(!self.isShowed_bl && !overwrite) return;
			self.isShowed_bl = false;
			if(isShowedFirstTime) self.isShowedFirstTime_bl = false;
			FWDRLTweenMax.killTweensOf(self.main_do);
			if(animate){
				if(self.descriptionAnimationType_str == "motion"){
					if(self.position_str == "top"){
						FWDRLTweenMax.to(self.main_do, .8, {y:-self.finalH, ease:Expo.easeInOut, onComplete:self.hideComplete});
					}else{
						FWDRLTweenMax.to(self.main_do, .8, {y:self.finalH, ease:Expo.easeInOut, onComplete:self.hideComplete});
					}
				}else{
					FWDRLTweenMax.to(self.main_do, .8, {alpha:0, ease:Quint.easeOut, onComplete:self.hideComplete});
				}
			}else{
				self.hideComplete();
			}
		};
		
		self.hideComplete = function(){
			self.setVisible(false);
			if(self.descriptionAnimationType_str == "motion"){
				if(self.position_str == "top"){
					self.main_do.setY(-self.finalH);
				}else{
					self.main_do.setY(self.finalH);
				}
			}else{
				self.main_do.setAlpha(0);
			}
		};
		
		self.show = function(animate){
			if(self.isShowed_bl) return;
			self.isShowed_bl = true;
	
			if(!self.isShowedFirstTime_bl){
				self.isShowedFirstTime_bl = true;
				self.hideComplete();
				self.resizeAndPosition();
			}
			self.setVisible(true);
			
			FWDRLTweenMax.killTweensOf(self.main_do);
			if(self.descriptionAnimationType_str == "motion"){
				if(self.main_do.alpha != 1) self.main_do.setAlpha(1);
				if(animate){
					FWDRLTweenMax.to(self.main_do, .8, {y:0, ease:Expo.easeInOut});
				}else{
					self.main_do.setY(0);
				}
			}else{
				self.main_do.setY(0);
				if(animate){
					FWDRLTweenMax.to(self.main_do, .8, {alpha:1, ease:Quint.easeOut});
				}else{
					self.main_do.setAlpha(1);
				}
			}
		};
		
		
		
		self.init();
	};
	
	/* set prototype */
	FWDRLDescriptionWindow.setPrototype =  function(){
		FWDRLDescriptionWindow.prototype = new FWDRLDisplayObject("div");
	};


	FWDRLDescriptionWindow.HIDE_COMPLETE = "infoWindowHideComplete";

	FWDRLDescriptionWindow.prototype = null;
	window.FWDRLDescriptionWindow = FWDRLDescriptionWindow;
	
}(window));