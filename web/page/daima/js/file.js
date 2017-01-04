var localhost = "../../../../";
var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];
var proId = document.cookie.split(";")[2].split("=")[1];

$(function(){
	$.ajax({
		url: localhost+"collaboration/getFileList",
		type: "GET",
		data:{
			projectId: proId
		}
	})
	.done(function(data){
		data = JSON.parse(data);
		console.log(data);
		var tpl = document.getElementById("tpl2").innerHTML;
    	var html = juicer(tpl,data);
    	$(".file-list").append(html);
	})
	// $("#project-tab-content").load("file.html");

	//单选
	$("li:not(.file-tab) .select_img").click(function(){
		var src = $(this).attr("src");
		if(src.indexOf("selected") > 0){
			$(this).attr("src","../img/select.png");
		}else{
			$(this).attr("src","../img/selected.png");
		}
	});

	//全选
	$(".file-tab .select_img").click(function(){
		var src = $(this).attr("src");
		if(src.indexOf("selected") > 0){
			$(this).attr("src","../img/select.png");
			$(".file-tab").siblings().find(".select_img").attr("src","../img/select.png");

			$(".file-tab .name > span").show();
			$(".file-tab .allfile").hide();
		}else{
			$(this).attr("src","../img/selected.png");
			$(".file-tab").siblings().find(".select_img").attr("src","../img/selected.png");

			$(".file-tab .name > span").hide();
			$(".file-tab .allfile").show();
		}
	});

	//上传文件
	$(".upload").on("click",function(){
		upLoadFile();
	});

	//添加文件
	function upLoadFile(){
		$(".upload_file").on("change",function(){
			var file_src = $(this).val();
			if(file_src != ""){
				var file = $(this).val().split("\\");
				var length = file.length;
	    		var html = "";
				html = "<li>"
					+"<div>"
					+"<hr class='line-list'>"
					+"<img class='select_img' src='../img/select.png'>"
					+"<div class='name'><img class='file' src='../img/file.png'><span class='file_name'>"+file[length-1]+"</span></div>"
					+"<span class='size'>大小</span>"
					+"<span class='creator'>创建者</span>"
					+"<span class='update'>更新时间</span>"
					+"<img class='load_img' src='../img/load_no.png' title='下载'>"
					+"<img class='rename_img' src='../img/change_no.png' title='重命名'>"
					+"<img class='del_img' src='../img/del_no.png' title='删除'>"
					+"</div>"
					+"</li>";
				$(".file-list").append(html);
				$(".upload_file").val("");    //清空

				//提示上传成功
				$("#filetip").show(function(){
					$(this).fadeOut(2000);
				});
			}
			
		});
	}

	//下载单个文件
	$(".load_img").on("click",function(){
		var file_name = $(this).siblings(".name").find(".file_name").text();
		console.log(file_name);   //将文件名传到后台 
		//下载成功提示
		$("#loadtip").show(function(){
			$(this).fadeOut(2000);
		});
	});
	$(document).on('click',function(e){
		if(e.target === $(".load_img")[0]){
			var filePath = $(e.target).parents("li").attr("data-src");
			var fileName = $(e.target).parents("li").find(".file_name").text();
			//console.log(fileName);
			$.ajax({
				url: localhost+"download",
				type: "GET",
				data:{
					filePath: filePath,
					fileName: fileName
				}
			})
			.done(function(data){
				console.log(data);
			})
		}
	})

	//下载所有文件
	$(".all_load").on("click",function(){
		var isLoad = $(".file-tab .select_img").attr("src");
		if(isLoad.indexOf("selected") > 0 && $(".file-list li:not(.file-tab)").length != 0){  //是否全选 or 是否为空
			var file_name = "";
			 $(".name .file_name").each(function(){
			    file_name = $(this).text();
			    console.log(file_name);   //将文件名传到后台 
			  });
			//下载成功提示
			$("#loadtip").show(function(){
				$(this).fadeOut(2000);
			});
		}  
	});

	//重命名input
	$(".rename_img").on("click",function(){
		var $rename_img = $(this);
		var $rename_box = $(this).siblings(".name").find(".rename_box");
		// console.log($rename_box);
		$(this).siblings(".name").find(".file_name").hide();
		$rename_box.css("display","inline-block");
		$rename_box.find(".rename").focus();
	});

	//重命名文件
	$(".btn_confirm").on("click",function(){
		var rename = $.trim($(this).prev().val());
		if(rename == ""){
			$(this).prev().focus();
		}else{
			// $(this).parent(".rename_box").submit();  //表单提交？
			$(this).parent(".rename_box").hide();
			$(this).parent(".rename_box").siblings(".file_name").text(rename);
			$(this).parent(".rename_box").siblings(".file_name").show();
		}
	});

	//取消重命名
	$(".btn_cancel").on("click",function(){
		$(this).parent(".rename_box").hide();
		$(this).parent(".rename_box").siblings(".file_name").show();
	});

	//弹出删除窗口
	var $del_img;
	$(".del_img").on("click",function(){
		$del_img = $(this);
		$("#popdelete").show();
	});

	//关闭删除窗口
	$("#popdelete .closetip").on("click",function(){
		$("#popdelete").hide();
	});

	//删除文件
	$("#del").on("click",function(){
		if($del_img){
			$del_img.parents("li").remove();
			$("#popdelete").hide();

			//提示删除成功
			$("#projecttip").show();
			$(function(){
				setTimeout(function(){
					$("#projecttip").fadeOut(500);
				},2000);
			});
		}
	});

	//删除所有文件弹框
	$(".all_del").on("click",function(){
		var isDel = $(".file-tab .select_img").attr("src");
		if(isDel.indexOf("selected") > 0 && $(".file-list li:not(.file-tab)").length != 0){  //是否全选 or 是否为空
			$("#alldelete").show();
		}  
	});

	//关闭删除窗口
	$("#alldelete .closetip").on("click",function(){
		$("#alldelete").hide();
	});

	//删除所有文件
	$("#btn_del").on("click",function(){
		$(".file-list li:not(.file-tab)").remove();
		$("#alldelete").hide();
		$(".file-tab .select_img").attr("src","../img/select.png");

		//提示删除成功
		$("#projecttip").show();
		$(function(){
			setTimeout(function(){
				$("#projecttip").fadeOut(500);
			},2000);
		});
	});

	//提示删除成功关闭窗口
	$("#projecttip .closetip").click(function(){
		$("#projecttip").hide();
	});

});