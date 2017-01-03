$(function(){
  
    //设置project-tab-content的高度
	var bodyHeight = $(document).height(),
	    height = bodyHeight-55-50-20;
	$("#project-tab-content").height(height);


	$("#project-tab-content").empty()
			                         .load("task.html");
	$.getScript('../js/render.js');
	$.getScript('../js/task.js');

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
		return false;
	});

	//项目页面中点击成员出现侧边栏
	var $projectNav = $("#project #project-nav"),
	    $projectNavUl = $("#project #project-nav > ul"),
	    $projectMembers = $("#project-nav .members"),
	    $projectInstall = $("#project-nav .project-install"),
	    $asideMembers = $("#project-nav .project-members"),
	    $inviteModal = $("#invite-members-modal"),
	    $inviteMembers = $("#project-nav .invite-members"),
	    $installModal = $("#project-install-modal"),
	    $installPreserve = $("#project-install-modal .preserve"),
	    $installDelete = $("#project-install-modal .delete"),
	    $close = $(".close");


	$projectNav.on('click',function(e){
		if(e.target === $projectMembers[0]){
			$(this).children(".project-members").show();
		}else if(e.target === $projectInstall[0]){
			$installModal.show();
		}else if($(e.target).text() === "任务"){
			$("#project-tab-content").empty()
			                         .load("task.html");
			$.getScript('../js/render.js');
			$.getScript('../js/task.js');
		}
	});
	$projectNavUl.on('click','li',function(){
		$(this).addClass('choose')
		       .siblings().removeClass('choose');
	});
	$asideMembers.on('click',function(e){
		for(var i=0;i<$close.length;i++){
			if(e.target === $close[i]){
				$(this).hide();
				break;
			}
		}
		if(e.target === $inviteMembers[0]){
			$inviteModal.show();
		}
	});
	$inviteModal.on('click',function(e){
		for(var i=0;i<$close.length;i++){
			if(e.target === $close[i]){
				$(this).hide();
				break;
			}
		}
	});
	$installModal.on('click',function(e){
		for(var i=0;i<$close.length;i++){
			if(e.target === $close[i]){
				$(this).hide();
				break;
			}
		}
		if(e.target === $installPreserve[0]){
			alert("保存成功！");
		}else if(e.target === $installDelete[0]){
			var isDelete = confirm("您确定要删除这个项目吗？");
		}
	});

	

})