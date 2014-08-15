/* Info screen */
(function (window){
	
	var FWDRLInfo = function(parent){
		
		var self = this;
		var prototype = FWDRLInfo.prototype;
		
		this.bk_do = null;
		this.textHolder_do = null;
		
		this.show_to = null;
		this.isShowed_bl = false;
		this.isShowedOnce_bl = false;
		this.allowToRemove_bl = true;
		
		//#################################//
		/* init */
		//#################################//
		this.init = function(){
			self.setResizableSizeAfterParent();
			
			self.bk_do = new FWDRLDisplayObject("div");
			//self.bk_do.setAlpha(.8);
			self.bk_do.setBkColor("#FF0000");
			self.getStyle().width = "80%";
			self.addChild(self.bk_do);
			
			self.textHolder_do = new FWDRLDisplayObject("div");
			self.textHolder_do.getStyle().wordWrap = "break-word";
			self.textHolder_do.getStyle().padding = "10px";
			self.textHolder_do.getStyle().paddingBottom = "0px";
			self.textHolder_do.getStyle().lineHeight = "18px";
			
			//self.textHolder_do.setBkColor("#FF0000");
			self.textHolder_do.getStyle().color = "#000000";
			self.addChild(self.textHolder_do);
		};
		
		this.showText = function(txt){
			if(!self.isShowedOnce_bl){
				if(self.screen.addEventListener){
					self.screen.addEventListener("click", self.closeWindow);
				}else if(self.screen.attachEvent){
					self.screen.attachEvent("onclick", self.closeWindow);
				}
				self.isShowedOnce_bl = true;
			}
			
			self.setVisible(false);
			//if(self.allowToRemove_bl){
			//	self.textHolder_do.setInnerHTML(txt  + "<p style='margin:0px; padding-bottom:10px;'><font color='#FFFFFF'>Click or tap to close this window.</font>");
		//	}else{
				self.textHolder_do.getStyle().paddingBottom = "10px";
				self.textHolder_do.setInnerHTML(txt);
			//}
			
			clearTimeout(self.show_to);
			self.show_to = setTimeout(self.show, 60);
			setTimeout(function(){
				self.positionAndResize();
			}, 10);
		};
		
		this.show = function(){
			self.isShowed_bl = true;
			self.setVisible(true);
			self.positionAndResize();
		};
		
		this.positionAndResize = function(){
			/*
			if(parent.main_do){
				self.stageWidth = parent.main_do.w;
				self.stageHeight = parent.main_do.h;
			}else{
				self.stageWidth = parent.stageWidth;
				self.stageHeight = parent.stageHeight;
			}
		
			var finalW = Math.min(600, self.stageWidth - 40);
			var finalH = self.textHolder_do.screen.offsetHeight;
			var finalX = parseInt((self.stageWidth - finalW)/2) - 10;
			var finalY = parseInt((self.stageHeight - finalH)/2);
			*/
			finalX = 0;
			finalY = 0;
			
			self.bk_do.setWidth(self.textHolder_do.getWidth());
			self.bk_do.setHeight(self.textHolder_do.getHeight());
			//self.setWidth(self.textHolder_do.getWidth());
			self.setHeight(self.textHolder_do.getHeight());
			self.textHolder_do.setX(finalX);
			self.textHolder_do.setY(finalY);
			//self.textHolder_do.setWidth(finalW);
	
		};
		
		this.closeWindow = function(){
			if(!self.allowToRemove_bl) return;
			self.isShowed_bl = false;
			clearTimeout(self.show_to);
			try{parent.main_do.removeChild(self);}catch(e){}
		};
		
		this.init();
	};
		
	/* set prototype */
	FWDRLInfo.setPrototype = function(){
		FWDRLInfo.prototype = new FWDRLDisplayObject("div", "relative");
	};
	
	FWDRLInfo.prototype = null;
	window.FWDRLInfo = FWDRLInfo;
}(window));