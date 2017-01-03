var localhost = "../../../../";

var userPhone;  //用户手机号

function getUserPhone(){   //获取手机号，直接登录
    var url = window.location.href;
    var phone;
    if(url.indexOf("?") == -1){
        return false;
    }else{
        phone = url.split("userPhone=")[1];
        return phone;
    }   
}

/*$(document).ready(function(){
    $(".inputBox:eq(1)").hide();
    userPhone = getUserPhone();  
    // console.log(userPhone);
    if(userPhone == false || userPhone == undefined){
         $("#registerPhone").show();
        //$("#registerForm").hide();
    }else{
        $("#registerPhone").hide();
       // $("#registerForm").show();
        $("#getUserPhone").text(userPhone);
        $(".registerBox").css("margin","120px auto");
    }
});*/

//注册页面时输入手机号
$("#userPhone").focus(function(){
	$(".inputBox:eq(1)").hide();
});

$("#firstBtn").on('click',function(){
	 userPhone = $.trim($("#userPhone").val());
	if(userPhone == ""){    //未输入手机号
		$(".inputBox:eq(1)").show();
        return false;  //阻止表单提交
	}else{
    	if(!(/^1[34578]\d{9}$/.test(userPhone))){
    		$(".inputBox:eq(1)").show();
            return false;  //阻止表单提交
    	}else{
    		$(".inputBox:eq(1)").hide();  //表单提交
            return false;

    	}
	}
});


//注册页面时输入个人信息
var userName,password,conPassword;

$("#userName").focus(function(){
    $("#name").hide();
});
$("#password").focus(function(){
    $("#pwd").hide();
    // $(".inputBox:eq(4)").show();
});
$("#conPassword").focus(function(){
    $("#conPwd").hide();
});

$("#secondBtn").on('click',function(){
     userName = $.trim($("#userName").val());
     password = $("#password").val();
     conPassword = $("#conPassword").val();
     if(userName == ""){
        $("#name").show();
     }
     if(password == ""){
        $("#pwd").show();
     }
     if(conPassword == ""){
        $("#conPwd").show();
     }
     if((password.length < 6 && password.length > 0) || (conPassword.length < 6 && conPassword.length > 0)){
        $("#error p").text("密码至少6位！");
         $("#error").show();
     }
     if(userName == "" || password == "" || conPassword == "" || password.length < 6 || conPassword.length < 6){
        return false;               //阻止表单提交
     }else if(password != conPassword && password != "" && conPassword != ""){
        console.log("error!");
        $("#error p").text("输入密码不一致！");
        $("#error").show();
        return false;               //阻止表单提交
     }else{
        // 表单提交
        $("#error").hide();
        var userId = $("#user").val();
        $.ajax({
            url: localhost+"collaboration/register",
            type: "POST",
            data: {
                userId: userId,
                userName: userName,
                password: password
            }
        })
        .done(function(data){
            data = JSON.parse(data);
            console.log(data);
            if(data.status === 200){
                window.location.href = "index0.html";
                document.cookie = "userId="+data.user.id;
                document.cookie = "userName="+data.user.name;
            }
        });
        return false;
     }
});

// 点击注册传值
$(function(){
    userPhone = $("#userPhone").val();
    $("#firstBtn").on('click',function(){
        if(userPhone){
            $.ajax({
                type: "POST",
                url: localhost+"collaboration/isRegister",
                data: {
                    userId: userPhone   //判断此手机号是否注册
                }
            })
            .done(function(data){
                data = JSON.parse(data);
                console.log(data);
                if(data.isExist){
                    window.location.href = "login.html";
                }else{
                    window.location.href = "register1.html";
                }
            });
        }
    });

    // var userId = $("#user").val();
    // var userName = $("#userName").val();
    // var password = $("#password").val();
    // $("#secondBtn").on("click",function(){
    //     if(userId && userName && password){
    //         $.ajax(function(){
    //             url: localhost+"collaboration/register",
    //             type: "POST",
    //             data: {
    //                 userId: userId,
    //                 userName: userName,
    //                 password: password
    //             }
    //         })
    //         .done(function(data){
    //             if(data.code === "success"){
    //                 window.location.href = "index0.html";
    //                 document.cookie = "userId="+data.user.id;
    //                 document.cookie = "userName="+data.user.name;
    //             }
    //         })
    //     }
    // })
    
})

