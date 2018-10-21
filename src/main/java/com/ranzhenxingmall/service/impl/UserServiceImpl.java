package com.ranzhenxingmall.service.impl;

import com.ranzhenxingmall.common.Const;
import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.common.TokenCache;
import com.ranzhenxingmall.dao.UserMapper;
import com.ranzhenxingmall.pojo.User;
import com.ranzhenxingmall.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String userName, String pwd) {
        int resultCount = userMapper.checkUserName(userName);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        User user = userMapper.selectLogin(userName, pwd);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        } else {
            user.setPassword(StringUtils.EMPTY);
            return ServerResponse.createBySuccess("登录成功", user);
        }
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> checkUserNameResponse = this.checkValid(Const.USERNAME, user.getUsername());
        if (!checkUserNameResponse.isSuccess()) {
            return checkUserNameResponse;
        }
        ServerResponse<String> checkEmailResponse = this.checkValid(Const.EMAIL, user.getEmail());
        if (!checkEmailResponse.isSuccess()) {
            return checkEmailResponse;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String type, String str) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int checkUserName = userMapper.checkUserName(str);
                if (checkUserName > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int checkEmail = userMapper.checkEmail(str);
                if (checkEmail > 0) {
                    return ServerResponse.createByErrorMessage("邮箱地址已存在");
                }
            }
            return ServerResponse.createBySuccessMessage("校验成功");
        }
        return ServerResponse.createByErrorMessage("参数错误");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String userName) {
        int checkUserName = userMapper.checkUserName(userName);
        if (checkUserName <= 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestionByUserName(userName);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码为空");
    }

    @Override
    public ServerResponse<String> forgetCheckAnswer(String userName, String question, String answer) {
        int userCount = userMapper.checkAnswer(userName, question, answer);
        if (userCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + userName, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题回答错误");
    }

    @Override
    public ServerResponse<String> forgetResertPassword(String userName, String pwdNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        ServerResponse<String> cheName = this.checkValid(Const.USERNAME, userName);
        if (cheName.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + userName);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效");
        }
        if (!token.equals(forgetToken)) {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        int result = userMapper.updatePwdByUserName(userName, pwdNew);
        if (result > 0) {
            return ServerResponse.createBySuccessMessage("用户修改密码成功");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<User> resetPassword(String pwdOld, String pwdNew, User user) {
        int result = userMapper.checkPwd(pwdOld, user.getId());
        if (result == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(pwdNew);
        int changResult = userMapper.updateByPrimaryKeySelective(user);
        if (changResult > 0) {
            return ServerResponse.createBySuccessMessage("用户更新密码成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        int checkEmailResult = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (checkEmailResult > 0) {
            return ServerResponse.createByErrorMessage("Email已存在，请修改Email再次更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setEmail(user.getEmail());
        updateUser.setAnswer(user.getAnswer());
        updateUser.setPhone(user.getPhone());
        int updateResult = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateResult > 0) {
            return ServerResponse.createBySuccess("用户信息更新成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("用户信息更新失败");
    }
}
