package cn.tf.ecps.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.tf.ecps.po.TaskBean;
import cn.tf.ecps.service.FlowService;

@Service
public class FlowServiceImpl implements FlowService{

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RuntimeService runtimeService;
	@Autowired
	private TaskService taskService;
	@Autowired
	private HistoryService historyService;

	
	
	
	public void deploy() {
		DeploymentBuilder db = repositoryService.createDeployment();
		db.addClasspathResource("activiti/OrderFlow.bpmn")
		.addClasspathResource("activiti/OrderFlow.png");
		db.deploy();
	}

	public String startFlow(Long orderId) {
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("OrderFlow", orderId+"");
		return pi.getId();
	}

	public void compeleteTaskByPid(String processInstanceId, String outcome) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("outcome", outcome);
		//根据流程实例的Id来查询当前的任务
		Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
		taskService.complete(task.getId(), map);
		
	}

	public List<TaskBean> selectTaskByAssignee(String assignee) {
		List<Task> taskList = taskService.createTaskQuery().processDefinitionKey("OrderFlow")
				.taskAssignee(assignee)
				.orderByTaskCreateTime()
				.desc()
				.list();
				List<TaskBean> tbList = new ArrayList<TaskBean>();
				ProcessInstanceQuery pq = runtimeService.createProcessInstanceQuery();
				for(Task task : taskList){
					TaskBean tb = new TaskBean();
					tb.setTask(task);
					//获得流程实例对象
					ProcessInstance pi =pq.processInstanceId(task.getProcessInstanceId()).singleResult();
					//获得业务键
					String businessKey = pi.getBusinessKey();
					tb.setBusinessKey(businessKey);
					tbList.add(tb);
				}
				return tbList;
	}
	
	

}
