package com.ranzhenxingmall.controller.portal;

import com.ranzhenxingmall.common.Const;
import com.ranzhenxingmall.common.ResponseCode;
import com.ranzhenxingmall.common.ServerResponse;
import com.ranzhenxingmall.pojo.User;
import com.ranzhenxingmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     *
     * @param userName 用户名
     * @param pwd      密码
     * @param session  session
     * @return 是否成功
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String userName, String pwd, HttpSession session) {
        ServerResponse<User> response = iUserService.login(userName, pwd);
        if (response.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 用户登出
     *
     * @param session session
     * @return 是否成功
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 结果
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register( User user) {
        return iUserService.register(user);
    }

    /**
     * 校验用户参数信息
     *
     * @param type 类型
     * @param str  参数
     * @return 结果
     */
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String type, String str) {
        return iUserService.checkValid(type, str);
    }

    /**
     * 获取用户信息
     *
     * @param session session
     * @return 用户信息
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户信息");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 忘记密码获取问题
     *
     * @param userName 用户名
     * @return 密码提示问题
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String userName) {
        return iUserService.forgetGetQuestion(userName);
    }

    /**
     * 忘记密码的问题校验
     *
     * @param userName 用户名
     * @param answer   问题的答案
     * @return 结果
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String userName, String question, String answer) {
        return iUserService.forgetCheckAnswer(userName, question, answer);
    }

    /**
     * 重置密码
     *
     * @param userName    用户名
     * @param pwdNew      新密码
     * @param forgetToken forgetToken
     * @return 结果
     */
    @RequestMapping(value = "forget_resert_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResertPassword(String userName, String pwdNew, String forgetToken) {
        return iUserService.forgetResertPassword(userName, pwdNew, forgetToken);
    }

    /**
     * 在线状态的重置密码
     *
     * @param session session
     * @return 用户信息
     */
    @RequestMapping(value = "reset_pwd.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> resetPassword(HttpSession session, String pwdOld, String pwdNew) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(pwdOld, pwdNew, user);
    }

    /**
     * 更新用户信息
     *
     * @param session session
     * @return 用户信息
     */
    @RequestMapping(value = "update_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateUserInfo(HttpSession session, User user) {
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUserName(currentUser.getUserName());
        ServerResponse<User> updateUserInfo = iUserService.updateUserInfo(user);
        if (updateUserInfo.isSuccess()) {
            session.setAttribute(Const.CURRENT_USER, user);
        }
        return updateUserInfo;
    }

    /**
     * 查询用户信息
     *
     * @param session 用户session
     * @return 用户信息
     */
    @RequestMapping(value = "query_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> queryUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录");
        }
        return iUserService.queryUserInfo(user.getId());
    }
}
