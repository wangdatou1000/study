package com.datou.springstudy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datou.springstudy.dal.dao.UserMapper;
import com.datou.springstudy.dal.model.User;
import com.datou.springstudy.service.MyDaoService;

@Component
public class MyDaoServiceImpl implements MyDaoService {

	@Autowired
	UserMapper userMapper;

	@Override
	public User getUserById(int id) {
		// TODO Auto-generated method stub
		return userMapper.selectByPrimaryKey(Integer.valueOf(id));
	}

}
