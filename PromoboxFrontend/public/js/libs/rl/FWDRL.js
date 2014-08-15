/* Gallery */
(function (window){
	
	var FWDRL = function(props){
		
		var self = this;
	
		/* init gallery */
		self.init = function(){
		
			TweenLite.ticker.useRAF(false);
			self.props_obj = props;
			 
			if(!self.props_obj){
				alert("FWDRL constructor properties object is not defined!");
				return;
			}
			
			
			this.stageContainer = document.getElementsByTagName("body")[0];
			if(!this.stageContainer) this.stageContainer = document.documentElement;
			this.listeners = {events_ar:[]};
			this.buttons_ar = null;
			this.buttonsMaxW_ar = null;
			
			this.ws = null;
			this.so = null;
			this.data = null;
			this.customContextMenu_do = null;
			this.thumbnailsManager_do = null;
			this.info_do = null;
			this.hider = null;
			this.main_do = null;
			this.bk_do = null;
			this.preloader_do = null;
			this.playlist_ar = null;
			this.mainItemHolder_do = null;
			this.itemBk_do = null;
			this.itemBorder_do = null;
			this.itemHolder_do = null;
			this.curItem_do = null;
			this.prevItem_do = null;
			this.image_img = null;
			this.closeButton_do = null;
			this.zoomButton_do = null;
			this.descButton_do = null;
			this.slideShowButton_do = null;
			this.nextButton_do = null;
			this.prevButton_do = null;
			this.hsThumbanilsButton_do = null;
			this.video_do = null;
			this.videoHolder_do = null;
			this.audioHolder_do = null;
			this.audio_do = null;
			
			this.rightClickContextMenu_str = this.props_obj.rightClickContextMenu || "developer";
			var test = this.rightClickContextMenu_str == "developer" 
				   || this.rightClickContextMenu_str == "disabled"
				   || this.rightClickContextMenu_str == "default";
			if(!test) this.rightClickContextMenu_str = "developer";
			
			this.buttonsAlignment_str = this.props_obj.buttonsAlignment || "in";
			var test = this.buttonsAlignment_str == "in" 
				   || this.buttonsAlignment_str == "out";
			if(!test) this.buttonsAlignment_str = "in";
			this.DFButtonsAlignment_str = this.buttonsAlignment_str;
		
			this.descriptionWindowPosition_str = this.props_obj.descriptionWindowPosition || "top";
			test = this.descriptionWindowPosition_str == "top" 
				   || this.descriptionWindowPosition_str == "bottom";
			if(!test) this.descriptionWindowPosition_str = "top";
			this.DFDescriptionWindowPosition_str = this.descriptionWindowPosition_str;
			
			this.descriptionAnimationType_str = this.props_obj.descriptionWindowAnimationType || "motion";
			test = this.descriptionAnimationType_str == "motion" 
				   || this.descriptionAnimationType_str == "opacity";
			if(!test) this.descriptionAnimationType_str = "motion";
			this.DFDescriptionAnimationType_str = this.descriptionAnimationType_str;
			
			this.descriptionAnimationType_str = this.props_obj.descriptionWindowAnimationType || "motion";
			test = this.descriptionAnimationType_str == "motion" 
				   || this.descriptionAnimationType_str == "opacity";
			if(!test) this.descriptionAnimationType_str = "motion";
			this.DFDescriptionAnimationType_str = this.descriptionAnimationType_str;
			
			this.thumbnailsHoverEffect_str = this.props_obj.thumbnailsHoverEffect || "scale";
			test = this.thumbnailsHoverEffect_str == "scale" 
				   || this.thumbnailsHoverEffect_str == "opacity";
			if(!test) this.thumbnailsHoverEffect_str = "opacity";
			this.DFThumbnailsHoverEffect_str = this.thumbnailsHoverEffect_str;
			
			this.facebookAppId_str = self.props_obj.facebookAppId || undefined;
			this.googleMapsAPIKey_str = "AIzaSyDYlgLIneg_UOd8STBfJEgq2JgmT5nNJKU";
			this.backgroundColor_str = this.props_obj.backgroundColor || "#000000";
			this.DFBackgroundColor_str = self.backgroundColor_str;
			this.playlistDOMOrObject = null;
			this.type_str;
			this.itemBorderColor_str = this.props_obj.itemBorderColor || "transparent";
			this.DFitemBorderColor_str = this.itemBorderColor_str;
			this.itemBkColor_str = this.props_obj.itemBackgroundColor || "transparent";
			this.DFItemBkColor_str = this.itemBkColor_str;
			this.playlistDomOrObj_str = undefined;
			this.itemBoxShadow_str = this.props_obj.itemBoxShadow || "none";
			this.DFItemBoxShadow_str = this.itemBoxShadow_str;
			this.thumbnailsBorderNormalColor_str = this.props_obj.thumbnailsBorderNormalColor || "#FF0000";
			this.DFThumbnailsBorderNormalColor = this.thumbnailsBorderNormalColor_str;
			this.thumbnailsBorderSelectedColor_str = this.props_obj.thumbnailsBorderSelectedColor || "#FF0000";
			this.DFThumbnailsBorderSelectedColor_str = this.thumbnailsBorderSelectedColor_str;
			this.descriptionWindowBackgroundColor_str = this.props_obj.descriptionWindowBackgroundColor || "#FF0000";
			this.DFDescriptionWindowBackgroundColor = this.descriptionWindowBackgroundColor_str;
			this.thumbnailsOverlayColor_str = this.props_obj.thumbnailsOverlayColor || "#FF0000";
			this.DFThumbnailsOverlayColor_str = this.thumbnailsOverlayColor_str;
			this.posterPath_str;
			this.DFVideoControllerBackgroundColor_str;
			this.DFVideoPosterBackgroundColor_str;
			this.DFTimeColor_str;
		
			this.descriptionWindowBackgroundOpacity = this.props_obj.descriptionWindowBackgroundOpacity || 1;
			this.DFDescriptionWindowBackgroundOpacity = this.descriptionWindowBackgroundOpacity;
			this.backgroundOpacity = this.props_obj.backgroundOpacity || .8;
			this.DFBackgroundOpacity = this.backgroundOpacity;
			if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
				this.buttonsOffsetIn = this.props_obj.buttonsOffsetIn || 0;
			}else{
				this.buttonsOffsetIn = this.props_obj.buttonsOffsetOut || 0;
			}
			this.DFButtonsOffsetIn = this.buttonsOffsetIn;
			
			if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
				this.buttonsOffsetOut = this.props_obj.buttonsOffsetOut || 0;
			}else{
				this.buttonsOffsetOut = this.props_obj.buttonsOffsetIn || 0;
			}
			this.DFButtonsOffsetOut = this.buttonsOffsetOut;
			
			this.audioPlayerMarginsOffset = 20;
			this.itemBorderRadius = this.props_obj.itemBorderRadius || 0; 
			this.DFItemBorderRadius = this.itemBorderRadius;
			this.itemBorderSize = this.props_obj.itemBorderSize || 0; 
			if(this.itemBorderSize == 0) this.itemBorderColor_str = "transparent";
			this.DFItemBorderSize = this.itemBorderSize;
			this.spaceBetweenButtons = this.props_obj.spaceBetweenButtons || 0; 
			this.DFSpaceBetweenButtons = this.spaceBetweenButtons;
			this.buttonsHideDelay = this.props_obj.buttonsHideDelay || 3;
			this.buttonsHideDelay *= 1000;
			this.DFbuttonsHideDelay = this.buttonsHideDelay;
			this.defaultItemW = this.props_obj.defaultItemWidth || 640;
			this.defaultItemH = this.props_obj.defaultItemHeight || 380;
			this.DFDefaultItemW = this.defaultItemW;
			this.DFDefaultItemH = this.defaultItemH;
			this.thumbnailsOffsetBottom = this.props_obj.thumbnailsOffsetBottom || 0;
			this.DFThumbnailsOffsetBottom = this.thumbnailsOffsetBottom;
			this.thumbnailsBorderSize = this.props_obj.thumbnailsBorderSize || 0;
			this.DFThumbnailsBorderSize = this.thumbnailsBorderSize;
			this.thumbnailsBorderRadius = this.props_obj.thumbnailsBorderRadius || 0;
			this.DFThumbnailsBorderRadius = this.thumbnailsBorderRadius;
			this.thumbnailH = this.props_obj.thumbnailsImageHeight || 50;
			this.thumbnailH += (this.thumbnailsBorderSize * 2) + this.thumbnailsOffsetBottom;
			this.DFThumbnailH = this.thumbnailH;
			this.spaceBetweenThumbnailsAndItem = this.props_obj.spaceBetweenThumbnailsAndItem || 0;
			this.spaceBetweenThumbnails = this.props_obj.spaceBetweenThumbnails || 0;
			this.DFSpaceBetweenThumbnails = this.spaceBetweenThumbnails;
			
			this.itemOffsetH = this.props_obj.itemOffsetHeight || 0;
			this.DFItemOffsetH = this.itemOffsetH;
			this.spaceBetweenThumbnailsAndItem = this.props_obj.spaceBetweenThumbnailsAndItem || 0;
			this.DFSpaceBetweenThumbnailsAndItem = this.spaceBetweenThumbnailsAndItem;
			this.slideShowDelay = parseInt(this.props_obj.slideShowDelay) * 1000;
			if(this.slideShowDelay < 1/1000) this.slideShowDelay = 1000;
			this.DFSlideShowDelay = this.slideShowDelay;
			this.thumbnailsOverlayOpacity = this.props_obj.thumbnailsOverlayOpacity || 1;
			this.DFThumbnailsOverlayOpacity = this.thumbnailsOverlayOpacity;
			this.id = -1;
			this.prevId = -2;
			this.stageWidth = 0;
			this.stageHeight = 0;
			this.totalItems = 0;
			this.originalW = 0;
			this.originalH = 0;
			this.maxButtonW = 0;
			this.finalW = 0;
			this.finalH = 0;
			this.prevVideoW = 0;
			this.prevVideoH = 0;
			this.finalX = 0;
			this.finalY = 0;
			this.gmx = 0;
			this.gmy = 0;
			this.lastPresedX = 0;
			this.lastPresedY = 0;
			this.friction = .9;
			this.vx = 0;
			this.vy = 0;
			this.dif = 0;
			this.mouseX = 0;
			this.mouseY = 0;
			
			this.resizeHandlerId_to;
			this.showOrHideCompleteId_to;
			this.hideCompleteId_to;
			this.animId_to;
			this.maximizeCompleteTimeOutId_to;
			this.minimizeCompleteTimeOutId_to;
			this.disableClickId_to;
			this.doNotAllowToHideId_to;
			this.updateImageWhenMaximized_int;
			
			this.isAnimForVideoAndAudioPlayersDone_bl = false;
			this.isMobile_bl = FWDRLUtils.isMobile;
			this.useDeepLinking_bl = this.props_obj.useDeepLinking; 
			this.useDeepLinking_bl = this.useDeepLinking_bl == "yes" ? true : false;
			if(FWDRLUtils.isLocal) this.useDeepLinking_bl = false;
			this.showCloseButton_bl = this.props_obj.showCloseButton; 
			this.showCloseButton_bl = this.showCloseButton_bl == "no" ? false : true;
			this.DFShowCloseButton_bl = this.showCloseButton_bl;
			this.defaultShowZoomButton_bl = this.props_obj.showZoomButton; 
			this.defaultShowZoomButton_bl = this.defaultShowZoomButton_bl == "no" ? false : true;
			this.DFShowZoomButton = this.defaultShowZoomButton_bl;
			this.showZoomButton_bl = false;
			this.defaultShowNextAndPrevButtons_bl = this.props_obj.showNextAndPrevButtons; 
			this.defaultShowNextAndPrevButtons_bl = this.defaultShowNextAndPrevButtons_bl == "no" ? false : true;
			if(this.props_obj.showNextAndPrevButtonsOnMobile == "no" && self.isMobile_bl)  this.defaultShowNextAndPrevButtons_bl = false;
			this.DFSefaultShowNextAndPrevButtons_bl = this.defaultShowNextAndPrevButtons_bl;
			this.defaultHideDescriptionButtons_bl = this.props_obj.showDescriptionButton;
			this.defaultHideDescriptionButtons_bl = this.defaultHideDescriptionButtons_bl == "yes" ? true : false;
			this.DFDefaultHideDescriptionButtons_bl = this.defaultHideDescriptionButtons_bl;
			this.showDescriptionButton_bl = false;
			this.hasItemDescription_bl = false;
			this.defaultShowDescriptionByDefault_bl = this.props_obj.showDescriptionByDefault;
			this.defaultShowDescriptionByDefault_bl = this.defaultShowDescriptionByDefault_bl == "yes" ? true : false;
			this.DFDefaultShowDescriptionByDefault_bl = this.defaultShowDescriptionByDefault_bl;
			this.showDescription_bl = this.defaultShowDescriptionByDefault_bl;
			this.addKeyboardSupport_bl = this.props_obj.addKeyboardSupport;
			this.addKeyboardSupport_bl = this.addKeyboardSupport_bl == "yes" ? true : false;
			this.DFSddKeyboardSupport_bl = this.addKeyboardSupport_bl;
			this.slideShowAutoPlay_bl = this.props_obj.slideShowAutoPlay;
			this.slideShowAutoPlay_bl = this.slideShowAutoPlay_bl == "yes" ? true : false;
			this.DFSlideShowAutoPlay_bl = this.slideShowAutoPlay_bl;
			this.videoAutoPlay_bl = this.props_obj.videoAutoPlay;
			this.videoAutoPlay_bl = this.videoAutoPlay_bl == "yes" ? true : false;
			if(self.isMobile_bl) self.videoAutoPlay_bl = false;
			this.DFVideoAutoPlay_bl = this.videoAutoPlay_bl;
			this.audioAutoPlay_bl = this.props_obj.audioAutoPlay;
			this.audioAutoPlay_bl = this.audioAutoPlay_bl == "yes" ? true : false;
			if(self.isMobile_bl) self.audioAutoPlay_bl = false;
			this.DFAudioAutoPlay_bl = this.audioAutoPlay_bl;
			this.nextVideoOrAudioAutoPlay_bl = this.props_obj.nextVideoOrAudioAutoPlay;
			this.nextVideoOrAudioAutoPlay_bl = this.nextVideoOrAudioAutoPlay_bl == "yes" ? true : false;
			if(self.isMobile_bl) self.nextVideoOrAudioAutoPlay_bl = false;
			this.DFNextVideoOrAudioAutoPlay_bl = this.nextVideoOrAudioAutoPlay_bl;
			this.defaultShowThumbnails_bl = this.props_obj.showThumbnails;
			this.defaultShowThumbnails_bl = this.defaultShowThumbnails_bl == "yes" ? true : false;
			this.DFDefaultThumbnails_bl = this.defaultShowThumbnails_bl;
			this.showThumbnailsByDefault_bl = this.props_obj.showThumbnailsByDefault;
			this.showThumbnailsByDefault_bl = this.showThumbnailsByDefault_bl == "yes" ? true : false;
			this.DFShowThumbnailsByDefault_bl = this.showThumbnailsByDefault_bl;
			this.defaultShowThumbnailsHideOrShowButton_bl = this.props_obj.showThumbnailsHideOrShowButton;
			this.defaultShowThumbnailsHideOrShowButton_bl = this.defaultShowThumbnailsHideOrShowButton_bl == "yes" ? true : false;
			this.DFDefaultShowThumbnailsHideOrShowButton_bl = this.defaultShowThumbnailsHideOrShowButton_bl;
			this.showSlideShowButton_bl = this.props_obj.showSlideShowButton;
			this.showSlideShowButton_bl = this.showSlideShowButton_bl == "yes" ? true : false;
			this.DFShowSlideShowButton_bl = this.showSlideShowButton_bl;
			this.defaultShowSlideShowAnimation_bl = this.props_obj.showSlideShowAnimation;
			this.defaultShowSlideShowAnimation_bl = this.defaultShowSlideShowAnimation_bl == "yes" ? true : false;
			this.DFSefaultShowSlideShowAnimation_bl = this.defaultShowSlideShowAnimation_bl;
			this.showSlideShowAnimation_bl = false;
			this.useAsModal_bl = this.props_obj.useAsModal;
			this.useAsModal_bl = this.useAsModal_bl == "yes" ? true : false;
			this.DFUseAsModal_bl = this.useAsModal_bl;
			this.showFacebookButton_bl = this.props_obj.showFacebookButton;
			this.showFacebookButton_bl = this.showFacebookButton_bl == "yes" ? true : false;
			this.DFShowFacebookButton_bl = this.showFacebookButton_bl;
			this.showThumbnailsOverlay_bl = this.props_obj.showThumbnailsOverlay; 
			this.showThumbnailsOverlay_bl = this.showThumbnailsOverlay_bl == "yes" ? true : false;
			this.DFShowThumbnailsOverlay_bl = this.showThumbnailsOverlay_bl; 
			this.showThumbnailsSmallIcon_bl = this.props_obj.showThumbnailsSmallIcon; 
			this.showThumbnailsSmallIcon_bl = this.showThumbnailsSmallIcon_bl == "yes" ? true : false;
			this.DFShowThumbnailsSmallIcon_bl = this.showThumbnailsSmallIcon_bl;
			
			this.doNotAllowToHide_bl = false;
			this.isVideoFullScreen_bl = false;
			this.hasKeyboardSupport_bl = false;
			this.isClickedDisabled_bl = false;
			this.showThumbnails_bl = false;
			this.areThumbnailsShowed_bl = false;
			this.showThumbnailsHideOrShowButton_bl = false;
			this.isDragging_bl = false;
			this.isAnimMaximizeOrMinimize_bl = false;
			this.swipeMoved_bl = false;
			this.isAPIReady_bl = false;
			this.isLoading_bl = false;
			this.isShowed_bl = false;
			self.isReady_bl = false;
			this.isAnim_bl = false;
			this.isFirstItemShowed_bl = false;
			this.firstVideoOrAudioAdded_bl = false;
			this.isMaximized_bl = false;
			this.useVideo_bl = false;
			this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
			
			this.initiallize();
		};
		
		//#############################################//
		/* setup main do */
		//#############################################//
		self.initiallize = function(){
			
			self.main_do = new FWDRLDisplayObject("div");
			self.main_do.screen.setAttribute("id", "RL");
			
			self.main_do.getStyle().msTouchAction = "none";
			self.main_do.getStyle().webkitTapHighlightColor = "rgba(0, 0, 0, 0)";
			self.main_do.setBackfaceVisibility();
			if(!self.isMobile_bl && FWDRLUtils.isChrome){
				self.main_do.hasTransform3d_bl =  false;
				self.main_do.hasTransform2d_bl =  false;
			}
			self.main_do.getStyle().width = "100%";
			self.main_do.getStyle().zIndex = "100000000000000000";
			
			self.bk_do = new FWDRLDisplayObject("div");
			self.bk_do.getStyle().width = "100%";
			self.bk_do.getStyle().height = "100%";
			self.bk_do.getStyle().backgroundColor = self.backgroundColor_str;
			self.bk_do.setAlpha(0);
		
			self.mainItemHolder_do = new FWDRLDisplayObject("div");	
			
			FWDRLDescriptionWindow.setPrototype();
			self.desc_do = new FWDRLDescriptionWindow(
					self, 
					self.descriptionAnimationType_str,
					self.descriptionWindowPosition_str,
					self.itemBorderSize, 
					self.descriptionWindowBackgroundColor_str, 
					self.descriptionWindowBackgroundOpacity);
			
			self.itemBorder_do = new FWDRLDisplayObject("div");
			self.itemBorder_do.getStyle().backgroundColor = self.itemBorderColor_str;
			if(!self.isMobile_bl && FWDRLUtils.isChrome){
				self.itemBorder_do.hasTransform3d_bl = false;
				self.itemBorder_do.hasTransform2d_bl = false;
				self.itemBorder_do.setBackfaceVisibility();
			}
			self.itemBk_do = new FWDRLDisplayObject("div");
			self.itemBk_do.getStyle().backgroundColor = self.itemBkColor_str;
			self.itemHolder_do = new FWDRLDisplayObject("div");
			self.itemHolder_do.setOverflow("visible");
		
			self.mainItemHolder_do.addChild(self.itemBorder_do);
			self.mainItemHolder_do.addChild(self.itemBk_do);
			self.mainItemHolder_do.addChild(self.itemHolder_do);
			//self.mainItemHolder_do.hasTransform3d_bl = false;
			//self.mainItemHolder_do.hasTransform2d_bl = false;
			self.mainItemHolder_do.addChild(self.desc_do);
			
			self.main_do.addChild(self.bk_do);
			self.main_do.addChild(self.mainItemHolder_do);
			self.stageContainer.appendChild(self.main_do.screen);
			
			if(!FWDRLUtils.isMobile || (FWDRLUtils.isMobile && FWDRLUtils.hasPointerEvent)) self.main_do.setSelectable(false);
			if(!self.isMobile_bl) self.setupContextMenu();
			self.setupInfoWindow();
			self.setupHider();
			self.setupDisableClick();
			self.setupData();
			
			if(self.useDeepLinking_bl){
				self.setupDL();
				setTimeout(function(){
					var playlistName_str = FWDAddress.getParameter("rl_playlist");
					var playlistId = FWDAddress.getParameter("rl_id");
					self.propsObjVariableName_str = FWDAddress.getParameter("rl_propsobj");
					if(location.href.indexOf("RL?") && playlistName_str && playlistId){
						FWDRL.show(playlistName_str, playlistId, self.propsObjVariableName_str);
					}
				}, 100);
			}
		};
		
		//#############################################//
		/* setup info_do */
		//#############################################//
		self.setupInfoWindow = function(){
			FWDRLInfo.setPrototype();
			self.info_do = new FWDRLInfo(self);
		};	
		
		//#############################################//
		/* setup context menu */
		//#############################################//
		self.setupContextMenu = function(){
			self.customContextMenu_do = new FWDRLContextMenu(self.main_do, self.rightClickContextMenu_str);
		};
		
		//#############################################//
		/* Setup hider */
		//#############################################//
		this.setupHider = function(){
			FWDRLHider.setPrototype();
			self.hider = new FWDRLHider(self.main_do, self.buttonsHideDelay);
			self.hider.addListener(FWDRLHider.SHOW, self.hiderShowHandler);
			self.hider.addListener(FWDRLHider.HIDE, self.hiderHideHandler);
		};
		
		this.hiderShowHandler = function(){
			self.showButtonsWithFade(true);
			self.positionButtons(true);
		};
		
		this.hiderHideHandler = function(){
		
			if(!self.isMobile_bl){

				if(self.showCloseButton_bl){
					if(FWDRLUtils.hitTest(self.closeButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
				
				if(self.showNextAndPrevButtons_bl){
					if(FWDRLUtils.hitTest(self.nextButton_do.screen, self.hider.globalX, self.hider.globalY)
					   || FWDRLUtils.hitTest(self.prevButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
				
				if(self.showZoomButton_bl){
					if(FWDRLUtils.hitTest(self.zoomButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
				
				if(self.showDescriptionButton_bl){
					if(FWDRLUtils.hitTest(self.descButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
				
				if(self.showSlideShowButton_bl){
					if(FWDRLUtils.hitTest(self.slideShowButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
			
				if(self.showFacebookButton_bl){
					if(FWDRLUtils.hitTest(self.fbButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
				
				if(self.showThumbnailsHideOrShowButton_bl){
					if(FWDRLUtils.hitTest(self.hsThumbanilsButton_do.screen, self.hider.globalX, self.hider.globalY)){
						self.hider.reset();
						return;
					}
				}
			}
			
			if(self.showSlideShowAnimation_bl){	
				if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
					FWDRLTweenMax.to(self.slp_do, .8, {y:self.finalY, ease:Expo.easeInOut});
				}else{
					FWDRLTweenMax.to(self.slp_do, .8, {y:self.buttonsOffsetIn, ease:Expo.easeInOut});
				}
			}
			self.hideButtonsWithFade(true);
		};
		
		//#####################################//
		/* Setup disable click */
		//#####################################//
		self.setupDisableClick = function(){
			self.disableClick_do = new FWDRLDisplayObject("div");
			if(FWDRLUtils.isIE){
				self.disableClick_do.setBkColor("#FFFFFF");
				self.disableClick_do.setAlpha(0.00001);
			}
		};
		
		self.disableClick = function(){
			self.showDisable();
			self.disableClickId_to =  setTimeout(function(){
				self.hideDisable();
			}, 100);
		};
		
		self.showDisable = function(){
			if(self.isClickedDisabled_bl) return;
			self.isClickedDisabled_bl = true;
			self.disableClick_do.setWidth(self.stageWidth);
			self.disableClick_do.setHeight(self.stageHeight);
		};
		
		self.hideDisable = function(){
			if(!self.isClickedDisabled_bl) return;
			self.isClickedDisabled_bl = false;
			self.disableClick_do.setWidth(0);
			self.disableClick_do.setHeight(0);
		};
		
	
		//#############################################//
		/* resize handler */
		//#############################################//
		self.startResizeHandler = function(){
			if(window.addEventListener){
				window.addEventListener("resize", self.onResizeHandler);
				window.addEventListener("scroll", self.scrollHandler);
				window.addEventListener ("mousewheel", self.mouseDummyHandler);
				window.addEventListener('DOMMouseScroll', self.mouseDummyHandler);
				if(self.isMobile_bl) window.addEventListener("touchmove", self.mouseDummyHandler);
			}else if(window.attachEvent){
				window.attachEvent("onresize", self.onResizeHandler);
				window.attachEvent("onscroll", self.scrollHandler);
				document.attachEvent("onmousewheel", self.mouseDummyHandler);
			}
			
			self.onResizeHandler();
			setTimeout(self.scrollHandler, 200);
			setTimeout(self.scrollHandler, 500);
		};
	
		
		self.stopResizeHandler = function(){
			clearTimeout(self.resizeHandlerId_to);
			if(window.removeEventListener){
				window.removeEventListener("resize", self.onResizeHandler);
				window.removeEventListener("scroll", self.scrollHandler);
				if(self.isMobile_bl) window.removeEventListener("touchmove", self.mouseDummyHandler);
			}else if(window.detachEvent){
				window.detachEvent("onresize", self.onResizeHandler);
				window.detachEvent("onscroll", self.scrollHandler);
				document.detachEvent("onmousewheel", self.mouseDummyHandler);
			}
		};
		
		self.onResizeHandler = function(e){
			self.resizeHandler();
		};
		
		self.scrollHandler = function(e){
			self.so = FWDRLUtils.getScrollOffsets();
			if(!self.isShowed_bl) return;
			self.main_do.setX(self.so.x);
			self.main_do.setY(self.so.y);
			if(e && e.preventDefault) e.preventDefault();
		};
		
		self.addPreventMouseWheel = function(){
			if(window.addEventListener){
				window.addEventListener ("mousewheel", self.mouseDummyHandler);
				window.addEventListener('DOMMouseScroll', self.mouseDummyHandler);
			}else if(document.attachEvent){
				document.attachEvent ("onmousewheel", self.mouseDummyHandler);
			}
		};
		
		self.removePreventMouseWheel = function(){
			if(window.removeEventListener){
				window.removeEventListener ("mousewheel", self.mouseDummyHandler);
				window.removeEventListener('DOMMouseScroll', self.mouseDummyHandler);
			}else if(document.detachEvent){
				document.detachEvent("onmousewheel", self.mouseDummyHandler);
			}
		};
		
		
		//###############################################//
		/* Disable scroll and touch events for the main browser scrollbar.*/
		//###############################################//
		this.mouseDummyHandler = function(e){
			if(e.preventDefault){
				e.preventDefault();
			}else{
				return false;
			}
		};
		
		self.resizeHandler = function(overwrite){
			if(!self.isShowed_bl) return;
			
			self.ws = FWDRLUtils.getViewportSize();
			self.stageWidth = self.ws.w;
			self.stageHeight = self.ws.h;
			
			if(self.isMobile_bl){
				self.main_do.setWidth(self.stageWidth);
				self.main_do.setHeight(self.stageHeight);
			}
			
			if(self.preloader_do) self.positionPreloader();
			if(self.info_do && self.info_do.isShowed_bl) self.info_do.positionAndResize();
			
			self.resizeCurrentItem();
			self.positionButtons();
			self.main_do.setX(self.so.x);
			self.main_do.setY(self.so.y);
			self.main_do.setHeight(self.stageHeight);
			if(self.thumbnailsManager_do && self.showThumbnails_bl) self.thumbnailsManager_do.positionAndResize();
		
			clearTimeout(self.resizeHandlerId_to);
			self.resizeHandlerId_to = setTimeout(self.checkStageSizeAndResize, 50);
		};
		
		self.checkStageSizeAndResize = function(){
			self.ws = FWDRLUtils.getViewportSize();
			if(self.stageWidth != self.ws.w) self.resizeHandler();
		};
	
		//#############################################//
		/* setup data */
		//#############################################//
		self.setupData = function(){
			FWDRLData.setPrototype();
			self.data = new FWDRLData(self.props_obj, self.rootElement_el, self);
			
			self.DFVideoControllerBackgroundColor_str = self.data.videoControllerBackgroundColor_str;
			self.DFVideoPosterBackgroundColor_str = self.data.videoPosterBackgroundColor_str;
			self.DFAudioControllerBackgroundColor_str = self.data.audioControllerBackgroundColor_str;
			
			self.data.addListener(FWDRLData.PRELOADER_LOAD_DONE, self.onPreloaderLoadDone);
			self.data.addListener(FWDRLData.LOAD_ERROR, self.dataLoadError);
			self.data.addListener(FWDRLData.SKIN_LOAD_COMPLETE, self.dataSkinLoadComplete);
		};
		
		self.onPreloaderLoadDone = function(){
			self.setupPreloader();
			if(self.isShowed_bl){
				self.positionPreloader();
				self.preloader_do.show(true);
				self.resizeHandler();
			}
		};
		
		self.dataLoadError = function(e){
			if(self.preloader_do) self.preloader_do.hide(false);
			self.main_do.addChild(self.info_do);
			self.info_do.showText(e.text);
			setTimeout(self.resizeHandler, 200);
			FWDRL.dispatchEvent(FWDRL.ERROR, {error:e.text});
		};
		
		self.dataSkinLoadComplete = function(){	
			self.isReady_bl = true;
			self.useVideo_bl = self.data.useVideo_bl;
			self.useAudio_bl = self.data.useAudio_bl;
			self.setupMainStuff();
			clearTimeout(self.showOrHideCompleteId_to);
			self.showOrHideCompleteId_to = setTimeout(self.showComplete, 401);
			setTimeout(function(){
				FWDRL.dispatchEvent(FWDRL.READY);
			}, 401);
		};
		
		//#############################################//
		/* Setup main instances */
		//#############################################//
		self.setupMainStuff = function(){
			self.setupButtons();
			self.setupTimerManager();
			self.setupFacebook();
			if(self.data.useVideo_bl) self.setupVideoPlayer();
			if(self.data.useAudio_bl) self.setupAudioPlayer();
			self.hideStuffForGood();
		};
		
		//###########################################//
		/* Setup video player */
		//###########################################//
		self.setupVideoPlayer = function(){
			self.videoHolder_do = new FWDRLDisplayObject("div");
			self.videoHolder_do.setWidth(500);
			self.videoHolder_do.setHeight(500);
			self.mainItemHolder_do.addChildAt(self.videoHolder_do, 3);
		
			self.video_do = new FWDRLEVPlayer(self.videoHolder_do.screen, self.data);
			self.video_do.addListener(FWDRLEVPlayer.ERROR, self.videoErrorHandler);
			self.video_do.addListener(FWDRLEVPlayer.GO_FULLSCREEN, self.videoFullScreenHandler);
			self.video_do.addListener(FWDRLEVPlayer.GO_NORMALSCREEN, self.videoNormalScreenHandler);
		};
		
		self.videoErrorHandler = function(e){
			self.main_do.addChild(self.info_do);
			self.info_do.showText(e.error);	
		};
		
		
		self.videoFullScreenHandler = function(){
			self.isVideoFullScreen_bl = true;
			self.resizeCurrentItem();
			self.mainItemHolder_do.getStyle().overflow = "visible";
			self.setButtonsInvisible();
			if(self.addKeyboardSupport_bl) self.removeKeyboardSupport();
			if(self.isMobile_bl) self.removeSwipeSupport();
		};
		
		self.videoNormalScreenHandler = function(){
			self.isVideoFullScreen_bl = false;
			self.resizeCurrentItem();
			self.mainItemHolder_do.getStyle().overflow = "hidden";
			self.setButtonsVisible();
			if(self.addKeyboardSupport_bl) self.addKeyboardSupport();
			if(self.isMobile_bl) self.addSwipeSupport();
		};
		
		//############################################//
		/* Setup audio player */
		//############################################//
		self.setupAudioPlayer = function(){
			self.audioHolder_do = new FWDRLDisplayObject("div");
			self.audioHolder_do.hasTransform3d_bl = false;
			self.audioHolder_do.hasTransform2d_bl = false;
			self.audioHolder_do.setWidth(500);
			self.audioHolder_do.setHeight(500);
			self.audioHolder_do.setHeight(self.data.audioControllerHeight);
			self.mainItemHolder_do.addChildAt(self.audioHolder_do, 3);
			self.mainItemHolder_do.addChildAt(self.audioHolder_do, 3);
		
			self.audio_do = new FWDRLEAP(self.audioHolder_do.screen, self.data);
			self.audio_do.addListener(FWDRLEAP.ERROR, self.videoErrorHandler);
		};
		
		//############################################//
		/* Setup slideshow timer */
		//###########################################//
		self.setupTimerManager = function(){
			FWDRLTimerManager.setProtptype();
			self.tm = new FWDRLTimerManager(self.slideShowDelay);
			self.tm.addListener(FWDRLTimerManager.STOP, self.tmStopHandler);
			self.tm.addListener(FWDRLTimerManager.START, self.tmStartHandler);
			self.tm.addListener(FWDRLTimerManager.PAUSE, self.tmPauseHandler);
			self.tm.addListener(FWDRLTimerManager.RESUME, self.tmResumeHandler);
			self.tm.addListener(FWDRLTimerManager.TIME, self.tmTimeHandler);
		};
		
		self.tmStopHandler = function(){
			self.slideShowButton_do.setButtonState(1);
			if(self.showSlideShowAnimation_bl){
				self.hideSlideShowAnimation();
				self.positionButtons(true);
			}
			self.showSlideShowAnimation_bl = false;
		};
		
		self.tmStartHandler = function(){
			self.slideShowButton_do.setButtonState(0);
			if(!self.showSlideShowAnimation_bl){
				self.showSlideShowAnimation();
				self.positionButtons(true);
				self.slp_do.animShow();
			}
			self.showSlideShowAnimation_bl = true;
		};
		
		self.tmPauseHandler = function(){
			if(self.showSlideShowAnimation_bl) self.slp_do.animHide();
		};
		
		self.tmResumeHandler = function(){
			if(self.showSlideShowAnimation_bl) self.slp_do.animShow();
		};
		
		self.tmTimeHandler = function(){
			self.gotoNextItem();
			if(self.showSlideShowAnimation_bl) self.slp_do.animHide();
		};
	
		//############################################//
		/* setup deeplink */
		//############################################//
		self.setupDL = function(){
			FWDAddress.onChange = self.dlChangeHandler;
			self.dlChangeHandler();
		};
		
		self.dlChangeHandler = function(){
			//if(self.so) window.scrollTo(self.so.x, self.so.y);
			
			if(!self.isReady_bl || self.isAnim_bl || self.isAnimMaximizeOrMinimize_bl || !self.useDeepLinking_bl) return;
			
			if(self.isMaximized_bl){
				self.maximizeOrMinimize();
				return;
			}
			
			var playlistName_str = FWDAddress.getParameter("rl_playlist");
			var playlistId = FWDAddress.getParameter("rl_id");
			self.propsObjVariableName_str = FWDAddress.getParameter("rl_propsobj");
			
			if(!self.isShowed_bl){
				if(location.href.indexOf("RL?") != -1 && playlistName_str && playlistId){
					FWDRL.show(playlistName_str, playlistId, self.propsObjVariableName_str);
				}
				
				return;
			}else{
				if(location.href.indexOf("RL?") == -1 || !playlistName_str || !playlistId){
					self.hide();
					return;
				}
			}
			
			self.id = parseInt(FWDAddress.getParameter("rl_id"));
			
			if(self.id == self.prevId) return;
			
			if(self.id < 0){
				self.id = 0;
				if(self.propsObjVariableName_str){
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + self.propsObjVariableName_str);
				}else{
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id);
				}
				return;
			}else if(self.id > self.totalItems - 1){
				self.id = self.totalItems - 1;
				if(self.propsObjVariableName_str){
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + self.propsObjVariableName_str);
				}else{
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id);
				}
				return;
			}
			
			self.createAndShowItem();
			self.prevId = self.id;
		};
		
		//#############################################//
		/* setup preloader */
		//#############################################//
		self.setupPreloader = function(){
			FWDRLPreloader.setPrototype();
			self.preloader_do = new FWDRLPreloader(self.data.mainPreloader_img, 38, 38, 30, 36, true);
			self.main_do.addChild(self.preloader_do);
		};
		
		self.positionPreloader = function(){
			self.preloader_do.setX(parseInt((self.stageWidth - self.preloader_do.w )/2));		
			if(self.thumbnailsManager_do && self.thumbnailsManager_do.areThumbnailsCreated_bl && self.areThumbnailsShowed_bl){
				self.preloader_do.setY(parseInt((self.stageHeight - self.preloader_do.h - self.thumbnailH)/2));
			}else{
				self.preloader_do.setY(parseInt((self.stageHeight - self.preloader_do.h)/2));
			}
		};
		
		//##########################################//
		/* Setup facebook */
		//##########################################//
		self.setupFacebook = function(){
			if(document.location.protocol == "file:") return;
			self.facebookShare = new FWDRLFacebookShare(self.facebookAppId_str);
		};
		
		//#############################################//
		/* Setup thumbnail manager */
		//#############################################//
		self.setupThumbnailManager = function(){
			if(self.thumbnailsManager_do) return;
			FWDRLThumbnailsManager.setPrototype();
			self.thumbnailsManager_do = new FWDRLThumbnailsManager(self);
			self.thumbnailsManager_do.addListener(FWDRLThumb.CLICK, self.thumbClickHandler);
			self.main_do.addChildAt(self.thumbnailsManager_do, 1);
		};
		
		self.hideOrShowThumbnails = function(){
			if(self.areThumbnailsShowed_bl){
				self.hsThumbanilsButton_do.setButtonState(0);
				self.thumbnailsManager_do.hide(true);
				self.areThumbnailsShowed_bl = false;
			}else{
				self.hsThumbanilsButton_do.setButtonState(1);
				self.thumbnailsManager_do.show(true);
				self.areThumbnailsShowed_bl = true;
			}
			self.resizeCurrentItem(false, true);
			self.positionButtons(true);
			self.startAnim(801);
		};
		
		self.thumbClickHandler = function(e){
			self.gotoToItem(e.id);
		};
		
		self.setupThumbnails = function(delay){
			setTimeout(function(){
				if(self.thumbnailsManager_do && self.showThumbnails_bl) self.thumbnailsManager_do.setupThumbnails();
			}, delay);
		};
		
		//#############################################//
		/* Setup buttons */
		//#############################################//
		self.setupButtons = function(){
			
			self.buttons_ar = [];
			self.buttonsMaxW_ar = [];
			
			FWDRLSimpleButton.setPrototype();
			self.closeButton_do = new FWDRLSimpleButton(self.data.closeN_img, self.data.closeSPath_str);
			self.closeButton_do.addListener(FWDRLSimpleButton.MOUSE_UP, self.closeButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.closeButton_do);
			self.main_do.addChild(self.closeButton_do); 

			FWDRLComplexButton.setPrototype();
			self.zoomButton_do = new FWDRLComplexButton(
					self.data.maximizeN_img, 
					self.data.maximizeSPath_str, 
					self.data.minimizeN_img, 
					self.data.minimizeSPath_str, 
					true);
			self.zoomButton_do.addListener(FWDRLComplexButton.MOUSE_UP, self.zoomButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.zoomButton_do);
			self.main_do.addChild(self.zoomButton_do); 
			
			FWDRLComplexButton.setPrototype();
			self.descButton_do = new FWDRLComplexButton(
					self.data.infoOpenN_img, 
					self.data.infoOpenS_str, 
					self.data.infoCloseN_img, 
					self.data.infoCloseS_str, 
					true);
			self.descButton_do.addListener(FWDRLComplexButton.MOUSE_UP, self.descButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.descButton_do);
			self.main_do.addChild(self.descButton_do); 
			
			FWDRLComplexButton.setPrototype();
			self.slideShowButton_do = new FWDRLComplexButton(
					self.data.playN_img, 
					self.data.playS_str, 
					self.data.pauseN_img, 
					self.data.pauseS_str, 
					true);
			self.slideShowButton_do.addListener(FWDRLComplexButton.MOUSE_UP, self.slideshowButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.slideShowButton_do);
			self.main_do.addChild(self.slideShowButton_do); 
			
			FWDRLSlideShowPreloader.setPrototype();
			self.slp_do = new FWDRLSlideShowPreloader(self.data.slideSwowImage_img, 30, 29, 60, self.slideShowDelay);
			self.buttonsMaxW_ar.push(self.slp_do);
			self.main_do.addChild(self.slp_do); 
			
			
			FWDRLSimpleButton.setPrototype();
			self.fbButton_do = new FWDRLSimpleButton(self.data.facebookImage_img, self.data.facebookImageSPath_str);
			self.fbButton_do.addListener(FWDRLSimpleButton.MOUSE_UP, self.facebookButtonOnMouseUpHandler);	
			self.buttonsMaxW_ar.push(self.fbButton_do);
			self.main_do.addChild(self.fbButton_do);
			
			FWDRLSimpleButton.setPrototype();
			self.nextButton_do = new FWDRLSimpleButton(self.data.nextN_img, self.data.nextSPath_str);
			self.nextButton_do.addListener(FWDRLSimpleButton.MOUSE_UP, self.nextButtonOnMouseUpHandler);	
			self.buttonsMaxW_ar.push(self.nextButton_do);
			self.main_do.addChild(self.nextButton_do);
			
			FWDRLSimpleButton.setPrototype();
			self.prevButton_do = new FWDRLSimpleButton(self.data.prevN_img, self.data.prevSPath_str);
			self.prevButton_do.addListener(FWDRLSimpleButton.MOUSE_UP, self.prevButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.prevButton_do);
			self.main_do.addChild(self.prevButton_do); 
			
			FWDRLComplexButton.setPrototype();
			self.hsThumbanilsButton_do = new FWDRLComplexButton(
					self.data.hideThumbnailsN_img, 
					self.data.hideThumbnailsSPath_str, 
					self.data.showThumbnailsN_img, 
					self.data.showThumbnailsSPath_str, 
					true);
			self.hsThumbanilsButton_do.addListener(FWDRLComplexButton.MOUSE_UP, self.hsButtonOnMouseUpHandler);
			self.buttonsMaxW_ar.push(self.hsThumbanilsButton_do);
			self.main_do.addChild(self.hsThumbanilsButton_do); 
			
			for(var i=0; i<self.buttonsMaxW_ar.length; i++){
				if(self.maxButtonW < self.buttonsMaxW_ar[i].h) self.maxButtonW = self.buttonsMaxW_ar[i].w;
			}
		};
		
		self.closeButtonOnMouseUpHandler = function(){
			self.hide();
		};
		
		self.zoomButtonOnMouseUpHandler = function(e){
			self.maximizeOrMinimize();
		};
		
		self.facebookButtonOnMouseUpHandler = function(){
			if(FWDRLUtils.isLocal){
				self.main_do.addChild(self.info_do);
				self.info_do.showText("Sharing locally is not allowed or possible! Please test online.");
				return;
			}
			
			var href = location.href;
			var thumbnailPath = self.playlist_ar[self.id].thumbnailPath_str;
			var desc = self.playlist_ar[self.id].descriptionText;
	
			if(thumbnailPath && thumbnailPath.indexOf("//") ==  -1){
				var absolutePath = location.pathname;
				absolutePath = location.protocol + "//" + location.host + absolutePath.substring(0, absolutePath.lastIndexOf("/") + 1);
				thumbnailPath = absolutePath + thumbnailPath;
			}
			
			self.facebookShare.share(href, thumbnailPath, desc);
		};
		
		self.nextButtonOnMouseUpHandler = function(){
			self.gotoNextItem();
		};
		
		self.prevButtonOnMouseUpHandler = function(){
			self.gotoPrevItem();
		};
		
		self.descButtonOnMouseUpHandler = function(){
			if(self.isAnim_bl) return;
			if(self.showDescription_bl){
				self.showDescription_bl = false;
				self.descButton_do.setButtonState(1);
				self.desc_do.hide(true);
			}else{
				self.showDescription_bl = true;
				self.descButton_do.setButtonState(0);
				self.desc_do.show(true);
			}
		};
		
		self.slideshowButtonOnMouseUpHandler = function(){
			if(self.tm.isStopped_bl){
				self.tm.start();
			}else{
				self.tm.stop();
			}			
		};
		
		self.hsButtonOnMouseUpHandler = function(){
		
			if(!self.isMobile_bl 
				&& self.stageWidth < self.thumbnailsManager_do.totalW + ((self.hsThumbanilsButton_do.w + self.buttonsOffsetIn) * 2)
				|| self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
				self.disableClick();
			}
			self.hideOrShowThumbnails();
		};
		
		//########################################//
		/* Show / hide buttons if available */
		//########################################//
		self.showCloseButton = function(){
			if(!self.showCloseButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) == -1){
				self.buttons_ar.splice(0, 0, self.closeButton_do);
			}
		};
		
		self.hideCloseButton = function(){
			//if(!self.showCloseButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.zoomButton_do);
				self.closeButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do), 1);
			}
			
		};
		
		self.hideZoomButton = function(){
			//if(!self.defaultShowZoomButton_bl) return;
			
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.zoomButton_do);
				self.zoomButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do), 1);
			}
		};
		
		self.showZoomButton = function(){
			if(!self.defaultShowZoomButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) == -1){
				FWDRLTweenMax.killTweensOf(self.zoomButton_do);
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.zoomButton_do.setX(self.closeButton_do.x);
					self.zoomButton_do.setY(self.closeButton_do.y + self.closeButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.zoomButton_do);
				}else{
					if(self.isFirstItemShowed_bl){
						self.zoomButton_do.setX(self.mainItemHolder_do.x + self.mainItemHolder_do.w + self.buttonsOffsetIn);
						self.zoomButton_do.setY(self.mainItemHolder_do.y);
					}
					self.buttons_ar.splice(0, 0, self.zoomButton_do);
				}
			}
		};
		
		self.showDescriptionButton = function(){
			if(!self.defaultHideDescriptionButtons_bl) return;
			
			self.showDescriptionButton_bl = true;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.descButton_do) == -1){
				FWDRLTweenMax.killTweensOf(self.descButton_do);
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) != -1){
					self.descButton_do.setX(self.zoomButton_do.x);
					self.descButton_do.setY(self.zoomButton_do.y + self.zoomButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) + 1, 0, self.descButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.descButton_do.setX(self.closeButton_do.x);
					self.descButton_do.setY(self.closeButton_do.y + self.closeButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.descButton_do);
				}else{
					if(self.isFirstItemShowed_bl){
						self.descButton_do.setX(self.mainItemHolder_do.x + self.mainItemHolder_do.w + self.buttonsOffsetIn);
						self.descButton_do.setY(self.mainItemHolder_do.y);
					}
					self.buttons_ar.splice(0, 0, self.descButton_do);
				}
			}
		};
		
		self.hideDescriptionButton = function(){
			//if(!self.defaultHideDescriptionButtons_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do) != -1){
				self.showDescriptionButton_bl = false;
				FWDRLTweenMax.killTweensOf(self.descButton_do);
				self.descButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do), 1);
			}
		};
		
		
		self.hideSlideshowButton = function(){
			//if(!self.showSlideShowButton_bl) return;	
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.slideShowButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.slideShowButton_do);
				self.slideShowButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slideShowButton_do), 1);
			}
		};
		
		self.showSlideshowButton = function(){
			if(!self.showSlideShowButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slideShowButton_do) == -1){
				FWDRLTweenMax.killTweensOf(self.slideShowButton_do);
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.descButton_do) != -1){
					self.slideShowButton_do.setX(self.descButton_do.x);
					self.slideShowButton_do.setY(self.descButton_do.y + self.descButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do) + 1, 0, self.slideShowButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) != -1){
					self.slideShowButton_do.setX(self.zoomButton_do.x);
					self.slideShowButton_do.setY(self.zoomButton_do.y + self.zoomButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) + 1, 0, self.slideShowButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.slideShowButton_do.setX(self.closeButton_do.x);
					self.slideShowButton_do.setY(self.closeButton_do.y + self.closeButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.slideShowButton_do);
				}else{
					if(self.isFirstItemShowed_bl){
						self.slideShowButton_do.setX(self.mainItemHolder_do.x + self.mainItemHolder_do.w + self.buttonsOffsetIn);
						self.slideShowButton_do.setY(self.mainItemHolder_do.y);
					}
					self.buttons_ar.splice(0, 0, self.slideShowButton_do);
				}
			}
		};
		
		self.hideSlideShowAnimation = function(){
			//if(!self.defaultShowSlideShowAnimation_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.slp_do) != -1){
				FWDRLTweenMax.killTweensOf(self.slp_do);
				self.slp_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slp_do), 1);
			}
		};
		
		self.showSlideShowAnimation = function(){
			if(!self.defaultShowSlideShowAnimation_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slp_do) == -1){
				FWDRLTweenMax.killTweensOf(self.slp_do);
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slideShowButton_do) != -1){
					self.slp_do.setX(self.slideShowButton_do.x);
					self.slp_do.setY(self.slideShowButton_do.y + self.slideShowButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slideShowButton_do) + 1, 0, self.slp_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.descButton_do) != -1){
					self.slp_do.setX(self.descButton_do.x);
					self.slp_do.setY(self.descButton_do.y + self.descButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do) + 1, 0, self.slp_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) != -1){
					self.slp_do.setX(self.zoomButton_do.x);
					self.slp_do.setY(self.zoomButton_do.y + self.zoomButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) + 1, 0, self.slp_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.slp_do.setX(self.closeButton_do.x);
					self.slp_do.setY(self.closeButton_do.y + self.closeButton_do.h + self.spaceBetweenButtons);
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.slp_do);
				}else{
					if(self.isFirstItemShowed_bl){
						self.slp_do.setX(self.mainItemHolder_do.x + self.mainItemHolder_do.w + self.buttonsOffsetIn);
						self.slp_do.setY(self.mainItemHolder_do.y);
					}
					self.buttons_ar.splice(0, 0, self.slp_do);
				}
			}
		};
		
		self.hideFacebookButton = function(){
			//if(!self.showFacebookButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.fbButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.fbButton_do);
				self.fbButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.fbButton_do), 1);
			}
		};
		
		
		self.showFacebookButton = function(){
			if(!self.showFacebookButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.fbButton_do) == -1){
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slp_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slp_do) + 1, 0, self.fbButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slideShowButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slideShowButton_do) + 1, 0, self.fbButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.descButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do) + 1, 0, self.fbButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) + 1, 0, self.fbButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.fbButton_do);
				}else{
					self.buttons_ar.splice(0, 0, self.fbButton_do);
				}
			}
		};
		
		
		self.hideNextAndPrevButtons = function(){
			//if(!self.defaultShowNextAndPrevButtons_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.nextButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.nextButton_do);
				FWDRLTweenMax.killTweensOf(self.prevButton_do);
				self.prevButton_do.setX(-5000);
				self.nextButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.nextButton_do), 1);
			}
		};
		
		self.showNextAndPrevButtons = function(){
			if(!self.defaultShowNextAndPrevButtons_bl || !self.showNextAndPrevButtons_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.nextButton_do) == -1){
				if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.fbButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.fbButton_do) + 1, 0, self.nextButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slp_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slp_do) + 1, 0, self.nextButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.slideShowButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.slideShowButton_do) + 1, 0, self.nextButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.descButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.descButton_do) + 1, 0, self.nextButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.zoomButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.zoomButton_do) + 1, 0, self.nextButton_do);
				}else if(FWDRLUtils.indexOfArray(self.buttons_ar,  self.closeButton_do) != -1){
					self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.closeButton_do) + 1, 0, self.nextButton_do);
				}else{
					self.buttons_ar.splice(0, 0, self.nextButton_do);
				}
			}
		};
		
		self.hideHsThumbnailButton = function(){	
			//if(!self.showThumbnailsHideOrShowButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.hsThumbanilsButton_do) != -1){
				FWDRLTweenMax.killTweensOf(self.hsThumbanilsButton_do);
				self.hsThumbanilsButton_do.setX(-5000);
				self.buttons_ar.splice(FWDRLUtils.indexOfArray(self.buttons_ar, self.hsThumbanilsButton_do), 1);
			}
		};
		
		self.showHsThumbnailButton = function(){	
			if(!self.showThumbnailsHideOrShowButton_bl) return;
			if(FWDRLUtils.indexOfArray(self.buttons_ar, self.hsThumbanilsButton_do) == -1){
				self.buttons_ar.splice(self.buttons_ar.length , 0, self.hsThumbanilsButton_do);
			}
		};
		
		//#######################################//
		/* Position buttons */
		//######################################//
		self.positionButtons = function(animate){
			if(!self.isFirstItemShowed_bl || !self.isShowed_bl || !self.isReady_bl) return;
			var offsetY = 0;
			var totalButtonsHeight = 0;
			
			if(self.areThumbnailsShowed_bl){
				offsetY = Math.round((self.thumbnailH + self.spaceBetweenThumbnailsAndItem)/2 - self.spaceBetweenThumbnailsAndItem/2);
			}
		
			if(self.showNextAndPrevButtons_bl){
				if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
					self.prevButton_do.finalX = self.finalX - self.prevButton_do.w - self.buttonsOffsetIn;
				}else{
					self.prevButton_do.finalX = self.buttonsOffsetIn;
				}
				self.prevButton_do.finalY = parseInt((self.stageHeight - self.prevButton_do.h)/2) - offsetY;
				if(self.prevButton_do.finalX == undefined) self.prevButton_do.finalX = -5000;
				if(self.prevButton_do.finalY == undefined) self.prevButton_do.finalY = -5000;
			}
			
			var button;
			var prevButton;
			var totalButtons = self.buttons_ar.length;
			
			for(var j=0; j<totalButtons; j++){
				button = self.buttons_ar[j];
				totalButtonsHeight += button.h + self.spaceBetweenButtons;
			}
			totalButtonsHeight -= self.spaceBetweenButtons;
		
			for(var i=0; i<totalButtons; i++){
			
				button = self.buttons_ar[i];
				if(i != 0) prevButton = self.buttons_ar[i-1];
				if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
					button.finalX  = self.finalX + self.finalW + self.buttonsOffsetIn;
				}else{
					button.finalX  = self.stageWidth - button.w - self.buttonsOffsetIn;
				}
				
				if(totalButtonsHeight > self.finalH && self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
					if(i == 0){
						if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
							if(self.areThumbnailsShowed_bl){
								button.finalY = parseInt((self.stageHeight - totalButtonsHeight - self.thumbnailH)/2);
							}else{
								button.finalY = parseInt((self.stageHeight - totalButtonsHeight)/2);
							}
						}else{
							button.finalY = self.buttonsOffsetIn;
						}
					}else{
						button.finalY = prevButton.finalY + prevButton.h + self.spaceBetweenButtons;
					}
				}else{
					
					if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
						button.finalY = self.finalY;
					}else{
						button.finalY = self.buttonsOffsetIn;
					}
					
					if(prevButton){
						button.finalY = prevButton.finalY + prevButton.h + self.spaceBetweenButtons;
						if(button == self.nextButton_do){
							if(button.finalY < self.prevButton_do.finalY){
								button.finalY = self.prevButton_do.finalY;
							}
						}else if(button == self.hsThumbanilsButton_do){
							button.finalY = self.finalY + self.finalH - button.h;
							if(button.finalY < prevButton.finalY + prevButton.h + self.spaceBetweenButtons
								&& self.stageWidth < self.thumbnailsManager_do.totalW + ((button.w + self.buttonsOffsetIn) * 2)){
								button.finalY = prevButton.finalY + prevButton.h + self.spaceBetweenButtons;
							}
						}
					}else{
						if(button == self.nextButton_do){
							if(button.finalY < self.prevButton_do.finalY){
								button.finalY =  self.prevButton_do.finalY;
							}
						}else if(button == self.hsThumbanilsButton_do){
							button.finalY = self.finalY + self.finalH - button.h;
						}
					}
				}
				
				if(button == self.zoomButton_do && self.isMaximized_bl){
					button.finalX = self.stageWidth - button.w - 1;
					button.finalY = 1;
				}
				
				if(button == self.hsThumbanilsButton_do){
					if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
						if(button.finalY + button.h > self.stageHeight - self.thumbnailH && self.areThumbnailsShowed_bl){
							button.finalX = self.finalX - button.w - self.buttonsOffsetIn;
							button.finalY = self.finalY + self.finalH - button.h;
							if(self.showNextAndPrevButtons_bl
							   && button.finalY < self.prevButton_do.finalY + self.prevButton_do.h + self.spaceBetweenButtons){
								button.finalY = self.prevButton_do.finalY + self.prevButton_do.h + self.spaceBetweenButtons;
							}
							if(i == totalButtons -1){
								for(var k=0; k<totalButtons - 1; k++){
									self.buttons_ar[k].finalY += parseInt(self.hsThumbanilsButton_do.h/2);
								}
							}
						}
					}else{
						if(self.areThumbnailsShowed_bl){
							if(self.thumbnailsManager_do 
								&& self.stageWidth > self.thumbnailsManager_do.totalW + ((button.w + self.buttonsOffsetIn) * 2)){
								button.finalY = self.stageHeight - button.h - self.buttonsOffsetIn;
							}else{
								button.finalY = self.stageHeight - button.h - self.thumbnailH - self.buttonsOffsetIn;
							}
						}else{
							button.finalY = self.stageHeight - button.h - self.buttonsOffsetIn;
						}
						if(prevButton 
						   && prevButton.finalY + prevButton.h + button.h + self.spaceBetweenButtons + self.buttonsOffsetIn> self.stageHeight - self.thumbnailH && self.areThumbnailsShowed_bl
						   && self.stageWidth < self.thumbnailsManager_do.totalW + ((button.w + self.buttonsOffsetIn) * 2)){
							button.finalX = self.buttonsOffsetIn;
						}
					}
				}
				
				if(self.hider.isHidden_bl && button == self.slp_do){
					if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
						button.finalY = self.finalY;
					}else{
						button.finalY = self.buttonsOffsetIn;
					}	
				}
				
			}
			
			if(self.showNextAndPrevButtons_bl){
				if(animate){
					FWDRLTweenMax.killTweensOf(self.prevButton_do);
					FWDRLTweenMax.to(self.prevButton_do, .8, {x:self.prevButton_do.finalX, y:self.prevButton_do.finalY, ease:Expo.easeInOut});
				}else{
					FWDRLTweenMax.killTweensOf(self.prevButton_do);
					self.prevButton_do.setX(self.prevButton_do.finalX);
					self.prevButton_do.setY(self.prevButton_do.finalY);
				}
			}
			
			for(var i=0; i<totalButtons; i++){		
				button = self.buttons_ar[i];
				
				if(button.x != button.finalX || button.y != button.finalY){
					FWDRLTweenMax.killTweensOf(button);
					if(animate){ 
						FWDRLTweenMax.to(button, .8, {x:button.finalX, y:button.finalY, ease:Expo.easeInOut});
					}else{
						button.setX(button.finalX);
						button.setY(button.finalY);
					}
				}
			}
		};
		
		self.hideButtons = function(animate){
			if(!self.isReady_bl) return;
			var button;
			var totalButtons = self.buttons_ar.length;
	
			if(self.showNextAndPrevButtons_bl){
				self.prevButton_do.finalX = -self.prevButton_do.w;
				if(self.prevButton_do.finalX == undefined) self.prevButton_do.finalX = -1;
				if(self.prevButton_do.finalY == undefined) self.prevButton_do.finalY = -1;
			}
			
			for(var i=0; i<totalButtons; i++){
				button = self.buttons_ar[i];	
				
				if(!isNaN(button.finalX)){
					if(button.finalX > self.stageWidth/2){
						button.finalX  = self.stageWidth;
					}else{
						button.finalX  = - button.w;
					}
				}
				
				if(button.finalX === undefined) button.finalX = -5000;
				if(button.finalY === undefined) button.finalY = -5000;
				
				if(animate){
					if(i == 0 && self.showNextAndPrevButtons_bl){
						FWDRLTweenMax.killTweensOf(self.prevButton_do);
						FWDRLTweenMax.to(self.prevButton_do, .8, {alpha:1, x:self.prevButton_do.finalX, y:self.prevButton_do.finalY, ease:Expo.easeInOut});
					}
					FWDRLTweenMax.killTweensOf(button);
					FWDRLTweenMax.to(button, .8, {alpha:1, x:button.finalX, y:button.finalY, ease:Expo.easeInOut});
				}else{
					if(i == 0 && self.showNextAndPrevButtons_bl){
						FWDRLTweenMax.killTweensOf(self.prevButton_do);
						self.prevButton_do.setX(self.prevButton_do.finalX);
						self.prevButton_do.setY(self.prevButton_do.finalY);
					}
					FWDRLTweenMax.killTweensOf(button);
					button.setAlpha(1);
					button.setX(button.finalX);
					button.setY(button.finalY);
				}
			}
		};
		
		self.hideStuffForGood = function(){
			self.fbButton_do.setX(-5000);
			self.prevButton_do.setX(-5000);
			self.nextButton_do.setX(-5000);
			self.closeButton_do.setX(-5000);
			self.zoomButton_do.setX(-5000);
			self.descButton_do.setX(-5000);
			self.slideShowButton_do.setX(-5000);
			self.slp_do.setX(-5000);
			self.hsThumbanilsButton_do.setX(-5000);
			if(self.videoHolder_do){
				self.video_do.stop();
				self.videoHolder_do.setX(-5000);
				self.videoHolder_do.w = 1;
				self.videoHolder_do.h = 1;
			}
			
			if(self.audioHolder_do){
				self.audio_do.stop();
				self.audioHolder_do.setX(-5000);
				self.audioHolder_do.w = 1;
				self.audioHolder_do.h = 1;
			}
		};
		
		self.showButtonsWithFade = function(animate){
			if(!self.isReady_bl) return;
			if(animate){
				FWDRLTweenMax.to(self.nextButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.prevButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.closeButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.zoomButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.hsThumbanilsButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.descButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.slideShowButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.fbButton_do.buttonsHolder_do, .8, {alpha:1, ease:Quint.easeOut});
			}else{
				
				FWDRLTweenMax.killTweensOf(self.nextButton_do.buttonsHolder_do);
				FWDRLTweenMax.killTweensOf(self.prevButton_do.buttonsHolder_do);
				self.nextButton_do.buttonsHolder_do.setAlpha(1);
				self.prevButton_do.buttonsHolder_do.setAlpha(1);
			
				FWDRLTweenMax.killTweensOf(self.nextButton_do.closeButton_do);
				self.closeButton_do.buttonsHolder_do.setAlpha(1);
		
				FWDRLTweenMax.killTweensOf(self.zoomButton_do.closeButton_do);
				self.zoomButton_do.buttonsHolder_do.setAlpha(1);		
				
				FWDRLTweenMax.killTweensOf(self.hsThumbanilsButton_do.hsThumbanilsButton_do);
				self.hsThumbanilsButton_do.buttonsHolder_do.setAlpha(1);	
				
				FWDRLTweenMax.killTweensOf(self.descButton_do.descButton_do);
				self.descButton_do.buttonsHolder_do.setAlpha(1);	
				
				FWDRLTweenMax.killTweensOf(self.fbButton_do.descButton_do);
				self.fbButton_do.buttonsHolder_do.setAlpha(1);		
			}
		};
		
		self.hideButtonsWithFade = function(animate){
			if(animate){
				FWDRLTweenMax.to(self.nextButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.prevButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.closeButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.zoomButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.hsThumbanilsButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.descButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.slideShowButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.fbButton_do.buttonsHolder_do, .8, {alpha:0, ease:Quint.easeOut});
			}else{
				
				FWDRLTweenMax.killTweensOf(self.nextButton_do.buttonsHolder_do);
				FWDRLTweenMax.killTweensOf(self.prevButton_do.buttonsHolder_do);
				self.nextButton_do.buttonsHolder_do.setAlpha(0);
				self.prevButton_do.buttonsHolder_do.setAlpha(0);
			
				FWDRLTweenMax.killTweensOf(self.nextButton_do.closeButton_do);
				self.closeButton_do.buttonsHolder_do.setAlpha(0);
		
				FWDRLTweenMax.killTweensOf(self.zoomButton_do.closeButton_do);
				self.zoomButton_do.buttonsHolder_do.setAlpha(0);		
				
				FWDRLTweenMax.killTweensOf(self.hsThumbanilsButton_do.hsThumbanilsButton_do);
				self.hsThumbanilsButton_do.buttonsHolder_do.setAlpha(0);	
				
				FWDRLTweenMax.killTweensOf(self.hsThumbanilsButton_do.descButton_do);
				self.descButton_do.buttonsHolder_do.setAlpha(0);	
				
				FWDRLTweenMax.killTweensOf(self.slideShowButton_do.descButton_do);
				self.slideShowButton_do.buttonsHolder_do.setAlpha(0);
				

				FWDRLTweenMax.killTweensOf(self.fbButton_do.descButton_do);
				self.fbButton_do.buttonsHolder_do.setAlpha(0);
			}
		};
		
		this.parsePlaylistObject = function(obj, i, json){
			if(i == 0 && obj.thumbnailPath_str){
				self.areThumbnailsShowed_bl = false;
				
				self.setupThumbnailManager();	
				
				if(self.showThumbnailsByDefault_bl){
					self.thumbnailsManager_do.show(false);
					self.areThumbnailsShowed_bl = true;
				}else{
					self.thumbnailsManager_do.hide(false);
					self.areThumbnailsShowed_bl = false;
				}
				
				if(self.defaultShowThumbnails_bl){
					self.showThumbnails_bl = true;
				}else{
					self.showThumbnails_bl = false;
					self.areThumbnailsShowed_bl = false;
				}
				
				if(self.defaultShowThumbnailsHideOrShowButton_bl && self.defaultShowThumbnails_bl){
					self.showThumbnailsHideOrShowButton_bl = true;
				}else{
					self.showThumbnailsHideOrShowButton_bl = false;
				}
			}
		
			if(i == 0 && !obj.thumbnailPath_str){
				self.areThumbnailsShowed_bl = false;
				self.showThumbnails_bl = false;
				self.showThumbnailsHideOrShowButton_bl = false;
			}
			
			if(/\.jpg|\.jpeg|\.png/i.test(obj.type_str)){
				obj.iconType_str = FWDRLThumb.IMAGE;
				obj.type_str = FWDRL.IMAGE_TYPE;
				obj.width = undefined;
				obj.height = undefined;
			}else if(/\.mp4/i.test(obj.type_str)){
				obj.iconType_str = FWDRLThumb.VIDEO;
				obj.type_str = FWDRL.VIDEO_TYPE;
			}else if(/\.mp3/i.test(obj.type_str)){
				obj.type_str = FWDRL.AUDIO_TYPE;
				obj.iconType_str = FWDRLThumb.AUDIO;
			}else if(/\.swf/i.test(obj.type_str)){
				obj.type_str = FWDRL.FLASH_TYPE;
				obj.iconType_str = FWDRLThumb.FLASH;
			}else if(/youtube\.|vimeo\./i.test(obj.type_str)){
				if(obj.type_str.indexOf("youtube.") != -1){
					obj.iconType_str = FWDRLThumb.YOUTUBE;
				}else{
					obj.iconType_str = FWDRLThumb.VIMEO;
				}
				obj.type_str = FWDRL.IFRAME_TYPE;
			}else{
				
				if(obj.type_str.indexOf("google.") != -1){
					obj.iconType_str = FWDRLThumb.MAPS;
				}else if(obj.type_str.indexOf("RL_AJAX") != -1){
					obj.iconType_str = FWDRLThumb.AJAX;
				}else if(obj.type_str.indexOf("RL_HTML") != -1){
					obj.iconType_str = FWDRLThumb.HTML;
				}else{
					obj.iconType_str = FWDRLThumb.IFRAME;
				}
				obj.type_str = FWDRL.IFRAME_TYPE;	
			}
	
			if(obj.type_str == FWDRL.IMAGE_TYPE || obj.type_str == FWDRL.VIDEO_TYPE){
				var firstUrlPath = encodeURI(obj.url.substr(0,obj.url.lastIndexOf("/") + 1));
				var secondUrlPath = encodeURIComponent(obj.url.substr(obj.url.lastIndexOf("/") + 1));
				obj.url = firstUrlPath + secondUrlPath;
			}
			
			self.playlist_ar[i] = obj;
		};
		
		//############################################//
		/* Show / hide */
		//############################################//
		FWDRL.show = function(playlistDomOrObj, id, propsObjVariableName_str){
			if(self.isShowed_bl) return;
			
			FWDRL.dispatchEvent(FWDRL.SHOW_START, {obj:playlistDomOrObj});
			
			if(!playlistDomOrObj){
				var error_str = "Please sepecify a playlist";
				alert("Revolution lightbox error! Please specify a playlist in the FWDRL.show() method.");
				return;
			}
			
			//change props
			self.setDefaultSettings();
			if(propsObjVariableName_str && window[propsObjVariableName_str]){
				var props_obj = window[propsObjVariableName_str];
				self.setObjectPropsSettings(props_obj);
			}else{
				self.setDefaultSettings();
			}
		
			//generate playlists...
			self.playlistDomOrObj_str = playlistDomOrObj;
			self.playlist_ar = [];
			
			if(playlistDomOrObj.indexOf("rlobj_") != -1){
				var playlistObj = window[playlistDomOrObj];
				if(!playlistObj){
					alert("Revolution lightbox error! The playlist JSON object with the label \"" + playlistDomOrObj + "\" doesn't exist!");
					return;
				}
				
				self.totalItems = playlistObj.playlistItems.length;
				
				var countPlaylistItems = 0;
				var dumyDiv = document.createElement("div");
				for(var i=0; i<self.totalItems; i++){
					var obj = {};
					var ch = playlistObj.playlistItems[i];
					obj.url = ch.url;
					obj.thumbnailPath_str = ch.thumbnailPath;
					obj.type_str = ch.url;
					obj.description = ch.description;
						
					if(obj.url.indexOf("RL_HTML") == -1){
						if(obj.description) dumyDiv.innerHTML = obj.description;
						if(obj.description){
							dumyDiv.innerHTML = obj.description;
							obj.descriptionText = dumyDiv.innerText;
						}
					}else{
						dumyDiv.innerHTML = ch.html;
						obj.html = dumyDiv.innerHTML;
					}
			
					obj.width = ch.width;
					obj.height = ch.height;
					
					self.parsePlaylistObject(obj, i, true);
					
					if(obj.type_str == FWDRL.AUDIO_TYPE){
						obj.height = self.data.audioControllerHeight + (self.itemBorderSize * 2);
					}
				}
				dumyDiv = null;
			}else{
				var playlistElement = document.getElementById(playlistDomOrObj);
				if(!playlistElement){
					alert("Revolution lightbox error! The HTML element with the id \"" + playlistDomOrObj + "\" doesn't exist!");
					return;
				}
				
				var ch_ar = FWDRLUtils.getChildren(playlistElement);
				self.totalItems = ch_ar.length;
				
				if(self.totalItems == 0){
					alert("Revolution lightbox error! The playlist with the id \"" + playlistDomOrObj + "\" must contain at least one entry.");
					return
				}
				
				for(var i=0; i<self.totalItems; i++){
					var obj = {};
					var ch = ch_ar[i];
					var test;
					
					if(!FWDRLUtils.hasAttribute(ch, "data-url")){
						alert("Revolution lightbox error! Attribute \"data-url\" is not found in the playlist at position nr: \"" + i + "\".");
						return;
					}
					
					obj.url = String(FWDRLUtils.getAttributeValue(ch, "data-url"));
					obj.posterPath = FWDRLUtils.getAttributeValue(ch, "data-poster-path");
					obj.type_str = FWDRLUtils.getAttributeValue(ch, "data-url");
					obj.width = FWDRLUtils.getAttributeValue(ch, "data-width");
					obj.height = FWDRLUtils.getAttributeValue(ch, "data-height");
					if(FWDRLUtils.hasAttribute(ch, "data-thumbnail-path")){
						obj.thumbnailPath_str = FWDRLUtils.getAttributeValue(ch, "data-thumbnail-path");
					}
					
					if(obj.url.indexOf("RL_HTML") == -1){
						try{
							if(FWDRLUtils.getChildren(ch).length != 0){
								obj.description = ch.innerHTML;
								obj.descriptionText = ch.innerText;
							}
						}catch(e){};
					}else{
						try{obj.html = ch.innerHTML;}catch(e){};
					}
					
					self.parsePlaylistObject(obj, i, false);
					
					if(obj.type_str == FWDRL.AUDIO_TYPE){
						obj.height = self.data.audioControllerHeight + (self.itemBorderSize * 2);
					}
				}
			}
			
			if(self.totalItems == 1){
				self.showNextAndPrevButtons_bl = false;
			}else{
				if(self.defaultShowNextAndPrevButtons_bl){
					self.showNextAndPrevButtons_bl = true;
				}else{
					self.showNextAndPrevButtons_bl = false;
				}
			}
			
			if(id){
				self.id = parseInt(id);
				if(self.id < 0){
					self.id = 0;
				}else if(self.id > self.totalItems -1){
					self.id = self.totalItems - 1;
				}
			}else{
				self.id = 0;
			}
			self.prevId = self.id;
			
			self.so = FWDRLUtils.getScrollOffsets();
			//window.scrollTo(self.so.x, self.so.y);
			
			if(self.useDeepLinking_bl){
				if(propsObjVariableName_str){
					location.hash = "RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + propsObjVariableName_str;	 
				}else{
					location.hash = "RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id;	
				}
			}
			
			//show...
			self.isShowed_bl = true;
			self.isAnim_bl = true;
			self.showSlideShowAnimation_bl = false;
			self.showDescription_bl = self.defaultShowDescriptionByDefault_bl;
			
			self.startResizeHandler();
			self.addPreventMouseWheel();
			
			clearTimeout(self.showOrHideCompleteId_to);
			self.showOrHideCompleteId_to = setTimeout(self.showComplete, 401);
			FWDRLTweenMax.to(self.bk_do, .8, {alpha:self.backgroundOpacity, ease:Quint.easeOut});
			
			if(self.preloader_do){
				self.positionPreloader();
				self.preloader_do.show(true);
			}
			self.main_do.addChild(self.disableClick_do);
			
			if(self.isReady_bl){	
				self.hideButtons();
				self.hideStuffForGood();
			}
			
			
			self.desc_do.hide(false, true, true);
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.destoryThumbnails();
		};
		
	
		self.showComplete = function(){
			if(!self.isReady_bl || self.id == -1 || self.curItem_do) return;	
			
			self.positionPreloader();
			self.preloader_do.show(true);
			
			if(self.showCloseButton_bl){
				self.showCloseButton();
			}else{
				self.hideCloseButton();
			}
			
			if(self.playlist_ar[self.id].type_str == FWDRL.IMAGE_TYPE && self.defaultShowZoomButton_bl){
				self.showZoomButton();
			}else{
				self.hideZoomButton();
			}
			
			if(self.playlist_ar[self.id].description && self.defaultHideDescriptionButtons_bl){
				self.hasItemDescription_bl = true;
				self.showDescriptionButton();
			}else{
				self.hasItemDescription_bl = false;
				self.hideDescriptionButton();
			}
			
			if(self.showSlideShowButton_bl){
				self.showSlideshowButton();
			}else{
				self.hideSlideshowButton();
			}			
			if(self.showFacebookButton_bl){
				self.showFacebookButton();
			}else{
				self.hideFacebookButton();
			}
		
			if(self.showNextAndPrevButtons_bl){
				self.showNextAndPrevButtons();
			}else{
				self.hideNextAndPrevButtons();
			}
			
			if(self.showThumbnailsHideOrShowButton_bl && self.showThumbnails_bl){
				self.showHsThumbnailButton();
				if(self.showThumbnailsByDefault_bl){
					self.hsThumbanilsButton_do.setButtonState(1);
				}else{
					self.hsThumbanilsButton_do.setButtonState(0);
				}
			}else{
				self.hideHsThumbnailButton();
			}
		
			if(self.showDescription_bl){
				self.descButton_do.setButtonState(0);
			}else{
				self.descButton_do.setButtonState(1);
			}
			
			self.hideButtons();
			self.createAndShowItem();
			
			if(!self.useAsModal_bl){
				self.addCloseEventsWhenBkIsPressed();
			}else{
				self.removeCloseEventsWhenBkIsPressed();
			}
			if(self.isMobile_bl) self.addSwipeSupport();
			self.startAnim(801);
		};
		
		self.hide = function(){
			if(self.isAnim_bl 
			   || !self.isShowed_bl 
			   || self.isAnimMaximizeOrMinimize_bl 
			   || self.isMaximized_bl
			   || self.swipeMoved_bl) return;
			
		
			if(self.isMobile_bl && self.closeButton_do && FWDRLTweenMax.isTweening(self.closeButton_do.buttonsHolder_do)) return;
			
			FWDRLTweenMax.to(self.bk_do, .8, {alpha:0, delay:.4, ease:Quint.easeOut});
			
			if(self.curItem_do && self.curItem_do.screen){
				FWDRLTweenMax.to(self.curItem_do, .6, {alpha:0, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.curItem_do, .8, {x:0, y:0, w:0, h:0, delay:.1, ease:Expo.easeInOut});
			}
			
			FWDRLTweenMax.to(self.mainItemHolder_do, .8, {x:self.stageWidth/2, y:self.stageHeight/2, w:0, h:0, delay:.1, ease:Expo.easeInOut});
			FWDRLTweenMax.to(self.itemBorder_do, .8, {w:0, h:0, alpha:0, delay:.1, ease:Expo.easeInOut});
			FWDRLTweenMax.to(self.itemBk_do, .8, {x:0, y:0, w:0, h:0, delay:.1, ease:Expo.easeInOut});
			
			self.isShowed_bl = false;
			self.isFirstItemShowed_bl = false;
			self.id == -1;
			self.curItem_do = null;
			self.prevItem_do = null;
			self.isAnimForVideoAndAudioPlayersDone_bl = false;
			self.stopResizeHandler();
			self.closeAjax();
			self.tm.stop();
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.hide(true);
			if(self.main_do.contains(self.info_do)) self.main_do.removeChild(self.info_do);
			self.closeImage();
			if(!self.useAsModal_bl) self.removeCloseEventsWhenBkIsPressed();
			self.hider.stop();
			self.preloader_do.hide(true);
			self.hideButtons(true);
			if(self.videoHolder_do){
				self.video_do.stop();
				self.video_do.setPosterSource("");
				self.videoHolder_do.setX(-5000);
				self.videoHolder_do.w = 1;
				self.videoHolder_do.h = 1;
			}
			
			if(self.audioHolder_do){
				self.audio_do.stop();
				self.audioHolder_do.setX(-5000);
				self.audioHolder_do.w = 1;
				self.audioHolder_do.h = 1;
			}
			self.desc_do.descriptionAnimationType_str = "opacity";
			FWDRL.dispatchEvent(FWDRL.HIDE_START);
			if(self.hasItemDescription_bl && self.showDescription_bl) self.desc_do.hide(true);
			clearTimeout(self.showOrHideCompleteId_to);
			self.showOrHideCompleteId_to = setTimeout(self.hideComplete, 1200);
			if(self.isMobile_bl) self.removeSwipeSupport();
			self.startAnim(1202);
		};
		
		self.hideComplete = function(){
			//window.scrollTo(self.so.x, self.so.y);
			if(self.useDeepLinking_bl) location.hash = "RL";
			//window.scrollTo(self.so.x, self.so.y);
			self.removePreventMouseWheel();
		
			self.isFirstItemShowed_bl = false;
			self.firstVideoOrAudioAdded_bl = false;
			self.curItem_do = null;
			self.prevItem_do = null;
			self.removeItems(0);
			if(self.thumbnailsManager_do){
				self.thumbnailsManager_do.destoryThumbnails();
				self.thumbnailsManager_do.hideForGood();
			}
			if(self.video_do && RLVideoPlayer) RLVideoPlayer.setPosterSource("");
			if(self.isMobile_bl) self.removeSwipeSupport();
			self.main_do.setX(-5000);
			//self.main_do.setY(-5000);
			FWDRL.dispatchEvent(FWDRL.HIDE_COMPLETE);
		};
		
		self.startAnim = function(delay){
			self.stopAnim();
			self.isAnim_bl = true;
			self.animId_to = setTimeout(self.animationDone, delay);
		};
		
		self.stopAnim = function(){
			self.isAnim_bl = false;
			if(self.tm) self.tm.pause();
			clearTimeout(self.animId_to);
		};
		
		self.animationDone = function(){
			self.isAnim_bl = false;
			self.tm.resume();
			self.removeItems(1);
			self.dlChangeHandler();
			if(self.hasItemDescription_bl && self.showDescription_bl) self.desc_do.show(true);	
		};
		
		self.addCloseEventsWhenBkIsPressed = function(){
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.bk_do.screen.addEventListener("MSPointerUp", self.onBkMouseUp);
				}else{
					self.bk_do.screen.addEventListener("touchend", self.onBkMouseUp);
					self.bk_do.screen.addEventListener("touchmove", self.onBkTouchMove);
				}
			}else if(self.bk_do.screen.addEventListener){	
				self.bk_do.screen.addEventListener("click", self.onBkMouseUp);
			}else if(self.bk_do.screen.attachEvent){
				self.bk_do.screen.attachEvent("onclick", self.onBkMouseUp);
			}
		};
		
		self.removeCloseEventsWhenBkIsPressed = function(){
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.bk_do.screen.removeEventListener("MSPointerUp", self.onBkMouseUp);
				}else{
					self.bk_do.screen.removeEventListener("touchend", self.onBkMouseUp);
					self.bk_do.screen.removeEventListener("touchmove", self.onBkTouchMove);
				}
			}else if(self.bk_do.screen.removeEventListener){	
				self.bk_do.screen.removeEventListener("click", self.onBkMouseUp);
			}else if(self.bk_do.screen.detachEvent){
				self.bk_do.screen.detachEvent("onclick", self.onBkMouseUp);
			}
		};
		
		
		self.onBkTouchMove = function(){
			clearTimeout(self.doNotAllowToHideId_to);
			self.doNotAllowToHideId_to = setTimeout(function(){self.doNotAllowToHide_bl = false;}, 100);
			self.doNotAllowToHide_bl = true;
		};
		
		self.onBkMouseUp = function(){
			if(self.doNotAllowToHide_bl) return
			self.hide();
		};
		
		
		//###################################//
		/* show item */
		//###################################//
		self.createAndShowItem = function(){
			
			var curPlaylistItem = self.playlist_ar[self.id];
			var isHttpS_bl;
			
			self.type_str = curPlaylistItem.type_str;
			self.url = curPlaylistItem.url;
			self.posterPath_str = curPlaylistItem.posterPath;
			
			self.closeAjax();
			self.tm.pause();
			self.closeImage();
			self.preloader_do.hide(true);
			if(self.main_do.contains(self.info_do)) self.main_do.removeChild(self.info_do);
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.disableOrEnableThumbnails();
			
			if((self.prevItem_do && self.prevItem_do.type_str != FWDRL.IMAGE_TYPE) ){
				self.removeItems(0);
				self.prevItem_do = null;
			}
			
			if(self.playlist_ar[self.id].description){
				self.hasItemDescription_bl = true;
				self.showDescriptionButton();
			}else{
				self.hasItemDescription_bl = false;
				self.hideDescriptionButton();
				self.desc_do.hide(false, false, true);
			}
				
			if(self.videoHolder_do){
				self.video_do.stop();
				if(self.type_str != FWDRL.VIDEO_TYPE){
					self.videoHolder_do.setX(-5000);
					self.videoHolder_do.w = 1;
					self.videoHolder_do.h = 1;
				}
			}
			
			if(self.audioHolder_do){		
				self.audio_do.stop();
				if(self.type_str != FWDRL.AUDIO_TYPE || !self.isFirstItemShowed_bl){
					self.audioHolder_do.setX(-5000);
					self.audioHolder_do.w = 1;
					self.audioHolder_do.h = 1;
				}
			}
			
			self.isAnimForVideoAndAudioPlayersDone_bl = false;
			
		
			if(self.type_str == FWDRL.IMAGE_TYPE){
				self.loadImage();
				self.firstVideoOrAudioAdded_bl = true;
			}else if(self.type_str == FWDRL.IFRAME_TYPE
					 || self.type_str == FWDRL.FLASH_TYPE
					 || self.type_str == FWDRL.VIDEO_TYPE
					 || self.type_str == FWDRL.AUDIO_TYPE){
				
				
				self.originalW = curPlaylistItem.width || self.defaultItemW;
				self.originalH = curPlaylistItem.height || self.defaultItemH;
				
				if(self.prevItem_do){
						self.resizeCurrentItem(true);
						FWDRLTweenMax.to(self.prevItem_do, .8, {alpha:0, ease:Quint.easeOut});
						FWDRLTweenMax.to(self.prevItem_do, .8, {
							x:parseInt((self.finalW - self.prevItem_do.w)/2), 
							y:parseInt((self.finalH - self.prevItem_do.h)/2), 
							ease:Expo.easeInOut});
				}
				
				self.curItem_do = new FWDRLDisplayObject("div");
				self.curItem_do.type_str = self.type_str;
				self.prevItem_do = self.curItem_do;
				if(self.isMobile_bl){
					self.curItem_do.getStyle().overflow = "scroll";
					self.curItem_do.getStyle().webkitOverflowScrolling = "touch";
				}
				self.itemHolder_do.addChild(self.curItem_do);
				
				if(!self.isFirstItemShowed_bl){
					self.resizeCurrentItem(false);
					self.showItemFirstTime();
					self.positionButtons(false);
					self.hideButtons();
					self.setupThumbnails(800);
				}else{
					self.resizeCurrentItem(false, true);
				}
				
				self.hideZoomButton();	
				
				if(self.playlist_ar[self.id].description){
					self.hasItemDescription_bl = true;
					self.desc_do.setText(self.playlist_ar[self.id].description);
					self.showDescriptionButton();
				}else{
					self.hasItemDescription_bl = false;
					self.hideDescriptionButton();
				}
				if(self.descriptionAnimationType_str == "opacity" && self.hasItemDescription_bl) self.desc_do.hide(false, true, false);
				
				
				self.positionButtons(true);
				
				if(self.type_str == FWDRL.VIDEO_TYPE){
					if(!self.data.DFUseVideo_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("To play video mp4 files please set <font color='#FFFFFF'>useVideo:\"yes\"</font>.");
						return
					}
					if(!FWDRLFlashTest.hasFlashPlayerVersion("9.0.18") && !FWDRLUtils.isLocal && !self.isMobile_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("Please install Adobe flash player! <a href='http://www.adobe.com/go/getflashplayer'>Click here to install.</a> to play this mp4 video file.");
						return
					}
					if(!self.videoHolder_do){
						if(FWDRLUtils.isLocal){
							self.main_do.addChild(self.info_do);
							self.info_do.showText("This browser can't play mp4 video files locally, please use a different browser like Chrome, IE9+, Firefox(WIN), Safari(MAC). It will work on all browsers when tested online.");
							return;
						}	
					}
				
					if(self.videoHolder_do.w == self.finalW - (self.itemBorderSize * 2)
					   && self.videoHolder_do.h == self.finalH - (self.itemBorderSize * 2)){
						setTimeout(self.addContent, 200);	
						self.startAnim(201);
						if(self.showSlideShowAnimation_bl) self.slp_do.animReset();
					}else{
						setTimeout(self.addContent, 800);
						self.startAnim(801);
					}
				}else if(self.type_str == FWDRL.AUDIO_TYPE){
					if(!self.data.DFUseAudio_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("To play audio mp3 files please set <font color='#FFFFFF'>useAudio:\"yes\"</font>.");
						return
					}
					if(!FWDRLFlashTest.hasFlashPlayerVersion("9.0.18") && !FWDRLUtils.isLocal && !self.isMobile_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("Please install Adobe flash player! <a href='http://www.adobe.com/go/getflashplayer'>Click here to install.</a> to play this mp3 audio file.");
						return
					}
					if(!self.audioHolder_do){
						if(FWDRLUtils.isLocal){
							self.main_do.addChild(self.info_do);
							self.info_do.showText("This browser can't play mp3 audio files locally, please use a different browser like Chrome, IE9+, Firefox(WIN), Safari(MAC). It will work on all browsers when tested online.");
							return;
						}
					}
						
					if(self.audioHolder_do.w == self.finalW - (self.itemBorderSize * 2)
					   && self.audioHolder_do.h == self.finalH - (self.itemBorderSize * 2)){
						setTimeout(self.addContent, 200);	
						self.startAnim(201);
						if(self.showSlideShowAnimation_bl) self.slp_do.animReset();
					}else{
						setTimeout(self.addContent, 800);
						self.startAnim(801);
					}
					
				}else if(self.type_str == FWDRL.IFRAME_TYPE){
					setTimeout(self.addContent, 800);
					self.startAnim(801);
				}else if(self.type_str == FWDRL.FLASH_TYPE){
					if(!FWDRLFlashTest.hasFlashPlayerVersion("9.0.18") && !self.isMobile_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("Please install Adobe flash player! <a href='http://www.adobe.com/go/getflashplayer'>Click here to install.</a> to view this flash content.");
						self.startAnim(801);
						return
					}
					
					if(self.isMobile_bl){
						self.main_do.addChild(self.info_do);
						self.info_do.showText("Adobe flash player is not supported on mobile devices, to view this content please use a desktop machine.");
						self.startAnim(801);
						return;
					}
					setTimeout(self.addContent, 800);
					self.startAnim(801);
				} 
				
				if(self.videoHolder_do){
					if(self.videoHolder_do.w != self.finalW - (self.itemBorderSize * 2)
					   || self.videoHolder_do.h != self.finalH - (self.itemBorderSize * 2)){
						self.videoHolder_do.setX(-5000);
						self.videoHolder_do.w = 1;
						self.videoHolder_do.h = 1;
					}
				}
				
			}
			FWDRL.dispatchEvent(FWDRL.UPDATE, {curId:self.id});
		};
		
		//###########################################//
		/* Add  content */
		//###########################################//
		self.addContent = function(){
			
			if(self.type_str == FWDRL.VIDEO_TYPE){
				self.isAnimForVideoAndAudioPlayersDone_bl = true;
				RLVideoPlayer.setVideoSource(self.url);
				RLVideoPlayer.setPosterSource(self.posterPath_str);
				
				if(self.videoAutoPlay_bl && !self.firstVideoOrAudioAdded_bl){
					RLVideoPlayer.play();
				}else if(self.nextVideoOrAudioAutoPlay_bl && self.firstVideoOrAudioAdded_bl){
					RLVideoPlayer.play();
				}
			
				self.resizeCurrentItem();
				self.prevVideoW = self.finalW;
				self.prevVideoH = self.finalH;
				self.firstVideoOrAudioAdded_bl = true;
				self.videoAutoPlay_bl = false;
				self.audioAutoPlay_bl = false;
				return
			};
			
			if(self.type_str == FWDRL.AUDIO_TYPE){
				self.isAnimForVideoAndAudioPlayersDone_bl = true;
				RLAudioPlayer.setSource(self.url);
				if(self.audioAutoPlay_bl && !self.firstVideoOrAudioAdded_bl){
					RLAudioPlayer.play();
				}else if(self.nextVideoOrAudioAutoPlay_bl && self.firstVideoOrAudioAdded_bl){
					RLAudioPlayer.play();
				}
				self.resizeCurrentItem();
				self.firstVideoOrAudioAdded_bl = true;
				self.audioAutoPlay_bl = false;
				self.videoAutoPlay_bl = false;
				return
			};
			
			if(self.type_str == FWDRL.FLASH_TYPE){
				var flashObjectMarkup_str = '<object id="RL_swf_' + parseInt((Math.random() * 99999999999)) + '" classid="clsid:d27cdb6e-ae6d-11cf-96b8-444553540000" width="100%" height="100%"><param name="movie" value="' + self.url + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/><object type="application/x-shockwave-flash" data="' + self.url + '" width="100%" height="100%"><param name="movie" value="' + self.url + '"/><param name="wmode" value="opaque"/><param name="scale" value="noscale"/></object></object>';
				self.curItem_do.setInnerHTML(flashObjectMarkup_str);
				self.resizeCurrentItem();
				return;
			}
			
			if(self.url.indexOf("RL_HTML") != -1){
				self.addInnerHTMLContent(self.playlist_ar[self.id].html);
				self.resizeCurrentItem();
				return
			};
			
			if(self.url.indexOf("RL_AJAX:") != -1){
				if(FWDRLUtils.isLocal){
					self.ajaxLoadError("Using ajax locally is not possible or allowed, please test online.");
					return;
				}
				
				self.url = self.url.substr(self.url.indexOf(":") + 1);
			
				self.xmlhttp = new XMLHttpRequest();
				self.xmlhttp.onerror = function(){self.ajaxLoadError("Ajax error with code: " + self.xmlhttp.status);};
				
				self.xmlhttp.onreadystatechange=function(){
					if (self.xmlhttp.readyState === 4){
						if(self.xmlhttp.status == 200){
							self.addInnerHTMLContent(self.xmlhttp.responseText);
						}else{
							self.ajaxLoadError("Ajax error with code: " + self.xmlhttp.status);
						}
					}
				};
				
				self.xmlhttp.open("GET", self.url, true);
				try{
					self.xmlhttp.send();
				}catch(e){
					if(e.message) self.ajaxLoadError(e.message);
				}
				return
			};
			
			var iFrame;
			var protocol = "http://";
			if(self.url.indexOf("https") != -1) protocol = "https://";
			if(self.nextVideoOrAudioAutoPlay_bl && self.firstVideoOrAudioAdded_bl) self.videoAutoPlay_bl = true;
			var videoAutoPlay_str = self.videoAutoPlay_bl ? "1" : "0";
			
			self.firstVideoOrAudioAdded_bl = true;
		
			var videoId_str;
			
			iFrame = document.createElement("iframe");
			iFrame.width = "100%";
			iFrame.height = "100%";
			iFrame.allowFullScreen  = 1;
			iFrame.setAttribute('allowFullScreen', '');
			iFrame.frameBorder = 0;
			
			if(self.url.indexOf("youtube.") != -1 || self.url.indexOf("vimeo.") != -1){
				if(self.url.indexOf("youtube.") != -1){
					videoId_str = self.url.replace(/.*\?v=|&.*/ig, "");
					iFrame.src = protocol + "www.youtube.com/embed/" + videoId_str + "?wmode=transparent&autoplay=" + videoAutoPlay_str;
				}else if(self.url.indexOf("vimeo.") != -1){
					videoId_str = self.url.replace(/.*\/|\?.*/ig, "");
					iFrame.src = protocol + "player.vimeo.com/video/" + videoId_str + "?autoplay=" + videoAutoPlay_str;
				}
				self.videoAutoPlay_bl = false;
			}else{
				if(self.url.indexOf("google.") != -1){
					self.url = self.url.replace(/&key=\.*|key=\.*|&key=*/ig, "");
					self.url += "&key=" + self.googleMapsAPIKey_str;
					iFrame.src = self.url;
				}else{
					iFrame.src = self.url;
				}
			
			}
		
			self.curItem_do.screen.appendChild(iFrame);
			
			self.resizeCurrentItem();
		};
		
		self.addInnerHTMLContent = function(htmlContent){
			self.curItem_do.getStyle().overflow = "auto";
			self.curItem_do.setInnerHTML(htmlContent);
			
			if(self.curItem_do.screen.addEventListener){
				self.curItem_do.screen.addEventListener ("mousewheel", function(e){
					if(e.stopImmediatePropagation) e.stopImmediatePropagation();
				});
				self.curItem_do.screen.addEventListener('DOMMouseScroll', function(e){
					if(e.stopImmediatePropagation) e.stopImmediatePropagation();
				});
				self.curItem_do.screen.addEventListener("touchmove", function(e){
					if(self.curItem_do.screen.scrollHeight > self.finalH - self.itemBorderSize * 2) e.stopImmediatePropagation();
				});
			}
		};
		
		self.ajaxLoadError = function(message){
			self.tm.stop();
			self.stopAnim();
			self.preloader_do.hide(true);
			self.main_do.addChild(self.info_do);
			self.info_do.showText(message);
		};
		
		self.closeAjax = function(){
			if(self.xmlhttp){
				self.xmlhttp.onerror = null;
				self.xmlhttp.onreadystatechange = null;
				self.xmlhttp.abort();
				self.xmlhttp = null;
			}
		};
		
	
		//###########################################//
		/* load image */
		//###########################################//
		self.closeImage = function(){
			if(self.image_img){
				self.image_img.onload = null;
				self.image_img.onerror = null;
				self.image_img = null;
			}
		};
		
		self.loadImage = function(){
			self.isLoading_bl = true;
			self.stopAnim();
			self.positionPreloader();
			self.preloader_do.show(true);
			
			
			self.image_img = new Image();
			self.image_img.onload = self.imageLoadComplete;
			self.image_img.onerror = self.imageLoadError;
			self.image_img.src = self.url;
		};
		
		self.imageLoadComplete = function(e){
			self.originalW = self.image_img.width;
			self.originalH = self.image_img.height;
			
			self.curItem_do = new FWDRLDisplayObject("img");
			self.curItem_do.setScreen(self.image_img);
			self.curItem_do.type_str = FWDRL.IMAGE_TYPE;
			
			if(!self.isFirstItemShowed_bl){
				self.resizeCurrentItem(false);
				self.showItemFirstTime();
				self.positionButtons(false);
				self.hideButtons();
				self.setupThumbnails(800);
			}else{
				self.resizeCurrentItem(true, false);
				if(self.prevItem_do){
					if(self.prevItem_do.type_str == FWDRL.IMAGE_TYPE){
						FWDRLTweenMax.to(self.prevItem_do, .8, {alpha:0, ease:Quint.easeOut});
						FWDRLTweenMax.to(self.prevItem_do, .8, {
							x:parseInt((self.finalW - self.prevItem_do.w)/2), 
							y:parseInt((self.finalH - self.prevItem_do.h)/2), 
							ease:Expo.easeInOut});
					}
				}
				self.curItem_do.setWidth(self.finalW- (self.itemBorderSize * 2));
				self.curItem_do.setHeight(self.finalH - (self.itemBorderSize * 2));
				self.curItem_do.setAlpha(0);
				self.resizeCurrentItem(false, true);
				FWDRLTweenMax.to(self.curItem_do, .8, {alpha:1, delay:.8, ease:Quint.easeOut});
			}
			
			self.startAnim(801);
			
			self.isLoading_bl = false;
			self.prevItem_do = self.curItem_do;
			self.preloader_do.hide(true);
			self.showZoomButton();
			
			if(self.hasItemDescription_bl){
				if(self.descriptionAnimationType_str == "opacity" && self.hasItemDescription_bl) self.desc_do.hide(false, true, false);
				self.showDescriptionButton();
				self.desc_do.setText(self.playlist_ar[self.id].description);
				
			}
		
			self.positionButtons(true);
			
			self.itemHolder_do.addChild(self.curItem_do);
		};
		
		self.imageLoadError = function(e){
			self.tm.stop();
			self.stopAnim();
			self.preloader_do.hide(true);
			self.main_do.addChild(self.info_do);
			self.info_do.showText("Image with path <span style='color:#FFFFFF;'>" + decodeURIComponent(self.url) + "</span> can't be loaded, probably the path is incorrect.");
		};
	
		//####################################//
		/* maximize / minimize image */
		//####################################//
		this.maximizeOrMinimize = function(){
			if(self.isLoading_bl || self.isAnim_bl) return;
			
			var scaleX;
			var scaleY;
			var finalX;
			var finalY;
			var finalW;
			var finalH;
			var totalScale;
			
			self.isAnimMaximizeOrMinimize_bl = true;
			
			clearTimeout(self.maximizeCompleteTimeOutId_to);
			clearTimeout(self.minimizeCompleteTimeOutId_to);
			
			if(self.isMaximized_bl){
				self.isMaximized_bl = false;
				self.zoomButton_do.setButtonState(1);
				
				if(self.isMobile_bl){
					self.removeEventsForScrollngImageOnMobile();
				}else{
					self.removeEventsForScrollngImageOnDesktop();
				}
				
				FWDRLTweenMax.to(self.curItem_do, .8, {
					x:self.finalX + self.itemBorderSize, 
					y:self.finalY + self.itemBorderSize, 
					w:self.finalW - (self.itemBorderSize * 2), 
					h:self.finalH - (self.itemBorderSize * 2), 
					ease:Expo.easeInOut});
				
				self.setButtonsVisible(true);
				
				self.positionButtons(true);
				self.minimizeCompleteTimeOutId_to = setTimeout(self.minimizeCompleteHandler, 801);
			}else{
				self.isMaximized_bl = true;
				self.zoomButton_do.setButtonState(0);
				self.tm.pause();
				
				scaleX = self.stageWidth/self.originalW;
				scaleY = self.stageHeight/self.originalH;
				totalScale = 0;
				if(scaleX >= scaleY){
					totalScale = scaleX;
				}else if(scaleX <= scaleY){
					totalScale = scaleY;
				}
				finalW = parseInt(self.originalW * totalScale);
				finalH = parseInt(self.originalH * totalScale);
				finalX = parseInt((self.stageWidth - finalW)/2);
				finalY = parseInt((self.stageHeight - finalH)/2);
				
				if(self.curItem_do.alpha != 1) self.curItem_do.setAlpha(1);			
				self.curItem_do.setX(self.curItem_do.getGlobalX());
				self.curItem_do.setY(self.curItem_do.getGlobalY());
				
				FWDRLTweenMax.to(self.zoomButton_do, .8, {x:self.stageWidth - self.zoomButton_do.w - 1, y:1, ease:Expo.easeInOut});
				
				if(self.isMobile_bl){
					FWDRLTweenMax.to(self.curItem_do, .8, { x:finalX, y:finalY, w:finalW, h:finalH, ease:Expo.easeInOut});
				}else{
					if(scaleX >= scaleY){
						FWDRLTweenMax.to(self.curItem_do, .8, {x:finalX, w:finalW, h:finalH, ease:Expo.easeInOut});
					}else if(scaleX < scaleY){
						FWDRLTweenMax.to(self.curItem_do, .8, {y:finalY, w:finalW, h:finalH, ease:Expo.easeInOut});
					}
					self.addEventsForScrollngImageOnDesktop();
				}
				
				if(self.itemHolder_do.contains(self.imteHolder_do)) self.itemHolder_do.removeChild(self.curItem_do);
				//self.zoomButton_do.disableHover();
				self.main_do.addChild(self.curItem_do);
				self.main_do.addChild(self.zoomButton_do);
				self.maximizeCompleteTimeOutId_to = setTimeout(self.maximizeCompleteHandler, 801);
			}
		};
		
		self.minimizeCompleteHandler = function(){
			self.isAnimMaximizeOrMinimize_bl = false;
			self.isTweening_bl = false;
			self.itemHolder_do.addChild(self.curItem_do);
			self.resizeCurrentItem();
			self.tm.resume();
			
			//self.zoomButton_do.enableHover();
			//if(!FWDRLUtils.hitTest(self.zoomButton_do.screen, self.gmx, self.gmy)){
			//	self.zoomButton_do.setNormalState();
			//}
			if(self.hasItemDescription_bl && self.showDescription_bl) self.desc_do.show(true);
			self.main_do.addChild(self.zoomButton_do);
			if(self.useDeepLinking_bl) self.dlChangeHandler();
		};
		
		self.maximizeCompleteHandler = function(){
			self.isAnimMaximizeOrMinimize_bl = false;
			self.setButtonsInvisible(true);
			if(self.isMobile_bl) self.addEventsForScrollngImageOnMobile();
			if(self.hasItemDescription_bl && self.showDescription_bl) self.desc_do.hide(false);
		};
		
		self.setButtonsInvisible = function(applyToMainHolder){
			if(self.showCloseButton_bl) self.closeButton_do.setVisible(false);
			if(self.showNextAndPrevButtons_bl){
				self.nextButton_do.setVisible(false);
				self.prevButton_do.setVisible(false);
			}
			if(self.showThumbnailsHideOrShowButton_bl) self.hsThumbanilsButton_do.setVisible(false);
			if(self.showThumbnails_bl) self.thumbnailsManager_do.setVisible(false);
			if(self.showDescriptionButton_bl) self.descButton_do.setVisible(false);
			if(self.showSlideShowButton_bl)  self.slideShowButton_do.setVisible(false);
			if(self.showFacebookButton_bl) self.fbButton_do.setVisible(false);
			if(self.showSlideShowAnimation_bl) self.slp_do.setVisible(false);
			if(self.showDescription_bl) self.desc_do.setVisible(false);
			if(applyToMainHolder) self.mainItemHolder_do.setVisible(false);
		};
		
		self.setButtonsVisible = function(applyToMainHolder){
			if(self.showCloseButton_bl) self.closeButton_do.setVisible(true);
			if(self.showNextAndPrevButtons_bl){
				self.nextButton_do.setVisible(true);
				self.prevButton_do.setVisible(true);
			}
			if(self.showThumbnailsHideOrShowButton_bl) self.hsThumbanilsButton_do.setVisible(true);
			if(self.showThumbnails_bl) self.thumbnailsManager_do.setVisible(true);
			if(self.showDescriptionButton_bl) self.descButton_do.setVisible(true);
			if(self.showSlideShowButton_bl)  self.slideShowButton_do.setVisible(true);
			if(self.showFacebookButton_bl) self.fbButton_do.setVisible(true);
			if(self.showSlideShowAnimation_bl) self.slp_do.setVisible(true);
			if(self.showDescription_bl) self.desc_do.setVisible(true);
			if(applyToMainHolder) self.mainItemHolder_do.setVisible(true);
		}
		
		//##############################################//
		/* Add events to pan the image on pc */
		//##############################################//
		this.addEventsForScrollngImageOnDesktop = function(){
			self.updateImageWhenMaximized_int = setInterval(self.updateMaximizedImageHandler, 16);
			if(window.addEventListener){
				window.addEventListener("mousemove", self.updateMaximizeImageOnMouseMovedHandler);
			}else{
				document.attachEvent("onmousemove", self.updateMaximizeImageOnMouseMovedHandler);
			}
			self.hider.stop();
		};
		
		this.removeEventsForScrollngImageOnDesktop = function(){
			clearInterval(self.updateImageWhenMaximized_int);
			if(window.addEventListener){
				window.removeEventListener("mousemove", self.updateMaximizeImageOnMouseMovedHandler);
			}else{
				document.detachEvent("onmousemove", self.updateMaximizeImageOnMouseMovedHandler);
			}
			self.hider.start();
		};
	
		this.updateMaximizeImageOnMouseMovedHandler = function(e){
			var vmc = FWDRLUtils.getViewportMouseCoordinates(e);
		
			self.gmx = vmc.screenX;
			self.gmy = vmc.screenY;
		};
		
		self.updateMaximizedImageHandler = function(){
			
			var targetX;
			var targetY;
			
			self.percentX = self.gmx/self.stageWidth;
			self.percentY = self.gmy/self.stageHeight;
			if(self.percentX > 1) self.percentX = 1;
			if(self.percentY > 1) self.percentY = 1;
			
			var scaleX = self.stageWidth/self.originalW;
			var scaleY = self.stageHeight/self.originalH;
		
			if(scaleX <= scaleY){
				targetX = Math.round(((self.stageWidth - self.curItem_do.w) * self.percentX));
				if(isNaN(targetX)) return;
				FWDRLTweenMax.to(self.curItem_do, .4, {x:targetX});
			}else {
				targetY = Math.round(((self.stageHeight - self.curItem_do.h) * self.percentY));
				if(isNaN(targetY)) return;
				FWDRLTweenMax.to(self.curItem_do, .4, {y:targetY});
			}
		};
		
		//##############################################//
		/* add events to scroll the image on mobile */
		//##############################################//
		self.addEventsForScrollngImageOnMobile = function(){
			if(self.hasPointerEvent_bl){
				window.addEventListener("MSPointerDown", self.onTouchStartScrollImage);
				window.addEventListener("MSPointerUp", self.onTouchEndScrollImage);
			}else{
				window.addEventListener("touchstart", self.onTouchStartScrollImage);
				window.addEventListener("touchend", self.onTouchEndScrollImage);
			}
		
			clearInterval(self.updateImageWhenMaximized_int);
			self.updateImageWhenMaximized_int = setInterval(self.updateMaximizedImageMobileHandler, 16);
		};
		
		self.removeEventsForScrollngImageOnMobile = function(){
			clearInterval(self.updateImageWhenMaximized_int);
			if(self.hasPointerEvent_bl){
				window.removeEventListener("MSPointerDown", self.onTouchStartScrollImage);
				window.removeEventListener("MSPointerUp", self.onTouchEndScrollImage);
				window.removeEventListener("MSPointerMove", self.onTouchMoveScrollImage);
			}else{
				window.removeEventListener("touchstart", self.onTouchStartScrollImage);
				window.removeEventListener("touchend", self.onTouchEndScrollImage);	
				window.removeEventListener("touchmove", self.onTouchMoveScrollImage);
			}
			self.isDragging_bl = false;
		};
		
		self.onTouchStartScrollImage =  function(e){
			var vc = FWDRLUtils.getViewportMouseCoordinates(e);	
			if(self.hasPointerEvent_bl){
				window.addEventListener("MSPointerMove", self.onTouchMoveScrollImage);
			}else{
				window.addEventListener("touchmove", self.onTouchMoveScrollImage);
			}
			
			self.lastPresedX = vc.screenX;
			self.lastPresedY = vc.screenY;
			
			e.preventDefault();
		};
		
		self.onTouchEndScrollImage = function(e){
			if(self.hasPointerEvent_bl){
				window.removeEventListener("MSPointerMove", self.onTouchMoveScrollImage);
			}else{
				window.removeEventListener("touchmove", self.onTouchMoveScrollImage);
			}
			self.isDragging_bl = false;
		};
		
		self.onTouchMoveScrollImage = function(e){
			if(e.preventDefault) e.preventDefault();
			
			var vc = FWDRLUtils.getViewportMouseCoordinates(e);	
			var scaleX = self.stageWidth/self.originalW;
			var scaleY = self.stageHeight/self.originalH;
			var toAddX = 0;
			var toAddY = 0;
			self.isDragging_bl = true;	
			
			if(scaleX < scaleY){
				//x
				toAddX = vc.screenX - self.lastPresedX;
				self.lastPresedX = vc.screenX;
				self.curItem_do.setX(self.curItem_do.x + toAddX);
			}else if(scaleX > scaleY){
				//y
				toAddY = vc.screenY - self.lastPresedY;
				self.lastPresedY = vc.screenY;
				self.curItem_do.setY(self.curItem_do.y + toAddY);
			}else{
				toAddX = vc.screenX - self.lastPresedX;
				self.lastPresedX = vc.screenX;
				self.curItem_do.setX(self.curItem_do.x + toAddX);
				
				toAddY = vc.screenY - self.lastPresedY;
				self.lastPresedY = vc.screenY;
				self.curItem_do.setY(self.curItem_do.y + toAddY);
			}
			
			self.vx = toAddX  * 2;
			self.vy = toAddY  * 2;
		};
		
		self.updateMaximizedImageMobileHandler = function(){
			
			var tempX;
			var tempY;
			var curX;
			var curY;
			var tempW;
			var tempH;
			
			if(!self.isDragging_bl){
				
				self.vy *= self.friction;
				self.vx *= self.friction;
				curX = self.curItem_do.x;
				curY = self.curItem_do.y;
				tempX = curX +  self.vx;
				tempY = curY +  self.vy;
				tempW = self.curItem_do.w;
				tempH = self.curItem_do.h;
				
				if(isNaN(tempX) || isNaN(tempY)) return;
				
				self.curItem_do.setX(tempX);
				self.curItem_do.setY(tempY);
				
				if(curY >= 0){
					self.vy2 = (0 - curY) * .3;
					self.vy *= self.friction;
					self.curItem_do.setY(curY + self.vy2);
				}else if(curY <= self.stageHeight - tempH){
					self.vy2 = (self.stageHeight - tempH - curY) * .3;
					self.vy *= self.friction;
					self.curItem_do.setY(curY + self.vy2);
				}
				
				if(curX >= 0){
					self.vx2 = (0 - curX) * .3;
					self.vx *= self.friction;
					self.curItem_do.setX(curX + self.vx2);
				}else if(curX <= self.stageWidth - tempW){
					self.vx2 = (self.stageWidth - tempW - curX) * .3;
					self.vx *= self.friction;
					self.curItem_do.setX(curX + self.vx2);
				}
			}
		};
		
		//####################################//
		/* resize current item */
		//####################################//
		self.resizeCurrentItem = function(onlySetData, animate){	
			if(!self.curItem_do) return;
		
			var containerWidth = self.stageWidth - (self.maxButtonW * 2) - ((self.buttonsOffsetIn + self.buttonsOffsetOut) *  2) - (self.itemBorderSize * 2);
			var containerHeight = self.stageHeight - self.itemOffsetH - (self.itemBorderSize * 2);
			var offsetY = 0;
			
			if(self.areThumbnailsShowed_bl){
				containerHeight -= self.thumbnailH + self.spaceBetweenThumbnailsAndItem;
				offsetY = Math.round((self.thumbnailH + self.spaceBetweenThumbnailsAndItem)/2 - self.spaceBetweenThumbnailsAndItem/2);
			}
			
			var scaleX = containerWidth/self.originalW;
			var scaleY = containerHeight/self.originalH;
			var totalScale = 0;
			
			if(scaleX <= scaleY){
				totalScale = scaleX;
			}else if(scaleX >= scaleY){
				totalScale = scaleY;
			}
			
			if(scaleX >= 1 && scaleY >=1) totalScale = 1;
			
			self.finalW = Math.round((self.originalW * totalScale)) + (self.itemBorderSize * 2);
			self.finalH = Math.round((self.originalH * totalScale)) + (self.itemBorderSize * 2);
			
			if(self.finalW < self.itemBorderSize * 2) self.finalW = self.itemBorderSize * 2;
			if(self.finalH < self.itemBorderSize * 2) self.finalH = self.itemBorderSize * 2;
			
			if(FWDRLUtils.isIEAndLessThen9){
				if(self.finalW < 150) self.finalW = 150;
				if(self.finalH < 150) self.finalH = 150;
			}
			
			if(self.type_str == FWDRL.AUDIO_TYPE && self.audioHolder_do) self.finalH = self.data.audioControllerHeight + (self.itemBorderSize * 2);
			
			self.finalX = Math.round((self.stageWidth  -  self.finalW)/2);
			self.finalY = Math.round((self.stageHeight - self.finalH)/2) - offsetY;
		
			if(onlySetData) return;
			
			FWDRLTweenMax.killTweensOf(self.mainItemHolder_do);
			FWDRLTweenMax.killTweensOf(self.itemBk_do);
			FWDRLTweenMax.killTweensOf(self.itemBorder_do);
			if(animate){
				FWDRLTweenMax.to(self.mainItemHolder_do, .8, {
					x:self.finalX, 
					y:self.finalY, 
					w:self.finalW, 
					h:self.finalH, 
					ease:Expo.easeInOut});
				
				FWDRLTweenMax.to(self.itemBk_do, .8, {
					x:self.itemBorderSize, 
					y:self.itemBorderSize, 
					w:self.finalW - (self.itemBorderSize * 2), 
					h:self.finalH - (self.itemBorderSize * 2), 
					ease:Expo.easeInOut});
				
				FWDRLTweenMax.to(self.itemBorder_do, .8, {
					x:0, 
					y:0, 
					w:self.finalW, 
					h:self.finalH, 
					ease:Expo.easeInOut});
				
				if(self.desc_do){
					FWDRLTweenMax.to(self.desc_do, .8, {
						finalW:self.finalW - (self.itemBorderSize * 2),
						onUpdate:self.desc_do.resizeAndPosition,
						ease:Expo.easeInOut});
				}
				
				if(self.type_str == FWDRL.VIDEO_TYPE && self.videoHolder_do){
					if(self.isAnimForVideoAndAudioPlayersDone_bl){
						
						FWDRLTweenMax.to(self.videoHolder_do, .8, {
							x:self.itemBorderSize,
							y:self.itemBorderSize,
							w:self.finalW - (self.itemBorderSize * 2),
							h:self.finalH - (self.itemBorderSize * 2),
							onUpdate:RLVideoPlayer.resizeHandler,
							ease:Expo.easeInOut});
					}
				}
			}else{
				self.mainItemHolder_do.setX(self.finalX);
				self.mainItemHolder_do.setY(self.finalY);
				self.mainItemHolder_do.setWidth(self.finalW);
				self.mainItemHolder_do.setHeight(self.finalH);
				
				self.itemBk_do.setX(self.itemBorderSize);
				self.itemBk_do.setY(self.itemBorderSize);
				self.itemBk_do.setWidth(self.finalW - (self.itemBorderSize * 2));
				self.itemBk_do.setHeight(self.finalH - (self.itemBorderSize * 2));
				
				self.itemBorder_do.setX(0);
				self.itemBorder_do.setY(0);
				self.itemBorder_do.setWidth(self.finalW);
				self.itemBorder_do.setHeight(self.finalH);
				if(self.itemBorder_do.alpha != 1) self.itemBorder_do.setAlpha(1);
				
				if(self.desc_do){
					self.desc_do.resizeAndPosition(self.finalW - (self.itemBorderSize * 2));	
				}
				
				if(self.type_str == FWDRL.VIDEO_TYPE && self.videoHolder_do){
					if(self.isAnimForVideoAndAudioPlayersDone_bl){
						if(self.isVideoFullScreen_bl){
							self.videoHolder_do.setX(-self.finalX);
							self.videoHolder_do.setY(-self.finalY);
						}else{
							self.videoHolder_do.setX(self.itemBorderSize);
							self.videoHolder_do.setY(self.itemBorderSize);
						}
						self.videoHolder_do.setWidth(self.finalW - (self.itemBorderSize * 2));
						self.videoHolder_do.setHeight(self.finalH - (self.itemBorderSize * 2));
						RLVideoPlayer.resizeHandler();
					}
				}else if(self.type_str == FWDRL.AUDIO_TYPE && self.audioHolder_do){
					if(self.isAnimForVideoAndAudioPlayersDone_bl){
						self.audioHolder_do.setX(self.itemBorderSize);
						self.audioHolder_do.setY(self.itemBorderSize);
						self.audioHolder_do.setWidth(self.finalW - (self.itemBorderSize * 2));
						self.audioHolder_do.setHeight(self.finalH - (self.itemBorderSize * 2));
						RLAudioPlayer.resizeHandler();
					}
				}
			}
			
			FWDRLTweenMax.killTweensOf(self.curItem_do);
		
			if(self.isMaximized_bl){
				
				scaleX = self.stageWidth/self.originalW;
				scaleY = self.stageHeight/self.originalH;
				
				if(scaleX >= scaleY){
					totalScale = scaleX;
				}else if(scaleX <= scaleY){
					totalScale = scaleY;
				}
				
				self.curItem_do.setX(parseInt((self.stageWidth - (self.originalW * totalScale))/2));
				self.curItem_do.setY(parseInt((self.stageHeight - (self.originalH * totalScale))/2));
				self.curItem_do.setWidth(Math.max(0,parseInt(self.originalW * totalScale)));
				self.curItem_do.setHeight(Math.max(0, parseInt(self.originalH * totalScale)));
			}else{
				if(animate){
					FWDRLTweenMax.to(self.curItem_do, .8,{
						x:self.itemBorderSize, 
						y:self.itemBorderSize,
						w:self.finalW - (self.itemBorderSize * 2),
						h:self.finalH - (self.itemBorderSize * 2), 
						ease:Expo.easeInOut});
					
				}else{
					if(self.type_str == FWDRL.IMAGE_TYPE){
						self.curItem_do.setAlpha(1);
					}
					self.curItem_do.setX(self.itemBorderSize);
					self.curItem_do.setY(self.itemBorderSize);
					self.curItem_do.setWidth(self.finalW - (self.itemBorderSize * 2));
					self.curItem_do.setHeight(self.finalH - (self.itemBorderSize * 2));
				}
			}
		};
		
		//####################################//
		/* Show / go to items */
		//####################################//
		self.showItemFirstTime = function(){
			
			self.isFirstItemShowed_bl = true;
		
			self.mainItemHolder_do.setX(self.stageWidth/2);
			self.mainItemHolder_do.setY(self.stageHeight/2);
			self.mainItemHolder_do.setWidth(0);
			self.mainItemHolder_do.setHeight(0);
			self.itemBk_do.setX(0);
			self.itemBk_do.setY(0);
			self.itemBk_do.setWidth(0);
			self.itemBk_do.setHeight(0);
			
			if(self.curItem_do.type_str == FWDRL.IMAGE_TYPE){
				self.curItem_do.setAlpha(0);
				self.curItem_do.setX(-self.finalW/2 + self.itemBorderSize);
				self.curItem_do.setY(-self.finalH/2 + self.itemBorderSize);
				FWDRLTweenMax.to(self.curItem_do, .8, {alpha:1, delay:.8, ease:Quint.easeOut});
				FWDRLTweenMax.to(self.curItem_do, .8, {x:self.itemBorderSize, y:self.itemBorderSize, ease:Expo.easeInOut});
				self.startAnim(1601);
			}
			
			
			FWDRLTweenMax.to(self.mainItemHolder_do, .8, {x:self.finalX, y:self.finalY, w:self.finalW, h:self.finalH, ease:Expo.easeInOut});
			self.itemBorder_do.setAlpha(0);
			FWDRLTweenMax.to(self.itemBorder_do, .8, {alpha:1, x:0, y:0, w:self.finalW, h:self.finalH, ease:Expo.easeInOut});
			
			FWDRLTweenMax.to(self.itemBk_do, .8, {
				x:self.itemBorderSize, 
				y:self.itemBorderSize,
				w:self.finalW - (self.itemBorderSize * 2), 
				h:self.finalH - (self.itemBorderSize * 2),
				ease:Expo.easeInOut
				});
		
			self.hider.start();
			setTimeout(function(){
				if(self.slideShowAutoPlay_bl) self.tm.start();
				FWDRL.dispatchEvent(FWDRL.SHOW_COMPLETE);
			}, 800);
			
			if(self.addKeyboardSupport_bl){
				self.addKeyboardSupport();
			}else{
				self.removeKeyboardSupport();
			}
		};
		
		self.gotoToItem = function(id){
			if(!self.isReady_bl || !self.isFirstItemShowed_bl || self.isAnim_bl) return;
			if(!self.isMobile_bl) self.disableClick();
			self.id  = id;
		
			if(self.useDeepLinking_bl){
				if(self.propsObjVariableName_str){
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + self.propsObjVariableName_str);
				}else{
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id);
				}
			}else{
				self.createAndShowItem();
			}
		};
		
		self.gotoNextItem = function(){
			if(!self.isReady_bl || !self.isFirstItemShowed_bl || self.isAnim_bl) return;
			if(!self.isMobile_bl) self.disableClick();
			self.id ++;
			if(self.id < 0){
				self.id = self.totalItems - 1;
			}else if(self.id > self.totalItems - 1){
				self.id = 0;
			}
			
			if(self.useDeepLinking_bl){
				if(self.propsObjVariableName_str){
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + self.propsObjVariableName_str);
				}else{
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id);
				}
			}else{
				self.createAndShowItem();
			}
		};
		
		self.gotoPrevItem = function(){
			if(!self.isReady_bl || !self.isFirstItemShowed_bl || self.isAnim_bl) return;
			if(!self.isMobile_bl) self.disableClick();
			self.id --;
			if(self.id < 0){
				self.id = self.totalItems - 1;
			}else if(self.id > self.totalItems - 1){
				self.id = 0;
			}
			
			if(self.useDeepLinking_bl){
				if(self.propsObjVariableName_str){
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id + "&rl_propsobj=" + self.propsObjVariableName_str);
				}else{
					FWDAddress.setValue("RL?rl_playlist=" + self.playlistDomOrObj_str + "&rl_id=" + self.id);
				}
			}else{
				self.createAndShowItem();
			}
		};
		
		self.removeItems = function(index){
			var child;
			var inChild;
			while(self.itemHolder_do.getNumChildren() > index){
				child = self.itemHolder_do.getChildAt(0);
				FWDRLTweenMax.killTweensOf(child);
				self.itemHolder_do.removeChild(child);
				child.destroy();
			};
			child = null;
		};
		
		//############################################//
		/* Add swipe support */
		//############################################//
		self.addSwipeSupport = function(){	
			if(self.hasPointerEvent_bl){
				self.main_do.screen.addEventListener("MSPointerDown", self.swipeStartHandler);
			}else{
				self.main_do.screen.addEventListener("touchstart", self.swipeStartHandler);
			}
		};
		
		self.removeSwipeSupport = function(){	
			if(self.hasPointerEvent_bl){
				window.removeEventListener("MSPointerDown", self.swipeStartHandler);
				window.removeEventListener("MSPointerUp", self.swipeUpHandler);
				window.removeEventListener("MSPointerMove", self.swipeMoveHandler);
			}else{
				window.removeEventListener("touchstart", self.swipeStartHandler);
				window.removeEventListener("touchend", self.swipeUpHandler);
				window.removeEventListener("touchmove", self.swipeMoveHandler);
			}
		
			self.swipeMoved_bl = false;
		};
		
		this.swipeStartHandler = function(e){
			if (e.touches) if(e.touches.length != 1) return;
			var vmc = FWDRLUtils.getViewportMouseCoordinates(e);	
			self.swipeMoved_bl = false;
			self.mouseX = vmc.screenX;;
			self.mouseY = vmc.screenY;
			if(self.hasPointerEvent_bl){
				window.addEventListener("MSPointerUp", self.swipeUpHandler);
				window.addEventListener("MSPointerMove", self.swipeMoveHandler);
			}else{
				window.addEventListener("touchend", self.swipeUpHandler);
				window.addEventListener("touchmove", self.swipeMoveHandler);
			}
		};
		
		self.swipeMoveHandler = function(e){
			if(e.preventDefault) e.preventDefault();
			if(self.isClickedDisabled_bl || (e.touches && e.touches.length != 1)) return;
			self.swipeMoved_bl = true;
			var viewportMouseCoordinates = FWDRLUtils.getViewportMouseCoordinates(e);
			self.dif = self.mouseX - viewportMouseCoordinates.screenX;
			self.mouseX = viewportMouseCoordinates.screenX;
			self.mouseY = viewportMouseCoordinates.screenY;
		};
		
		
		self.swipeUpHandler = function(e){
			if(self.isAnim_bl || self.isAnimMaximizeOrMinimize_bl || self.isMaximized_bl) return;
			var sensitivity;
			if(FWDRLUtils.isApple){
				sensitivity = 20;
			}else{
				sensitivity = 4;
			}
			
			if(self.dif > sensitivity){
				if(!self.isClickedDisabled_bl) self.gotoNextItem();
			}else if(self.dif < -sensitivity){
				if(!self.isClickedDisabled_bl) self.gotoPrevItem();
			}
			
			self.dif = 0;
			
			if(self.hasPointerEvent_bl){
				window.removeEventListener("MSPointerUp", self.swipeUpHandler);
				window.removeEventListener("MSPointerMove", self.swipeMoveHandler);
			}else{
				window.removeEventListener("touchend", self.swipeUpHandler);
				window.removeEventListener("touchmove", self.swipeMoveHandler);
			}
		};
		
		//###########################################//
		/* Add keyboard support */
		//###########################################//
		self.addKeyboardSupport = function(){
			if(self.hasKeyboardSupport_bl) return; 
			self.hasKeyboardSupport_bl = true;
			if(document.addEventListener){
				document.addEventListener("keydown",  self.onKeyDownHandler);	
				document.addEventListener("keyup",  self.onKeyUpHandler);	
			}else{
				document.attachEvent("onkeydown",  self.onKeyDownHandler);	
				document.attachEvent("onkeyup",  self.onKeyUpHandler);	
			}
		};
		
		self.removeKeyboardSupport = function(){
			if(!self.hasKeyboardSupport_bl) return; 
			self.hasKeyboardSupport_bl = false;
			if(document.removeEventListener){
				document.removeEventListener("keydown",  self.onKeyDownHandler);	
				document.removeEventListener("keyup",  self.onKeyUpHandler);	
			}else{
				document.detachEvent("onkeydown",  self.onKeyDownHandler);	
				document.detachEvent("onkeyup",  self.onKeyUpHandler);	
			}
		};
		
		self.onKeyDownHandler = function(e){
			if(document.removeEventListener){
				document.removeEventListener("keydown",  self.onKeyDownHandler);	
			}else{
				document.detachEvent("onkeydown",  self.onKeyDownHandler);	
			}
			
			if(e.keyCode == 39){	
				self.gotoNextItem();
				if(e.preventDefault) e.preventDefault();
				return false;
			}else if(e.keyCode == 37){
				self.gotoPrevItem();
				if(e.preventDefault) e.preventDefault();
				return false;
			}
			
		
		};
		
		this.onKeyUpHandler = function(e){
			if(document.addEventListener){
				document.addEventListener("keydown",  self.onKeyDownHandler);	
			}else{
				document.attachEvent("onkeydown",  self.onKeyDownHandler);	
			}
		};
		
		//###################################################//
		/* Set default settings */
		//###################################################//
		self.setDefaultSettings = function(){
			self.buttonsAlignment_str = self.DFButtonsAlignment_str;
			
			self.defaultItemW = self.DFDefaultItemW;
			self.defaultItemH = self.DFDefaultItemH;
			
			self.descriptionWindowPosition_str = self.DFDescriptionWindowPosition_str;
			if(self.desc_do) self.desc_do.position_str = self.descriptionWindowPosition_str;
			
			self.descriptionAnimationType_str = self.DFDescriptionAnimationType_str;
			if(self.desc_do) self.desc_do.descriptionAnimationType_str = self.descriptionAnimationType_str;
			
			self.backgroundColor_str = self.DFBackgroundColor_str;
			self.bk_do.getStyle().backgroundColor = self.backgroundColor_str;
			
			self.itemBorderColor_str  = self.DFitemBorderColor_str;
			if(self.itemBorder_do) self.itemBorder_do.getStyle().backgroundColor = self.DFitemBorderColor_str;
			
			self.spaceBetweenButtons  = self.DFSpaceBetweenButtons;
			
			self.buttonsHideDelay = self.DFbuttonsHideDelay;
			if(self.hider) self.hider.hideDelay = self.buttonsHideDelay;
			
			self.nextVideoOrAudioAutoPlay_bl = self.DFNextVideoOrAudioAutoPlay_bl;
			
			self.useAsModal_bl = self.DFUseAsModal_bl;
			self.slideShowAutoPlay_bl = self.DFSlideShowAutoPlay_bl;
			self.videoAutoPlay_bl = self.DFVideoAutoPlay_bl;
			self.audioAutoPlay_bl = self.DFAudioAutoPlay_bl;
			self.addKeyboardSupport_bl = self.DFSddKeyboardSupport_bl;
			self.showCloseButton_bl = self.DFShowCloseButton_bl;
			self.showFacebookButton_bl = self.DFShowFacebookButton_bl;
			self.defaultShowZoomButton_bl = self.DFShowZoomButton;
			self.showSlideShowButton_bl = self.DFShowSlideShowButton_bl;
			self.defaultShowSlideShowAnimation_bl = self.DFSefaultShowSlideShowAnimation_bl;
			self.defaultShowNextAndPrevButtons_bl = self.DFSefaultShowNextAndPrevButtons_bl;
			self.slideShowDelay = self.DFSlideShowDelay;
			if(self.tm) self.tm.delay = self.slideShowDelay;
			if(self.slp_do) self.slp_do.duration = self.slideShowDelay/1000;
			self.itemOffsetH  = self.DFItemOffsetH;
			self.buttonsOffsetIn = self.DFButtonsOffsetIn;
			self.buttonsOffsetOut = self.DFButtonsOffsetOut;
			self.itemBorderSize = self.DFItemBorderSize;
			if(self.desc_do) self.desc_do.margins = self.itemBorderSize;
			self.itemBorderRadius = self.DFItemBorderRadius;
			if(!self.itemBorderRadius){
				self.mainItemHolder_do.getStyle().borderRadius = "";
			}else{
				self.mainItemHolder_do.getStyle().borderRadius = self.itemBorderRadius + "px";
			}
			self.backgroundOpacity = self.DFBackgroundOpacity;
			self.itemBoxShadow_str = self.DFItemBoxShadow_str;
			if(self.itemBoxShadow_str == "none"){
				self.mainItemHolder_do.getStyle().boxShadow = "none";
			}else{
				self.mainItemHolder_do.getStyle().boxShadow = self.itemBoxShadow_str;
			}
			self.itemBkColor_str  = self.DFItemBkColor_str;
			self.itemBk_do.getStyle().backgroundColor = self.itemBkColor_str;
			self.defaultShowThumbnails_bl = self.DFDefaultThumbnails_bl;
			self.defaultShowThumbnailsHideOrShowButton_bl = self.DFDefaultShowThumbnailsHideOrShowButton_bl;
			self.showThumbnailsByDefault_bl = self.DFShowThumbnailsByDefault_bl;
			self.showThumbnailsOverlay_bl = self.DFShowThumbnailsOverlay_bl;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.showThumbnailsOverlay_bl = self.showThumbnailsOverlay_bl;
			self.showThumbnailsSmallIcon_bl = self.DFShowThumbnailsSmallIcon_bl;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.showThumbnailsSmallIcon_bl = self.showThumbnailsSmallIcon_bl;
			self.thumbnailsOffsetBottom = self.DFThumbnailsOffsetBottom;
			self.thumbnailH = self.DFThumbnailH;
			if(self.thumbnailsManager_do){
				self.thumbnailsManager_do.thumbnailsOffsetBottom = self.thumbnailsOffsetBottom;
				self.thumbnailsManager_do.thumbnailH = self.thumbnailH - self.thumbnailsOffsetBottom;
				self.thumbnailsManager_do.stageHeight = self.thumbnailH;
			}
			self.thumbnailsBorderSize = self.DFThumbnailsBorderSize;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderSize = self.thumbnailsBorderSize;
			self.thumbnailsBorderRadius = self.DFThumbnailsBorderRadius;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderRadius = self.thumbnailsBorderRadius;
			self.spaceBetweenThumbnailsAndItem = self.DFSpaceBetweenThumbnailsAndItem;
			self.spaceBetweenThumbnails = self.DFSpaceBetweenThumbnails;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.spaceBetweenThumbnails = self.spaceBetweenThumbnails;
			self.thumbnailsOverlayOpacity = self.DFThumbnailsOverlayOpacity;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsOverlayOpacity = self.thumbnailsOverlayOpacity;
			self.thumbnailsOverlayColor_str = self.DFThumbnailsOverlayColor_str;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsOverlayColor_str = self.thumbnailsOverlayColor_str;
			self.thumbnailsBorderNormalColor_str = self.DFThumbnailsBorderNormalColor;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderNormalColor_str = self.thumbnailsBorderNormalColor_str;
			self.thumbnailsBorderSelectedColor_str = self.DFThumbnailsBorderSelectedColor_str ;
			if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderSelectedColor_str = self.thumbnailsBorderNormalColor_str;
			self.defaultHideDescriptionButtons_bl = self.DFDefaultHideDescriptionButtons_bl;
			self.defaultShowDescriptionByDefault_bl = self.DFDefaultShowDescriptionByDefault_bl;
			self.showDescription_bl = self.defaultShowDescriptionByDefault_bl;
			self.descriptionWindowBackgroundColor_str = self.DFDescriptionWindowBackgroundColor;
			
			if(self.desc_do){
				self.desc_do.backgroundColor_str = self.descriptionWindowBackgroundColor_str;
				self.desc_do.bk_do.setBkColor(self.descriptionWindowBackgroundColor_str);
			}
			self.descriptionWindowBackgroundOpacity = self.DFDescriptionWindowBackgroundOpacity;
			if(self.desc_do){
				self.desc_do.backgroundOpacity = self.descriptionWindowBackgroundOpacity;
				self.desc_do.bk_do.setAlpha(self.desc_do.backgroundOpacity);
			}
			
			self.data.videoControllerBackgroundColor_str = self.DFVideoControllerBackgroundColor_str;
			self.data.videoPosterBackgroundColor_str = self.DFVideoPosterBackgroundColor_str;
			self.data.videoPosterBackgroundColor_str = self.DFVideoPosterBackgroundColor_str;
			
			
			if(self.video_do && self.video_do.controller_do){
				self.video_do.controller_do.mainHolder_do.getStyle().backgroundColor = self.data.videoControllerBackgroundColor_str;
				self.video_do.videoPoster_do.getStyle().backgroundColor = self.data.videoPosterBackgroundColor_str;
			}
			
			self.data.audioControllerBackgroundColor_str = self.DFAudioControllerBackgroundColor_str;
			if(self.audio_do && self.audio_do.controller_do) self.audio_do.controller_do.getStyle().backgroundColor = self.data.audioControllerBackgroundColor_str;
		};
		
		//################################################//
		/* Set setings based on object */
		//################################################//
		self.setObjectPropsSettings = function(props_obj){
			var test;
			for(var prop in props_obj){
				switch(prop){
					case "defaultItemWidth":
						self.defaultItemW = props_obj.defaultItemWidth || 640;
						break;
					case "defaultItemHeight":
						self.defaultItemH = props_obj.defaultItemHeight || 380;
						break;
					case "buttonsAlignment":
						self.buttonsAlignment_str = props_obj.buttonsAlignment || "in";
						var test = self.buttonsAlignment_str == "in" 
							   || self.buttonsAlignment_str == "out";
						if(!test) self.buttonsAlignment_str = "in";
						break;
					case "descriptionWindowPosition":
						self.descriptionWindowPosition_str = props_obj.descriptionWindowPosition || "top";
						test = self.descriptionWindowPosition_str == "top" 
							   || self.descriptionWindowPosition_str == "bottom";
						if(!test) self.descriptionWindowPosition_str = "top";
						if(self.desc_do) self.desc_do.position_str = self.descriptionWindowPosition_str;
						break;
					case "showDescriptionButton":
						self.defaultHideDescriptionButtons_bl = props_obj.showDescriptionButton;
						self.defaultHideDescriptionButtons_bl = self.defaultHideDescriptionButtons_bl == "yes" ? true : false;
						break;
					case "showDescriptionByDefault":
						self.defaultShowDescriptionByDefault_bl = props_obj.showDescriptionByDefault;
						self.defaultShowDescriptionByDefault_bl = self.defaultShowDescriptionByDefault_bl == "yes" ? true : false;
						self.showDescription_bl = self.defaultShowDescriptionByDefault_bl;
						break;
					case "descriptionWindowAnimationType":
						self.descriptionAnimationType_str = props_obj.descriptionWindowAnimationType || "motion";
						test = self.descriptionAnimationType_str == "motion" 
							   || self.descriptionAnimationType_str == "opacity";
						if(!test) self.descriptionAnimationType_str = "motion";
						if(self.desc_do) self.desc_do.descriptionAnimationType_str = self.descriptionAnimationType_str;
						break;
					case "descriptionWindowBackgroundColor":
						self.descriptionWindowBackgroundColor_str = props_obj.descriptionWindowBackgroundColor || "#FF0000";
						if(self.desc_do){
							self.desc_do.backgroundColor_str = self.descriptionWindowBackgroundColor_str;
							self.desc_do.bk_do.setBkColor(self.descriptionWindowBackgroundColor_str);
						}
						break;
					case "descriptionWindowBackgroundOpacity":
						self.descriptionWindowBackgroundOpacity = props_obj.descriptionWindowBackgroundOpacity || 1;
						if(self.desc_do){
							self.desc_do.backgroundOpacity = self.descriptionWindowBackgroundOpacity;
							self.desc_do.bk_do.setAlpha(self.desc_do.backgroundOpacity);
						}
						break;
					case "backgroundColor":
						self.backgroundColor_str = props_obj.backgroundColor || "#000000";
						self.bk_do.getStyle().backgroundColor = self.backgroundColor_str;
						break;
					case "itemBorderColor":
						self.itemBorderColor_str = props_obj.itemBorderColor || "transparent";
						if(self.itemBorder_do) self.itemBorder_do.getStyle().backgroundColor = self.itemBorderColor_str;
						break;
					case "spaceBetweenButtons":
						self.spaceBetweenButtons = props_obj.spaceBetweenButtons || 0; 
						break;
					case "buttonsHideDelay":
						self.buttonsHideDelay = props_obj.buttonsHideDelay || 3;
						self.buttonsHideDelay *= 1000;
						if(self.hider) self.hider.hideDelay = self.buttonsHideDelay;
						break;
					case "useAsModal":
						self.useAsModal_bl = props_obj.useAsModal;
						self.useAsModal_bl = self.useAsModal_bl == "yes" ? true : false;
						break;
					case "slideShowAutoPlay":
						self.slideShowAutoPlay_bl = props_obj.slideShowAutoPlay;
						self.slideShowAutoPlay_bl = self.slideShowAutoPlay_bl == "yes" ? true : false;
						break;
					case "videoAutoPlay":
						self.videoAutoPlay_bl = props_obj.videoAutoPlay;
						self.videoAutoPlay_bl = self.videoAutoPlay_bl == "yes" ? true : false;
						if(self.isMobile_bl) self.videoAutoPlay_bl = false;
						break;
					case "nextVideoOrAudioAutoPlay":
						self.nextVideoOrAudioAutoPlay_bl = props_obj.nextVideoOrAudioAutoPlay;
						self.nextVideoOrAudioAutoPlay_bl = self.nextVideoOrAudioAutoPlay_bl == "yes" ? true : false;
						if(self.isMobile_bl) self.nextVideoOrAudioAutoPlay_bl = false;
						break;
					case "audioAutoPlay":
						self.audioAutoPlay_bl = props_obj.audioAutoPlay;
						self.audioAutoPlay_bl = self.audioAutoPlay_bl == "yes" ? true : false;
						if(self.isMobile_bl) self.audioAutoPlay_bl = false;
						break;
					case "addKeyboardSupport":
						self.addKeyboardSupport_bl = props_obj.addKeyboardSupport;
						self.addKeyboardSupport_bl = self.addKeyboardSupport_bl == "yes" ? true : false;
						break;
					case "showCloseButton":
						self.showCloseButton_bl = props_obj.showCloseButton; 
						self.showCloseButton_bl = self.showCloseButton_bl == "no" ? false : true;
						break;
					case "showFacebookButton":
						self.showFacebookButton_bl = props_obj.showFacebookButton;
						self.showFacebookButton_bl = self.showFacebookButton_bl == "yes" ? true : false;
						break;
					case "showZoomButton":
						self.defaultShowZoomButton_bl = props_obj.showZoomButton; 
						self.defaultShowZoomButton_bl = self.defaultShowZoomButton_bl == "no" ? false : true;
						break;
					case "showSlideShowButton":
						self.showSlideShowButton_bl = props_obj.showSlideShowButton;
						self.showSlideShowButton_bl = self.showSlideShowButton_bl == "yes" ? true : false;
						break;
					case "showSlideShowAnimation":
						self.defaultShowSlideShowAnimation_bl = props_obj.showSlideShowAnimation;
						self.defaultShowSlideShowAnimation_bl = self.defaultShowSlideShowAnimation_bl == "yes" ? true : false;
						break;	
					case "showNextAndPrevButtons":
						self.defaultShowNextAndPrevButtons_bl = props_obj.showNextAndPrevButtons; 
						self.defaultShowNextAndPrevButtons_bl = self.defaultShowNextAndPrevButtons_bl == "no" ? false : true;
						if(props_obj.showNextAndPrevButtonsOnMobile == "no" && self.isMobile_bl)  self.defaultShowNextAndPrevButtons_bl = false;
						break;	
					case "slideShowDelay":
						self.slideShowDelay = parseInt(props_obj.slideShowDelay) * 1000;
						if(self.slideShowDelay < 1/1000) self.slideShowDelay = 1000;
						if(self.tm) self.tm.delay = self.slideShowDelay;
						if(self.slp_do) self.slp_do.duration = self.slideShowDelay/1000;
						break;	
					case "itemOffsetHeight":
						self.itemOffsetH = props_obj.itemOffsetHeight || 0;
						break;
					case "buttonsOffsetIn":
						if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
							this.buttonsOffsetIn = props_obj.buttonsOffsetIn || 0;
						}else{
							this.buttonsOffsetIn = props_obj.buttonsOffsetOut || 0;
						}
						break;	
					case "buttonsOffsetOut":
						if(self.buttonsAlignment_str == FWDRL.BUTTONS_IN){
							self.buttonsOffsetOut = props_obj.buttonsOffsetOut || 0;
						}else{
							self.buttonsOffsetOut = props_obj.buttonsOffsetIn || 0;
						}
						break;	
					case "itemBorderSize":
						self.itemBorderSize = props_obj.itemBorderSize || 0; 
						//if(self.itemBorderSize == 0) self.itemBorderColor_str = "transparent";
						if(self.desc_do) self.desc_do.margins = self.itemBorderSize;
						break;	
					case "itemBorderRadius":
						self.itemBorderRadius = props_obj.itemBorderRadius || 0; 
						if(!self.itemBorderRadius){
							self.mainItemHolder_do.getStyle().borderRadius = "";
						}else{
							self.mainItemHolder_do.getStyle().borderRadius = self.itemBorderRadius + "px";
						}
						break;	
					case "backgroundOpacity":
						self.backgroundOpacity = props_obj.backgroundOpacity || .8;
						break;	
					case "itemBoxShadow":
						self.itemBoxShadow_str = props_obj.itemBoxShadow || "none";
						if(self.itemBoxShadow_str == "none"){
							self.mainItemHolder_do.getStyle().boxShadow = "none";
						}else{
							self.mainItemHolder_do.getStyle().boxShadow = self.itemBoxShadow_str;
						}
						break;
					case "itemBackgroundColor":
						self.itemBkColor_str = props_obj.itemBackgroundColor || "transparent";
						self.itemBk_do.getStyle().backgroundColor = self.itemBkColor_str;
						break;
					case "showThumbnails":
						self.defaultShowThumbnails_bl = props_obj.showThumbnails;
						self.defaultShowThumbnails_bl = self.defaultShowThumbnails_bl == "yes" ? true : false;
						break;	
					case "showThumbnailsHideOrShowButton":
						self.defaultShowThumbnailsHideOrShowButton_bl = props_obj.showThumbnailsHideOrShowButton;
						self.defaultShowThumbnailsHideOrShowButton_bl = self.defaultShowThumbnailsHideOrShowButton_bl == "yes" ? true : false;
						break;
					case "showThumbnailsByDefault":
						self.showThumbnailsByDefault_bl = props_obj.showThumbnailsByDefault;
						self.showThumbnailsByDefault_bl = self.showThumbnailsByDefault_bl == "yes" ? true : false;
						break;	
					case "showThumbnailsOverlay":
						self.showThumbnailsOverlay_bl = props_obj.showThumbnailsOverlay; 
						self.showThumbnailsOverlay_bl = self.showThumbnailsOverlay_bl == "yes" ? true : false;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.showThumbnailsOverlay_bl = self.showThumbnailsOverlay_bl;
						break;
					case "showThumbnailsSmallIcon":
						self.showThumbnailsSmallIcon_bl = props_obj.showThumbnailsSmallIcon; 
						self.showThumbnailsSmallIcon_bl = self.showThumbnailsSmallIcon_bl == "yes" ? true : false;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.showThumbnailsSmallIcon_bl = self.showThumbnailsSmallIcon_bl;
						break;
					case "thumbnailsOffsetBottom":
						self.thumbnailsOffsetBottom = props_obj.thumbnailsOffsetBottom || 0;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsOffsetBottom = self.thumbnailsOffsetBottom;
						break;
					case "thumbnailsImageHeight":
						self.thumbnailH = props_obj.thumbnailsImageHeight || 50;
						break;
					case "thumbnailsBorderSize":
						self.thumbnailsBorderSize = props_obj.thumbnailsBorderSize || 0;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderSize = self.thumbnailsBorderSize;
						break;
					case "thumbnailsBorderRadius":
						self.thumbnailsBorderRadius = props_obj.thumbnailsBorderRadius || 0;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderRadius = self.thumbnailsBorderRadius;
						break;
					case "spaceBetweenThumbnailsAndItem":
						self.spaceBetweenThumbnailsAndItem = props_obj.spaceBetweenThumbnailsAndItem || 0;
						break;
					case "spaceBetweenThumbnails":
						self.spaceBetweenThumbnails = props_obj.spaceBetweenThumbnails || 0;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.spaceBetweenThumbnails = self.spaceBetweenThumbnails;
						break;
					case "thumbnailsOverlayOpacity":
						self.thumbnailsOverlayOpacity = props_obj.thumbnailsOverlayOpacity || 1;
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsOverlayOpacity = self.thumbnailsOverlayOpacity;
						break;
					case "thumbnailsOverlayColor":
						self.thumbnailsOverlayColor_str = props_obj.thumbnailsOverlayColor || "#FF0000";
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsOverlayColor_str = self.thumbnailsOverlayColor_str;
						break;
					case "thumbnailsBorderNormalColor":
						self.thumbnailsBorderNormalColor_str = props_obj.thumbnailsBorderNormalColor || "#FF0000";
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderNormalColor_str = self.thumbnailsBorderNormalColor_str;
						break;
					case "thumbnailsBorderSelectedColor":
						self.thumbnailsBorderSelectedColor_str = props_obj.thumbnailsBorderSelectedColor || "#FF0000";
						if(self.thumbnailsManager_do) self.thumbnailsManager_do.thumbnailsBorderSelectedColor_str = self.thumbnailsBorderNormalColor_str;
						break;
					case "videoControllerBackgroundColor":
						self.data.videoControllerBackgroundColor_str = props_obj.videoControllerBackgroundColor || "transparent";
						if(self.video_do && self.video_do.controller_do) self.video_do.controller_do.mainHolder_do.getStyle().backgroundColor = self.data.videoControllerBackgroundColor_str;
						break;
					case "videoPosterBackgroundColor":
						self.data.videoPosterBackgroundColor_str = props_obj.videoPosterBackgroundColor || "transparent";
						if(self.video_do) self.video_do.videoPoster_do.getStyle().backgroundColor = self.data.videoPosterBackgroundColor_str;
						break;
					case "audioControllerBackgroundColor":
						self.data.audioControllerBackgroundColor_str = props_obj.audioControllerBackgroundColor || "transparent";
						if(self.audio_do && self.audio_do.controller_do) self.audio_do.controller_do.getStyle().backgroundColor = self.data.audioControllerBackgroundColor_str;
						break;
				}	
			}
			
			if(props_obj.thumbnailsImageHeight){
				
				self.thumbnailH += (self.thumbnailsBorderSize * 2) + self.thumbnailsOffsetBottom;
				if(self.thumbnailsManager_do){
					self.thumbnailsManager_do.thumbnailH = self.thumbnailH - self.thumbnailsOffsetBottom;
					self.thumbnailsManager_do.stageHeight = self.thumbnailH;
				}
				
			}
		};
		
		//###########################################//
		/* event dispatcher */
		//###########################################//
		FWDRL.addListener = function (type_str, listener){
	    	if(!self.listeners) return;
	    	if(type_str == undefined) throw Error("type_str is required.");
	    	if(typeof type_str === "object") throw Error("type_str must be of type_str String.");
	    	if(typeof listener != "function") throw Error("listener must be of type_str Function.");
	    	
	        var event = {};
	        event.type_str = type_str;
	        event.listener = listener;
	        event.target = self;
	        self.listeners.events_ar.push(event);
	    };
	    
	    FWDRL.dispatchEvent = function(type_str, props){
	    	if(self.listeners == null) return;
	    	if(type_str == undefined) throw Error("type_str is required.");
	    	if(typeof type_str === "object") throw Error("type_str must be of type_str String.");
	    	
	        for (var i=0, len=self.listeners.events_ar.length; i < len; i++){
	        	if(self.listeners.events_ar[i].target === self && self.listeners.events_ar[i].type_str === type_str){		
	    	        if(props){
	    	        	for(var prop in props){
	    	        		self.listeners.events_ar[i][prop] = props[prop];
	    	        	}
	    	        }
	        		self.listeners.events_ar[i].listener.call(self, self.listeners.events_ar[i]);
	        	}
	        }
	    };
	    
	    FWDRL.removeListener = function(type_str, listener){
	    	if(type_str == undefined) throw Error("type_str is required.");
	    	if(typeof type_str === "object") throw Error("type_str must be of type_str String.");
	    	if(typeof listener != "function") throw Error("listener must be of type_str Function." + type_str);
	    	
	        for (var i=0, len=self.listeners.events_ar.length; i < len; i++){
	        	if(self.listeners.events_ar[i].target === self 
	        			&& self.listeners.events_ar[i].type_str === type_str
	        			&& self.listeners.events_ar[i].listener ===  listener
	        	){
	        		self.listeners.events_ar.splice(i,1);
	        		break;
	        	}
	        }  
	    };		
		self.init();
	};
	
	/* set prototype */
	FWDRL.setPrototype =  function(){
		FWDRL.prototype = new FWDRVPEventDispatcher();
	};
	
	FWDRL.READY = "ready";
	FWDRL.SHOW_START = "showStart";
	FWDRL.SHOW_COMPLETE = "showComplete";
	FWDRL.HIDE_START = "hideStart";
	FWDRL.HIDE_COMPLETE	= "hidecComplete";
	FWDRL.UPDATE = "update";
	FWDRL.BUTTONS_IN = "in";
	FWDRL.READY = "ready";
	FWDRL.ERROR = "error";
	FWDRL.IMAGE_TYPE = "image";
	FWDRL.VIDEO_TYPE = "video";
	FWDRL.AUDIO_TYPE = "audio";
	FWDRL.FLASH_TYPE = "flash";
	FWDRL.IFRAME_TYPE = "iframe";
	FWDRL.MAXIMIZE_COMPLETE = "maximizeComplete";
	
	window.FWDRL = FWDRL;
	
}(window));