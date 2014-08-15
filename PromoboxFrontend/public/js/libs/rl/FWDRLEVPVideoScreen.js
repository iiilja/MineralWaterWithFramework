/* thumbs manager */
(function(window){
	
	var FWDRLEVPVideoScreen = function(parent, backgroundColor_str, volume){
		
		var self = this;
		var prototype = FWDRLEVPVideoScreen.prototype;
	
		this.video_el = null;
	
		this.sourcePath_str = null;
		
		this.backgroundColor_str = backgroundColor_str;
		
		this.controllerHeight = parent.data.controllerHeight;
		this.stageWidth = 0;
		this.stageHeight = 0;
		this.lastPercentPlayed = 0;
		this.volume = volume;
		this.curDuration = 0;
		this.countNormalMp3Errors = 0;
		this.countShoutCastErrors = 0;
		this.maxShoutCastCountErrors = 5;
		this.maxNormalCountErrors = 1;
		
		this.disableClickForAWhileId_to;
		
		this.disableClick_bl = false;
		this.allowScrubing_bl = false;
		this.hasError_bl = true;
		this.isPlaying_bl = false;
		this.isStopped_bl = true;
		this.hasPlayedOnce_bl = false;
		this.isStartEventDispatched_bl = false;
		this.isSafeToBeControlled_bl = false;
		this.isMobile_bl = FWDRLUtils.isMobile;
		
		//###############################################//
		/* init */
		//###############################################//
		this.init = function(){
			self.setupVideo();
			self.setBkColor(self.backgroundColor_str);
		};
	
		//###############################################//
		/* Setup audio element */
		//##############################################//
		this.setupVideo = function(){
			if(self.video_el == null){
				self.video_el = document.createElement("video");
				self.screen.appendChild(self.video_el);
				self.video_el.controls = false;
				self.video_el.volume = self.volume;
				self.video_el.style.position = "relative";
				self.video_el.style.left = "0px";
				self.video_el.style.top = "0px";
				self.video_el.style.width = "100%";
				self.video_el.style.height = "100%";
				self.video_el.style.margin = "0px";
				self.video_el.style.padding = "0px";
				self.video_el.style.maxWidth = "none";
				self.video_el.style.maxHeight = "none";
				self.video_el.style.border = "none";
				self.video_el.style.lineHeight = "0";
				self.video_el.style.msTouchAction = "none";
				self.screen.appendChild(self.video_el);
			}
			
			self.video_el.addEventListener("error", self.errorHandler);
			self.video_el.addEventListener("canplay", self.safeToBeControlled);
			self.video_el.addEventListener("canplaythrough", self.safeToBeControlled);
			self.video_el.addEventListener("progress", self.updateProgress);
			self.video_el.addEventListener("timeupdate", self.updateVideo);
			self.video_el.addEventListener("pause", self.pauseHandler);
			self.video_el.addEventListener("play", self.playHandler);
			if(!FWDRLUtils.isIE){
				self.video_el.addEventListener("waiting", self.startToBuffer);
			}
			self.video_el.addEventListener("playing", self.stopToBuffer);
			self.video_el.addEventListener("ended", self.endedHandler);
			self.resizeAndPosition();
		};	
		
		
		this.destroyVideo = function(){
			if(self.video_el){
				self.video_el.removeEventListener("error", self.errorHandler);
				self.video_el.removeEventListener("canplay", self.safeToBeControlled);
				self.video_el.removeEventListener("canplaythrough", self.safeToBeControlled);
				self.video_el.removeEventListener("progress", self.updateProgress);
				self.video_el.removeEventListener("timeupdate", self.updateVideo);
				self.video_el.removeEventListener("pause", self.pauseHandler);
				self.video_el.removeEventListener("play", self.playHandler);
				if(!FWDRLUtils.isIE){
					self.video_el.removeEventListener("waiting", self.startToBuffer);
				}
				self.video_el.removeEventListener("playing", self.stopToBuffer);
				self.video_el.removeEventListener("ended", self.endedHandler);
				if(self.isMobile_bl){	
					self.screen.removeChild(self.video_el);
					self.video_el = null;
				}else{
					self.video_el.style.visibility = "hidden";
					self.video_el.src = "";
					self.video_el.load();
				}
			}
		};
		
		this.startToBuffer = function(overwrite){
			self.dispatchEvent(FWDRLEVPVideoScreen.START_TO_BUFFER);
		};
		
		this.stopToBuffer = function(){
			self.dispatchEvent(FWDRLEVPVideoScreen.STOP_TO_BUFFER);
		};
		
		//##########################################//
		/* Video error handler. */
		//##########################################//
		this.errorHandler = function(e){
			
			var error_str;
			self.hasError_bl = true;
			
			if(self.video_el.networkState == 0){
				error_str = "error 'self.video_el.networkState = 0'";
			}else if(self.video_el.networkState == 1){
				error_str = "error 'self.video_el.networkState = 1'";
			}else if(self.video_el.networkState == 2){
				error_str = "'self.video_el.networkState = 2'";
			}else if(self.video_el.networkState == 3){
				error_str = "Video source not found <font color='#FFFFFF'>" + self.sourcePath_str + "</font>";
			}else{
				error_str = e;
			}
			
			if(window.console) window.console.log(self.video_el.networkState);
			self.dispatchEvent(FWDRLEVPVideoScreen.ERROR, {text:error_str });
		};
		
		//##############################################//
		/* Resize and position */
		//##############################################//
		this.resizeAndPosition = function(width, height){
			if(width){
				self.stageWidth = width;
				self.stageHeight = height;
			}
			
			self.setWidth(self.stageWidth);
			if(FWDRLUtils.isIphone){	
				self.setHeight(self.stageHeight - self.controllerHeight);
			}else{
				self.setHeight(self.stageHeight);
			}
		};
		
		//##############################################//
		/* Set path */
		//##############################################//
		this.setSource = function(sourcePath){
			self.sourcePath_str = sourcePath;
			if(self.video_el) self.stop();
		};
	
		//##########################################//
		/* Play / pause / stop methods */
		//##########################################//
		this.play = function(overwrite){
			FWDRLEVPlayer.curInstance = parent;
			if(self.isStopped_bl){
				self.isPlaying_bl = false;
				self.hasError_bl = false;
				self.allowScrubing_bl = false;
				self.isStopped_bl = false;
				self.setupVideo();
				self.setVolume();
				self.video_el.src = self.sourcePath_str;
				self.play();
				self.startToBuffer(true);
				self.isPlaying_bl = true;
			}else if(!self.video_el.ended || overwrite){
				try{
					self.isPlaying_bl = true;
					self.hasPlayedOnce_bl = true;
					self.video_el.play();
					if(FWDRLUtils.isIE) self.dispatchEvent(FWDRLEVPVideoScreen.PLAY);
				}catch(e){};
			}
		};

		this.pause = function(){
			if(self == null || self.isStopped_bl || self.hasError_bl) return;
			if(!self.video_el.ended){
				try{
					self.video_el.pause();
					self.isPlaying_bl = false;
					if(FWDRLUtils.isIE) self.dispatchEvent(FWDRLEVPVideoScreen.PAUSE);
				}catch(e){};
			}
		};
		
		this.togglePlayPause = function(){
			if(self == null) return;
			if(!self.isSafeToBeControlled_bl) return;
			if(self.isPlaying_bl){
				self.pause();
			}else{
				self.play();
			}
		};
		
		this.pauseHandler = function(){
			if(self.allowScrubing_bl) return;
			self.dispatchEvent(FWDRLEVPVideoScreen.PAUSE);
		};
		
		this.playHandler = function(){
			if(self.allowScrubing_bl) return;
			if(!self.isStartEventDispatched_bl){
				self.dispatchEvent(FWDRLEVPVideoScreen.START);
				self.isStartEventDispatched_bl = true;
			}
			self.dispatchEvent(FWDRLEVPVideoScreen.PLAY);
		};
		
		this.endedHandler = function(){
			self.dispatchEvent(FWDRLEVPVideoScreen.PLAY_COMPLETE);
		};
		
		this.resume = function(){
			if(self.isStopped_bl) return;
			self.play();
		};
		
		this.stop = function(overwrite){
			if((self == null || self.video_el == null || self.isStopped_bl) && !overwrite) return;
			//logger.log("# VID stop #" + parent.instanceName_str);
			self.isPlaying_bl = false;
			self.isStopped_bl = true;
			self.hasPlayedOnce_bl = true;
			self.isSafeToBeControlled_bl = false;
			self.isStartEventDispatched_bl = false;
			self.destroyVideo();
			self.dispatchEvent(FWDRLEVPVideoScreen.LOAD_PROGRESS, {percent:0});
			self.dispatchEvent(FWDRLEVPVideoScreen.UPDATE_TIME, {curTime:"00:00" , totalTime:"00:00"});
			self.dispatchEvent(FWDRLEVPVideoScreen.STOP);
			self.stopToBuffer();
		};

		//###########################################//
		/* Check if audio is safe to be controlled */
		//###########################################//
		this.safeToBeControlled = function(){
			self.stopToScrub();
			if(!self.isSafeToBeControlled_bl){
				self.hasHours_bl = Math.floor(self.video_el.duration / (60 * 60)) > 0;
				self.isPlaying_bl = true;
				self.isSafeToBeControlled_bl = true;
				self.video_el.style.visibility = "visible";
				self.dispatchEvent(FWDRLEVPVideoScreen.SAFE_TO_SCRUBB);
			}
		};
	
		//###########################################//
		/* Update progress */
		//##########################################//
		this.updateProgress = function(){
			var buffered;
			var percentLoaded = 0;
			
			if(self.video_el.buffered.length > 0){
				buffered = self.video_el.buffered.end(self.video_el.buffered.length - 1);
				percentLoaded = buffered.toFixed(1)/self.video_el.duration.toFixed(1);
				if(isNaN(percentLoaded) || !percentLoaded) percentLoaded = 0;
			}
			
			if(percentLoaded == 1) self.video_el.removeEventListener("progress", self.updateProgress);
			
			self.dispatchEvent(FWDRLEVPVideoScreen.LOAD_PROGRESS, {percent:percentLoaded});
		};
		
		//##############################################//
		/* Update audio */
		//#############################################//
		this.updateVideo = function(){
			var percentPlayed; 
			if (!self.allowScrubing_bl) {
				percentPlayed = self.video_el.currentTime /self.video_el.duration;
				self.dispatchEvent(FWDRLEVPVideoScreen.UPDATE, {percent:percentPlayed});
			}
			
			var totalTime = self.formatTime(self.video_el.duration);
			var curTime = self.formatTime(self.video_el.currentTime);
			
			
			if(!isNaN(self.video_el.duration)){
				self.dispatchEvent(FWDRLEVPVideoScreen.UPDATE_TIME, {curTime: curTime, totalTime:totalTime});
			}else{
				self.dispatchEvent(FWDRLEVPVideoScreen.UPDATE_TIME, {curTime:"00:00" , totalTime:"00:00"});
			}
			
			self.lastPercentPlayed = percentPlayed;
			self.curDuration = curTime;
		};
		
		//###############################################//
		/* Scrub */
		//###############################################//
		this.startToScrub = function(){
			self.allowScrubing_bl = true;
		};
		
		this.stopToScrub = function(){
			self.allowScrubing_bl = false;
		};
		
		this.scrub = function(percent, e){
			//if(!self.allowScrubing_bl) return;
			if(e) self.startToScrub();
			try{
				self.video_el.currentTime = self.video_el.duration * percent;
				var totalTime = self.formatTime(self.video_el.duration);
				var curTime = self.formatTime(self.video_el.currentTime);
				self.dispatchEvent(FWDRLEVPVideoScreen.UPDATE_TIME, {curTime: curTime, totalTime:totalTime});
			}catch(e){}
		};
		
		//###############################################//
		/* replay */
		//###############################################//
		this.replay = function(){
			self.scrub(0);
			self.play();
		};
		
		//###############################################//
		/* Volume */
		//###############################################//
		this.setVolume = function(vol){
			if(vol) self.volume = vol;
			if(self.video_el) self.video_el.volume = self.volume;
		};
		
		this.formatTime = function(secs){
			var hours = Math.floor(secs / (60 * 60));
			
		    var divisor_for_minutes = secs % (60 * 60);
		    var minutes = Math.floor(divisor_for_minutes / 60);

		    var divisor_for_seconds = divisor_for_minutes % 60;
		    var seconds = Math.ceil(divisor_for_seconds);
		    
		    minutes = (minutes >= 10) ? minutes : "0" + minutes;
		    seconds = (seconds >= 10) ? seconds : "0" + seconds;
		    
		    if(isNaN(seconds)) return "00:00";
			if(self.hasHours_bl){
				 return hours + ":" + minutes + ":" + seconds;
			}else{
				 return minutes + ":" + seconds;
			}
		};

	
		this.init();
	};

	/* set prototype */
	FWDRLEVPVideoScreen.setPrototype = function(){
		FWDRLEVPVideoScreen.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLEVPVideoScreen.ERROR = "error";
	FWDRLEVPVideoScreen.UPDATE = "update";
	FWDRLEVPVideoScreen.UPDATE_TIME = "updateTime";
	FWDRLEVPVideoScreen.SAFE_TO_SCRUBB = "safeToControll";
	FWDRLEVPVideoScreen.LOAD_PROGRESS = "loadProgress";
	FWDRLEVPVideoScreen.START = "start";
	FWDRLEVPVideoScreen.PLAY = "play";
	FWDRLEVPVideoScreen.PAUSE = "pause";
	FWDRLEVPVideoScreen.STOP = "stop";
	FWDRLEVPVideoScreen.PLAY_COMPLETE = "playComplete";
	FWDRLEVPVideoScreen.START_TO_BUFFER = "startToBuffer";
	FWDRLEVPVideoScreen.STOP_TO_BUFFER = "stopToBuffer";


	window.FWDRLEVPVideoScreen = FWDRLEVPVideoScreen;

}(window));