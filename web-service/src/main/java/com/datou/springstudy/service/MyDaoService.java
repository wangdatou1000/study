package com.datou.springstudy.service;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.datou.springstudy.dal.model.User;

public interface MyDaoService {
	public User getUserById(int id);

	@Transactional(propagation = Propagation.REQUIRED)
	public int insertUser(User user);
}
