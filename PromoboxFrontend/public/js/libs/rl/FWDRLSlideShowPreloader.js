/* Slideshow preloader */
(function (window){
	
	var FWDRLSlideShowPreloader = function(imageSource_img, segmentWidth, segmentHeight, totalSegments, duration){
		
		var self  = this;
		var prototype = FWDRLSlideShowPreloader.prototype;
		
		this.imageSource_img = imageSource_img;
		this.image_do = null;
		this.tweenObj = {currentPos:0};
		
		this.segmentWidth = segmentWidth;
		this.segmentHeight = segmentHeight;
		this.totalSegments = totalSegments;
		this.duration = duration/1000;
		this.delayTimerId_int;
		
		//###################################//
		/* init */
		//###################################//
		self.init = function(){
			self.setWidth(self.segmentWidth);
			self.setHeight(self.segmentHeight);
		
			self.image_do = new FWDRLDisplayObject("img");
			self.image_do.setScreen(self.imageSource_img);
			self.addChild(self.image_do);
			self.onUpdateHandler();
			//self.hide(false);
		};
		
		//###################################//
		/* start / stop preloader animation */
		//###################################//
		self.animShow = function(){
			FWDRLTweenMax.killTweensOf(self.tweenObj);
			self.currentPos = 0;
			FWDRLTweenMax.to(self.tweenObj, self.duration, {currentPos:1, ease:Linear.easeNone, onUpdate:self.onUpdateHandler});
		};
		
		self.animHide = function(){
			FWDRLTweenMax.killTweensOf(self.tweenObj);
			FWDRLTweenMax.to(self.tweenObj, .8, {currentPos:0, onUpdate:self.onUpdateHandler});
		};
		
		self.animReset = function(){
			FWDRLTweenMax.killTweensOf(self.tweenObj);
			self.tweenObj.currentPos = 0;
			self.onUpdateHandler();
		};
		
		self.onUpdateHandler = function(){
			var posX = Math.round((self.tweenObj.currentPos/1) * (self.totalSegments - 1)) * self.segmentWidth;
			self.image_do.setX(-posX);
		};
		
		//###################################//
		/* show / hide preloader animation */
		//###################################//
		self.show = function(){
			self.setVisible(true);
			if(self.opacityType == "opacity"){
				FWDRLTweenMax.killTweensOf(self.image_do);
				FWDRLTweenMax.to(self.image_do, 1, {alpha:1});
			}else{
				self.setWidth(self.segmentWidth);
			}
		};
		
		self.hide = function(animate){
			if(animate){
				if(self.opacityType == "opacity"){
					FWDRLTweenMax.killTweensOf(self.image_do);
					FWDRLTweenMax.to(self.image_do, 1, {alpha:0, onComplete:hideCompleteHandler});
				}else{
					self.setWidth(0);
				}
			}else{
				self.setVisible(false);
				if(self.opacityType == "opacity"){
					FWDRLTweenMax.killTweensOf(self.image_do);
					self.image_do.setAlpha(0);
				}else{
					self.setWidth(0);
				}
			}
		};
		
		self.hideCompleteHandler = function(){
			self.setVisible(false);
		};
	
		self.init();
	};
	
	FWDRLSlideShowPreloader.setPrototype = function(){
		FWDRLSlideShowPreloader.prototype = new FWDRLDisplayObject("div");
	};
	
	FWDRLSlideShowPreloader.prototype = null;
	window.FWDRLSlideShowPreloader = FWDRLSlideShowPreloader;
	
}(window));
