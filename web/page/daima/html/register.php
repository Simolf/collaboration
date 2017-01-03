<?php
$userPhone = $_POST['userPhone'];
$myPhone = "13728737324";  //测试已存在id

if($userPhone == ""){	
}else if($userPhone == $myPhone){
	echo "<script>location.href='login.html?userPhone=$userPhone';</script>"; // 跳转到登录页面
}else{
	echo "<script>location.href='register.html?userPhone=$userPhone';</script>";
}

?>