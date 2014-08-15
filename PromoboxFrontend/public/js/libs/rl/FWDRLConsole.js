/* Thumb */
(function (window){
	
	var FWDRLConsole = function(){
		
		var self  = this;
		var prototype = FWDRLConsole.prototype;
		
		this.main_do = null;
	
		this.init = function(){
			this.setupScreen();
			window.onerror = this.showError;
			this.screen.style.zIndex = 99999999999999999999;
			setTimeout(this.addConsoleToDom, 100);
			setInterval(this.position, 100);
		};
		
		this.position = function(){
			var scrollOffsets = FWDRLUtils.getScrollOffsets();
			self.setX(scrollOffsets.x);
			self.setY(scrollOffsets.y + 30);
		};
		
		this.addConsoleToDom  = function(){
			if(navigator.userAgent.toLowerCase().indexOf("msie 7") != -1){
				document.getElementsByTagName("body")[0].appendChild(self.screen);
			}else{
				document.documentElement.appendChild(self.screen);
			}
		};
		
		/* setup screens */
		this.setupScreen = function(){
			this.main_do = new FWDRLDisplayObject("div", "absolute");
			this.main_do.setOverflow("auto");
			this.main_do.setWidth(300);
			this.main_do.setHeight(200);
			this.setWidth(300);
			this.setHeight(200);
			this.main_do.setBkColor("#FFFFFF");
			this.addChild(this.main_do);
		};
		
		this.showError = function(message, url, linenumber) {
			var currentInnerHTML = self.main_do.getInnerHTML() + "<br>" + "JavaScript error: " + message + " on line " + linenumber + " for " + url;
			self.main_do.setInnerHTML(currentInnerHTML);
			self.main_do.screen.scrollTop = self.main_do.screen.scrollHeight;
		};
		
		this.log = function(message){
			var currentInnerHTML = self.main_do.getInnerHTML() + "<br>" + message;
			self.main_do.setInnerHTML(currentInnerHTML);  
			self.main_do.getScreen().scrollTop = 10000;
		};
		
		this.init();
	};
	
	/* set prototype */
    FWDRLConsole.setPrototype = function(){
    	FWDRLConsole.prototype = new FWDRLDisplayObject("div", "absolute");
    };
    
    FWDRLConsole.prototype = null;
	window.FWDRLConsole = FWDRLConsole;
}(window));