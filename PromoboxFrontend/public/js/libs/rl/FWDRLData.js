/* Data */
(function(window){
	
	var FWDRLData = function(props, playListElement, parent){
		
		var self = this;
		var prototype = FWDRLData.prototype;
		
		this.xhr = null;
		this.emailXHR = null;
		this.playlist_ar = null;
	
		this.props_obj = props;
		this.skinPaths_ar = [];
		this.images_ar = [];
		this.cats_ar = [];
	
		this.lightboxSkinPath_str = null;
		this.facebookAppId_str = null;
	
		this.countLoadedSkinImages = 0;
		this.showLoadPlaylistErrorId_to;
		this.loadPreloaderId_to;

		this.allowToChangeVolume_bl = true;
		this.autoPlay_bl = false;
		this.showFacebookButton_bl = false;
		this.isDataLoaded_bl = false;
		this.useDeepLinking_bl = false;
		this.isMobile_bl = FWDRLUtils.isMobile;
		this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
	
		//###################################//
		/*init*/
		//###################################//
		self.init = function(){
			self.parseProperties();
		};
		
		//#############################################//
		// parse properties.
		//#############################################//
		self.parseProperties = function(){
			
			self.mainFolderPath_str = self.props_obj.mainFolderPath;
			if(!self.mainFolderPath_str){
				setTimeout(function(){
					if(self == null) return;
					errorMessage_str = "The <font color='#FFFFFF'>mainFolderPath</font> property is not defined in the constructor function!";
					self.dispatchEvent(FWDRLData.LOAD_ERROR, {text:errorMessage_str});
				}, 50);
				return;
			}
			
			if((self.mainFolderPath_str.lastIndexOf("/") + 1) != self.mainFolderPath_str.length){
				self.mainFolderPath_str += "/";
			}
			
			self.lightboxSkinPath_str = self.props_obj.skinPath;
			if(!self.lightboxSkinPath_str){
				setTimeout(function(){
					if(self == null) return;
					errorMessage_str = "The <font color='#FFFFFF'>skinPath</font> property is not defined in the constructor function!";
					self.dispatchEvent(FWDRLData.LOAD_ERROR, {text:errorMessage_str});
				}, 50);
				return;
			}
			
		
			if((self.lightboxSkinPath_str.lastIndexOf("/") + 1) != self.lightboxSkinPath_str.length){
				self.lightboxSkinPath_str += "/";
			}
			
			self.flashPath_str = self.mainFolderPath_str + "video_player.swf";
			self.audioFlashPath_str = self.mainFolderPath_str + "audio_player.swf";
			self.lightboxSkinPath_str = self.mainFolderPath_str + self.lightboxSkinPath_str;
			self.videoSkinPath_str = self.lightboxSkinPath_str + "video_player_skin/";
			self.audioSkinPath_str = self.lightboxSkinPath_str + "audio_player_skin/";
			
			self.rightClickContextMenu_str = self.props_obj.rightClickContextMenu || "developer";
			test = self.rightClickContextMenu_str == "developer" 
				   || self.rightClickContextMenu_str == "disabled"
				   || self.rightClickContextMenu_str == "default";
			if(!test) self.rightClickContextMenu_str = "developer";
			
			
			self.autoPlay_bl = self.props_obj.autoPlay; 
			self.autoPlay_bl = self.autoPlay_bl == "yes" ? true : false;
			self.useVideo_bl = self.props_obj.useVideo == "no" ? false : true;
			self.DFUseVideo_bl = self.useVideo_bl;
			if(!FWDRLEVPlayer.hasHTML5Video && FWDRLUtils.isLocal) self.useVideo_bl = false;
			self.useAudio_bl = self.props_obj.useAudio == "no" ? false : true;
			self.DFUseAudio_bl = self.useAudio_bl;
			if(!FWDRLEAP.hasHTML5Audio && FWDRLUtils.isLocal) self.useAudio_bl = false;
			
			
			//video settings
			self.timeColor_str = self.props_obj.timeColor || "#FF0000";
			self.videoPosterBackgroundColor_str = self.props_obj.videoPosterBackgroundColor || "transparent";
			self.videoControllerBackgroundColor_str = self.props_obj.videoControllerBackgroundColor || "transparent";
			self.audioControllerBackgroundColor_str = self.props_obj.audioControllerBackgroundColor || "transparent";
		
			self.volume = 1;
			self.controllerHeight = self.props_obj.videoControllerHeight || 50;
			self.startSpaceBetweenButtons = self.props_obj.startSpaceBetweenButtons || 0;
			self.controllerHideDelay = self.props_obj.videoControllerHideDelay || 2;
			self.controllerHideDelay *= 1000;
			self.vdSpaceBetweenButtons = self.props_obj.vdSpaceBetweenButtons || 0;
			self.scrubbersOffsetWidth = self.props_obj.scrubbersOffsetWidth || 0;
			self.volumeScrubberOffsetRightWidth = self.props_obj.volumeScrubberOffsetRightWidth || 0;
			self.timeOffsetLeftWidth = self.props_obj.timeOffsetLeftWidth || 0;
			self.timeOffsetRightWidth = self.props_obj.timeOffsetRightWidth || 0;
			self.timeOffsetTop = self.props_obj.timeOffsetTop || 0;
			self.logoMargins = self.props_obj.logoMargins || 0;
			self.mainScrubberOffestTop = self.props_obj.mainScrubberOffestTop || 0;
			self.volumeScrubberWidth = self.props_obj.volumeScrubberWidth || 10;
			self.audioScrubbersOffestTotalWidth = self.props_obj.audioScrubbersOffestTotalWidth || 0;
			self.audioControllerHeight =  self.props_obj.audioControllerHeight || 40;
			if(self.volumeScrubberWidth > 200) self.volumeScrubberWidth = 200;

			if(self.isMobile_bl) self.allowToChangeVolume_bl = false;
			
			
			self.addKeyboardSupport_bl = self.props_obj.addVideoKeyboardSupport; 
			self.addKeyboardSupport_bl = self.addKeyboardSupport_bl == "no" ? false : true;
			
			self.videoAutoPlay_bl = self.props_obj.videoAutoPlay; 
			self.videoAutoPlay_bl = self.videoAutoPlay_bl == "yes" ? true : false;
			if(FWDRLUtils.isMobile) self.videoAutoPlay_bl = false;
			
			self.audioAutoPlay_bl = self.props_obj.audioAutoPlay; 
			self.audioAutoPlay_bl = self.audioAutoPlay_bl == "yes" ? true : false;
			if(FWDRLUtils.isMobile) self.audioAutoPlay_bl = false;
			
			self.videoLoop_bl = self.props_obj.videoLoop; 
			self.videoLoop_bl = self.videoLoop_bl == "yes" ? true : false;
			
			self.audioLoop_bl = self.props_obj.audioLoop; 
			self.audioLoop_bl = self.audioLoop_bl == "yes" ? true : false;
			
			self.showLogo_bl = self.props_obj.showLogo; 
			self.showLogo_bl = self.showLogo_bl == "yes" ? true : false;
			
			self.hideLogoWithController_bl = self.props_obj.hideLogoWithController; 
			self.hideLogoWithController_bl = self.hideLogoWithController_bl == "yes" ? true : false;
			
			self.showPoster_bl = self.props_obj.showPoster; 
			self.showPoster_bl = self.showPoster_bl == "yes" ? true : false;
			
			self.showVolumeScrubber_bl = self.props_obj.showVolumeScrubber; 
			self.showVolumeScrubber_bl = self.showVolumeScrubber_bl == "no" ? false : true;
			
			self.showVolumeButton_bl = self.props_obj.showVolumeButton; 
			self.showVolumeButton_bl = self.showVolumeButton_bl == "no" ? false : true;
			
			self.showControllerWhenVideoIsStopped_bl = true; 
			
			self.showTime_bl = self.props_obj.showTime; 
			self.showTime_bl = self.showTime_bl == "no" ? false : true;
			
			self.videoShowFullScreenButton_bl = self.props_obj.videoShowFullScreenButton; 
			self.videoShowFullScreenButton_bl = self.videoShowFullScreenButton_bl == "no" ? false : true;
				
			
			//load lightbox skin
			self.mainPreloader_img = new Image();
			self.mainPreloader_img.onerror = self.onSkinLoadErrorHandler;
			self.mainPreloader_img.onload = self.onPreloaderLoadHandler;
			self.mainPreloader_img.src = self.lightboxSkinPath_str + "linghtbox_skin/preloader.png";
			
			self.skinPaths_ar = [
			     {img:self.playN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/play-button.png"},
			     {img:self.nextN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/next-button.png"},
			     {img:self.prevN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/prev-button.png"},
			     {img:self.closeN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/close-button.png"},
			     {img:self.infoOpenN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/info-open-button.png"},
			     {img:self.infoCloseN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/info-close-button.png"},
			     {img:self.maximizeN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/maximize-button.png"},
			     {img:self.minimizeN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/minimize-button.png"},
			     {img:self.playN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/play-button.png"},
			     {img:self.pauseN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/pause-button.png"},
			     {img:self.hideThumbnailsN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/hide-thumbnails-button.png"},
			     {img:self.showThumbnailsN_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/show-thumbnails-button.png"},
			     {img:self.slideSwowImage_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/slideshow-preloader.png"},
			     {img:self.facebookImage_img = new Image(), src:self.lightboxSkinPath_str + "linghtbox_skin/facebook-button.png"}
    		];

			//setup skin paths
			self.prevSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/prev-button-over.png"; 	
			self.nextSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/next-button-over.png"; 
			self.closeSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/close-button-over.png"; 
			self.infoOpenS_str = self.lightboxSkinPath_str + "linghtbox_skin/info-open-button-over.png"; 	
			self.infoCloseS_str = self.lightboxSkinPath_str + "linghtbox_skin/info-close-button-over.png"; 	
			self.maximizeSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/maximize-button-over.png"; 	
			self.minimizeSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/minimize-button-over.png"; 	
			self.playS_str = self.lightboxSkinPath_str + "linghtbox_skin/play-button-over.png"; 	
			self.pauseS_str = self.lightboxSkinPath_str + "linghtbox_skin/pause-button-over.png";
			self.hideThumbnailsSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/hide-thumbnails-button-over.png";
			self.showThumbnailsSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/show-thumbnails-button-over.png";
			self.facebookImageSPath_str = self.lightboxSkinPath_str + "linghtbox_skin/facebook-button-over.png";
			
			self.imageIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/image-icon.png"; 
			self.flashIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/flash-icon.png"; 
			self.audioIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/audio-icon.png"; 
			self.videoIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/video-icon.png"; 
			self.vimeoIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/vimeo-icon.png"; 
			self.youtubeIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/youtube-icon.png"; 
			self.mapsIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/maps-icon.png"; 
			self.ajaxIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/ajax-icon.png"; 
			self.htmlIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/html-icon.png"; 
			self.iframeIconPath_str = self.lightboxSkinPath_str + "linghtbox_skin/iframe-icon.png"; 
			
			
			if(self.useVideo_bl){
				self.skinPaths_ar.push(
					{img:self.videoMainPreloader_img = new Image(), src:self.videoSkinPath_str + "preloader.png"},
				    {img:self.videoPlayN_img = new Image(), src:self.videoSkinPath_str + "play-button.png"},
				    {img:self.videoPauseN_img = new Image(), src:self.videoSkinPath_str + "pause-button.png"},
				    {img:self.videoMainScrubberBkLeft_img = new Image(), src:self.videoSkinPath_str + "scrubber-left-background.png"},
				    {img:self.videoMainScrubberDragLeft_img = new Image(), src:self.videoSkinPath_str + "scrubber-left-drag.png"},
				    {img:self.videoMainScrubberLine_img = new Image(), src:self.videoSkinPath_str + "scrubber-line.png"},
					{img:self.videoVolumeN_img = new Image(), src:self.videoSkinPath_str + "volume-button.png"},
					{img:self.videoProgressLeft_img = new Image(), src:self.videoSkinPath_str + "progress-left.png"},
				    {img:self.videoLargePlayN_img = new Image(), src:self.videoSkinPath_str + "large-play-button.png"},
				    {img:self.videoFullScreenN_img = new Image(), src:self.videoSkinPath_str + "full-screen-button.png"},
					{img:self.videoNormalScreenN_img = new Image(), src:self.videoSkinPath_str + "normal-screen-button.png"}
				);
				self.videoPlaySPath_str = self.videoSkinPath_str + "play-button-over.png"; 
				self.videoPauseSPath_str = self.videoSkinPath_str + "pause-button-over.png";
				self.videoBkMiddlePath_str = self.videoSkinPath_str + "controller-middle.png";
				
				self.videoMainScrubberBkRightPath_str = self.videoSkinPath_str + "scrubber-right-background.png";
				self.videoMainScrubberBkMiddlePath_str = self.videoSkinPath_str + "scrubber-middle-background.png";
				self.videoMainScrubberDragMiddlePath_str = self.videoSkinPath_str + "scrubber-middle-drag.png";
				
				self.videoVolumeScrubberBkRightPath_str = self.videoSkinPath_str + "scrubber-right-background.png";
				self.videoVolumeScrubberBkMiddlePath_str = self.videoSkinPath_str + "scrubber-middle-background.png";
				self.videoVolumeScrubberDragMiddlePath_str = self.videoSkinPath_str + "scrubber-middle-drag.png";	
				
				self.videoVolumeSPath_str = self.videoSkinPath_str + "volume-button-over.png";
				self.videoVolumeDPath_str = self.videoSkinPath_str + "volume-button-disabled.png";
				self.videoLargePlayS_str = self.videoSkinPath_str + "large-play-button-over.png";
				self.videoFullScreenSPath_str = self.videoSkinPath_str + "full-screen-button-over.png";
				self.videoNormalScreenSPath_str = self.videoSkinPath_str + "normal-screen-button-over.png";
				self.videoProgressMiddlePath_str = self.videoSkinPath_str + "progress-middle.png";
				
			}
			
			if(self.useAudio_bl){
				self.skinPaths_ar.push(
					{img:self.audioPlayN_img = new Image(), src:self.audioSkinPath_str + "play-button.png"},
					{img:self.audioPauseN_img = new Image(), src:self.audioSkinPath_str + "pause-button.png"},
					{img:self.audioMainScrubberBkLeft_img = new Image(), src:self.audioSkinPath_str + "scrubber-left-background.png"},
					{img:self.mainScrubberBkRight_img = new Image(), src:self.audioSkinPath_str + "scrubber-right-background.png"},    
					{img:self.mainScrubberDragLeft_img = new Image(), src:self.audioSkinPath_str + "scrubber-left-drag.png"},
					{img:self.mainScrubberLine_img = new Image(), src:self.audioSkinPath_str + "scrubber-line.png"},
					{img:self.volumeN_img = new Image(), src:self.audioSkinPath_str + "volume-button.png"},
					{img:self.progressLeft_img = new Image(), src:self.audioSkinPath_str + "progress-left.png"}
				);
				
				self.audioPlaySPath_str = self.audioSkinPath_str + "play-button-over.png"; 
				self.audioPauseSPath_str = self.audioSkinPath_str + "pause-button-over.png";
		
				var mainScrubberBkLeftPath_str = self.audioSkinPath_str + "scrubber-left-background.png"; 
				self.mainScrubberBkRightPath_str = self.audioSkinPath_str + "scrubber-right-background.png";
				self.mainScrubberBkMiddlePath_str = self.audioSkinPath_str + "scrubber-middle-background.png";
				self.mainScrubberDragMiddlePath_str = self.audioSkinPath_str + "scrubber-middle-drag.png";
			
				self.volumeScrubberBkLeftPath_str = self.audioSkinPath_str + "scrubber-left-background.png"; 
				self.volumeScrubberBkRightPath_str = self.audioSkinPath_str + "scrubber-right-background.png";
				self.volumeScrubberDragLeftPath_str = self.audioSkinPath_str + "scrubber-left-drag.png";
				self.volumeScrubberLinePath_str = self.audioSkinPath_str + "scrubber-line.png";
				self.volumeScrubberBkMiddlePath_str = self.audioSkinPath_str + "scrubber-middle-background.png";
				self.volumeScrubberDragMiddlePath_str = self.audioSkinPath_str + "scrubber-middle-drag.png";	
			
				self.volumeSPath_str = self.audioSkinPath_str + "volume-button-over.png";
				self.volumeDPath_str = self.audioSkinPath_str + "volume-button-disabled.png";
				self.progressMiddlePath_str = self.audioSkinPath_str + "progress-middle.png";
			}
			
			
		
			self.totalGraphics = self.skinPaths_ar.length;
			self.loadSkin();
		};
		
		//####################################//
		/* Preloader load done! */
		//###################################//
		this.onPreloaderLoadHandler = function(){
			setTimeout(function(){
				self.dispatchEvent(FWDRLData.PRELOADER_LOAD_DONE);
			}, 50);
		};
		
		//####################################//
		/* load buttons graphics */
		//###################################//
		self.loadSkin = function(){
			var img;
			var src;
			for(var i=0; i<self.totalGraphics; i++){
				img = self.skinPaths_ar[i].img;
				src = self.skinPaths_ar[i].src;
				img.onload = self.onSkinLoadHandler;
				img.onerror = self.onSkinLoadErrorHandler;
				img.src = src;
			}
		};
		
		this.onSkinLoadHandler = function(e){
			self.countLoadedSkinImages++;
			if(self.countLoadedSkinImages == self.totalGraphics){
				setTimeout(function(){
					self.dispatchEvent(FWDRLData.SKIN_LOAD_COMPLETE);
				}, 50);
			}
		};
		
		self.onSkinLoadErrorHandler = function(e){
			if (FWDRLUtils.isIEAndLessThen9){
				message = "Graphics image not found!";
			}else{
				message = "The skin icon with label <font color='#FFFFFF'>" + e.target.src + "</font> can't be loaded, check path!";
			}
			
			if(window.console) console.log(e);
			var err = {text:message};
			setTimeout(function(){
				self.dispatchEvent(FWDRLData.LOAD_ERROR, err);
			}, 50);
		};
		
		//####################################//
		/* show error if a required property is not defined */
		//####################################//
		self.showPropertyError = function(error){
			self.dispatchEvent(FWDRLData.LOAD_ERROR, {text:"The property called <font color='#FFFFFF'>" + error + "</font> is not defined."});
		};
		
		self.init();
	};
	
	/* set prototype */
	FWDRLData.setPrototype = function(){
		FWDRLData.prototype = new FWDRLEventDispatcher();
	};
	
	FWDRLData.prototype = null;
	
	FWDRLData.PRELOADER_LOAD_DONE = "onPreloaderLoadDone";
	FWDRLData.LOAD_DONE = "onLoadDone";
	FWDRLData.LOAD_ERROR = "onLoadError";
	FWDRLData.IMAGE_LOADED = "onImageLoaded";
	FWDRLData.SKIN_LOAD_COMPLETE = "onSkinLoadComplete";
	FWDRLData.SKIN_PROGRESS = "onSkinProgress";
	FWDRLData.IMAGES_PROGRESS = "onImagesPogress";
	FWDRLData.PLAYLIST_LOAD_COMPLETE = "onPlaylistLoadComplete";
	
	window.FWDRLData = FWDRLData;
}(window));