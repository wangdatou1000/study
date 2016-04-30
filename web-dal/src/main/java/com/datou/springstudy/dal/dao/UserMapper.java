package com.datou.springstudy.dal.dao;

import org.springframework.stereotype.Repository;

import com.datou.springstudy.dal.model.User;

@Repository
public interface UserMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    int deleteByPrimaryKey(Integer iduser);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    int insert(User record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    int insertSelective(User record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    User selectByPrimaryKey(Integer iduser);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    int updateByPrimaryKeySelective(User record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table user
     *
     * @mbggenerated Sat Apr 30 18:29:48 CST 2016
     */
    int updateByPrimaryKey(User record);
}