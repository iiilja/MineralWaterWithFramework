/* FWDRLEAPController */
(function(){
var FWDRLEAPController = function(
			data,
			parent
		){
		
		var self = this;
		var prototype = FWDRLEAPController.prototype;
		
		this.bkPath_img = data.bkPath_img;
		this.playN_img = data.audioPlayN_img;
		this.pauseN_img = data.audioPauseN_img;
		this.audioMainScrubberBkLeft_img = data.audioMainScrubberBkLeft_img;
		this.mainScrubberBkRight_img = data.mainScrubberBkRight_img;
		this.mainScrubberDragLeft_img = data.mainScrubberDragLeft_img;
		this.mainScrubberLine_img = data.mainScrubberLine_img;
		this.volumeScrubberBkLeft_img = data.volumeScrubberBkLeft_img;
		this.volumeScrubberBkRight_img = data.volumeScrubberBkRight_img;
		this.volumeScrubberDragLeft_img = data.volumeScrubberDragLeft_img;
		this.volumeScrubberLine_img = data.volumeScrubberLine_img;
		this.timeBk_img = data.timeBk_img;
		this.volumeN_img = data.volumeN_img;
		this.volumeS_img = data.volumeS_img;
		this.volumeD_img = data.volumeD_img;
		this.progressLeft_img = data.progressLeft_img;
		
		this.buttons_ar = [];
		
		this.disable_do = null;
		this.mainHolder_do = null;
		this.bk_do = null;
		this.playPauseButton_do = null;
		this.mainScrubber_do = null;
		this.mainScrubberBkLeft_do = null;
		this.mainScrubberBkMiddle_do = null;
		this.mainScrubberBkRight_do = null;
		this.mainScrubberDrag_do = null;
		this.mainScrubberDragLeft_do = null;
		this.mainScrubberDragMiddle_do = null;
		this.mainScrubberBarLine_do = null;
		this.mainProgress_do = null;
		this.progressLeft_do = null;
		this.progressMiddle_do = null;
		this.time_do = null;
		this.volumeButton_do = null;
		this.volumeScrubber_do = null;
		this.volumeScrubberBkLeft_do = null;
		this.volumeScrubberBkMiddle_do = null;
		this.volumeScrubberBkRight_do = null;
		this.volumeScrubberDrag_do = null;
		this.volumeScrubberDragLeft_do = null;
		this.volumeScrubberDragMiddle_do = null;
		this.volumeScrubberBarLine_do = null;
		
		this.bkMiddlePath_str = data.bkMiddlePath_str;
		this.mainScrubberBkMiddlePath_str = data.mainScrubberBkMiddlePath_str;
		this.volumeScrubberBkMiddlePath_str = data.volumeScrubberBkMiddlePath_str;
		this.mainScrubberDragMiddlePath_str = data.mainScrubberDragMiddlePath_str;
		this.volumeScrubberDragMiddlePath_str = data.volumeScrubberDragMiddlePath_str;
		this.timeColor_str = data.timeColor_str;
		this.progressMiddlePath_str = data.progressMiddlePath_str;
		this.audioControllerBackgroundColor_str = data.audioControllerBackgroundColor_str;

		this.stageWidth = 0;
		this.scrubbersBkLeftAndRightWidth = this.audioMainScrubberBkLeft_img.width;
		this.mainScrubberWidth = 0;
		this.mainScrubberMinWidth = 150;
		this.volumeScrubberWidth = data.volumeScrubberWidth;
		this.scrubbersHeight = this.audioMainScrubberBkLeft_img.height;
		this.mainScrubberDragLeftWidth = self.mainScrubberDragLeft_img.width;
		this.scrubbersOffsetWidth = data.scrubbersOffsetWidth;
		this.scrubbersOffestTotalWidth = data.audioScrubbersOffestTotalWidth;
		this.volume = data.volume;
		this.lastVolume = self.volume;
		this.startSpaceBetweenButtons = data.startSpaceBetweenButtons;
		this.spaceBetweenButtons = data.vdSpaceBetweenButtons;
		this.timeOffestTotalWidth = 0;
		this.percentPlayed = 0;
		this.timeOffestLeftWidth = data.timeOffsetLeftWidth;
		this.timeOffsetRightWidth = data.timeOffsetRightWidth;
		this.lastTimeLength = 0;
		
		this.showAnimationIntroId_to;
	
		this.allowToChangeVolume_bl = data.allowToChangeVolume_bl;
		this.isMainScrubberScrubbing_bl = false;
		this.isMainScrubberDisabled_bl = false;
		this.isVolumeScrubberDisabled_bl = false;
		this.isMainScrubberLineVisible_bl = false;
		this.isVolumeScrubberLineVisible_bl = false;
		this.isMute_bl = false;
		this.isMobile_bl = FWDRLUtils.isMobile;
		this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;

		//##########################################//
		/* initialize this */
		//##########################################//
		self.init = function(){
			//self.setOverflow("visible");
			self.mainHolder_do = new FWDRLDisplayObject("div");
			self.mainHolder_do.setOverflow("visible");
			self.setBkColor(self.audioControllerBackgroundColor_str);
			self.addChild(self.mainHolder_do);
			self.setupPlayPauseButton();
			self.setupMainScrubber();
			
			self.setupTime();
			self.setupVolumeButton();	
			self.setupVolumeScrubber();
			if(!self.isMobile_bl) self.setupDisable();
		};
		
		//###########################################//
		// Resize and position self...
		//###########################################//
		self.resizeAndPosition = function(overwrite){
			if(parent.stageWidth == self.stageWidth && parent.stageHeight == self.stageHeight && !overwrite) return;
			self.stageWidth = parent.stageWidth;
			self.stageHeight = parent.stageHeight;
			self.positionButtons();
		};
		
		//##############################//
		/* setup background */
		//##############################//
		self.positionButtons = function(){
			var button;
			var prevButton;
			if(!self.stageWidth) return;
			
			if(self.bk_do) self.bk_do.setWidth(self.stageWidth);
			if(self.playPauseButton_do){
				FWDRLTweenMax.killTweensOf(self.mainHolder_do);
				self.mainHolder_do.setWidth(self.stageWidth);
				self.mainHolder_do.setHeight(self.stageHeight);
				self.setWidth(self.stageWidth);
				self.setHeight(self.stageHeight);
			}
		
			var buttonsCopy_ar = [];
			for (var i=0; i < self.buttons_ar.length; i++) {
				buttonsCopy_ar[i] = self.buttons_ar[i];
			}
			
			self.mainScrubberWidth = self.stageWidth - self.startSpaceBetweenButtons * 2;
			for (var i=0; i < buttonsCopy_ar.length; i++) {
				button = buttonsCopy_ar[i];
				if(button != self.mainScrubber_do){
					self.mainScrubberWidth -= button.w + self.spaceBetweenButtons;
				}
			};
			
		
			while(self.mainScrubberWidth < self.mainScrubberMinWidth && buttonsCopy_ar.length > 3){
				self.mainScrubberWidth = self.stageWidth - self.startSpaceBetweenButtons * 2;
				
				if(self.volumeScrubber_do && FWDRLUtils.indexOfArray(buttonsCopy_ar, self.volumeScrubber_do) != -1){
					buttonsCopy_ar.splice(FWDRLUtils.indexOfArray(buttonsCopy_ar, self.volumeScrubber_do), 1);
					self.volumeScrubber_do.setX(-1000);
				}else if(self.time_do && FWDRLUtils.indexOfArray(buttonsCopy_ar, self.time_do) != -1){
					buttonsCopy_ar.splice(FWDRLUtils.indexOfArray(buttonsCopy_ar, self.time_do), 1);
					self.time_do.setX(-1000);
				}else if(self.mainScrubber_do && FWDRLUtils.indexOfArray(buttonsCopy_ar, self.mainScrubber_do) != -1){
					buttonsCopy_ar.splice(FWDRLUtils.indexOfArray(buttonsCopy_ar, self.mainScrubber_do), 1);
					self.mainScrubber_do.setX(-1000);
				}else if(self.volumeButton_do && FWDRLUtils.indexOfArray(buttonsCopy_ar, self.volumeButton_do) != -1){
					buttonsCopy_ar.splice(FWDRLUtils.indexOfArray(buttonsCopy_ar, self.volumeButton_do), 1);
					self.volumeButton_do.setX(-1000);
				}
				
				for (var i=0; i < buttonsCopy_ar.length; i++) {
					button = buttonsCopy_ar[i];
					if(button != self.mainScrubber_do){
						self.mainScrubberWidth -= button.w + self.spaceBetweenButtons;
					}
				};
			};
			
			if(buttonsCopy_ar[buttonsCopy_ar.length -1] == self.volumeScrubber_do
			   || buttonsCopy_ar[buttonsCopy_ar.length -1] == self.mainScrubber_do){
			   self.mainScrubberWidth -= self.scrubbersOffestTotalWidth;	
			};
			
			if(buttonsCopy_ar[buttonsCopy_ar.length -1] == self.time_do){
				self.mainScrubberWidth -= self.timeOffestTotalWidth;	
			};
			
			for (var i=0; i < buttonsCopy_ar.length; i++) {
				button = buttonsCopy_ar[i];
				FWDRLTweenMax.killTweensOf(button);
				if(i == 0){
					button.setX(self.startSpaceBetweenButtons);
				}else if(button == self.mainScrubber_do){
					prevButton = buttonsCopy_ar[i - 1];
					self.mainScrubber_do.setX(prevButton.x + prevButton.w + self.spaceBetweenButtons);
					self.mainScrubber_do.setWidth(self.mainScrubberWidth);
					self.mainScrubberBkMiddle_do.setWidth(self.mainScrubberWidth - self.scrubbersBkLeftAndRightWidth * 2);
					self.mainScrubberBkRight_do.setX(self.mainScrubberWidth - self.scrubbersBkLeftAndRightWidth);
					self.mainScrubberDragMiddle_do.setWidth(self.mainScrubberWidth - self.scrubbersBkLeftAndRightWidth - self.scrubbersOffsetWidth);
				}else{
					prevButton = buttonsCopy_ar[i - 1];
					button.setX(prevButton.x + prevButton.w + self.spaceBetweenButtons);
				}
				
				button.setY(parseInt((self.stageHeight - button.h)/2));	
			};	
			
			
			if(self.disable_do){
				self.disable_do.setWidth(self.stageWidth);
				self.disable_do.setHeight(self.stageHeight);
			}
			
			if(((!self.mainScrubber_do) || (self.mainScrubber_do && self.mainScrubber_do.x < 0)) && button){
				parent.stageWidth = button.x + button.w + self.startSpaceBetweenButtons;
				self.stageWidth = parent.stageWidth;
				parent.resizeHandler(true);
			}
			
			if(self.progressMiddle_do) self.progressMiddle_do.setWidth(self.mainScrubberWidth - self.scrubbersBkLeftAndRightWidth - self.scrubbersOffsetWidth);
			self.updateMainScrubber(self.percentPlayed);
		};
		
		//###############################//
		/* setup disable */
		//##############################//
		this.setupDisable = function(){
			self.disable_do = new FWDRLDisplayObject("div");
			if(FWDRLUtils.isIE){
				self.disable_do.setBkColor("#FFFFFF");
				self.disable_do.setAlpha(0);
			}
		};
	
		//################################################//
		/* Setup main scrubber */
		//################################################//
		this.setupMainScrubber = function(){

			//setup background bar
			self.mainScrubber_do = new FWDRLDisplayObject("div");

			//self.mainScrubber_do.setY(parseInt((self.stageHeight - self.scrubbersHeight)/2));
			self.mainScrubber_do.setHeight(self.scrubbersHeight);
			
			self.mainScrubberBkLeft_do = new FWDRLDisplayObject("img");
			self.mainScrubberBkLeft_do.setScreen(self.audioMainScrubberBkLeft_img);
			
			self.mainScrubberBkRight_do = new FWDRLDisplayObject("img");
			var mainScrubberBkRight_img = new Image();
			mainScrubberBkRight_img.src = data.mainScrubberBkRightPath_str;
			self.mainScrubberBkRight_do.setScreen(mainScrubberBkRight_img);
			self.mainScrubberBkRight_do.setWidth(self.mainScrubberBkLeft_do.w);
			self.mainScrubberBkRight_do.setHeight(self.mainScrubberBkLeft_do.h);
			
			var middleImage = new Image();
			middleImage.src = self.mainScrubberBkMiddlePath_str;
			
			if(self.isMobile_bl){
				self.mainScrubberBkMiddle_do = new FWDRLDisplayObject("div");	
				self.mainScrubberBkMiddle_do.getStyle().background = "url('" + self.mainScrubberBkMiddlePath_str + "') repeat-x";
			}else{
				self.mainScrubberBkMiddle_do = new FWDRLDisplayObject("img");
				self.mainScrubberBkMiddle_do.setScreen(middleImage);
			}
				
			self.mainScrubberBkMiddle_do.setHeight(self.scrubbersHeight);
			self.mainScrubberBkMiddle_do.setX(self.scrubbersBkLeftAndRightWidth);
			
			//setup progress bar
			self.mainProgress_do = new FWDRLDisplayObject("div");
			self.mainProgress_do.setHeight(self.scrubbersHeight);
		
			self.progressLeft_do = new FWDRLDisplayObject("img");
			self.progressLeft_do.setScreen(self.progress);
			
			middleImage = new Image();
			middleImage.src = self.progressMiddlePath_str;
			
			self.progressMiddle_do = new FWDRLDisplayObject("div");	
			self.progressMiddle_do.getStyle().background = "url('" + self.progressMiddlePath_str + "') repeat-x";
		
			self.progressMiddle_do.setHeight(self.scrubbersHeight);
			self.progressMiddle_do.setX(self.mainScrubberDragLeftWidth);
			
			//setup darg bar.
			self.mainScrubberDrag_do = new FWDRLDisplayObject("div");
			self.mainScrubberDrag_do.setHeight(self.scrubbersHeight);
		
			self.mainScrubberDragLeft_do = new FWDRLDisplayObject("img");
			self.mainScrubberDragLeft_do.setScreen(self.mainScrubberDragLeft_img);
			
			middleImage = new Image();
			middleImage.src = self.mainScrubberDragMiddlePath_str;
			if(self.isMobile_bl){
				self.mainScrubberDragMiddle_do = new FWDRLDisplayObject("div");	
				self.mainScrubberDragMiddle_do.getStyle().background = "url('" + self.mainScrubberDragMiddlePath_str + "') repeat-x";
			}else{
				self.mainScrubberDragMiddle_do = new FWDRLDisplayObject("img");
				self.mainScrubberDragMiddle_do.setScreen(middleImage);
			}
			self.mainScrubberDragMiddle_do.setHeight(self.scrubbersHeight);
			self.mainScrubberDragMiddle_do.setX(self.mainScrubberDragLeftWidth);
			
			self.mainScrubberBarLine_do = new FWDRLDisplayObject("img");
			self.mainScrubberBarLine_do.setScreen(self.mainScrubberLine_img);
			self.mainScrubberBarLine_do.setAlpha(0);
			self.mainScrubberBarLine_do.hasTransform3d_bl = false;
			self.mainScrubberBarLine_do.hasTransform2d_bl = false;
			
			self.buttons_ar.push(self.mainScrubber_do);
			
			//add all children
			self.mainScrubber_do.addChild(self.mainScrubberBkLeft_do);
			self.mainScrubber_do.addChild(self.mainScrubberBkMiddle_do);
			self.mainScrubber_do.addChild(self.mainScrubberBkRight_do);
			self.mainScrubber_do.addChild(self.mainScrubberBarLine_do);
			self.mainScrubberDrag_do.addChild(self.mainScrubberDragLeft_do);
			self.mainScrubberDrag_do.addChild(self.mainScrubberDragMiddle_do);
			self.mainProgress_do.addChild(self.progressLeft_do);
			self.mainProgress_do.addChild(self.progressMiddle_do);
			self.mainScrubber_do.addChild(self.mainProgress_do);
			self.mainScrubber_do.addChild(self.mainScrubberDrag_do);
			self.mainScrubber_do.addChild(self.mainScrubberBarLine_do);
			self.mainHolder_do.addChild(self.mainScrubber_do);
		
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.mainScrubber_do.screen.addEventListener("MSPointerOver", self.mainScrubberOnOverHandler);
					self.mainScrubber_do.screen.addEventListener("MSPointerOut", self.mainScrubberOnOutHandler);
					self.mainScrubber_do.screen.addEventListener("MSPointerDown", self.mainScrubberOnDownHandler);
				}else{
					self.mainScrubber_do.screen.addEventListener("touchstart", self.mainScrubberOnDownHandler);
				}
			}else if(self.screen.addEventListener){	
				self.mainScrubber_do.screen.addEventListener("mouseover", self.mainScrubberOnOverHandler);
				self.mainScrubber_do.screen.addEventListener("mouseout", self.mainScrubberOnOutHandler);
				self.mainScrubber_do.screen.addEventListener("mousedown", self.mainScrubberOnDownHandler);
			}else if(self.screen.attachEvent){
				self.mainScrubber_do.screen.attachEvent("onmouseover", self.mainScrubberOnOverHandler);
				self.mainScrubber_do.screen.attachEvent("onmouseout", self.mainScrubberOnOutHandler);
				self.mainScrubber_do.screen.attachEvent("onmousedown", self.mainScrubberOnDownHandler);
			}
			
			self.disableMainScrubber();
			self.updateMainScrubber(0);
		};
		
		this.mainScrubberOnOverHandler =  function(e){
			if(self.isMainScrubberDisabled_bl) return;
		};
		
		this.mainScrubberOnOutHandler =  function(e){
			if(self.isMainScrubberDisabled_bl) return;
		};
		
		this.mainScrubberOnDownHandler =  function(e){
			if(self.isMainScrubberDisabled_bl) return;
			if(e.preventDefault) e.preventDefault();
			self.isMainScrubberScrubbing_bl = true;
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);	
			var localX = viewportMouseCoordinates.screenX - self.mainScrubber_do.getGlobalX();
			
			if(localX < 0){
				localX = 0;
			}else if(localX > self.mainScrubberWidth - self.scrubbersOffsetWidth){
				localX = self.mainScrubberWidth - self.scrubbersOffsetWidth;
			}
			var percentScrubbed = localX/self.mainScrubberWidth;
			
			if(self.disable_do) self.addChild(self.disable_do);
			self.updateMainScrubber(percentScrubbed);
			
			self.dispatchEvent(FWDRLEAPController.START_TO_SCRUB);
			self.dispatchEvent(FWDRLEAPController.SCRUB, {percent:percentScrubbed});
			
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					window.addEventListener("MSPointerMove", self.mainScrubberMoveHandler);
					window.addEventListener("MSPointerUp", self.mainScrubberEndHandler);
				}else{
					window.addEventListener("touchmove", self.mainScrubberMoveHandler);
					window.addEventListener("touchend", self.mainScrubberEndHandler);
				}
			}else{
				if(window.addEventListener){
					window.addEventListener("mousemove", self.mainScrubberMoveHandler);
					window.addEventListener("mouseup", self.mainScrubberEndHandler);		
				}else if(document.attachEvent){
					document.attachEvent("onmousemove", self.mainScrubberMoveHandler);
					document.attachEvent("onmouseup", self.mainScrubberEndHandler);		
				}
			}
		};
		
		this.mainScrubberMoveHandler = function(e){
			if(e.preventDefault) e.preventDefault();
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);	
			var localX = viewportMouseCoordinates.screenX - self.mainScrubber_do.getGlobalX();
			
			if(localX < 0){
				localX = 0;
			}else if(localX > self.mainScrubberWidth - self.scrubbersOffsetWidth){
				localX = self.mainScrubberWidth - self.scrubbersOffsetWidth;
			}
			
			var percentScrubbed = localX/self.mainScrubberWidth;
			self.updateMainScrubber(percentScrubbed);
			self.dispatchEvent(FWDRLEAPController.SCRUB, {percent:percentScrubbed});
		};
		
		this.mainScrubberEndHandler = function(e){
			if(self.disable_do){
				if(self.contains(self.disable_do)) self.removeChild(self.disable_do);
			}
			/*
			if(e){
				if(e.preventDefault) e.preventDefault();
				self.mainScrubberMoveHandler(e);
			}
			*/
			self.dispatchEvent(FWDRLEAPController.STOP_TO_SCRUB);
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					window.removeEventListener("MSPointerMove", self.mainScrubberMoveHandler);
					window.removeEventListener("MSPointerUp", self.mainScrubberEndHandler);
				}else{
					window.removeEventListener("touchmove", self.mainScrubberMoveHandler);
					window.removeEventListener("touchend", self.mainScrubberEndHandler);
				}
			}else{
				if(window.removeEventListener){
					window.removeEventListener("mousemove", self.mainScrubberMoveHandler);
					window.removeEventListener("mouseup", self.mainScrubberEndHandler);		
				}else if(document.detachEvent){
					document.detachEvent("onmousemove", self.mainScrubberMoveHandler);
					document.detachEvent("onmouseup", self.mainScrubberEndHandler);		
				}
			}
		};
		
		this.disableMainScrubber = function(){
			if(!self.mainScrubber_do) return;
			self.isMainScrubberDisabled_bl = true;
			self.mainScrubber_do.setButtonMode(false);
			self.mainScrubberEndHandler();
			self.updateMainScrubber(0);
			self.updatePreloaderBar(0);
		};
		
		this.enableMainScrubber = function(){
			if(!self.mainScrubber_do) return;
			self.isMainScrubberDisabled_bl = false;
			self.mainScrubber_do.setButtonMode(true);
		};
		
		this.updateMainScrubber = function(percent){
			if(!self.mainScrubber_do || isNaN(percent)) return;
			var finalWidth = parseInt(percent * self.mainScrubberWidth); 
			self.percentPlayed = percent;
			if(!FWDRLEAP.hasHTML5Audio && finalWidth >= self.mainProgress_do.w) finalWidth = self.mainProgress_do.w;
			
			if(finalWidth < 1 && self.isMainScrubberLineVisible_bl){
				self.isMainScrubberLineVisible_bl = false;
				FWDRLTweenMax.to(self.mainScrubberBarLine_do, .5, {alpha:0});
			}else if(finalWidth > 1 && !self.isMainScrubberLineVisible_bl){
				self.isMainScrubberLineVisible_bl = true;
				FWDRLTweenMax.to(self.mainScrubberBarLine_do, .5, {alpha:1});
			}
			if(isNaN(finalWidth) || finalWidth < 0) finalWidth = 0;
			self.mainScrubberDrag_do.setWidth(finalWidth);
			if(finalWidth > self.mainScrubberWidth - self.scrubbersOffsetWidth) finalWidth = self.mainScrubberWidth - self.scrubbersOffsetWidth;
			FWDRLTweenMax.to(self.mainScrubberBarLine_do, .8, {x:finalWidth + 1, ease:Expo.easeOut});
		};
		
		this.updatePreloaderBar = function(percent){
			if(!self.mainProgress_do || isNaN(percent)) return;
			var finalWidth = parseInt(percent * self.mainScrubberWidth); 
			
			if(percent == 1){
				self.mainProgress_do.setY(-30);
			}else if(self.mainProgress_do.y != 0 && percent!= 1){
				self.mainProgress_do.setY(0);
			}
			if(finalWidth > self.mainScrubberWidth - self.scrubbersOffsetWidth) finalWidth = self.mainScrubberWidth - self.scrubbersOffsetWidth;
			if(isNaN(finalWidth) || finalWidth < 0) finalWidth = 0;
			self.mainProgress_do.setWidth(finalWidth);
		};
		
		//################################################//
		/* Setup play button */
		//################################################//
		this.setupPlayPauseButton = function(){
			FWDRLComplexButton.setPrototype();
			self.playPauseButton_do = new FWDRLComplexButton(
					self.playN_img,
					data.audioPlaySPath_str,
					self.pauseN_img,
					data.audioPauseSPath_str,
					true
			);
			
			self.buttons_ar.push(self.playPauseButton_do);
			self.playPauseButton_do.addListener(FWDRLComplexButton.MOUSE_UP, self.playButtonMouseUpHandler);
			self.mainHolder_do.addChild(self.playPauseButton_do);
		};
		
		this.showPlayButton = function(){
			if(!self.playPauseButton_do) return;
			self.playPauseButton_do.setButtonState(1);
		};
		
		this.showPauseButton = function(){
			if(!self.playPauseButton_do) return;
			self.playPauseButton_do.setButtonState(0);
		};
		
		this.playButtonMouseUpHandler = function(){
			if(self.playPauseButton_do.currentState == 0){
				self.dispatchEvent(FWDRLEAPController.PAUSE);
			}else{
				self.dispatchEvent(FWDRLEAPController.PLAY);
			}
		};
		
		//########################################//
		/* Setup time*/
		//########################################//
		this.setupTime = function(){
			self.time_do = new FWDRLDisplayObject("div");
			self.time_do.hasTransform3d_bl = false;
			self.time_do.hasTransform2d_bl = false;
			self.time_do.setBackfaceVisibility();
			self.time_do.getStyle().paddingLeft = self.timeOffestLeftWidth + "px";
			self.time_do.getStyle().paddingRight = self.timeOffsetRightWidth + "px";
			self.time_do.getStyle().fontFamily = "Arial";
			self.time_do.getStyle().fontSize= "12px";
			self.time_do.getStyle().whiteSpace= "nowrap";
			self.time_do.getStyle().textAlign = "center";
			self.time_do.getStyle().color = self.timeColor_str;
			
			self.time_do.getStyle().fontSmoothing = "antialiased";
			self.time_do.getStyle().webkitFontSmoothing = "antialiased";
			self.time_do.getStyle().textRendering = "optimizeLegibility";	
			self.mainHolder_do.addChild(self.time_do);
			self.updateTime("00:00/00:00");
			self.buttons_ar.push(self.time_do);
			
		};
		
		
		this.updateTime = function(time){
			if(!self.time_do) return;
			self.time_do.setInnerHTML(time);
			
			if(self.lastTimeLength != time.length){
				self.time_do.w = self.time_do.getWidth();
				self.positionButtons();
				setTimeout(function(){
					self.time_do.w = self.time_do.getWidth();
					self.time_do.h = self.time_do.getHeight();
					self.positionButtons();
				}, 50);
				self.lastTimeLength = time.length;
			}
		};
		

		//##########################################//
		/* Setup volume button */
		//#########################################//
		this.setupVolumeButton = function(){
			FWDRLEVPVolumeButton.setPrototype();
			self.volumeButton_do = new FWDRLEVPVolumeButton(self.volumeN_img, data.volumeSPath_str, data.volumeDPath_str);
			self.volumeButton_do.addListener(FWDRLEVPVolumeButton.MOUSE_UP, self.volumeOnMouseUpHandler);
			self.buttons_ar.push(self.volumeButton_do);
			self.mainHolder_do.addChild(self.volumeButton_do); 
			if(!self.allowToChangeVolume_bl) self.volumeButton_do.disable();
		};
		
		this.volumeOnMouseUpHandler = function(){
			var vol = self.lastVolume;
			
			if(self.isMute_bl){
				vol = self.lastVolume;
				self.isMute_bl = false;
			}else{
				vol = 0.000001;
				self.isMute_bl = true;
			};
			self.updateVolume(vol);
		};
		
		//################################################//
		/* Setup volume scrubber */
		//################################################//
		this.setupVolumeScrubber = function(){
			//setup background bar
			self.volumeScrubber_do = new FWDRLDisplayObject("div");
			self.volumeScrubber_do.setHeight(self.scrubbersHeight);
			
			
			self.volumeScrubberBkLeft_do = new FWDRLDisplayObject("img");
			var volumeScrubberBkLeft_img = new Image();
			volumeScrubberBkLeft_img.src = data.volumeScrubberBkLeftPath_str;
			self.volumeScrubberBkLeft_do.setScreen(volumeScrubberBkLeft_img);
			self.volumeScrubberBkLeft_do.setWidth(self.audioMainScrubberBkLeft_img.width);
			self.volumeScrubberBkLeft_do.setHeight(self.audioMainScrubberBkLeft_img.height);
			
			self.volumeScrubberBkRight_do = new FWDRLDisplayObject("img");
			var volumeScrubberBkRight_img = new Image();
			volumeScrubberBkRight_img.src = data.volumeScrubberBkRightPath_str;
			self.volumeScrubberBkRight_do.setScreen(volumeScrubberBkRight_img);
			self.volumeScrubberBkRight_do.setWidth(self.mainScrubberBkRight_img.width);
			self.volumeScrubberBkRight_do.setHeight(self.mainScrubberBkRight_img.height);
			
			var middleImage = new Image();
			middleImage.src = self.volumeScrubberBkMiddlePath_str;
			
			if(self.isMobile_bl){
				self.volumeScrubberBkMiddle_do = new FWDRLDisplayObject("div");	
				self.volumeScrubberBkMiddle_do.getStyle().background = "url('" + self.volumeScrubberBkMiddlePath_str + "') repeat-x";
			}else{
				self.volumeScrubberBkMiddle_do = new FWDRLDisplayObject("img");
				self.volumeScrubberBkMiddle_do.setScreen(middleImage);
			}
				
			self.volumeScrubberBkMiddle_do.setHeight(self.scrubbersHeight);
			self.volumeScrubberBkMiddle_do.setX(self.scrubbersBkLeftAndRightWidth);
			
			//setup darg bar.
			self.volumeScrubberDrag_do = new FWDRLDisplayObject("div");
			self.volumeScrubberDrag_do.setHeight(self.scrubbersHeight);
		
			self.volumeScrubberDragLeft_do = new FWDRLDisplayObject("img");
			var volumeScrubberDragLeft_img = new Image();
			volumeScrubberDragLeft_img.src = data.volumeScrubberDragLeftPath_str;
			self.volumeScrubberDragLeft_do.setScreen(volumeScrubberDragLeft_img);
			self.volumeScrubberDragLeft_do.setWidth(self.mainScrubberDragLeft_img.width);
			self.volumeScrubberDragLeft_do.setHeight(self.mainScrubberDragLeft_img.height);
			
			middleImage = new Image();
			middleImage.src = self.volumeScrubberDragMiddlePath_str;
			if(self.isMobile_bl){
				self.volumeScrubberDragMiddle_do = new FWDRLDisplayObject("div");	
				self.volumeScrubberDragMiddle_do.getStyle().background = "url('" + self.volumeScrubberDragMiddlePath_str + "') repeat-x";
			}else{
				self.volumeScrubberDragMiddle_do = new FWDRLDisplayObject("img");
				self.volumeScrubberDragMiddle_do.setScreen(middleImage);
			}
			self.volumeScrubberDragMiddle_do.setHeight(self.scrubbersHeight);
			self.volumeScrubberDragMiddle_do.setX(self.mainScrubberDragLeftWidth);
		
			self.volumeScrubberBarLine_do = new FWDRLDisplayObject("img");
			var volumeScrubberBarLine_img = new Image();
			volumeScrubberBarLine_img.src = data.volumeScrubberLinePath_str;
			self.volumeScrubberBarLine_do.setScreen(volumeScrubberBarLine_img);
			self.volumeScrubberBarLine_do.setWidth(self.mainScrubberLine_img.width);
			self.volumeScrubberBarLine_do.setHeight(self.mainScrubberLine_img.height);
			
			self.volumeScrubberBarLine_do.setAlpha(0);
			self.volumeScrubberBarLine_do.hasTransform3d_bl = false;
			self.volumeScrubberBarLine_do.hasTransform2d_bl = false;
			
			self.volumeScrubber_do.setWidth(self.volumeScrubberWidth);
			self.volumeScrubberBkMiddle_do.setWidth(self.volumeScrubberWidth - self.scrubbersBkLeftAndRightWidth * 2);
			self.volumeScrubberBkRight_do.setX(self.volumeScrubberWidth - self.scrubbersBkLeftAndRightWidth);
			self.volumeScrubberDragMiddle_do.setWidth(self.volumeScrubberWidth - self.scrubbersBkLeftAndRightWidth - self.scrubbersOffsetWidth);
			
			//add all children
			self.volumeScrubber_do.addChild(self.volumeScrubberBkLeft_do);
			self.volumeScrubber_do.addChild(self.volumeScrubberBkMiddle_do);
			self.volumeScrubber_do.addChild(self.volumeScrubberBkRight_do);
			self.volumeScrubber_do.addChild(self.volumeScrubberBarLine_do);
			self.volumeScrubberDrag_do.addChild(self.volumeScrubberDragLeft_do);
			self.volumeScrubberDrag_do.addChild(self.volumeScrubberDragMiddle_do);
			self.volumeScrubber_do.addChild(self.volumeScrubberDrag_do);
			self.volumeScrubber_do.addChild(self.volumeScrubberBarLine_do);
			
			self.buttons_ar.push(self.volumeScrubber_do);
			
			self.mainHolder_do.addChild(self.volumeScrubber_do);
		
			if(self.allowToChangeVolume_bl){
				if(self.isMobile_bl){
					if(self.hasPointerEvent_bl){
						self.volumeScrubber_do.screen.addEventListener("MSPointerOver", self.volumeScrubberOnOverHandler);
						self.volumeScrubber_do.screen.addEventListener("MSPointerOut", self.volumeScrubberOnOutHandler);
						self.volumeScrubber_do.screen.addEventListener("MSPointerDown", self.volumeScrubberOnDownHandler);
					}else{
						self.volumeScrubber_do.screen.addEventListener("touchstart", self.volumeScrubberOnDownHandler);
					}
				}else if(self.screen.addEventListener){	
					self.volumeScrubber_do.screen.addEventListener("mouseover", self.volumeScrubberOnOverHandler);
					self.volumeScrubber_do.screen.addEventListener("mouseout", self.volumeScrubberOnOutHandler);
					self.volumeScrubber_do.screen.addEventListener("mousedown", self.volumeScrubberOnDownHandler);
				}else if(self.screen.attachEvent){
					self.volumeScrubber_do.screen.attachEvent("onmouseover", self.volumeScrubberOnOverHandler);
					self.volumeScrubber_do.screen.attachEvent("onmouseout", self.volumeScrubberOnOutHandler);
					self.volumeScrubber_do.screen.attachEvent("onmousedown", self.volumeScrubberOnDownHandler);
				}
			}
			
			self.enableVolumeScrubber();
			self.updateVolumeScrubber(self.volume);
		};
		
		this.volumeScrubberOnOverHandler =  function(e){
			if(self.isVolumeScrubberDisabled_bl) return;
		};
		
		this.volumeScrubberOnOutHandler =  function(e){
			if(self.isVolumeScrubberDisabled_bl) return;
		};
		
		this.volumeScrubberOnDownHandler =  function(e){
			if(self.isVolumeScrubberDisabled_bl) return;
			if(e.preventDefault) e.preventDefault();
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);	
			var localX = viewportMouseCoordinates.screenX - self.volumeScrubber_do.getGlobalX();
			
			if(localX < 0){
				localX = 0;
			}else if(localX > self.volumeScrubberWidth - self.scrubbersOffsetWidth){
				localX = self.volumeScrubberWidth - self.scrubbersOffsetWidth;
			}
			var percentScrubbed = localX/self.volumeScrubberWidth;
			if(self.disable_do) self.addChild(self.disable_do);
			self.lastVolume = percentScrubbed;
			self.updateVolume(percentScrubbed);
			
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					window.addEventListener("MSPointerMove", self.volumeScrubberMoveHandler);
					window.addEventListener("MSPointerUp", self.volumeScrubberEndHandler);
				}else{
					window.addEventListener("touchmove", self.volumeScrubberMoveHandler);
					window.addEventListener("touchend", self.volumeScrubberEndHandler);
				}
			}else{
				if(window.addEventListener){
					window.addEventListener("mousemove", self.volumeScrubberMoveHandler);
					window.addEventListener("mouseup", self.volumeScrubberEndHandler);		
				}else if(document.attachEvent){
					document.attachEvent("onmousemove", self.volumeScrubberMoveHandler);
					document.attachEvent("onmouseup", self.volumeScrubberEndHandler);		
				}
			}
		};
		
		this.volumeScrubberMoveHandler = function(e){
			if(self.isVolumeScrubberDisabled_bl) return;
			if(e.preventDefault) e.preventDefault();
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);	
			var localX = viewportMouseCoordinates.screenX - self.volumeScrubber_do.getGlobalX();
			
			if(localX < 0){
				localX = 0;
			}else if(localX > self.volumeScrubberWidth - self.scrubbersOffsetWidth){
				localX = self.volumeScrubberWidth - self.scrubbersOffsetWidth;
			}
			var percentScrubbed = localX/self.volumeScrubberWidth;
			self.lastVolume = percentScrubbed;
			self.updateVolume(percentScrubbed);
		};
		
		this.volumeScrubberEndHandler = function(){
			if(self.disable_do){
				if(self.contains(self.disable_do)) self.removeChild(self.disable_do);
			}
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					window.removeEventListener("MSPointerMove", self.volumeScrubberMoveHandler);
					window.removeEventListener("MSPointerUp", self.volumeScrubberEndHandler);
				}else{
					window.removeEventListener("touchmove", self.volumeScrubberMoveHandler);
					window.removeEventListener("touchend", self.volumeScrubberEndHandler);
				}
			}else{
				if(window.removeEventListener){
					window.removeEventListener("mousemove", self.volumeScrubberMoveHandler);
					window.removeEventListener("mouseup", self.volumeScrubberEndHandler);		
				}else if(document.detachEvent){
					document.detachEvent("onmousemove", self.volumeScrubberMoveHandler);
					document.detachEvent("onmouseup", self.volumeScrubberEndHandler);		
				}
			}
		};
		
		this.disableVolumeScrubber = function(){
			self.isVolumeScrubberDisabled_bl = true;
			self.volumeScrubber_do.setButtonMode(false);
			self.volumeScrubberEndHandler();
		};
		
		this.enableVolumeScrubber = function(){
			self.isVolumeScrubberDisabled_bl = false;
			self.volumeScrubber_do.setButtonMode(true);
		};
		
		this.updateVolumeScrubber = function(percent){
			var finalWidth = parseInt(percent * self.volumeScrubberWidth); 
			self.volumeScrubberDrag_do.setWidth(finalWidth);
			
			if(finalWidth < 1 && self.isVolumeScrubberLineVisible_bl){
				self.isVolumeScrubberLineVisible_bl = false;
				FWDRLTweenMax.to(self.volumeScrubberBarLine_do, .5, {alpha:0});
			}else if(finalWidth > 1 && !self.isVolumeScrubberLineVisible_bl){
				self.isVolumeScrubberLineVisible_bl = true;
				FWDRLTweenMax.to(self.volumeScrubberBarLine_do, .5, {alpha:1});
			}
			
			if(finalWidth > self.volumeScrubberWidth - self.scrubbersOffsetWidth) finalWidth = self.volumeScrubberWidth - self.scrubbersOffsetWidth;
			FWDRLTweenMax.to(self.volumeScrubberBarLine_do, .8, {x:finalWidth + 1, ease:Expo.easeOut});
		};
		
		this.updateVolume = function(volume){
			self.volume = volume;
			if(self.volume <= 0.000001){
				self.isMute_bl = true;
				self.volume = 0.000001;
			}else if(self.voume >= 1){
				self.isMute_bl = false;
				self.volume = 1;
			}else{
				self.isMute_bl = false;
			}
			
			if(self.volume == 0.000001){
				if(self.volumeButton_do) self.volumeButton_do.setDisabledState();
			}else{
				if(self.volumeButton_do) self.volumeButton_do.setEnabledState();
			}
			
			if(self.volumeScrubberBarLine_do) self.updateVolumeScrubber(self.volume);
			self.dispatchEvent(FWDRLEAPController.CHANGE_VOLUME, {percent:self.volume});
		};
		
		//###################################//
		/* clean main events */
		//###################################//
		this.cleanMainEvents = function(){
			
		};
	
		this.init();
	};
	
	/* set prototype */
	FWDRLEAPController.setPrototype = function(){
		FWDRLEAPController.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLEAPController.PLAY = "play";
	FWDRLEAPController.PAUSE = "pause";
	FWDRLEAPController.START_TO_SCRUB = "startToScrub";
	FWDRLEAPController.SCRUB = "scrub";
	FWDRLEAPController.STOP_TO_SCRUB = "stopToScrub";
	FWDRLEAPController.CHANGE_VOLUME = "changeVolume";
	
	
	FWDRLEAPController.prototype = null;
	window.FWDRLEAPController = FWDRLEAPController;
	
}());