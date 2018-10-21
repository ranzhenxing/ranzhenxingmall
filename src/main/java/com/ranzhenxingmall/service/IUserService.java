package com.ranzhenxingmall.service;

import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.pojo.User;

public interface IUserService {
    ServerResponse<User> login(String userName, String pwd);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String type, String str);

    ServerResponse<String> forgetGetQuestion(String userName);

    ServerResponse<String> forgetCheckAnswer(String userName, String question, String answer);

    ServerResponse<String> forgetResertPassword(String userName, String pwdNew, String forgetToken);

    ServerResponse<User> resetPassword(String pwdOld, String pwdNew,User user);

    ServerResponse<User> updateUserInfo(User user);
}
