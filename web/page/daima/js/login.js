//全局url loaclhost
var localhost = "../../../../";

var userPhone;  //用户手机号
var password;  //用户密码

function getUserPhone(){   //获取已注册的手机号，直接登录
	var url = window.location.href;
	var phone;
	if(url.indexOf("?") == -1){
		return false;
	}else{
		phone = url.split("userPhone=")[1];
		return phone;
	}	
}

$(document).ready(function(){
	$(".inputBox:eq(0)").hide();
	userPhone = getUserPhone();  
	// console.log(userPhone);
	if(userPhone == false || userPhone == undefined){
	}else{
		$(".inputBox:eq(0)").show();
		$("#userPhone").val(userPhone);
	}
});

//注册页面时输入个人信息
$("#loginBtn").on('click',function(){
	userPhone = $.trim($("#userPhone").val());
	password = $("#password").val();
	if(userPhone == ""){
		$(".inputBox:eq(3) p").text("手机号不能为空！");
		$(".inputBox:eq(3)").show();
		return false;   //阻止表单提交 
	}else if(!/^1[34578]\d{9}$/.test(userPhone)){
		$(".inputBox:eq(3) p").text("请输入正确的手机号！");
		$(".inputBox:eq(3)").show();
		return false;   //阻止表单提交
	}else{
		if(password == ""){
			$(".inputBox:eq(3) p").text("密码不能为空！");
			$(".inputBox:eq(3)").show();
			return false;  //阻止表单提交
		}else{
  			// 表单提交
  			$(".inputBox:eq(3)").hide();
  			return false;
		}
	}
	
});

//登录传值
$(function(){
	userPhone = $("#userPhone").val();
	password = $("#password").val();
	$(".loginBtn").on('click',function(){
		if(userPhone && password){
			$.ajax({
				type: "POST",
				url: localhost+"collaboration/login",
				data: {
					userId: userPhone,
					password: password
				}
			})
			.done(function(data){
				data = JSON.parse(data);
				console.log(data);
				if(data.status === 200){
					window.location.href = "index0.html";
					document.cookie = "userId="+data.user.id;
			        document.cookie = "userName="+data.user.userName;
			        console.log(document.cookie);
				}
			});
		}
	});
	
})