var localhost = "../../../../";

var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];

function renderIndex(){
		$.ajax({
			url: localhost+"collaboration/index",
			type: "GET",
			data: {
				userId : userId
			}
		})
		.done(function(data){
			data = JSON.parse(data);
			//console.log(data);
			if(data.status === 200){
				var tpl = document.getElementById("tpl").innerHTML;
				var html = juicer(tpl,data);
				$(".prolist").prepend(html);
			}
		});
	}

$(function(){
	var setProName,setProSum;  //项目名称和介绍

	 // 进入某个具体项目
    $(".prolist").on("click",".pro",function(){
    	var proId = $(this).parent().attr("data-taskId");
    	//console.log(proId);
    	document.cookie = "proId="+proId;
    	window.location.href = "project.html";
    })

	//点击项目设置图标
	$(".prolist").on("click",".pro img",function(){
		var $this = $(this);
		var taskId = $(this).parents(".task_li").attr("data-taskId");
		var creatorId;

		$.ajax({
	    	url: localhost+"collaboration/getProjectById",
	    	type: "GET",
	    	data: {
	    		userId: userId,
	    		projectId: taskId
	    	}
	    })
	    .done(function(data){
	    	data = JSON.parse(data);
	    	//console.log(data);
	    	if(data.status === 200){
	    		creatorId = data.project.creatorId;
	    		setProName = data.project.name;
	    		setProSum = data.project.brief;
	    		 if(userId == creatorId){
			    	$(".hidebg").show();     //灰色背景
					$(".popsetting").show();   

					$("#setproname").val(setProName);
					$("#setprosum").val(setProSum);  

					$(".popsetting").attr("data-taskId",taskId);

			    }
	    	}
	    })

		//点击删除
		//$("#delpro").unbind("click").click($this,function(){
			//$(".hidebg1").show();     //2级灰色背景
		//	$(".popdelete").show();
			
			//删除项目
			// $("#del").on("click",$this,function(){
			// 	$this.parents("li").remove();

			// 	$(".popdelete").hide();
			// 	$(".popsetting").hide();  
			// 	$(".hidebg1").hide();
			// 	$(".hidebg").hide();

			// 	//提示删除成功
			// 	$(".poptip").show();
			// 	$(function(){
			// 		setTimeout(function(){
			// 			$(".poptip").fadeOut(500);
			// 		},2000);
			// 	});
			// });
		//});

		//点击保存
		$("#save").unbind("click").click($this,function(){
			setProName = $.trim($("#setproname").val());
			setProSum = $.trim($("#setprosum").val());	
			if(setProName == ""){
				$("#setproname").focus();
			}else if(setProSum == ""){
				$("#setprosum").focus();
			}
			if(setProName != "" && setProSum != ""){
				// $("#setform").submit();  //表单提交
				// $this.prev().text(setProName);
				// $this.next("p").text(setProSum);

				$(".hidebg").hide();     
				$(".popsetting").hide();   

				var taskId = $(this).parents(".popsetting").attr("data-taskId");
				//console.log(taskId);
				$.ajax({
					url: localhost+"collaboration/updateProject",
					type: "GET",
					data: {
						creatorId: userId,
						projectId: taskId,
						projectName: setProName,
						brief: setProSum
					}
				})
				.done(function(data){
					data = JSON.parse(data);
					//console.log(data);
					if(data.status === 200){
						$(".task_li").remove();
						renderIndex();
					}
				})
			}
		});
	});

	//关闭删除窗口
	$(".popdelete .closetip").click(function(){
		$(".popdelete").hide();
		$(".hidebg1").hide();
	});

	//提示删除成功关闭窗口
	$(".poptip .closetip").click(function(){
		$(".poptip").hide();
	});

	//关闭设置窗口
	$(".popsetting .closetip").click(function(){
		$(".popsetting").hide();
		$(".hidebg").hide();
	});

	//弹出创建项目窗口
	$(".newpro > div").on("click",function(){
		$(".hidebg").show();
		$(".popwindow").show();
	});

	//表单提交
	$("#confirm").on("click", function(){
		var proName = $.trim($("#proname").val());
		if(proName == ""){
			$("#proname").focus();
		}else{
			// $("#proform").submit();  //表单提交
			$("#proname").val("");   //清空输入框 

			$.ajax({
				url: localhost+"collaboration/createProject",
				type: "GET",
				data: {
					userId: userId,
					projectName: proName
				}
			})
			.done(function(data){
				data = JSON.parse(data);
				//console.log(data);
				if(data.status === 200){
					$(".popwindow").hide();
					$(".hidebg").hide();
					$(".task_li").remove();
					renderIndex();
				}
			})

		}
	});

	//关闭创建窗口
	$(".popwindow .closetip").click(function(){
		$(".popwindow").hide();
		$(".hidebg").hide();
	});
	
});

$(function(){
	if(userId && userName){
		$(".user-name").text(userName);
	}

	//渲染项目页面
	
	renderIndex();


	//删除某个项目
	$("#delpro").on("click",function(){
		var taskId = $(this).parents(".popsetting").attr("data-taskId");
		
		$.ajax({
			url: localhost+"collaboration/deleteProject",
			type: "GET",
			data:{
				userId: userId,
				projectId: taskId
			}
		})
		.done(function(data){
			data = JSON.parse(data);
			//console.log(data);
			if(data.status === 200){
				
			 	$(".popdelete").hide();
				$(".popsetting").hide();  
			 	$(".hidebg1").hide();
			 	$(".hidebg").hide();

			// 	//提示删除成功
			 	$(".poptip").show();
			 	$(function(){
			 		setTimeout(function(){
			 			$(".poptip").fadeOut(500);
			 		},2000);
			 	});

				$(".task_li").remove();
				renderIndex();
			}
		})
	});

   
})