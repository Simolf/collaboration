var localhost = "../../../../";

var userId = document.cookie.split(";")[0].split("=")[1];
var userName = document.cookie.split(";")[1].split("=")[1];
var proId = document.cookie.split(";")[2].split("=")[1];

$(function(){
	// var json = {
	// 	user: {
	// 		id: 13728792027,
	// 		name: "肖丽霞"
	// 	},
	// 	project: {
	// 		creator: "肖丽霞",
	// 		creatorId: 13728792027,      //项目所属人
	// 		participant: [
	// 					{
	// 						id: "13728737324",
	// 						name: "彭俐"
	// 					},
	// 					{
	// 						id: "13728733333",
	// 						name: "李伟光"
	// 					},
	// 					{
	// 						id: "13728733323",
	// 						name: "黄云峰"
	// 					}
	// 					],
	// 		name: "项目一",
	// 		brief: "简介啊简介简介"
	// 	},
	// 	task: [
	// 		{
	// 			taskId: 1,
	// 			taskContent: "我是任务内容一",
	// 			createTime: "2016年12月12日",
	// 			creator: "肖丽霞",
	// 			creatorId: "13728792027",       //项目所属人
	// 			participant: [
	// 						{
	// 							id: "13728737324",
	// 							name: "彭俐"
	// 						},
	// 						{
	// 							id: "13728733333",
	// 							name: "李伟光"
	// 						}
	// 						],
	// 			projectId: 1,          //项目ID
 //                status: 1        //任务状态 1：待处理  2：进行中  3：已完成
	// 		},
	// 		{
	// 			taskId: 2,
	// 			taskContent: "我是任务内容er",
	// 			createTime: "2016年12月12日",
	// 			participant: [
	// 						{
	// 							id: "13728737324",
	// 							name: "彭俐"
	// 						},
	// 						{
	// 							id: "13728733333",
	// 							name: "李伟光"
	// 						}
	// 						],
	// 			projectId: 1,          //项目ID
 //                status: 2        //任务状态 1：待处理  2：进行中  3：已完成
	// 		}
	// 	]
	// };

	$.ajax({
		url: localhost+"collaboration/itemDetail",
		type: "GET",
		data: {
			phone: userId,
			projectId: proId
		}
	})
	.done(function(data){
		data = JSON.parse(data);
		console.log(data);
		
		var tpl = document.getElementById("tpl").innerHTML;
    	var html = juicer(tpl,data);
    	$("#project-task").append(html);
	})
 
})