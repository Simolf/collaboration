$(function(){
	var taskId;
	$("#project-tab-content").on('mousedown',".tasks li",function(){
		taskId = $(this).attr("data-taskId");
		$(this).draggable({
			appendTo: "body",
			helper: "clone",
			snap: "#project-tab-content .tasks"
		});
	}).on('mouseup',".tasks",function(){
		$(this).droppable({
			activeClass: "ui-state-default",
			hoverClass: "ui-state-hover",
			accept: ":not(.ui-sortable-helper)",
			drop: function(e,ui){
				var status;
				if($(e.target).hasClass("task1")){
					status = 1;
				}else if($(e.target).hasClass("task2")){
					status = 2;
				}else if($(e.target).hasClass("task3")){
					status = 3;
				}
				//console.log(status);
				 $("<li data-taskId="+taskId+"></li>").append($(ui.draggable).html()).appendTo(this);
				 $(ui.draggable).remove();
				 $.ajax({
				 	url: localhost+"collaboration/updateTaskId",
				 	type: "GET",
				 	data:{
				 		taskId: taskId,
				 		status: status
				 	}
				 })
				 .done(function(data){
				 	data = JSON.parse(data);
				 	console.log(data);
				 	if(data.status === 200){
				 		console.log("拖成功");
				 	}
				 });
			}
		}).sortable({
			items: "li",
			sort: function(){
				$(this).removeClass("ui-state-default");
			}
		});
	})
	
})