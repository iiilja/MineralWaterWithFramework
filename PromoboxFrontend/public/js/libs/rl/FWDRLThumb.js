/* thumb */
(function(window){
	
	var FWDRLThumb = function(
			parent,
			id, 
			thumbnailH,
			thumbnailsOffsetBottom,
			borderSize,
			thumbnailsBorderRadius,
			overlayOpacity,
			thumbnailBorderNormalColor,
			thumbnailBorderSelectedColor,
			thumbnailsOverlayColor,
			thumbnailsHoverEffect,
			iconPath,
			showOverlay,
			showIcon
		){
		
		var self = this;
		var prototype = FWDRLThumb.prototype;

		this.background_do = null;
		this.image_do = null;
		this.overlay_do = null;
		this.icon_do = null;
		this.iconImg_img = null;
		
		this.borderNormalColor_str = thumbnailBorderNormalColor || data.thumbnailBorderNormalColor_str;
		this.borderSelectedColor_str = thumbnailBorderSelectedColor || data.thumbnailBorderSelectedColor_str;
		this.thumbnailsOverlayColor_str = thumbnailsOverlayColor;
		this.iconPath_str = iconPath;
		this.thumbnailsHoverEffect_str = thumbnailsHoverEffect;
	
		this.id = id;
		this.borderSize = borderSize;
		this.borderRadius = thumbnailsBorderRadius;
		this.thumbnailH = thumbnailH;
		this.thumbnailsOffsetBottom = thumbnailsOffsetBottom;
		this.overlayOpacity = overlayOpacity;
		
		this.isSelected_bl = true;
		this.isDisabled_bl = false;
		this.hasPointerEvent_bl = FWDRLUtils.hasPointerEvent;
		this.isMobile_bl = FWDRLUtils.isMobile;
		this.showOverlay_bl = showOverlay;
		if(this.isMobile_bl) this.showOverlay_bl = false;
		this.showIcon_bl = showIcon;
		if(this.isMobile_bl) this.showIcon_bl = false;
		
		/* init */
		self.init = function(){
			self.setButtonMode(true);
			self.setupScreen();
		};
		
		/* setup screen */
		self.setupScreen = function(){
			self.background_do = new FWDRLDisplayObject("div");
			if(self.borderRadius) self.getStyle().borderRadius = self.borderRadius + "px";
			self.setNormalState(false);
			if(self.borderRadius != 0) self.background_do.getStyle().borderRadius = self.borderRadius + "px";
			self.addChild(self.background_do);
		};
		
		
		//######################################//
		/* add image */
		//######################################//
		self.setImage = function(image){
			
			self.image_do = new FWDRLDisplayObject("img");
			self.image_do.setScreen(image);
			var imgW = image.width;
			var imgH = image.height;
			var imageHeight = self.thumbnailH - self.borderSize * 2;
			
			var scale = imageHeight/imgH;
			
			var finalH = parseInt(imageHeight + self.borderSize * 2);
			var finalW = parseInt((imgW * scale) + self.borderSize * 2);
			
			if(self.background_do){
				self.background_do.setWidth(finalW);
				self.background_do.setHeight(finalH);
			}
			
			self.image_do.setX(self.borderSize);
			self.image_do.setY(self.borderSize);
			self.image_do.setWidth(parseInt(finalW - self.borderSize * 2));
			self.image_do.setHeight(imageHeight);
			self.setWidth(finalW);
			self.setHeight(finalH);
			
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.screen.addEventListener("MSPointerUp", self.onMouseClickHandler);
				}
				self.screen.addEventListener("click", self.onMouseClickHandler);
			}else if(self.screen.addEventListener){
				self.screen.addEventListener("mouseover", self.onMouseOverHandler);
				self.screen.addEventListener("click", self.onMouseClickHandler);
			}else if(self.screen.attachEvent){
				self.screen.attachEvent("onmouseover", self.onMouseOverHandler);
				self.screen.attachEvent("onclick", self.onMouseClickHandler);
			}
			
			self.addChild(self.image_do);
			if(!self.isMobile_bl){
				if(self.showOverlay_bl){
					
					self.overlay_do = new FWDRLDisplayObject("div");
					self.overlay_do.setX(self.borderSize);
					self.overlay_do.setY(self.borderSize);
					self.overlay_do.setWidth(finalW - self.borderSize * 2);
					self.overlay_do.setHeight(finalH - self.borderSize * 2);
					self.overlay_do.setBkColor(this.thumbnailsOverlayColor_str);
					self.addChild(self.overlay_do);
					setTimeout(function(){
						if(self) self.overlay_do.setAlpha(0);
					}, 50);
				}
				
				if(self.showIcon_bl){
					self.icon_do = new FWDRLTransformDisplayObject("img");
					self.iconImg_img = new Image();
					self.iconImg_img.onload = function(){
						self.icon_do.setScreen(self.iconImg_img);
						self.icon_do.setX(parseInt((finalW - self.icon_do.w)/2));
						self.icon_do.setY(parseInt((finalH - self.icon_do.h)/2));
						self.addChild(self.icon_do);
						setTimeout(function(){
							if(self) self.icon_do.setAlpha(0);
						}, 50);
						
					};
					self.iconImg_img.src = self.iconPath_str;
				}
			}
			self.hide(false);
			self.show(true);
			if(parent.id == self.id) self.disable();
			
		};
		
		self.onMouseOverHandler = function(e){
			
			self.dispatchEvent(FWDRLThumb.HOVER);
			if(self.isDisabled_bl) return;
			if(!e.pointerType || e.pointerType == e.MSPOINTER_TYPE_MOUSE){
				self.setSelectedState(true);
			}
			self.startToCheckTest();
		};
		
		self.startToCheckTest = function(){
			if(window.addEventListener){
				window.addEventListener("mousemove", self.checkHitTest);
			}else if(document.attachEvent){
				document.detachEvent("onmousemove", self.checkHitTest);
				document.attachEvent("onmousemove", self.checkHitTest);
			}
		};
		
		self.stopToCheckTest = function(){
			if(window.removeEventListener){
				window.removeEventListener("mousemove", self.checkHitTest);
			}else if(document.detachEvent){
				document.detachEvent("onmousemove", self.checkHitTest);
			}
		};
		
		self.checkHitTest = function(e){
			var wc = FWDRLUtils.getViewportMouseCoordinates(e);
			
			if(!FWDRLUtils.hitTest(self.screen, wc.screenX, wc.screenY)){
				self.onMouseOutHandler(e);
				self.stopToCheckTest();
			}
		};

		self.onMouseOutHandler = function(e){
			if(self.isDisabled_bl) return;
			if(!e.pointerType || e.pointerType == e.MSPOINTER_TYPE_MOUSE){
				self.setNormalState(true);
			}
		};
	
		self.onMouseClickHandler = function(e){
			if(self.isDisabled_bl) return;
			self.dispatchEvent(FWDRLThumb.CLICK, {id:self.id});
		};
		
		//#########################################//
		/* Set normal/selected display states */
		//########################################//
		self.setNormalState = function(animate){
			if(!self.isSelected_bl) return;
			self.isSelected_bl = false;
			FWDRLTweenMax.killTweensOf(self.background_do.screen);
			if(self.overlay_do && self.showOverlay_bl) FWDRLTweenMax.to(self.overlay_do, .8, {alpha:0, ease:Expo.easeOut});
			if(self.icon_do && self.showIcon_bl){
				FWDRLTweenMax.killTweensOf(self.icon_do);
				if(self.icon_do.hasTransform2d_bl && self.thumbnailsHoverEffect_str == "scale"){
					FWDRLTweenMax.to(self.icon_do, .5, {scale:1, alpha:0, ease:Expo.easeOut});
				}else{
					FWDRLTweenMax.to(self.icon_do, .8, {alpha:0, ease:Expo.easeOut});
				}
			}
			if(animate){
				if(self.borderSize != 0) FWDRLTweenMax.to(self.background_do.screen, .8, {css : {backgroundColor:self.borderNormalColor_str}, ease : Expo.easeOut});
			}else{
				if(self.borderSize != 0) self.background_do.getStyle().backgroundColor = self.borderNormalColor_str;
			}
		};

		self.setSelectedState = function(animate){
			if(self.isSelected_bl) return;
			self.isSelected_bl = true;
			if(self.overlay_do && self.showOverlay_bl) FWDRLTweenMax.to(self.overlay_do, .8, {alpha:self.overlayOpacity, ease:Expo.easeOut});
			if(self.icon_do && self.showIcon_bl){
				FWDRLTweenMax.killTweensOf(self.icon_do);
				if(self.icon_do.hasTransform2d_bl && self.thumbnailsHoverEffect_str == "scale"){
					self.icon_do.setAlpha(0);
					self.icon_do.setScale2(3);
					FWDRLTweenMax.to(self.icon_do, .5, {scale:1, alpha:1, ease:Expo.easeInOut});
				}else{
					FWDRLTweenMax.to(self.icon_do, .8, {alpha:1, ease:Expo.easeOut});
				}
			}
			if(animate){
				if(self.borderSize != 0) FWDRLTweenMax.to(self.background_do.screen, .8, {css : {backgroundColor:self.borderSelectedColor_str}, ease : Expo.easeOut});
			}else{
				if(self.borderSize != 0) self.background_do.getStyle().backgroundColor = self.borderSelectedColor_str;
			}
		};

		//########################################//
		/* show/hide thumb */
		//########################################//
		self.show = function(animate){
			FWDRLTweenMax.killTweensOf(self);
			if(animate){
				FWDRLTweenMax.to(self, .8, {y:0, ease:Expo.easeInOut});
			}else{
				self.setY(0);
			}
		};
		
		self.hide = function(animate){	
			
			FWDRLTweenMax.killTweensOf(self);
			if(animate){
				FWDRLTweenMax.to(self, .8, {y:self.thumbnailsOffsetBottom + self.thumbnailH + 2});
			}else{
				self.setY(self.thumbnailsOffsetBottom + self.thumbnailH + 2);
			}
		};
		
		//#####################################//
		/* disable /  enable */
		//#####################################//
		self.enable = function(){
			if(!self.isDisabled_bl) return;
			self.isDisabled_bl = false;
			FWDRLTweenMax.to(self.background_do, .8, {alpha:1, ease:Quint.easeOut});
			if(self.icon_do) FWDRLTweenMax.to(self.icon_do, .8, {alpha:1, ease:Quint.easeOut});
			if(self.image_do) FWDRLTweenMax.to(self.image_do, .8, {alpha:1, ease:Quint.easeOut});
			if(self.overlay_do) FWDRLTweenMax.to(self.overlay_do, .8, {alpha:0, ease:Quint.easeOut});
			self.setNormalState(true);
			self.setButtonMode(true);
		};
		
		self.disable = function(){
			self.isDisabled_bl = true;
			FWDRLTweenMax.to(self.background_do, .8, {alpha:.4, ease:Quint.easeOut});
			self.setSelectedState(true);
			if(self.icon_do) FWDRLTweenMax.to(self.icon_do, .8, {alpha:0, ease:Quint.easeOut});
			if(self.image_do) FWDRLTweenMax.to(self.image_do, .8, {alpha:.4, ease:Quint.easeOut});
			if(self.overlay_do) FWDRLTweenMax.to(self.overlay_do, .8, {alpha:0, ease:Quint.easeOut});
			self.stopToCheckTest();
			self.setButtonMode(false);
		};
		
		//####################################//
		/* destroy */
		//####################################//
		self.destroy = function(){
			
			if(self.iconImg_img){
				self.iconImg_img.onload = null;
				self.iconImg_img.onerror = null;
			}
			
			FWDRLTweenMax.killTweensOf(self.background_do);
			self.background_do.destroy();
			
			if(self.image_do){
				FWDRLTweenMax.killTweensOf(self.image_do);
				self.image_do.destroy();
			}
			
			if(self.overlay_do){
				FWDRLTweenMax.killTweensOf(self.overlay_do);
				self.overlay_do.destroy();
			}
			
			if(self.icon_do){
				FWDRLTweenMax.killTweensOf(self.icon_do);
				self.icon_do.destroy();
			}
			
			if(self.isMobile_bl){
				if(self.hasPointerEvent_bl){
					self.screen.removeEventListener("MSPointerOver", self.onMouseOverHandler);
					self.screen.removeEventListener("MSPointerUp", self.onMouseClickHandler);
				}else{
					self.screen.removeEventListener("touchend", self.onMouseClickHandler);
				}
				
			}else if(self.screen.removeEventListener){
				self.screen.removeEventListener("mouseover", self.onMouseOverHandler);
				self.screen.removeEventListener("click", self.onMouseClickHandler);
				window.removeEventListener("mousemove", self.checkHitTest);
			}else if(self.screen.detachEvent){
				self.screen.detachEvent("onmouseover", self.onMouseOverHandler);
				self.screen.detachEvent("onclick", self.onMouseClickHandler);
				document.detachEvent("onmousemove", self.checkHitTest);
			}
			
			self.iconImg_img = null;
			self.background_do = null;
			self.image_do = null;
			self.overlay_do = null;
			self.icon_do = null;
			
			self.setInnerHTML("");
			prototype.destroy();
			prototype = null;
			self = null;
			FWDRLThumb.prototype = null;
		};

		self.init();
	};

	/* set prototype */
	FWDRLThumb.setPrototype = function(){
		FWDRLThumb.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLThumb.HOVER =  "onHover";
	FWDRLThumb.CLICK =  "onClick";
	
	FWDRLThumb.IFRAME = "iframe";
	FWDRLThumb.IMAGE = "image";
	FWDRLThumb.FLASH = "flash";
	FWDRLThumb.AUDIO = "audio";
	FWDRLThumb.VIDEO = "video";
	FWDRLThumb.VIMEO= "vimeo";
	FWDRLThumb.YOUTUBE = "youtube";
	FWDRLThumb.MAPS = "maps";
	FWDRLThumb.AJAX = "ajax";
	FWDRLThumb.HTML = "html";
	
	FWDRLThumb.prototype = null;
	window.FWDRLThumb = FWDRLThumb;
}(window));