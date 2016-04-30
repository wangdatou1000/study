package com.datou.springstudy.dal.dao;

import java.io.IOException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.datou.springstudy.dal.model.User;

public class UserMapperImpl {

	private static Integer iduser = 1;


	public static void main(String[] args) throws IOException {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("configs/spring-mybatis.xml");
		// String resource = "mapperConfigs.xml";
		// InputStream inputStream = Resources.getResourceAsStream(resource);
		// SqlSessionFactory sqlSessionFactory = new
		// SqlSessionFactoryBuilder().build(inputStream);
		// SqlSession sqlSession = sqlSessionFactory.openSession();
		// User cus = (User) sqlSession.selectOne("test.selectByPrimaryKey", 1);
		UserMapper test = (UserMapper) appContext.getBean(UserMapper.class);

		User user = new User();
		user.setUserName("wangdatou");
		user.setTel("1111111");
		user.setCreateTime(Integer.valueOf(10000));
		user.setUpdateTime(Integer.valueOf(2000));
		// user.setIduser(Integer.valueOf(1));
		int cus = test.insertSelective(user);
		// User user = test.selectByPrimaryKey(Integer.valueOf(1));
		System.out.println(user.getUserName());
	}

}
