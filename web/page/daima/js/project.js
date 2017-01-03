var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];

$(function(){
	if(userId && userName){
		$(".user-name").text(userName);
	}
})
