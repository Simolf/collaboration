$(function(){
	$("#project-tab-content").on('mousedown',".tasks li",function(){
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
				 $("<li></li>").append($(ui.draggable).html()).appendTo(this);
				 $(ui.draggable).remove();
			}
		}).sortable({
			items: "li",
			sort: function(){
				$(this).removeClass("ui-state-default");
			}
		});
	})
	
})