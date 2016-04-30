package com.datou.springstudy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.datou.springstudy.requestResponseMode.RequestMode;
import com.datou.springstudy.service.SortService;

@Controller
public class HelloController {
	@Autowired
	SortService sortService;
	@RequestMapping(value = "/hello", method = { RequestMethod.POST })
	@ResponseBody
	public RequestMode helloObject(@RequestBody RequestMode bo) {
		int temp = (int) (bo.getPassword() + Math.random() * 1000);
		bo.setPassword(temp);
		bo.setUsername("mydatou-" + Math.random() * 100);
		sortService.quickSort(bo.getArray());
		return bo;
	}
}
