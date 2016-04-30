package com.datou.springstudy.service;

import org.springframework.transaction.annotation.Transactional;

import com.datou.springstudy.dal.model.User;

@Transactional(timeout = 10)
public interface MyDaoService {
	public User getUserById(int id);
}
