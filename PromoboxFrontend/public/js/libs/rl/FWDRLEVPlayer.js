/* Gallery */
(function (window){
	
	var FWDRLEVPlayer = function(stageContainer, data){
		
		var self = this;
		
		self.displayType = FWDRLEVPlayer.AFTER_PARENT;
	
		/* init gallery */
		self.init = function(){

			this.mustHaveHolderDiv_bl = false;
			
			window["RLVideoPlayer"] = this;
			self.instanceName_str = "RLVideoPlayer";
			
			if(self.displayType == FWDRLEVPlayer.AFTER_PARENT) self.mustHaveHolderDiv_bl = true;
		
			this.body = document.getElementsByTagName("body")[0];
			this.stageContainer = stageContainer;
			this.data = data;
	
			this.listeners = {events_ar:[]};
			this.main_do = null;
			this.preloader_do = null;
			this.controller_do = null;
			this.videoScreen_do = null;
			this.flash_do = null;
			this.flashObject = null;
			this.videoPoster_do = null;
			this.largePlayButton_do = null;
			this.hider = null;
			
			this.backgroundColor_str = "#000000";
			this.videoBackgroundColor_str = "#000000";
			this.flashObjectMarkup_str =  null;
			
			this.lastX = 0;
			this.lastY = 0;
			this.stageWidth = 0;
			this.stageHeight = 0;
			this.firstTapX;
			this.firstTapY;
			this.curTime;
			this.totalTime;
			
			this.videoSourcePath_str;
			this.prevVideoSourcePath_str;
			this.posterPath_str;
			this.videoType_str;
			this.videoStartBehaviour_str;
			this.prevVideoSource_str;
			this.prevPosterSource_str;
			this.finalVideoPath_str;
		
			this.resizeHandlerId_to;
			this.hidePreloaderId_to;
			this.orientationChangeId_to;
			this.disableClickId_to;
			this.clickDelayId_to;
			this.secondTapId_to;
			
			this.isVideoPlayingWhenOpenWindows_bl = false;
			this.isSpaceDown_bl = false;
			this.isPlaying_bl = false;
			this.firstTapPlaying_bl = false;
			this.stickOnCurrentInstanceKey_bl = false;
			this.isFullScreen_bl = false;
			this.isFlashScreenReady_bl = false;
			this.orintationChangeComplete_bl = true;
			this.disableClick_bl = false;
			this.isAPIReady_bl = false;
			this.isInstantiate_bl = true;
			this.isMobile_bl = FWDRLUtils.isMobile;
			this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
		
			this.setupMainDo();
			this.setupNormalVideoPlayers();
		};
		
		//#############################################//
		/* setup main do */
		//#############################################//
		self.setupMainDo = function(){
			self.main_do = new FWDRLDisplayObject("div");
			self.main_do.getStyle().msTouchAction = "none";
			self.main_do.getStyle().webkitTapHighlightColor = "rgba(0, 0, 0, 0)";
			self.main_do.getStyle().webkitFocusRingColor = "rgba(0, 0, 0, 0)";
			self.main_do.getStyle().width = "100%";
			self.main_do.getStyle().height = "100%";
			self.main_do.setBackfaceVisibility();
			self.main_do.setBkColor(self.backgroundColor_str);
			if(!FWDRLUtils.isMobile || (FWDRLUtils.isMobile && FWDRLUtils.hasPointerEvent)) self.main_do.setSelectable(false);
			
			self.stageContainer.style.overflow = "visible";
			self.stageContainer.appendChild(self.main_do.screen);
			setTimeout(self.resizeHandler, 300);
		};
	

		self.resizeHandler = function(){	
		
			if(self.isFullScreen_bl || self.displayType == FWDRLEVPlayer.FULL_SCREEN){	
				var ws = FWDRLUtils.getViewportSize();
				self.main_do.setX(0);
				self.main_do.setY(0);
				self.stageWidth = ws.w;
				self.stageHeight = ws.h;
			}else{
				self.stageWidth = self.stageContainer.offsetWidth;
				self.stageHeight = self.stageContainer.offsetHeight;
			}
		
			self.main_do.setWidth(self.stageWidth);
			self.main_do.setHeight(self.stageHeight);
		
			if(self.isFlashScreenReady_bl && self.videoType_str == FWDRLEVPlayer.VIDEO){
				self.flash_do.setWidth(self.stageWidth);
				self.flash_do.setHeight(self.stageHeight);
			}
			
			if(self.controller_do) self.controller_do.resizeAndPosition();
			
			if(self.videoScreen_do && self.videoType_str == FWDRLEVPlayer.VIDEO){
				self.videoScreen_do.resizeAndPosition(self.stageWidth, self.stageHeight);
			}
			
			if(self.preloader_do) self.positionPreloader();
			if(self.dumyClick_do){
				self.dumyClick_do.setWidth(self.stageWidth);
				if(self.isMobile_bl){
					self.dumyClick_do.setHeight(self.stageHeight);
				}else{
					self.dumyClick_do.setHeight(self.stageHeight);
				}
			}
			if(self.largePlayButton_do) self.positionLargePlayButton();
			if(self.videoPoster_do && self.videoPoster_do.allowToShow_bl) self.videoPoster_do.positionAndResize();
			
		};
		
		//###############################################//
		/* Setup click screen */
		//###############################################//
		this.setupClickScreen = function(){
			self.dumyClick_do = new FWDRLDisplayObject("div");
			if(FWDRLUtils.isIE){
				self.dumyClick_do.setBkColor("#00FF00");
				self.dumyClick_do.setAlpha(.0001);
			}
			if(self.dumyClick_do.screen.addEventListener){
				self.dumyClick_do.screen.addEventListener("click", self.playPauseClickHandler);
			}else if(self.dumyClick_do.screen.attachEvent){
				self.dumyClick_do.screen.attachEvent("onclick", self.playPauseClickHandler);
			}
			self.hideClickScreen();
			self.main_do.addChild(self.dumyClick_do);
		};
		
		this.playPauseClickHandler = function(e){
			if(e.button == 2) return;
			if(self.disableClick_bl) return;
			self.firstTapPlaying_bl = self.isPlaying_bl;
			
			FWDRLEVPlayer.keyboardCurInstance = self;
			
			if(self.controller_do.mainHolder_do.y != 0 && self.isMobile_bl) return;
			
			
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.togglePlayPause();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.togglePlayPause();
			}
		};
		
		this.showClickScreen = function(){
			self.dumyClick_do.setVisible(true);
		};
		
		this.hideClickScreen = function(){
			self.dumyClick_do.setVisible(false);
		};
		
		this.disableClick = function(){
			self.disableClick_bl = true;
			clearTimeout(self.disableClickId_to);
			self.disableClickId_to =  setTimeout(function(){
				self.disableClick_bl = false;
			}, 500);
		};
		
		//########################################//
		/* add double click and tap support */
		//########################################//
		this.addDoubleClickSupport = function(){	
			if(!self.isMobile_bl && self.dumyClick_do.screen.addEventListener){
				self.dumyClick_do.screen.addEventListener("mousedown", self.onFirstDown);
				if(FWDRLUtils.isIEWebKit) self.dumyClick_do.screen.addEventListener("dblclick", self.onSecondDown);
			}else if(self.isMobile_bl){
				self.dumyClick_do.screen.addEventListener("touchstart", self.onFirstDown);
			}else if(self.dumyClick_do.screen.addEventListener){
				self.dumyClick_do.screen.addEventListener("mousedown", self.onFirstDown);
			}
		};
		
		this.onFirstDown = function(e){
			if(e.button == 2) return;
			if(self.isFullscreen_bl && e.preventDefault) e.preventDefault();
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);
			self.firstTapX = viewportMouseCoordinates.screenX;
			self.firstTapY = viewportMouseCoordinates.screenY;
			
			self.firstTapPlaying_bl = self.isPlaying_bl;
			
			if(FWDRLUtils.isIEWebKit) return;
			
			if(self.isMobile_bl){
				self.dumyClick_do.screen.addEventListener("touchstart", self.onSecondDown);
				self.dumyClick_do.screen.removeEventListener("touchstart", self.onFirstDown);
			}else{
				if(self.dumyClick_do.screen.addEventListener){
					self.dumyClick_do.screen.addEventListener("mousedown", self.onSecondDown);
					self.dumyClick_do.screen.removeEventListener("mousedown", self.onFirstDown);
				}
			}
			clearTimeout(self.secondTapId_to);
			self.secondTapId_to = setTimeout(self.doubleTapExpired, 250);
		};
		
		this.doubleTapExpired = function(){
			clearTimeout(self.secondTapId_to);
			if(self.isMobile_bl){
				self.dumyClick_do.screen.removeEventListener("touchstart", self.onSecondDown);
				self.dumyClick_do.screen.addEventListener("touchstart", self.onFirstDown);
			}else{
				if(self.dumyClick_do.screen.addEventListener){
					self.dumyClick_do.screen.removeEventListener("mousedown", self.onSecondDown);
					self.dumyClick_do.screen.addEventListener("mousedown", self.onFirstDown);
				}
			}
		};
		
		this.onSecondDown = function(e){
			if(e.preventDefault) e.preventDefault();
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);
			var dx;
			var dy;
			
			if(FWDRLUtils.isIEWebKit) self.firstTapPlaying_bl = self.isPlaying_bl;

			if(e.touches && e.touches.length != 1) return;
			dx = Math.abs(viewportMouseCoordinates.screenX - self.firstTapX);   
			dy = Math.abs(viewportMouseCoordinates.screenY - self.firstTapY); 
		
			if(self.isMobile_bl && (dx > 10 || dy > 10)){
				return;
			}else if(!self.isMobile_bl && (dx > 2 || dy > 2)){
				return
			}
			self.switchFullScreenOnDoubleClick();
			
			if(!FWDRLUtils.isIEWebKit){
				if(self.firstTapPlaying_bl){
					self.play();
				}else{
					self.pause();
				}
			}
		};
		
		this.switchFullScreenOnDoubleClick = function(){
			self.disableClick();
			if(!self.isFullScreen_bl){
				self.goFullScreen();
			}else{
				self.goNormalScreen();
			}
		};
	
		
		this.setupNormalVideoPlayers = function(){
			self.setupPreloader();
			if(FWDRLEVPlayer.hasHTML5Video){
				self.isAPIReady_bl = true;
				self.setupVideoScreen();
				self.setupVideoPoster();
				self.main_do.addChild(self.preloader_do);	
				self.setupClickScreen();
				self.addDoubleClickSupport();
				self.setupController();
				self.setupLargePlayPauseButton();
				self.setupHider();
				self.dispatchEvent(FWDRLEVPlayer.READY);
				self.setPosterSource(self.posterPath_str);
			}else{
				self.setupFlashScreen();
			}
			
			self.resizeHandler();
		};
		
		//#############################################//
		/* setup preloader */
		//#############################################//
		this.setupPreloader = function(){
			FWDRLPreloader.setPrototype();
			self.preloader_do = new FWDRLPreloader(self.data.videoMainPreloader_img, 30, 30, 30, 40);
			self.preloader_do.show(true);
			self.main_do.addChild(self.preloader_do);
		};
	
		this.positionPreloader = function(){
			self.preloader_do.setX(parseInt((self.stageWidth - self.preloader_do.w)/2));
			self.preloader_do.setY(parseInt((self.stageHeight - self.preloader_do.h)/2));
		};
		
		//##########################################//
		/* setup video poster */
		//##########################################//
		this.setupVideoPoster = function(){
			FWDRLEVPPoster.setPrototype();
			self.videoPoster_do = new FWDRLEVPPoster(self, self.data.videoPosterBackgroundColor_str, self.data.show);
			self.main_do.addChild(self.videoPoster_do);
		};
		
		//###########################################//
		/* Setup large play / pause button */
		//###########################################//
		this.setupLargePlayPauseButton = function(){
			FWDRLSimpleButton.setPrototype(true);
			self.largePlayButton_do = new FWDRLSimpleButton(self.data.videoLargePlayN_img, self.data.videoLargePlayS_str);
			self.largePlayButton_do.addListener(FWDRLSimpleButton.MOUSE_UP, self.largePlayButtonUpHandler);
			self.largePlayButton_do.setOverflow("visible");
			self.largePlayButton_do.hide(false);
			self.main_do.addChild(self.largePlayButton_do);
		};
		
		this.largePlayButtonUpHandler = function(){
			self.disableClick();
			self.largePlayButton_do.hide();
			self.play();
		};
		
		this.positionLargePlayButton =  function(){
			self.largePlayButton_do.setX(parseInt((self.stageWidth - self.largePlayButton_do.w)/2));
			self.largePlayButton_do.setY(parseInt((self.stageHeight - self.largePlayButton_do.h)/2));
		};
		
		//###########################################//
		/* setup controller */
		//###########################################//
		this.setupController = function(){
			FWDRLEVPController.setPrototype();
			self.controller_do = new FWDRLEVPController(self.data, self);
			self.controller_do.addListener(FWDRLEVPController.PLAY, self.controllerOnPlayHandler);
			self.controller_do.addListener(FWDRLEVPController.PAUSE, self.controllerOnPauseHandler);
			self.controller_do.addListener(FWDRLEVPController.START_TO_SCRUB, self.controllerStartToScrubbHandler);
			self.controller_do.addListener(FWDRLEVPController.SCRUB, self.controllerScrubbHandler);
			self.controller_do.addListener(FWDRLEVPController.STOP_TO_SCRUB, self.controllerStopToScrubbHandler);
			self.controller_do.addListener(FWDRLEVPController.CHANGE_VOLUME, self.controllerChangeVolumeHandler);
			self.controller_do.addListener(FWDRLEVPController.FULL_SCREEN, self.controllerFullScreenHandler);
			self.controller_do.addListener(FWDRLEVPController.NORMAL_SCREEN, self.controllerNormalScreenHandler);
			self.main_do.addChild(self.controller_do);
		};
		
		this.controllerOnPlayHandler = function(e){
			self.play();
		};
		
		this.controllerOnPauseHandler = function(e){
			self.pause();
		};
		
		this.controllerStartToScrubbHandler = function(e){
			self.startToScrub();
		};
		
		this.controllerScrubbHandler = function(e){
			self.scrub(e.percent);
		};
		
		this.controllerStopToScrubbHandler = function(e){
			self.stopToScrub();
		};
		
		this.controllerChangeVolumeHandler = function(e){
			self.setVolume(e.percent);
		};
		
		this.controllerFullScreenHandler = function(){
			self.goFullScreen();
		};
		
		this.controllerNormalScreenHandler = function(){
			self.goNormalScreen();
		};
		
		
		
		//###########################################//
		/* setup FWDRLEVPVideoScreen */
		//###########################################//
		this.setupVideoScreen = function(){
			FWDRLEVPVideoScreen.setPrototype();
			self.videoScreen_do = new FWDRLEVPVideoScreen(self, self.backgroundColor_str, self.data.volume);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.ERROR, self.videoScreenErrorHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.SAFE_TO_SCRUBB, self.videoScreenSafeToScrubbHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.STOP, self.videoScreenStopHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.PLAY, self.videoScreenPlayHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.PAUSE, self.videoScreenPauseHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.UPDATE, self.videoScreenUpdateHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.UPDATE_TIME, self.videoScreenUpdateTimeHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.LOAD_PROGRESS, self.videoScreenLoadProgressHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.START_TO_BUFFER, self.videoScreenStartToBuferHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.STOP_TO_BUFFER, self.videoScreenStopToBuferHandler);
			self.videoScreen_do.addListener(FWDRLEVPVideoScreen.PLAY_COMPLETE, self.videoScreenPlayCompleteHandler);
			self.main_do.addChild(self.videoScreen_do);
		};
		
		this.videoScreenErrorHandler = function(e){
			var error;
			self.isPlaying_bl = false;
			
			error = e.text;
			if(window.console) console.log(e.text);
			
			if(self.controller_do){
				self.controller_do.disableMainScrubber();
				if(!self.data.showControllerWhenVideoIsStopped_bl) self.controller_do.hide(!self.isMobile_bl, true);
				self.largePlayButton_do.hide();
				self.hideClickScreen();
				self.hider.stop();
			}
			
			
			if(FWDRLUtils.isIphone){
				if(self.videoScreen_do) self.videoScreen_do.setX(-5000);
			}
			
			self.preloader_do.hide(false);
			self.showCursor();
			self.stop();
			self.dispatchEvent(FWDRLEVPlayer.ERROR, {error:error});
		};
		
		this.videoScreenSafeToScrubbHandler = function(){
			if(self.controller_do){
				self.controller_do.enableMainScrubber();
				self.controller_do.show(true);
				self.hider.start();
			}
			if(self.data.addKeyboardSupport_bl) self.addKeyboardSupport();
			self.showClickScreen();
		};
		
		this.videoScreenStopHandler = function(e){
			
			self.videoPoster_do.allowToShow_bl = true;
			self.isPlaying_bl = false;
			
			if(self.controller_do){
				self.controller_do.disableMainScrubber();
				self.controller_do.showPlayButton();
				if(!self.data.showControllerWhenVideoIsStopped_bl){
					self.controller_do.hide(!self.isMobile_bl, true);
				}else{
					self.controller_do.show(!self.isMobile_bl);
				}
				self.hider.stop();
			}

			self.hideClickScreen();
			
			self.hider.reset();
			self.showCursor();
			self.dispatchEvent(FWDRLEVPlayer.STOP);
		};
		
		this.videoScreenPlayHandler = function(){
			FWDRLEVPlayer.keyboardCurInstance = self;
		
			self.isPlaying_bl = true;
			
			if(self.controller_do){
				self.controller_do.showPauseButton();
				self.controller_do.show(true);
			}
			self.largePlayButton_do.hide();
			self.hider.start();
			self.showCursor();
			self.dispatchEvent(FWDRLEVPlayer.PLAY);
		};
		
		this.videoScreenPauseHandler = function(){
			
			self.isPlaying_bl = false;
			
			if(self.controller_do) self.controller_do.showPlayButton(); 
			if(!FWDRLUtils.isIphone) self.largePlayButton_do.show();
			self.controller_do.show(true);
			self.hider.stop();
			self.hider.reset();
			self.showCursor();
			self.showClickScreen();
			self.dispatchEvent(FWDRLEVPlayer.PAUSE);
		};
		
		this.videoScreenUpdateHandler = function(e){
			var percent;	
			if(FWDRLEVPlayer.hasHTML5Video){
				percent = e.percent;
				if(self.controller_do) self.controller_do.updateMainScrubber(percent);
			}else{
				percent = e;
				if(self.controller_do) self.controller_do.updateMainScrubber(percent);
			}
			self.dispatchEvent(FWDRLEVPlayer.UPDATE, {percent:percent});
		};
		
		this.videoScreenUpdateTimeHandler = function(e, e2){
			var time;
			if(FWDRLEVPlayer.hasHTML5Video){
				self.curTime = e.curTime;
				self.totalTime = e.totalTime;
				time = self.curTime + "/" + self.totalTime;
				if(self.controller_do) self.controller_do.updateTime(time);
			}else{
				self.curTime = e;
				self.totalTime = e2;
				time = self.curTime + "/" + self.totalTime;
				if(e == undefined || e2 ==  undefined) time = "00:00/00:00";
				if(self.controller_do) self.controller_do.updateTime(time);
			}
			self.dispatchEvent(FWDRLEVPlayer.UPDATE_TIME, {currentTime:self.curTime, totalTime:self.totalTime});
		};
		
		this.videoScreenLoadProgressHandler = function(e){
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.controller_do) self.controller_do.updatePreloaderBar(e.percent);
			}else{
				if(self.controller_do) self.controller_do.updatePreloaderBar(e);
			}
		};
		
		this.videoScreenStartToBuferHandler = function(){
			self.preloader_do.show();
		};
		
		this.videoScreenStopToBuferHandler = function(){
			self.preloader_do.hide(true);
		};
		
		this.videoScreenPlayCompleteHandler = function(){
			if(self.data.videoLoop_bl){
				self.scrub(0);
				self.play();
			}else{
				self.stop();
			}
			self.hider.reset();
			self.dispatchEvent(FWDRLEVPlayer.PLAY_COMPLETE);
		};
		
		
		//#############################################//
		/* Flash screen... */
		//#############################################//
		this.setupFlashScreen = function(){
			if(self.flash_do) return;
			
			if(!FWDRLFlashTest.hasFlashPlayerVersion("9.0.18")){
				var error = "Please install Adobe flash player! <a href='http://www.adobe.com/go/getflashplayer'>Click here to install.</a>";
				self.dispatchEvent(FWDRLEVPlayer.ERROR, {error:error});
				return;
			}
			
			self.flash_do = new FWDRLDisplayObject("div");
			self.flash_do.setBackfaceVisibility();
			self.flash_do.setResizableSizeAfterParent();	
			self.main_do.addChild(self.flash_do);
		
			self.flashObjectMarkup_str = '<object id="' + self.instanceName_str + '"classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="100%" height="100%"><param name="movie" value="' + self.data.flashPath_str + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/><param name=FlashVars value="instanceName=' + self.instanceName_str + '&volume=' + self.data.volume + '&bkColor_str=' + self.videoBackgroundColor_str + '"/><object type="application/x-shockwave-flash" data="' + self.data.flashPath_str + '" width="100%" height="100%"><param name="movie" value="' + self.data.flashPath_str + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/><param name=FlashVars value="instanceName=' + self.instanceName_str + '&volume=' + self.data.volume + '&bkColor_str=' + self.videoBackgroundColor_str + '"/></object></object>';
			
			self.flash_do.screen.innerHTML = self.flashObjectMarkup_str;
			self.flashObject = self.flash_do.screen.firstChild;
			if(!FWDRLUtils.isIE) self.flashObject = self.flashObject.getElementsByTagName("object")[0];
		};
	
		this.flashScreenIsReady = function(){
			if(console) console.dir("flash video ready " + self.instanceName_str);
			self.isFlashScreenReady_bl = true;
			self.isAPIReady_bl = true;
			self.setupVideoPoster();
			self.main_do.addChild(self.preloader_do);
			self.setupClickScreen();
			self.addDoubleClickSupport();
			self.setupController();
			self.setupLargePlayPauseButton();
			self.setupHider();
			self.setPosterSource(self.posterPath_str);
			self.dispatchEvent(FWDRLEVPlayer.READY);
		};
		
		this.flashScreenFail = function(){
			self.dispatchEvent(FWDRLEVPlayer.ERROR, {error:error});
		};
		
		//######################################//
		/* Add keyboard support */
		//######################################//
		this.addKeyboardSupport = function(){
			if(document.addEventListener){
				document.addEventListener("keydown",  this.onKeyDownHandler);	
				document.addEventListener("keyup",  this.onKeyUpHandler);	
			}else if(document.attachEvent){
				document.attachEvent("onkeydown",  this.onKeyDownHandler);	
				document.attachEvent("onkeyup",  this.onKeyUpHandler);	
			}
		};
		
		this.removeKeyboardSupport = function(){
			if(document.removeEventListener){
				document.removeEventListener("keydown",  this.onKeyDownHandler);	
				document.removeEventListener("keyup",  this.onKeyUpHandler);	
			}else if(document.detachEvent){
				document.detachEvent("onkeydown",  this.onKeyDownHandler);	
				document.detachEvent("onkeyup",  this.onKeyUpHandler);	
			}
		};
		
		this.onKeyDownHandler = function(e){
			if(self.isSpaceDown_bl) return;
			self.isSpaceDown_bl = true;
			if (e.keyCode == 32){
				if(self != FWDRLEVPlayer.keyboardCurInstance 
				   && (FWDRLEVPlayer.videoStartBehaviour == "pause" || FWDRLEVPlayer.videoStartBehaviour == "none")) return
				self.stickOnCurrentInstanceKey_bl = true;
				if(FWDRLEVPlayer.hasHTML5Video){
					if(!self.videoScreen_do.isSafeToBeControlled_bl) return;
					self.videoScreen_do.togglePlayPause();
				}else if(self.isFlashScreenReady_bl){
					self.flashObject.togglePlayPause();
				}
				if(e.preventDefault) e.preventDefault();
				return false;
			}
		};
		
		this.onKeyUpHandler = function(e){
			self.isSpaceDown_bl = false;
		};
		
		//####################################//
		/* Setup hider */
		//####################################//
		this.setupHider = function(){
			FWDRLHider.setPrototype();
			self.hider = new FWDRLHider(self.main_do, self.data.controllerHideDelay);
			self.hider.addListener(FWDRLHider.SHOW, self.hiderShowHandler);
			self.hider.addListener(FWDRLHider.HIDE, self.hiderHideHandler);
			self.hider.addListener(FWDRLHider.HIDE_COMPLETE, self.hiderHideCompleteHandler);
		};
		
		this.hiderShowHandler = function(){
			if(self.isPlaying_bl) self.controller_do.show(true);
			self.showCursor();
		};
		
		this.hiderHideHandler = function(){
			if(FWDRLUtils.isIphone) return;
			
			if(FWDRLUtils.hitTest(self.controller_do.screen, self.hider.globalX, self.hider.globalY)){
				self.hider.reset();
				return;
			}
			
			self.controller_do.hide(true);
			if(self.isFullScreen_bl) self.hideCursor();
		};
		
		this.hiderHideCompleteHandler = function(){
			self.controller_do.positionScrollBarOnTopOfTheController();
		};
		
		//####################################//
		// API
		//###################################//
		this.play = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLUtils.isIphone) self.videoScreen_do.setX(0);
			
			
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.play();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.playVideo();
			}
			
			FWDRLEVPlayer.keyboardCurInstance = self;
			self.videoPoster_do.allowToShow_bl = false;
			self.largePlayButton_do.hide();
			self.videoPoster_do.hide();
		};
		
		this.pause = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLUtils.isIphone) self.videoScreen_do.setX(0);
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.pause();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.pauseVideo();
			}
		};
		
		this.resume = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLUtils.isIphone) self.videoScreen_do.setX(0);
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.resume();
			}
		};
		
		this.stop = function(source){
			if(!self.isAPIReady_bl) return;
			self.isPlaying_bl = false;
			self.hider.reset();
			if(FWDRLUtils.isIphone) self.videoScreen_do.setX(-5000);
			if(FWDRLEVPlayer.hasHTML5Video){
				self.videoScreen_do.stop();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.stopVideo();
			}
				
			if(self.isMobile_bl){
				if(source && source.indexOf(".") != -1){
					if(self.data.showControllerWhenVideoIsStopped_bl) self.controller_do.show(true);
					self.videoPoster_do.show();
					self.largePlayButton_do.show();
				}else{
					if(!source){
						self.videoPoster_do.show();
						self.largePlayButton_do.show();
					}
				}
			}else{
				if(self.data.showControllerWhenVideoIsStopped_bl) self.controller_do.show(true);
				self.videoPoster_do.show();
				self.largePlayButton_do.show();
			}
			if(self.data.addKeyboardSupport_bl) self.removeKeyboardSupport();
		};
		
		this.startToScrub = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.startToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.startToScrub();
			}
		};
		
		this.stopToScrub = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.stopToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.stopToScrub();
			}
		};
		
		this.scrub = function(percent){
			if(!self.isAPIReady_bl) return;
			if(isNaN(percent)) return;
			if(percent < 0){
				percent = 0;
			}else if(percent > 1){
				percent = 1;
			}
			
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.scrub(percent);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.scrub(percent);
			}
		};
		
		this.setVolume = function(volume){
			if(!self.isAPIReady_bl || self.isMobile_bl) return;
			self.controller_do.updateVolume(volume, true);
			
			if(FWDRLEVPlayer.hasHTML5Video){
				if(self.videoScreen_do) self.videoScreen_do.setVolume(volume);
			}
			
			if(self.isFlashScreenReady_bl){
				self.flashObject.setVolume(volume);
			}
			self.dispatchEvent(FWDRLEVPlayer.VOLUME_SET, {volume:volume});
		};
		
		this.setPosterSource = function(path){
			if(!self.isAPIReady_bl || !path) return;
			var path_ar = path.split(",");
				
			if(self.isMobile_bl && path_ar[1] != undefined){
				path = path_ar[1];
			}else{
				path = path_ar[0];
			}
		
			self.posterPath_str = path;
			
			self.videoPoster_do.setPoster(self.posterPath_str);
			if(self.prevPosterSource_str != path) self.dispatchEvent(FWDRLEVPlayer.UPDATE_POSTER_SOURCE);
			
			self.prevPosterSource_str = path;
		};
		
		this.setVideoSource = function(source, overwrite){
			if(!self.isAPIReady_bl) return;
			
			if(source ==  self.prevVideoSource_str && !overwrite) return;
			self.prevVideoSource_str = source;
			
			if(!source){
				self.dispatchEvent(FWDRLEVPlayer.ERROR, {error:"Video source is not defined!"});
				return;
			}
		
			self.stop(source);
			self.videoSourcePath_str = source;
			self.finalVideoPath_str = source;
		
		
			self.videoType_str = FWDRLEVPlayer.VIDEO;
			
			var path_ar = source.split(",");
			
			if(self.isMobile_bl && path_ar[1] != undefined){
				source = path_ar[1];
			}else{
				source = path_ar[0];
			}
			self.finalVideoPath_str = source;
		
			if(FWDRLEVPlayer.hasHTML5Video && self.videoType_str == FWDRLEVPlayer.VIDEO){
				self.setPosterSource(self.posterPath_str);
			
				self.videoPoster_do.show();
				self.largePlayButton_do.show();
				
				if(FWDRLUtils.isIphone) self.videoScreen_do.setX(-5000);
				
				self.videoScreen_do.setVisible(true);
				if(self.videoScreen_do){
					self.videoScreen_do.setSource(source);
					if(self.data.videoAutoPlay_bl) self.play();
				}
				
			}else if(self.isFlashScreenReady_bl && self.videoType_str == FWDRLEVPlayer.VIDEO){
				if(source.indexOf("://") == -1 && source.indexOf("/") != 1){
					source =  source.substr(source.indexOf("/") + 1);
				}
				
				self.videoPoster_do.show();
				self.largePlayButton_do.show();
				
				self.flashObject.setSource(source);
				if(self.data.videoAutoPlay_bl) self.play();
			}
			
			self.prevVideoSourcePath_str = self.videoSourcePath_str;
			self.resizeHandler();
			if(self.getVideoSource()) self.dispatchEvent(FWDRLEVPlayer.UPDATE_VIDEO_SOURCE);
		};
		
	
		//#############################################//
		/* go fullscreen / normal screen */
		//#############################################//
		this.goFullScreen = function(){
			if(!self.isAPIReady_bl) return;
			
			if(document.addEventListener){
				document.addEventListener("fullscreenchange", self.onFullScreenChange);
				document.addEventListener("mozfullscreenchange", self.onFullScreenChange);
				document.addEventListener("webkitfullscreenchange", self.onFullScreenChange);
				document.addEventListener("MSFullscreenChange", self.onFullScreenChange);
			}
			
			//if(!self.isMobile_bl){
				if(self.main_do.screen.requestFullScreen) {
					self.main_do.screen.requestFullScreen();
				}else if(self.main_do.screen.mozRequestFullScreen){ 
					self.main_do.screen.mozRequestFullScreen();
				}else if(self.main_do.screen.webkitRequestFullScreen){
					self.main_do.screen.webkitRequestFullScreen();
				}else if(self.main_do.screen.msRequestFullscreen){
					self.main_do.screen.msRequestFullscreen();
				}
			//}
			
			self.disableClick();
			
			
			//self.main_do.getStyle().position = "fixed";
			document.documentElement.style.overflow = "hidden";
			self.main_do.getStyle().zIndex = 9999999999999999999;
			self.stageContainer.style.overflow = "visible";
		
			self.isFullScreen_bl = true;
			self.controller_do.showNormalScreenButton();
			self.controller_do.setNormalStateToFullScreenButton();
			var scrollOffsets = FWDRLUtils.getScrollOffsets();
			self.lastX = scrollOffsets.x;
			self.lastY = scrollOffsets.y;
			
			if(self.isMobile_bl) window.addEventListener("touchmove", self.disableFullScreenOnMobileHandler);
			self.dispatchEvent(FWDRLEVPlayer.GO_FULLSCREEN);
			setTimeout(self.resizeHandler, 50);
		};
		
		this.disableFullScreenOnMobileHandler = function(e){
			if(e.preventDefault) e.preventDefault();
		};
		
		this.goNormalScreen = function(){		
			if(!self.isAPIReady_bl) return;
			
			if (document.cancelFullScreen) {  
				document.cancelFullScreen();  
			}else if (document.mozCancelFullScreen) {  
				document.mozCancelFullScreen();  
			}else if (document.webkitCancelFullScreen) {  
				document.webkitCancelFullScreen();  
			}else if (document.msExitFullscreen) {  
				document.msExitFullscreen();  
			}
		
			self.addMainDoToTheOriginalParent();
			self.isFullScreen_bl = false;
		};
		
		this.addMainDoToTheOriginalParent = function(){
			if(!self.isFullScreen_bl) return;
			
			if(document.removeEventListener){
				document.removeEventListener("fullscreenchange", self.onFullScreenChange);
				document.removeEventListener("mozfullscreenchange", self.onFullScreenChange);
				document.removeEventListener("webkitfullscreenchange", self.onFullScreenChange);
				document.removeEventListener("MSFullscreenChange", self.onFullScreenChange);
			}
				
			self.controller_do.setNormalStateToFullScreenButton();
			
			
			if(FWDRLUtils.isIEAndLessThen9){
				document.documentElement.style.overflow = "auto";
			}else{
				document.documentElement.style.overflow = "visible";
			}
			self.main_do.getStyle().position = "relative";
			self.main_do.getStyle().zIndex = 0;
			
			self.controller_do.showFullScreenButton();
			//window.scrollTo(self.lastX, self.lastY);
			
			setTimeout(function(){
				//window.scrollTo(self.lastX, self.lastY);
				self.resizeHandler();
			}, 50);
			
			if(self.isMobile_bl) window.removeEventListener("touchmove", self.disableFullScreenOnMobileHandler);
			self.dispatchEvent(FWDRLEVPlayer.GO_NORMALSCREEN);
		};
		
		this.onFullScreenChange = function(e){
			if(!(document.fullScreen || document.msFullscreenElement  || document.mozFullScreen || document.webkitIsFullScreen || document.msieFullScreen)){
				self.controller_do.showNormalScreenButton();
				self.addMainDoToTheOriginalParent();
				self.isFullScreen_bl = false;
			}
		};
		
		this.getVideoSource = function(){
			if(!self.isAPIReady_bl) return;
			return self.finalVideoPath_str;
		};
		
		this.getPosterSource = function(){
			if(!self.isAPIReady_bl) return;
			return self.posterPath_str;
		};
		
		this.getCurrentTime = function(){
			var tm;
			if(!self.curTime){
				tm = "00:00";
			}else{
				tm = self.curTime;
			}
			return tm;
		};
		
		this.getTotalTime = function(){
			var tm;
			if(!self.totalTime){
				tm = "00:00";
			}else{
				tm = self.totalTime;
			}
			return tm;
		};
		
		//###########################################//
		/* Hide / show cursor */
		//###########################################//
		this.hideCursor = function(){
			document.documentElement.style.cursor = "none";
			document.getElementsByTagName("body")[0].style.cursor = "none";
		};
		
		this.showCursor = function(){
			document.documentElement.style.cursor = "auto";
			document.getElementsByTagName("body")[0].style.cursor = "auto";
		};

		
		//###########################################//
		/* event dispatcher */
		//###########################################//
		this.addListener = function (type, listener){
	    	if(type == undefined) throw Error("type is required.");
	    	if(typeof type === "object") throw Error("type must be of type String.");
	    	if(typeof listener != "function") throw Error("listener must be of type Function.");
	    	
	        var event = {};
	        event.type = type;
	        event.listener = listener;
	        event.target = this;
	        this.listeners.events_ar.push(event);
	    };
	    
	    this.dispatchEvent = function(type, props){
	    	if(this.listeners == null) return;
	    	if(type == undefined) throw Error("type is required.");
	    	if(typeof type === "object") throw Error("type must be of type String.");
	    	
	        for (var i=0, len=this.listeners.events_ar.length; i < len; i++){
	        	if(this.listeners.events_ar[i].target === this && this.listeners.events_ar[i].type === type){		
	    	        if(props){
	    	        	for(var prop in props){
	    	        		this.listeners.events_ar[i][prop] = props[prop];
	    	        	}
	    	        }
	        		this.listeners.events_ar[i].listener.call(this, this.listeners.events_ar[i]);
	        	}
	        }
	    };
	    
	   this.removeListener = function(type, listener){
	    	
	    	if(type == undefined) throw Error("type is required.");
	    	if(typeof type === "object") throw Error("type must be of type String.");
	    	if(typeof listener != "function") throw Error("listener must be of type Function." + type);
	    	
	        for (var i=0, len=this.listeners.events_ar.length; i < len; i++){
	        	if(this.listeners.events_ar[i].target === this 
	        			&& this.listeners.events_ar[i].type === type
	        			&& this.listeners.events_ar[i].listener ===  listener
	        	){
	        		this.listeners.events_ar.splice(i,1);
	        		break;
	        	}
	        }  
	    };
	    
	    self.init();
	};
	
	
	/* set prototype */
	FWDRLEVPlayer.setPrototype =  function(){
		FWDRLEVPlayer.prototype = new FWDRLEventDispatcher();
	};

	FWDRLEVPlayer.hasHTML5Video = (function(){
		var videoTest_el = document.createElement("video");
		var flag = false;
		if(videoTest_el.canPlayType){
			flag = Boolean(videoTest_el.canPlayType('video/mp4') == "probably" || videoTest_el.canPlayType('video/mp4') == "maybe");
			FWDRLEVPlayer.canPlayMp4 = Boolean(videoTest_el.canPlayType('video/mp4') == "probably" || videoTest_el.canPlayType('video/mp4') == "maybe");
		}
		
		if(self.isMobile_bl) return true;
		//return false;
		return flag;
	}());
	
	
	FWDRLEVPlayer.instaces_ar = [];
	
	FWDRLEVPlayer.curInstance = null;
	FWDRLEVPlayer.keyboardCurInstance = null;
	FWDRLEVPlayer.areInstancesCreated_bl = null;
	
	FWDRLEVPlayer.PAUSE_ALL_VIDEOS = "pause";
	FWDRLEVPlayer.STOP_ALL_VIDEOS = "stop";
	FWDRLEVPlayer.DO_NOTHING = "none";
	FWDRLEVPlayer.VIDEO = "video";
	
	FWDRLEVPlayer.READY = "ready";
	FWDRLEVPlayer.STOP = "stop";
	FWDRLEVPlayer.PLAY = "play";
	FWDRLEVPlayer.PAUSE = "pause";
	FWDRLEVPlayer.UPDATE = "update";
	FWDRLEVPlayer.UPDATE_TIME = "updateTime";
	FWDRLEVPlayer.UPDATE_VIDEO_SOURCE = "updateVideoSource";
	FWDRLEVPlayer.UPDATE_POSTER_SOURCE = "udpatePosterSource";
	FWDRLEVPlayer.ERROR = "error";
	FWDRLEVPlayer.PLAY_COMPLETE = "playComplete";
	FWDRLEVPlayer.VOLUME_SET = "volumeSet";
	FWDRLEVPlayer.GO_FULLSCREEN = "goFullScreen";
	FWDRLEVPlayer.GO_NORMALSCREEN = "goNormalScreen";
	
	FWDRLEVPlayer.RESPONSIVE = "responsive";
	FWDRLEVPlayer.FULL_SCREEN = "fullscreen";
	FWDRLEVPlayer.AFTER_PARENT = "afterparent";
	
	
	window.FWDRLEVPlayer = FWDRLEVPlayer;
	
}(window));