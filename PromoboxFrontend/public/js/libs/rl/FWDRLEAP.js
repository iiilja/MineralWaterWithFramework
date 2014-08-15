/* Gallery */
(function (window){
	
	var FWDRLEAP = function(stageContainer, data){
		
		var self = this;
	
		/* init gallery */
		self.init = function(){
			
			window["RLAudioPlayer"] = this;
			self.instanceName_str = "RLAudioPlayer";
			
			this.data = data;
			this.stageContainer = stageContainer;
			this.listeners = {events_ar:[]};
			this.main_do = null;
			this.controller_do = null;
			this.audioScreen_do = null;
			this.flash_do = null;
			this.flashObject = null;
			
			this.backgroundColor_str = self.data.audioControllerBackgroundColor_str || "transparent";
			this.flashObjectMarkup_str =  null;
			this.sourcePath_str;
			
			this.stageWidth = 0;
			this.stageHeight = 0;
	
			this.isAPIReady_bl = false;
			this.isFlashScreenReady_bl = false;
			this.orintationChangeComplete_bl = true;
			this.isMobile_bl = FWDRLUtils.isMobile;
			this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
			this.hasLoadingSkinError_bl = false;
			this.setupMainDo();
	
			
			if(FWDRLEAP.hasHTML5Audio){
				this.setupAudioScreen(self.data);
				this.setupController();
				this.isAPIReady_bl = true;
				this.dispatchEvent(FWDRLEAP.READY);
			}else{
				this.setupFlashScreen();
			}
		};
		
		//#############################################//
		/* setup main do */
		//#############################################//
		self.setupMainDo = function(){
			self.main_do = new FWDRLDisplayObject("div", "relative");
			self.main_do.getStyle().msTouchAction = "none";
			self.main_do.setBackfaceVisibility();
			self.main_do.setBkColor(self.backgroundColor_str);
			if(!FWDRLUtils.isMobile || (FWDRLUtils.isMobile && FWDRLUtils.hasPointerEvent)) self.main_do.setSelectable(false);
			self.stageContainer.appendChild(self.main_do.screen);
			setTimeout(self.resizeHandler, 300);
		};
		
		
		self.resizeHandler = function(){
			self.stageWidth = self.stageContainer.offsetWidth;
			self.stageHeight = self.stageContainer.offsetHeight;
	
			self.main_do.setWidth(self.stageWidth);
			self.main_do.setHeight(self.stageHeight);
			
			if(self.controller_do) self.controller_do.resizeAndPosition();
		};

		
		//###########################################//
		/* setup controller */
		//###########################################//
		this.setupController = function(){
			FWDRLEAPController.setPrototype();
			self.controller_do = new FWDRLEAPController(self.data, self);
			self.controller_do.addListener(FWDRLEAPController.PLAY, self.controllerOnPlayHandler);
			self.controller_do.addListener(FWDRLEAPController.PAUSE, self.controllerOnPauseHandler);
			self.controller_do.addListener(FWDRLEAPController.START_TO_SCRUB, self.controllerStartToScrubbHandler);
			self.controller_do.addListener(FWDRLEAPController.SCRUB, self.controllerScrubbHandler);
			self.controller_do.addListener(FWDRLEAPController.STOP_TO_SCRUB, self.controllerStopToScrubbHandler);
			self.controller_do.addListener(FWDRLEAPController.CHANGE_VOLUME, self.controllerChangeVolumeHandler);
			self.main_do.addChild(self.controller_do);
		};
		
		this.controllerOnPlayHandler = function(e){
			self.play();
		};
		
		this.controllerOnPauseHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.pause();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.pauseAudio();
			}
		};
		
		this.controllerStartToScrubbHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.startToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.pause();
				self.flashObject.startToScrub();
			}
		};
		
		this.controllerScrubbHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.scrub(e.percent);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.scrub(e.percent);
			}
		};
		
		this.controllerStopToScrubbHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.stopToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.stopToScrub();
			}
		};
		
		this.controllerChangeVolumeHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.setVolume(e.percent);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.setVolume(e.percent);
			}
		};
		
		//###########################################//
		/* setup FWDRLEAPAudioScreen */
		//###########################################//
		this.setupAudioScreen = function(id){	
			FWDRLEAPAudioScreen.setPrototype();
			self.audioScreen_do = new FWDRLEAPAudioScreen(self, self.data);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.START, self.audioScreenStartHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.ERROR, self.audioScreenErrorHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.SAFE_TO_SCRUBB, self.audioScreenSafeToScrubbHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.STOP, self.audioScreenStopHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.PLAY, self.audioScreenPlayHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.PAUSE, self.audioScreenPauseHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.UPDATE, self.audioScreenUpdateHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.UPDATE_TIME, self.audioScreenUpdateTimeHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.LOAD_PROGRESS, self.audioScreenLoadProgressHandler);
			self.audioScreen_do.addListener(FWDRLEAPAudioScreen.PLAY_COMPLETE, self.audioScreenPlayCompleteHandler);
			self.main_do.addChild(self.audioScreen_do);	
		};
		
		this.audioScreenStartHandler = function(){
			self.dispatchEvent(FWDRLEAP.START);
		};
		
		this.audioScreenErrorHandler = function(e){
			var error;
			self.hasLoadingSkinError_bl = true;
			if(FWDRLEAP.hasHTML5Audio){
				error = e.text;
				if(window.console) console.log(e);
			}else{
				error = e;
			}
			self.dispatchEvent(FWDRLEAP.ERROR, {error:error});
		};
		
		this.audioScreenSafeToScrubbHandler = function(){
			if(self.controller_do) self.controller_do.enableMainScrubber(); 
		};
		
		this.audioScreenStopHandler = function(e){
			if(self.controller_do){
				self.controller_do.disableMainScrubber();
				self.controller_do.showPlayButton();
			}
			self.dispatchEvent(FWDRLEAP.STOP);
		};
		
		this.audioScreenPlayHandler = function(){
			//console.log("play " + self.controller_do);
			if(self.controller_do) self.controller_do.showPauseButton(); 
			self.dispatchEvent(FWDRLEAP.PLAY);
		};
		
		this.audioScreenPauseHandler = function(){
			if(self.controller_do) self.controller_do.showPlayButton(); 
			self.dispatchEvent(FWDRLEAP.PAUSE);
		};
		
		this.audioScreenUpdateHandler = function(e){
			var percent;	
			if(FWDRLEAP.hasHTML5Audio){
				percent = e.percent;
				if(self.controller_do) self.controller_do.updateMainScrubber(percent);
			}else{
				percent = e;
				if(self.controller_do) self.controller_do.updateMainScrubber(percent);
			}
			self.dispatchEvent(FWDRLEAP.UPDATE, {percent:percent});
		};
		
		this.audioScreenUpdateTimeHandler = function(e){
			var time;
			if(FWDRLEAP.hasHTML5Audio){
				time = e.time;
				if(self.controller_do) self.controller_do.updateTime(time);
			}else{
				time = e;
				if(self.controller_do) self.controller_do.updateTime(time);
			}
		
			self.dispatchEvent(FWDRLEAP.UPDATE_TIME, {time:time});
		};
		
		this.audioScreenLoadProgressHandler = function(e){
			if(FWDRLEAP.hasHTML5Audio){
				if(self.controller_do) self.controller_do.updatePreloaderBar(e.percent);
			}else{
				if(self.controller_do) self.controller_do.updatePreloaderBar(e);
			}
		};
		
		this.audioScreenPlayCompleteHandler = function(){
			self.dispatchEvent(FWDRLEAP.PLAY_COMPLETE);
		};
		
		//#############################################//
		/* Flash screen... */
		//#############################################//
		this.setupFlashScreen = function(){

			if(!FWDRLFlashTest.hasFlashPlayerVersion("9.0.18")){
				var error = "Please install Adobe flash player! <a href='http://www.adobe.com/go/getflashplayer'>Click here to install.</a>";
				self.dispatchEvent(FWDRLEAP.ERROR, {error:error});
				return;
			}
			
			self.flash_do = new FWDRLDisplayObject("div");
			self.flash_do.setBackfaceVisibility();
			self.flash_do.setResizableSizeAfterParent();
		
			self.main_do.addChild(self.flash_do);
		
			self.flashObjectMarkup_str = '<object id="' + self.instanceName_str + '"classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="100%" height="100%"><param name="movie" value="' + self.data.audioFlashPath_str + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/><param name=FlashVars value="instanceName=' + self.instanceName_str + '&volume=' + self.data.volume + '&loop=' + self.data.audioLoop_bl + '"/><object type="application/x-shockwave-flash" data="' + self.data.audioFlashPath_str + '" width="100%" height="100%"><param name="movie" value="' + self.data.audioFlashPath_str + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/><param name=FlashVars value="instanceName=' + self.instanceName_str + '&volume=' + self.data.volume + '&loop=' + self.data.audioLoop_bl + '"/></object></object>';
			
			self.flash_do.screen.innerHTML = self.flashObjectMarkup_str;
			
			self.flashObject = self.flash_do.screen.firstChild;
			if(!FWDRLUtils.isIE) self.flashObject = self.flashObject.getElementsByTagName("object")[0];
		};
	
		this.flashScreenIsReady = function(){
			if(console) console.dir("flash  audio is ready " + self.instanceName_str);
			self.isFlashScreenReady_bl = true;
			self.setupController();
			self.isAPIReady_bl = true;
			self.dispatchEvent(FWDRLEAP.READY);
		};
		
		this.flashScreenFail = function(){
			var error = "External interface error!";
			self.dispatchEvent(FWDRLEAP.ERROR, {error:error});
		};
		
		//####################################//
		// API
		//###################################//
		this.play = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEAP.hasHTML5Audio){
				self.audioScreen_do.play();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.playAudio();
				
			}
		};
		
		this.pause = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.pause();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.pauseAudio();
			}
		};
		
		this.stop = function(){
			if(!self.isAPIReady_bl) return;
			self.hasLoadingSkinError_bl = false;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.stop();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.stopAudio();
			}
		};
		
		this.startToScrub = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.startToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.startToScrub();
			}
		};
		
		this.stopToScrub = function(){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.stopToScrub();
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
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.scrub(percent);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.scrub(percent);
			}
		};
	
		this.stopToScrub = function(e){
			if(!self.isAPIReady_bl) return;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.stopToScrub();
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.stopToScrub();
			}
		};
		
		this.setSource = function(source){
			if(!self.isAPIReady_bl) return;
			self.hasLoadingSkinError_bl = false;
			self.sourcePath_str = source;
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.setSource(source);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.setSource(source);
			}
		};
		
		this.getSourcePath = function(){
			if(!self.isAPIReady_bl) return;
			return self.sourcePath_str;
		};
		
		this.setVolume = function(volume){
			if(!self.isAPIReady_bl) return;
			if(self.controller_do) self.controller_do.updateVolume(volume);
			if(FWDRLEAP.hasHTML5Audio){
				if(self.audioScreen_do) self.audioScreen_do.setVolume(volume);
			}else if(self.isFlashScreenReady_bl){
				self.flashObject.setVolume(volume);
			}
		};
		
		this.getIsAPIReady = function(){
			return self.isAPIReady_bl;
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
	FWDRLEAP.setPrototype =  function(){
		FWDRLEAP.prototype = new FWDRLEventDispatcher();
	};
	
	FWDRLEAP.hasHTML5Audio = (function(){
		var soundTest_el = document.createElement("audio");
		var flag = false;
		if(soundTest_el.canPlayType){
			flag = Boolean(soundTest_el.canPlayType('audio/mpeg') == "probably" || soundTest_el.canPlayType('audio/mpeg') == "maybe");
		}
		if(self.isMobile_bl) return true;
		//return false;
		return flag;
	}());
	
	FWDRLEAP.getAudioFormats = (function(){
		var audio_el = document.createElement("audio");
		if(!audio_el.canPlayType) return;
		var extention_str = "";
		var extentions_ar = [];
		if(audio_el.canPlayType('audio/mpeg') == "probably" || audio_el.canPlayType('audio/mpeg') == "maybe"){
			extention_str += ".mp3";
		}
		
		if(audio_el.canPlayType("audio/ogg") == "probably" || audio_el.canPlayType("audio/ogg") == "maybe"){
			extention_str += ".ogg";
		}
		
		if(audio_el.canPlayType("audio/mp4") == "probably" || audio_el.canPlayType("audio/mp4") == "maybe"){
			extention_str += ".webm";
		}
		
		extentions_ar = extention_str.split(".");
		extentions_ar.shift();
		
		audio_el = null;
		return extentions_ar;
	})();
	
	FWDRLEAP.instaces_ar = [];
	
	FWDRLEAP.START = "start";
	FWDRLEAP.READY = "ready";
	FWDRLEAP.STOP	 = "stop";
	FWDRLEAP.PLAY = "play";
	FWDRLEAP.PAUSE = "pause";
	FWDRLEAP.UPDATE = "update";
	FWDRLEAP.UPDATE_TIME = "updateTime";
	FWDRLEAP.ERROR = "error";
	FWDRLEAP.PLAY_COMPLETE = "playComplete";
	
	
	window.FWDRLEAP = FWDRLEAP;
	
}(window));