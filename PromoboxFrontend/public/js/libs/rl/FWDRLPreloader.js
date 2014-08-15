/* Thumb */
(function (window){
	
	var FWDRLPreloader = function(
			imageSource_img, 
			segmentWidth, 
			segmentHeight, 
			totalSegments, 
			animDelay,
			skipFirstFrame){
		
		var self  = this;
		var prototype = FWDRLPreloader.prototype;
		
		this.imageSource_img = imageSource_img;
		this.image_sdo = null;
		
		this.segmentWidth = segmentWidth;
		this.segmentHeight = segmentHeight;
		this.totalSegments = totalSegments;
		this.animDelay = animDelay || 300;
		this.count = 0;
		
		this.delayTimerId_int;
		this.isShowed_bl = true;
		this.skipFirstFrame_bl = skipFirstFrame;
		
		//###################################//
		/* init */
		//###################################//
		self.init = function(){
			self.getStyle().pointerEvents = "none";
			self.setWidth(self.segmentWidth);
			self.setHeight(self.segmentHeight);
		
			self.image_sdo = new FWDRLDisplayObject("img");
			self.image_sdo.setScreen(self.imageSource_img);
			self.image_sdo.hasTransform3d_bl = false;
			self.image_sdo.hasTransform2d_bl = false;
			self.addChild(self.image_sdo);
			
			self.hide(false);
		};
		
		//###################################//
		/* start / stop preloader animation */
		//###################################//
		self.start = function(){
			if(self == null) return;
			clearInterval(self.delayTimerId_int);
			self.delayTimerId_int = setInterval(self.updatePreloader, self.animDelay);
		};
		
		self.stop = function(){
			clearInterval(self.delayTimerId_int);
			self.image_sdo.setX(0);
		};
		
		self.updatePreloader = function(){
			if(self == null) return;
			self.count++;
			if(self.count > self.totalSegments - 1){
				if(self.skipFirstFrame_bl){
					self.count = 1;
				}else{
					self.count = 0;
				}	
			}
			
			var posX = self.count * self.segmentWidth;
			self.image_sdo.setX(-posX);
		};
		
		
		//###################################//
		/* show / hide preloader animation */
		//###################################//
		self.show = function(){
			self.setVisible(true);
			self.start();
			FWDRLTweenMax.killTweensOf(self);
			FWDRLTweenMax.to(self, .8, {alpha:1, ease:Quart.easeOut});
			self.isShowed_bl = true;
		};
		
		self.hide = function(animate){
			if(!self.isShowed_bl) return;
			FWDRLTweenMax.killTweensOf(self);
			if(animate){
				FWDRLTweenMax.to(self, .8, {alpha:0, onComplete:self.onHideComplete, ease:Quart.easeOut});
			}else{
				self.setVisible(false);
				self.setAlpha(0);
			}
			self.isShowed_bl = false;
		};
		
		self.onHideComplete = function(){
			self.stop();
			self.setVisible(false);
			self.dispatchEvent(FWDRLPreloader.HIDE_COMPLETE);
		};

		self.init();
	};
	
	/* set prototype */
    FWDRLPreloader.setPrototype = function(){
    	FWDRLPreloader.prototype = new FWDRLDisplayObject("div");
    };
    
    FWDRLPreloader.HIDE_COMPLETE = "hideComplete";
    
    FWDRLPreloader.prototype = null;
	window.FWDRLPreloader = FWDRLPreloader;
}(window));