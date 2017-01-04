var localhost = "../../../../";
var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];

$(function(){
		//头部导航
	var $document = $(document),
	    $headNav = $('#myHeader .header-nav'),
	    $searchBtn = $('#myHeader .search'),
	    $searchInput = $('#myHeader .search-input');

	$document.on('click',function(e){
		if(e.target === $searchBtn[0]){
			$searchInput.val("")
			            .toggleClass('show');
		}else if(e.target === $searchInput[0]){
			$searchInput.addClass('show');
		}else{
			$searchInput.val("")
			            .removeClass('show');
		}
		return false;
	});
	$headNav.on('click','li',function(){
		$(this).addClass('active')
		       .siblings().removeClass('active');
		if($(this).children("a").text() === "项目"){
			window.location.href = "project.html";
		}
		return false;
	});

	$.ajax({
		url: localhost+"collaboration/getMessageList",
		type: "GET",
		data:{
			userId: userId
		}
	})
	.done(function(data){
		data = JSON.parse(data);
		console.log(data);
		if(data.status === 200){
			var tpl = document.getElementById("tpl").innerHTML;
	    	var html = juicer(tpl,data);
	    	$(".inform_list").append(html);
		}
	})

})