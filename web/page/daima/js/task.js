var localhost = "../../../../";

var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];
var proId = document.cookie.split(";")[2].split("=")[1];

$(function(){

    var $document = $(document),
        $close = $(".close");

		//项目 中 任务栏
	var $tasks = $("#project-task .tasks"),
	    $projectTasks = $("#project-task"),
	    $addTaskA = $("#project-task .add-task a"),
	    $addTasks = $("#project-task .add-task"),
	    $addTaskContent = $("#project-task .add-task-content");

    // $tasks.each(function(){
    // 	var taskNum = $(this).children("li").length;
    // 	if(taskNum > 0){
    // 		$(this).siblings("h1").children(".num").text(taskNum).show();
    // 	}
    // });
    $document.on('click',function(e){
    	if($(e.target).parents(".add-task-content").length>0){
    		$("button.create-task").each(function(){
    			var $addTaskC = $(e.target).parents(".add-task-content");
    			if(e.target != this){
		            $addTaskC.show()
		                     .siblings(".add-task").hide();
    			}else{
    				if($(this).siblings("textarea").val() == ""){
			    		alert("请输入任务内容！");
			    	}else{
			    		var text = $(this).siblings("textarea").val(),
			    		    $atc = $(this).parents(".add-task-content");
			    		$atc.siblings(".add-task").show()
			    		    .hide();
			    		$addTaskC.hide()
		                     .siblings(".add-task").show();
			    	}
    				
    			}
    		})
    	}else{
    		$addTasks.show()
    		         .siblings(".add-task-content").hide();
    		$addTasks.siblings(".add-task-content").find(".add-members").hide();
    	}

		 /*$addTaskA.each(function(){
		 	if(e.target === this){

	    		var $addTaskP = $(e.target).parent();
	            $addTaskP.hide()
	                     .siblings(".add-task-content").show();
	    	}
		 });*/
         if(e.target === $(this).find(".add-task a")[0]){
            var $addTaskP = $(e.target).parent();
                $addTaskP.hide()
                         .siblings(".add-task-content").show();
         }
    });

    var $addTaskParticipant = $(".add-task-content .participant"),
        $add = $(".add-task-content .add > span");

    $addTaskParticipant.children("ul").on("mouseover mouseout",'.participant-name',function(e){
    	if(e.type == "mouseover"){
    		$(e.target).parent().addClass('hover');
    	}else if(e.type == "mouseout"){
    		$(e.target).parent().removeClass('hover');
    	}   	
    });
    $addTaskParticipant.on('click',function(e){
    	$addTaskParticipant.children("ul").children("li").each(function(){
    		if(e.target === this){
    			$(this).remove();
    		}
    	});
   //  	$add.each(function(){
   //  		addMembersInit(this);
		 // 	if(e.target === this){
   //              console.log("ss");
	  //           $(this).siblings(".add-members").show();
	  //   	}else{
	  //   		$(this).siblings(".add-members").hide();
	  //   	}
		 // });
         //console.log(e.target);
        // console.log(this);
    });

    function addMembersInit(that){
    	var $participant = $(that).parent().siblings("li"),
    	    $allMembers = $(that).parent().find(".all-members").children("li");

    	$allMembers.each(function(){
    		$(this).removeClass("choose");
    	});
        
        var num = 0;

    	$participant.each(function(){
    		var id = $(this).attr("data-id");
    		$allMembers.each(function(){
    			if($(this).attr("data-id") === id){
    				num++;
    				$(this).addClass("choose");
    			}
    		});
    		if($allMembers.length === (num+1)){
    			$allMembers.eq(0).addClass("choose");
    		}
    	});
    }

    //添加成员至参与者
    var $allMembers = $(".add-task-content .all-members");
    // $(".add-task-content .all-members").on('click',function(e){
    // 	var that = this;
    // 	if(e.target === $(this).children("li")[0]){
    // 		$(this).children("li").each(function(){
    // 			if(!$(this).hasClass("choose")){
    // 				if($(this).attr("data-id") !== "0"){
    // 					addMembers(this,that);
    // 				}
    // 				$(this).addClass("choose");
    // 			}
    // 		});
    // 	}else if(!$(e.target).hasClass("choose")){
    // 		addMembers(e.target,that);
    // 		$(e.target).addClass("choose");

    // 		var num = 0;
    // 		$(this).children("li").each(function(){
    // 			if($(this).hasClass("choose")){
    // 				num++;
    // 			}
    // 		});
    // 		if($(this).children("li").length === (num+1)){
    // 			$(this).children("li").eq(0).addClass("choose");
    // 		}
    // 	}
    // });

/*     $(document).on('click',function(e){
     //var that = this;
     if(e.target === $(".add-task-content .all-members").children("li")[0]){
         $(".add-task-content .all-members").children("li").each(function(){
             if(!$(this).hasClass("choose")){
                 if($(this).attr("data-id") !== "0"){
                     addMembers(this,$(".add-task-content .all-members")[0]);
                 }
                 $(this).addClass("choose");
             }
         });
     }else if(!$(e.target).hasClass("choose")){
         addMembers(e.target,$(".add-task-content .all-members")[0]);
         $(e.target).addClass("choose");

         var num = 0;
         $(".add-task-content .all-members").children("li").each(function(){
             if($(".add-task-content .all-members").hasClass("choose")){
                 num++;
             }
         });
         if($(".add-task-content .all-members").children("li").length === (num+1)){
             $(".add-task-content .all-members").children("li").eq(0).addClass("choose");
         }
     }
    });*/


    
    function addMembers(target,that){
    	var $addLi = $(that).parents(".add"),
    	    text = $(target).text(),
    	    id = $(target).attr("data-id");

    	var $newSpan = $("<span class='members-name participant-name'></span>"),
            $newLi = $("<li></li>");
    	$newSpan.text(text);
        $newLi.append($newSpan)
              .attr("data-id",id)
              .insertBefore($addLi);
    }
    
    // 创建任务
    $addTaskContent.on('click','button',function(){
    	if($(this).siblings("textarea").val() == ""){
    		alert("请输入任务内容！");
    	}else{
    		var text = $(this).siblings("textarea").val(),
    		    $atc = $(this).parents(".add-task-content");
    		$atc.siblings(".add-task").show()
    		    .hide();
    		addTask(this);
    	}
    });
    function addTask(that){
    	var text = $(that).siblings("textarea").val(),
    	    $participants = $(that).siblings(".participant").find(".all-members").children("li"),
    	    $appendUl = $(that).parents(".add-task-content").siblings(".tasks");
    
        var $newSpan = $("<span class='task-content'></span>"),
            $newLi = $("<li></li>");
        $newSpan.text(text);
        $newLi.append($newSpan)
              .appendTo($appendUl);
    }

    // 展示任务内容
    var $projectTask = $("#project-task"),
        $taskInfoModal = $("#task-info-modal"),
        $taskDelete = $("#task-info-modal .delete");
    $projectTask.on('click',"ul.tasks li",function(){
        var taskId = $(this).attr("data-taskId");
        //点击查看任务细节
        $.ajax({
            url: localhost+"collaboration/getTaskDetailById",
            type: "GET",
            data: {
                projectId: proId,
                taskId: taskId
            }
        })
        .done(function(data){
            data = JSON.parse(data);
            console.log(data);
            $("#task-info-modal .content").text(data.task.taskContent);
            $("#task-info-modal .creator").text(data.task.creator);
            $("#task-info-modal .participant").text(data.task.participantName);
            $("#task-info-modal").attr("data-taskId",taskId);
            $taskInfoModal.show();
        });
    });

    $taskInfoModal.on('click',function(e){
        var taskId = $(this).attr("data-taskId");
		for(var i=0;i<$close.length;i++){
			if(e.target === $close[i]){
				$(this).hide();
				break;
			}
		}
		if(e.target === $taskDelete[0]){
			var isDelete = confirm("您确定要删除这个任务吗？");
			if(isDelete){
				$(this).hide();
                 //点击删除任务
                $.ajax({
                    url: localhost+"collaboration/deleteTaskById",
                    type: "GET",
                    data: {
                        userId: userId,
                        projectId: proId,
                        taskId: taskId
                    }
                })
                .done(function(data){
                    data = JSON.parse(data);
                    console.log(data);
                    if(data.status === 200){
                        window.location.reload();
                    }
                })
			}
		}
	});

    //点击查看任务细节
    // $.ajax(function(){
    //     url: "",
    //     type: "GET",
    //     data: {
    //         projectId: projectId,
    //         taskId: taskId
    //     }
    // })
    // .done(function(data){
    //     var json = {
    //         projectId: 1,
    //         taskId: 11,
    //         creatorName: "",
    //         taskContent: "",
    //         participantName: ""
    //     }
    // });

    //点击添加任务
    // $.ajax(function(){
    //     url: localhost+"collaboration/createTask",
    //     type: "POST",
    //     data: {
    //         userId: userId,
    //         projectId: proId,
    //         taskContent: taskContent,
    //         participantId: participantId
    //     }
    // })
    // .done(function(data){
    //     var json = {
    //         status: ""   //成功或失败
    //     }
    // });

    //点击删除任务
    // $.ajax(function(){
    //     url: "",
    //     type: "GET",
    //     data: {
    //         userId: userId,
    //         projectId: projectId,
    //         taskId: taskId
    //     }
    // })
    // .done(function(data){
    //     var json = {
    //         status: ""
    //     }
    // })


    $(document).on('click','.add-task-content .add > span',function(e){
        console.log(e.target);
        addMembersInit(e.target);
        $(e.target).siblings(".add-members").show();
    })

    $(document).on('click',function(e){
        if($(e.target).attr("data-p") === "pp"){
            var that = $(document).find(".all-members")[0];
            addMembers(e.target,that);
            $(".add-members").hide();
        }
        if(e.target === $(".create-task")[0]){
            var taskContent = $(".content").text();
            var participantId = $(".mm").attr("data-id");
             console.log(participantId);
            //点击添加任务
            // $.ajax({
            //     url: localhost+"collaboration/createTask",
            //     type: "POST",
            //     data: {
            //         userId: userId,
            //         projectId: proId,
            //         taskContent: taskContent,
            //         participantId: participantId
            //     }
            // })
            // .done(function(data){
            //     console.log(data);
            // });
        }
    })
 
})