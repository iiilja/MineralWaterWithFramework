/* thumbs manager */
(function(window){
	
	var FWDRLEAPAudioScreen = function(parent, data){
		
		var self = this;
		var prototype = FWDRLEAPAudioScreen.prototype;
	
		this.audio_el = null;
	
		this.sourcePath_str = data.sourcePath_str;
		this.prevSourcePath_str = "none";
		
		this.volume = data.volume;
		this.countShoutCastErrors = 0;
		this.maxCountShoutCastErrors = 5;		
		
		this.testShoutCastId_to;
		
		self.preload_bl = false;
		this.autoPlay_bl = data.autoPlay_bl;
		this.loop_bl = data.audioLoop_bl;
		this.allowScrubing_bl = false;
		this.hasError_bl = true;
		this.isPlaying_bl = false;
		this.isStopped_bl = true;
		this.hasPlayedOnce_bl = false;
		this.isSafeToBeControlled_bl = false;
		this.isShoutcast_bl = false;
		this.isStartEventDispatched_bl = false;
		
		//###############################################//
		/* init */
		//###############################################//
		this.init = function(){
			self.setHeight(0);
		};
	
		//###############################################//
		/* Setup audio element */
		//##############################################//
		this.setupAudio = function(){
			if(self.audio_el == null){
				self.audio_el = document.createElement("audio");
				self.screen.appendChild(self.audio_el);
				self.audio_el.controls = false;
				self.audio_el.preload = "auto";
				self.audio_el.volume = self.volume;
			}
			
			self.audio_el.addEventListener("error", self.errorHandler);
			self.audio_el.addEventListener("canplay", self.safeToBeControlled);
			self.audio_el.addEventListener("canplaythrough", self.safeToBeControlled);
			self.audio_el.addEventListener("progress", self.updateProgress);
			self.audio_el.addEventListener("timeupdate", self.updateAudio);
			self.audio_el.addEventListener("pause", self.pauseHandler);
			self.audio_el.addEventListener("play", self.playHandler);
			self.audio_el.addEventListener("ended", self.endedHandler);
		};
		
		this.destroyAudio = function(){
			if(self.audio_el){
				self.audio_el.removeEventListener("error", self.errorHandler);
				self.audio_el.removeEventListener("canplay", self.safeToBeControlled);
				self.audio_el.removeEventListener("canplaythrough", self.safeToBeControlled);
				self.audio_el.removeEventListener("progress", self.updateProgress);
				self.audio_el.removeEventListener("timeupdate", self.updateAudio);
				self.audio_el.removeEventListener("pause", self.pauseHandler);
				self.audio_el.removeEventListener("play", self.playHandler);
				self.audio_el.removeEventListener("ended", self.endedHandler);
				self.audio_el.src = "";
				self.audio_el.load();
			}
			//try{
			//	self.screen.removeChild(self.audio_el);
			//}catch(e){}
			//self.audio_el = null;
		};
		
		//##########################################//
		/* Video error handler. */
		//##########################################//
		this.errorHandler = function(e){
			if(self.isShoutcast_bl && self.countShoutCastErrors <= self.maxCountShoutCastErrors && self.audio_el.networkState == 0){
				self.testShoutCastId_to = setTimeout(self.play, 200);
				self.countShoutCastErrors ++;
				return;
			}
			
			var error_str;
			self.hasError_bl = true;
			self.stop();
			
			if(self.audio_el.networkState == 0){
				error_str = "error 'self.audio_el.networkState = 1'";
			}else if(self.audio_el.networkState == 1){
				error_str = "error 'self.audio_el.networkState = 1'";
			}else if(self.audio_el.networkState == 2){
				error_str = "'self.audio_el.networkState = 2'";
			}else if(self.audio_el.networkState == 3){
				error_str = "Audio source not found <font color='#FFFFFF'>" + self.sourcePath_str + "</font>";
			}else{
				error_str = e;
			}
			
			if(window.console) window.console.log(self.audio_el.networkState);
			
			self.dispatchEvent(FWDRLEAPAudioScreen.ERROR, {text:error_str });
		};
		
		//##############################################//
		/* Set path */
		//##############################################//
		this.setSource = function(sourcePath){
			self.sourcePath_str = sourcePath;
			var paths_ar = self.sourcePath_str.split(",");
			var formats_ar = FWDRLEAP.getAudioFormats;
			//console.log("PATHS " +  "[" + paths_ar + "]");
			//console.log("FORMATS " + "[" + formats_ar + "]");
			
			for(var i=0; i<paths_ar.length; i++){
				var path = paths_ar[i];
				paths_ar[i] = FWDRLUtils.trim(path);
			}
			
			loop1:for(var j=0; j<paths_ar.length; j++){
				var path = paths_ar[j];
				for(var i=0; i<formats_ar.length; i++){
					var format = formats_ar[i];
					if(path.indexOf(format) != -1){
						self.sourcePath_str = path;			
						break loop1;
					}
				}
			}
			
			clearTimeout(self.testShoutCastId_to);
			
			if(self.sourcePath_str.indexOf(";") != -1 && FWDRLUtils.isChrome){
				self.isShoutcast_bl = true;
				self.countShoutCastErrors = 0;
			}else{
				self.isShoutcast_bl = false;
			}
			
			parent.sourcePath_str = self.sourcePath_str;
			if(self.audio_el) self.stop(true);
		};
	
		//##########################################//
		/* Play / pause / stop methods */
		//##########################################//
		this.play = function(overwrite){
			if(self.isStopped_bl){
				self.isPlaying_bl = false;
				self.hasError_bl = false;
				self.allowScrubing_bl = false;
				self.isStopped_bl = false;
				//if(self.audio_el == null)	
				self.setupAudio();
				self.audio_el.src = self.sourcePath_str;
				//self.audio_el.load();
				self.play();
			}else if(!self.audio_el.ended || overwrite){
				try{
					self.isPlaying_bl = true;
					self.hasPlayedOnce_bl = true;
					self.audio_el.play();
					
					if(FWDRLUtils.isIE) self.dispatchEvent(FWDRLEAPAudioScreen.PLAY);
				}catch(e){};
			}
		};

		this.pause = function(){
			if(self == null) return;
			if(self.audio_el == null) return;
			if(!self.audio_el.ended){
				try{
					self.audio_el.pause();
					self.isPlaying_bl = false;
					if(FWDRLUtils.isIE) self.dispatchEvent(FWDRLEAPAudioScreen.PAUSE);
				}catch(e){};
				
			}
		};
		
		this.pauseHandler = function(){
			if(self.allowScrubing_bl) return;
			self.dispatchEvent(FWDRLEAPAudioScreen.PAUSE);
		};
		
		this.playHandler = function(){
			if(self.allowScrubing_bl) return;
			if(!self.isStartEventDispatched_bl){
				self.dispatchEvent(FWDRLEAPAudioScreen.START);
				self.isStartEventDispatched_bl = true;
			}
			self.dispatchEvent(FWDRLEAPAudioScreen.PLAY);
		};
		
		this.endedHandler = function(){
			if(self.loop_bl){
				self.scrub(0);
				self.play();
			}else{
				self.stop();
			}
			self.dispatchEvent(FWDRLEAPAudioScreen.PLAY_COMPLETE);
		};
		
		this.stop = function(overwrite){
			if((self == null || self.audio_el == null || self.isStopped_bl) && !overwrite) return;
			self.isPlaying_bl = false;
			self.isStopped_bl = true;
			self.hasPlayedOnce_bl = true;
			self.isSafeToBeControlled_bl = false;
			self.isStartEventDispatched_bl = false;
			clearTimeout(self.testShoutCastId_to);
			self.audio_el.pause();
			self.destroyAudio();
			self.dispatchEvent(FWDRLEAPAudioScreen.STOP);
			self.dispatchEvent(FWDRLEAPAudioScreen.UPDATE_TIME, {time:"00:00/00:00"});
			self.dispatchEvent(FWDRLEAPAudioScreen.LOAD_PROGRESS, {percent:0});
		};

		//###########################################//
		/* Check if audio is safe to be controlled */
		//###########################################//
		this.safeToBeControlled = function(){
			if(!self.isSafeToBeControlled_bl){
				self.isPlaying_bl = true;
				self.isSafeToBeControlled_bl = true;
				self.dispatchEvent(FWDRLEAPAudioScreen.SAFE_TO_SCRUBB);
				self.dispatchEvent(FWDRLEAPAudioScreen.SAFE_TO_UPDATE_VOLUME);
			}
		};
	
		//###########################################//
		/* Update progress */
		//##########################################//
		this.updateProgress = function(){
			var buffered;
			var percentLoaded = 0;
			
			if(self.audio_el.buffered.length > 0){
				buffered = self.audio_el.buffered.end(self.audio_el.buffered.length - 1);
				percentLoaded = buffered.toFixed(1)/self.audio_el.duration.toFixed(1);
				if(isNaN(percentLoaded) || !percentLoaded) percentLoaded = 0;
			}
			
			if(percentLoaded == 1) self.audio_el.removeEventListener("progress", self.updateProgress);
			
			self.dispatchEvent(FWDRLEAPAudioScreen.LOAD_PROGRESS, {percent:percentLoaded});
		};
		
		//##############################################//
		/* Update audio */
		//#############################################//
	
		this.updateAudio = function(){
			var percentPlayed; 
			if (!self.allowScrubing_bl) {
				percentPlayed = self.audio_el.currentTime /self.audio_el.duration;
				self.dispatchEvent(FWDRLEAPAudioScreen.UPDATE, {percent:percentPlayed});
			}
			self.dispatchEvent(FWDRLEAPAudioScreen.UPDATE_TIME, {time:self.formatTime(self.audio_el.currentTime) + "/" + self.formatTime(self.audio_el.duration)});
		};
		
		
		this.formatTime = function(seconds){
			seconds = Math.round(seconds);
			minutes = Math.floor(seconds / 60);
			minutes = (minutes >= 10) ? minutes : "0" + minutes;
			seconds = Math.floor(seconds % 60);
			seconds = (seconds >= 10) ? seconds : "0" + seconds;
			if(isNaN(seconds)) return "00:00";
			return minutes + ":" + seconds;
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
			if(self.audio_el == null || !self.audio_el.duration) return;
			if(e) self.startToScrub();
			try{
				self.audio_el.currentTime = self.audio_el.duration * percent;
			//if(self.audio_el.paused && !self.audio_el.ended) self.play();
				self.dispatchEvent(FWDRLEAPAudioScreen.UPDATE_TIME, {time:self.formatTime(self.audio_el.currentTime) + "/" + self.formatTime(self.audio_el.duration)});
			}catch(e){}
		};
		
		//###############################################//
		/* Volume */
		//###############################################//
		this.setVolume = function(vol){
			if(vol) self.volume = vol;
			if(self.audio_el) self.audio_el.volume = self.volume;
		};
		
		//###############################################//
		/* destroy */
		//###############################################//
		this.destroy = function(){
				
			if(self.audio_el) self.audio_el.pause();
			self.destroyAudio();
			self.audio_el = null;
		
			parent = null;
			
			self.setInnerHTML("");
			self = null;
			prototype.destroy();
			prototype = null;
			FWDRLEAPAudioScreen.prototype = null;
		};
		
		this.init();
	};

	/* set prototype */
	FWDRLEAPAudioScreen.setPrototype = function(){
		FWDRLEAPAudioScreen.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLEAPAudioScreen.ERROR = "error";
	FWDRLEAPAudioScreen.UPDATE = "update";
	FWDRLEAPAudioScreen.UPDATE_TIME = "updateTime";
	FWDRLEAPAudioScreen.SAFE_TO_SCRUBB = "safeToControll";
	FWDRLEAPAudioScreen.SAFE_TO_UPDATE_VOLUME = "safeToUpdateVolume";
	FWDRLEAPAudioScreen.LOAD_PROGRESS = "loadProgress";
	FWDRLEAPAudioScreen.START = "start";
	FWDRLEAPAudioScreen.PLAY = "play";
	FWDRLEAPAudioScreen.PAUSE = "pause";
	FWDRLEAPAudioScreen.STOP = "stop";
	FWDRLEAPAudioScreen.PLAY_COMPLETE = "playComplete";



	window.FWDRLEAPAudioScreen = FWDRLEAPAudioScreen;

}(window));