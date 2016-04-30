package com.datou.springstudy.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloController {

	@RequestMapping(value = "/hello", method = { RequestMethod.POST })
	@ResponseBody
	public Bo helloObject(@RequestBody Bo bo) {
		int temp = (int) (bo.getPassword() + Math.random() * 1000);
		bo.setPassword(temp);
		bo.setUsername("mydatou-4------");
		return bo;
	}
}
