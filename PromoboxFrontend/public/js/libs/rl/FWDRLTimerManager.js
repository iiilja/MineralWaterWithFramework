/* Slide show time manager */
(function(window){
	
	var FWDRLTimerManager = function(delay){
		
		var self = this;
		var prototpype = FWDRLTimerManager.prototype;
		
		this.timeOutId;
		this.delay = delay;
		this.isStopped_bl = true;
		
		self.stop = function(){
			if(self.isStopped_bl) return;
			self.pause();
			self.isStopped_bl = true;
			self.dispatchEvent(FWDRLTimerManager.STOP);
		};
		
		self.start = function(){
			if(!self.isStopped_bl) return;
			self.isStopped_bl = false;
			
			self.timeOutId = setTimeout(self.onTimeHanlder, self.delay);
			self.dispatchEvent(FWDRLTimerManager.START);
		};
		
		self.pause = function(){
			if(self.isStopped_bl) return;
			clearTimeout(self.timeOutId);
			self.dispatchEvent(FWDRLTimerManager.PAUSE);
		};
		
		self.resume = function(){
			if(self.isStopped_bl) return;
			clearTimeout(self.timeOutId);
			self.timeOutId = setTimeout(self.onTimeHanlder, self.delay);
			self.dispatchEvent(FWDRLTimerManager.RESUME);
		};
		
		self.onTimeHanlder = function(){
			self.dispatchEvent(FWDRLTimerManager.TIME);
		};
	};

	FWDRLTimerManager.setProtptype = function(){
		FWDRLTimerManager.prototype = new FWDRLEventDispatcher();
	};
	
	FWDRLTimerManager.START = "start";
	FWDRLTimerManager.STOP = "stop";
	FWDRLTimerManager.RESUME = "resume";
	FWDRLTimerManager.PAUSE = "pause";
	FWDRLTimerManager.TIME = "time";
	
	FWDRLTimerManager.prototype = null;
	window.FWDRLTimerManager = FWDRLTimerManager;
	
}(window));