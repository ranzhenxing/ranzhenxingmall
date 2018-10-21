package com.ranzhenxingmall.dao;

import com.ranzhenxingmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUserName(String userName);

    User selectLogin(@Param("userName") String userName,@Param("pwd") String pwd);

    int checkEmail(String email);

    String selectQuestionByUserName(String userName);

    int checkAnswer(@Param("userName") String userName, @Param("question") String question, @Param(("answer")) String answer);

    int updatePwdByUserName(@Param("userName")String userName, @Param("pwdNew")String pwdNew);

    int checkPwd(@Param("pwdOld")String pwdOld, @Param("userId")Integer userId);

    int checkEmailByUserId(@Param("email")String email, @Param("userId")Integer userId);
}