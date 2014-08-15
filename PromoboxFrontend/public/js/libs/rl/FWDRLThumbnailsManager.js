/* Info screen */
(function (window){
	
	var FWDRLThumbnailsManager = function(parent){
		
		var self = this;
		var prototype = FWDRLThumbnailsManager.prototype;
		
		this.playlist_ar = null;
		this.thumbs_ar = null;
		
		this.mainHolder_do = null;
		this.thumbnailsHolder_do = null;
	
		this.thumbnailsBorderNormalColor_str = parent.thumbnailsBorderNormalColor_str;
		this.thumbnailsBorderSelectedColor_str = parent.thumbnailsBorderSelectedColor_str;
		this.thumbnailsOverlayColor_str = parent.thumbnailsOverlayColor_str;
		this.thumbnailsHoverEffect_str = parent.thumbnailsHoverEffect_str;
		
		this.stageWidth = 0;
		this.stageHeight = parent.thumbnailH;
		this.thumbnailsBorderSize = parent.thumbnailsBorderSize;
		this.thumbnailsBorderRadius = parent.thumbnailsBorderRadius;
		this.thumbnailsOffsetBottom = parent.thumbnailsOffsetBottom;
		this.thumbnailH = parent.thumbnailH - this.thumbnailsOffsetBottom;
		this.spaceBetweenThumbnails = parent.spaceBetweenThumbnails;
		this.totalW = 0;
		this.spaceBetweenThumbnails = parent.spaceBetweenThumbnails;
		this.thumbnailsOverlayOpacity = parent.thumbnailsOverlayOpacity;
		this.vx = 0;
		this.vx2 = 0;
		this.friction = .9;
		this.lastPresedX = 0;
		this.totalThumbnails = 0;	
		this.countLoadedThumbs = 0;
		this.id = 0;
		
		this.loadWithDelayId_to;
		this.disableOnMoveId_to;
		this.updateMobileScrollBarId_int;
		
		this.showThumbnailsOverlay_bl = parent.showThumbnailsOverlay_bl;
		this.showThumbnailsSmallIcon_bl = parent.showThumbnailsSmallIcon_bl;
		this.areThumbnailTouched_bl = false;
		this.isScrolling_bl = false;
		this.isShowed_bl = false;
		this.areButtonsPositioned_bl = false;
		this.areThumbnailsCreated_bl = false;
		this.hasSupportForDesktopScroll_bl = false;
		this.isMobile_bl = FWDRLUtils.isMobile;
	
		//#################################//
		/* init */
		//#################################//
		self.init = function(){
			self.setOverflow("visible");
			self.mainHolder_do = new FWDRLDisplayObject("div");
			self.mainHolder_do.setOverflow("visible");
			
			self.thumbnailsHolder_do = new FWDRLDisplayObject("div"); 
			self.thumbnailsHolder_do.setOverflow("visible");
			self.mainHolder_do.addChild(self.thumbnailsHolder_do);
			
			self.addChild(self.mainHolder_do);
		};
		
		//######################################//
		/* Position and resize */
		//######################################//
		self.positionAndResize = function(){
			self.areButtonsPositioned_bl = false;
			self.stageWidth = parent.stageWidth;
			self.setY(parent.stageHeight);
			self.mainHolder_do.setWidth(self.stageWidth);
			self.mainHolder_do.setHeight(self.stageHeight);
			if(self.areThumbnailsCreated_bl) self.positionThumbnails(false);
		};
		
		//#####################################//
		/* Create / destory thumbnails */
		//#####################################//
		self.setupThumbnails = function(){
			self.areThumbnailsCreated_bl = true;
			self.areButtonsPositioned_bl = false;
			self.thumbs_ar = [];
			self.playlist_ar = parent.playlist_ar;
			self.totalThumbnails = self.playlist_ar.length;
			self.countLoadedThumbs = 0;
			self.loadThumbnails();
			if(self.isMobile_bl) self.addMobileScrollSupport();
		};
		
		self.loadThumbnails = function(){
			if(self.countLoadedThumbs > self.totalThumbnails-1) return;
			self.image_img = new Image();
			self.image_img.onload = self.onThumbnailLoadComplete;
			self.image_img.src = self.playlist_ar[self.countLoadedThumbs].thumbnailPath_str;
		};
	
		self.onThumbnailLoadComplete = function(e){
			
			var iconType_str = parent.playlist_ar[self.countLoadedThumbs].iconType_str;
			var iconPath_str;
			if(iconType_str == FWDRLThumb.IMAGE){
				iconPath_str = parent.data.imageIconPath_str;
			}else if(iconType_str == FWDRLThumb.FLASH){
				iconPath_str = parent.data.flashIconPath_str;
			}else if(iconType_str == FWDRLThumb.AUDIO){
				iconPath_str = parent.data.audioIconPath_str;
			}else if(iconType_str == FWDRLThumb.VIDEO){
				iconPath_str = parent.data.videoIconPath_str;
			}else if(iconType_str == FWDRLThumb.VIMEO){
				iconPath_str = parent.data.vimeoIconPath_str;
			}else if(iconType_str == FWDRLThumb.YOUTUBE){
				iconPath_str = parent.data.youtubeIconPath_str;
			}else if(iconType_str == FWDRLThumb.MAPS){
				iconPath_str = parent.data.mapsIconPath_str;
			}else if(iconType_str == FWDRLThumb.AJAX){
				iconPath_str = parent.data.ajaxIconPath_str;
			}else if(iconType_str == FWDRLThumb.HTML){
				iconPath_str = parent.data.htmlIconPath_str;
			}else if(iconType_str == FWDRLThumb.IFRAME){
				iconPath_str = parent.data.iframeIconPath_str;
			}
			
			FWDRLThumb.setPrototype();
			var thumb = new FWDRLThumb(
					self,
					self.countLoadedThumbs, 
					self.thumbnailH,
					self.thumbnailsOffsetBottom,
					self.thumbnailsBorderSize,
					self.thumbnailsBorderRadius,
					self.thumbnailsOverlayOpacity,
					self.thumbnailsBorderNormalColor_str,
					self.thumbnailsBorderSelectedColor_str,
					self.thumbnailsOverlayColor_str,
					self.thumbnailsHoverEffect_str,
					iconPath_str,
					self.showThumbnailsOverlay_bl,
					self.showThumbnailsSmallIcon_bl);
			self.thumbs_ar[self.countLoadedThumbs] = thumb;
			thumb.addListener(FWDRLThumb.HOVER, self.thumbHoverHandler);
			thumb.addListener(FWDRLThumb.CLICK, self.thumbClickHandler);
			thumb.setImage(self.image_img);
			self.totalW += thumb.w + self.spaceBetweenThumbnails;
			if(self.countLoadedThumbs == self.totalThumbnails - 1) self.totalW -= self.spaceBetweenThumbnails;
			
			if(self.countLoadedThumbs !=0){
				thumb.setX(self.thumbs_ar[self.countLoadedThumbs - 1].x + self.thumbs_ar[self.countLoadedThumbs - 1].w + self.spaceBetweenThumbnails);
			}

			if(self.countLoadedThumbs == 0) self.thumbnailsHolder_do.setX(parseInt(self.stageWidth - thumb.w)/2);
		
			self.thumbnailsHolder_do.addChild(thumb);
			if(!self.isScrolling_bl && !self.areThumbnailTouched_bl)  self.positionThumbnails(true);
			if(self.totalW > parent.stageWidth 
			   && !self.areButtonsPositioned_bl
			   && parent.buttonsAlignment_str != FWDRL.BUTTONS_IN){
				parent.positionButtons(true);
				self.areButtonsPositioned_bl = true;
			}
			
			
			self.countLoadedThumbs++;
			self.loadWithDelayId_to = setTimeout(self.loadThumbnails, 100);	
		};
		
		self.stopToLoadThumbanils = function(){
			if(self.image_img){
				self.image_img.onload = null;
				self.image_img.onerror = null;
				self.image_img.src = "";
				self.image_img = null;
			}
			clearTimeout(self.loadWithDelayId_to);
		};
		
		self.thumbClickHandler = function(e){
			if(!parent.isShowed_bl)return;
			self.dispatchEvent(FWDRLThumb.CLICK, {id:e.id});
		};
		
		self.thumbHoverHandler = function(){
			if(!parent.isShowed_bl) return;
			self.addDesktopScrollSupport();
		};
		
		//#####################################//
		/* Position thumbnails */
		//#####################################//
		self.positionThumbnails = function(animate){
			if(!self.areThumbnailsCreated_bl && parent.showThumbnails_bl || self.isScrolling_bl) return;
			self.finalX;
			var curThumb = self.thumbs_ar[self.id];
			var lastCreateThumb = self.thumbs_ar[self.thumbs_ar.length - 1];
			
			if(self.totalW <= self.stageWidth){
				self.finalX = parseInt((self.stageWidth - self.totalW)/2);
			}else{
				if(curThumb){
					self.finalX = parseInt(-curThumb.x + (self.stageWidth - curThumb.w)/2);
				}else{
					self.finalX = parseInt(-lastCreateThumb.x + (self.stageWidth - lastCreateThumb.w)/2);
				}
				
				if(self.finalX > 0){
					self.finalX = 0;
				}else if(self.finalX < (self.stageWidth - self.totalW)){
					self.finalX = self.stageWidth - self.totalW;
				}
			}
			
			FWDRLTweenMax.killTweensOf(self.thumbnailsHolder_do);
			if(animate){
				FWDRLTweenMax.to(self.thumbnailsHolder_do, .7,{x:self.finalX, ease:Expo.easeOut});
			}else{
				self.thumbnailsHolder_do.setX(self.finalX);
			}
		};
		
		//#####################################//
		/* Add mobile scroll support */
		//#####################################//
		self.addMobileScrollSupport = function(){
			if(self.hasPointerEvent_bl){
				self.mainHolder_do.screen.addEventListener("MSPointerDown", self.scrollBarTouchStartHandler);
			}else{
				self.mainHolder_do.screen.addEventListener("touchstart", self.scrollBarTouchStartHandler);
			}
			self.mainHolder_do.screen.addEventListener("mousedown", self.scrollBarTouchStartHandler);
			self.updateMobileScrollBarId_int = setInterval(self.updateMobileScrollBar, 16);
		};
		
		self.removeMobileScrollSupport = function(){
			if(self.hasPointerEvent_bl){
				self.mainHolder_do.screen.removeEventListener("MSPointerDown", self.scrollBarTouchStartHandler);
				window.removeEventListener("MSPointerUp", self.scrollBarTouchEndHandler);
				window.removeEventListener("MSPointerMove", self.scrollBarTouchMoveHandler);
			}else{
				self.mainHolder_do.screen.removeEventListener("touchstart", self.scrollBarTouchStartHandler);
				window.removeEventListener("touchend", self.scrollBarTouchEndHandler);
				window.removeEventListener("touchmove", self.scrollBarTouchMoveHandler);
			}
			//self.mainHolder_do.screen.removeEventListener("mousedown", self.scrollBarTouchStartHandler);
			clearInterval(self.updateMobileScrollBarId_int);
			clearInterval(self.updateMoveMobileScrollbarId_int);
		};
		
		self.scrollBarTouchStartHandler = function(e){
			//if(e.preventDefault) e.preventDefault();
			if(self.stageWidth > self.totalW) return;
			
			var vmc = FWDRLUtils.getViewportMouseCoordinates(e);
			self.areThumbnailTouched_bl = true;
			
			FWDRLTweenMax.killTweensOf(self.thumbnailsHolder_do);		
			self.isScrolling_bl = true;
			self.finalX = self.thumbnailsHolder_do.x;
			self.lastPresedX = vmc.screenX;
			
			if(self.hasPointerEvent_bl){
				window.addEventListener("MSPointerUp", self.scrollBarTouchEndHandler);
				window.addEventListener("MSPointerMove", self.scrollBarTouchMoveHandler);
			}else{
				window.addEventListener("touchend", self.scrollBarTouchEndHandler);
				window.addEventListener("touchmove", self.scrollBarTouchMoveHandler);
			}
			//window.addEventListener("mouseup", self.scrollBarTouchEndHandler);
			//window.addEventListener("mousemove", self.scrollBarTouchMoveHandler);
			clearInterval(self.updateMoveMobileScrollbarId_int);
			self.updateMoveMobileScrollbarId_int = setInterval(self.updateMoveMobileScrollbar, 16);
		};
		
		self.scrollBarTouchMoveHandler = function(e){
			if(e.preventDefault) e.preventDefault();
			if(self.stageWidth > self.totalW) return;
			var vmc = FWDRLUtils.getViewportMouseCoordinates(e);
			
			
			var toAdd = vmc.screenX - self.lastPresedX;
			self.finalX += toAdd;
			self.finalX = Math.round(self.finalX);
		
			self.lastPresedX = vmc.screenX;
			self.vx = toAdd  * 2;
			parent.showDisable();
		};
		
		self.scrollBarTouchEndHandler = function(e){
			self.isScrolling_bl = false;
			
			if(parent.hider.globalY < parent.stageHeight - self.stageHeight){
				self.areThumbnailTouched_bl = false;
			}
			
			clearInterval(self.updateMoveMobileScrollbarId_int);
			clearTimeout(self.disableOnMoveId_to);
			self.disableOnMoveId_to = setTimeout(function(){
				parent.hideDisable();
			},100);
			
			if(self.hasPointerEvent_bl){
				window.removeEventListener("MSPointerUp", self.scrollBarTouchEndHandler);
				window.removeEventListener("MSPointerMove", self.scrollBarTouchMoveHandler);
			}else{
				window.removeEventListener("touchend", self.scrollBarTouchEndHandler);
				window.removeEventListener("touchmove", self.scrollBarTouchMoveHandler);
			}
			//window.removeEventListener("mouseup", self.scrollBarTouchEndHandler);
			//window.removeEventListener("mousemove", self.scrollBarTouchMoveHandler);
		};
		
		self.updateMoveMobileScrollbar = function(){
			self.thumbnailsHolder_do.setX(self.finalX);
		};
		
		self.updateMobileScrollBar = function(animate){
			if(self.stageWidth > self.totalW
			  || self.finalX == self.prevX) return;
		
			if(!self.isScrolling_bl){
				self.vx *= self.friction;
				self.finalX += self.vx;	
				
				if(self.finalX > 0){
					self.vx2 = (0 - self.finalX) * .3;
					self.vx *= self.friction;
					self.finalX += self.vx2;
				}else if(self.finalX < self.stageWidth - self.totalW){
					self.vx2 = (self.stageWidth - self.totalW - self.finalX) * .3;
					self.vx *= self.friction;
					self.finalX += self.vx2;
				}
				
				self.finalX = Math.round(self.finalX);
				self.prevX = self.thumbnailsHolder_do.x;
				FWDRLTweenMax.killTweensOf(self.thumbnailsHolder_do);
				FWDRLTweenMax.to(self.thumbnailsHolder_do, .3,{x:self.finalX, ease:Expo.easeOut});
			}
		};
	
		
		//#####################################//
		/* Add desktop scroll support */
		//#####################################//
		self.addDesktopScrollSupport = function(){
			if(self.hasSupportForDesktopScroll_bl || self.totalW < self.stageWidth) return;
			self.hasSupportForDesktopScroll_bl = true;
			self.isScrolling_bl = true;
			
			if(window.addEventListener){
				window.addEventListener("mousemove", self.checkHitTest);
			}else if(document.attachEvent){
				document.detachEvent("onmousemove", self.checkHitTest);
				document.attachEvent("onmousemove", self.checkHitTest);
			}
		};
		
		self.removeDesktopScrollSupport = function(){
			if(!self.hasSupportForDesktopScroll_bl) return;
			self.hasSupportForDesktopScroll_bl = false;
			
			if(window.removeEventListener){
				window.removeEventListener("mousemove", self.checkHitTest);
			}else if(document.detachEvent){
				document.detachEvent("onmousemove", self.checkHitTest);
			}
		};
		
		self.checkHitTest = function(e){
			var vmc = FWDRLUtils.getViewportMouseCoordinates(e);
			self.scrollOnDesktop();
			if(parent.hider.globalY < parent.stageHeight - self.stageHeight){
				self.isScrolling_bl = false;
				self.removeDesktopScrollSupport();
				self.positionThumbnails(true);
			}
		};
		
		self.scrollOnDesktop = function(){
			var percent = (parent.hider.globalX - 100)/(self.stageWidth - 200);
			if(percent < 0){
				percent = 0;
			}else if(percent > 1){
				percent = 1;
			}
			
			self.finalX = parseInt((self.stageWidth - self.totalW) * percent);
			FWDRLTweenMax.killTweensOf(self.thumbnailsHolder_do);
			FWDRLTweenMax.to(self.thumbnailsHolder_do, .4,{x:self.finalX, ease:Expo.easeOut});
		};
		
		//#####################################//
		/* Disable / enable */
		//#####################################//
		self.disableOrEnableThumbnails = function(){
			self.id = parent.id;
			if(!self.thumbs_ar) return;
			var thumb;
			var totalThumbnails = self.thumbs_ar.length;
			for(var i=0; i<totalThumbnails; i++){
				thumb = self.thumbs_ar[i];
				if(i == parent.id){
					thumb.disable();
				}else{
					thumb.enable();
				}
			}
			self.positionThumbnails(true);
		};
		
		//#####################################//
		/* Destroy */
		//#####################################//
		self.destoryThumbnails = function(){
			if(!self.areThumbnailsCreated_bl && !self.thumbs_ar) return;
			self.areThumbnailsCreated_bl = false;
			self.areThumbnailTouched_bl = false;
			var thumb;
			var totalThumbnails = self.thumbs_ar.length;
			for(var i=0; i<totalThumbnails; i++){
				thumb = self.thumbs_ar[i];
				FWDRLTweenMax.killTweensOf(thumb);
				self.thumbnailsHolder_do.removeChild(thumb);
				thumb.destroy();
			}
			self.thumbs_ar = null;
			self.totalW = 0;
			self.stopToLoadThumbanils();
			self.removeDesktopScrollSupport();
			if(self.isMobile_bl) self.removeMobileScrollSupport();
		};
		
		//#####################################//
		/* Show / hide */
		//#####################################//
		self.show = function(animate){
			self.isShowed_bl = true;
			FWDRLTweenMax.killTweensOf(self.mainHolder_do);
			if(animate){
				FWDRLTweenMax.to(self.mainHolder_do, .8, {y:-self.stageHeight, ease:Expo.easeInOut});
			}else{
				self.mainHolder_do.setY(-self.stageHeight);
			}
		};
		
		self.hide = function(animate){
			self.isShowed_bl = false;
			FWDRLTweenMax.killTweensOf(self.mainHolder_do);
			if(animate){
				FWDRLTweenMax.to(self.mainHolder_do, .8, {y:0, ease:Expo.easeInOut});
			}else{
				self.mainHolder_do.setY(0);
			}
		};
		
		self.hideForGood = function(){
			self.mainHolder_do.setY(-5000);
		};
		
		self.init();
	};
		
	/* set prototype */
	FWDRLThumbnailsManager.setPrototype = function(){
		FWDRLThumbnailsManager.prototype = new FWDRLDisplayObject("div", "relative");
	};
	
	FWDRLThumbnailsManager.prototype = null;
	window.FWDRLThumbnailsManager = FWDRLThumbnailsManager;
}(window));