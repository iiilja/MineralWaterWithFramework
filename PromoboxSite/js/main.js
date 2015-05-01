$(document).ready(function(){
	
	$('input, select').styler();

	tabs(".tab",".tab-menu");

	$("#screenshot-carousel").owlCarousel({
 
      navigation : true, // Show next and prev buttons
      slideSpeed : 800,
      paginationSpeed : 800,
      singleItem: true,
  	});

  	// .main-nav

	$('.menu-btn').click(function(e){
		e.preventDefault();
		$('.main-nav').slideToggle();
	});

	$('.menu-btn-tabs').click(function(e){
		e.preventDefault();
		$('.tab-menu ul').slideToggle();
	});


	$(document).on('click', function(e) {
		if (!$(e.target).closest('.main-nav,.menu-btn').length) {
			$('.main-nav').slideUp();
		}
	});

	$(document).on('click', function(e) {
		if (!$(e.target).closest('.menu-btn-tabs').length) {
			$('.tab-menu ul').slideUp();
		}
	});

	// ===
	$("a.anchorLink").anchorAnimate();
	
	$("#change-lang").change(function(e) {
		$("[data-translate]").jqTranslate('index', {forceLang: this.value});
	});
        
        
});


function tabs(tab_wrap,tab_nav) {
	$(tab_nav).find('a').click(function(e){
		e.preventDefault();
		var tab = $(this).attr('href');

		$(tab_nav).find('a').removeClass('active');
		$(this).addClass('active');

		$(tab_wrap).removeClass('active');
		$(tab).addClass('active');
	});
}


jQuery.fn.anchorAnimate = function(settings) {

 	settings = jQuery.extend({
		speed : 1100
	}, settings);	
	
	return this.each(function(){
		var caller = this
		$(caller).click(function (event) {	
			event.preventDefault()
			var locationHref = window.location.href
			var elementClick = $(caller).attr("href")
			
			var destination = $(elementClick).offset().top - parseInt(50);
			$("html:not(:animated),body:not(:animated)").animate({ scrollTop: destination}, settings.speed, function() {
				window.location.hash = elementClick
			});
			$('.main-nav').slideUp();
		  	return false;
		})
	})
}