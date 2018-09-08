package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;

import javax.servlet.http.HttpSession;

/**
 * @author liuzhiliang
 * @create 2018-09-06 9:51
 */
public interface IUserService {
    ServerResponse<User> login(String username,String password);
    ServerResponse<String> register(User user);
    ServerResponse<String> checkValid(String str,String type);
    ServerResponse<String> forgetGetQuestion(String username);
    ServerResponse<String> forgetCheckAnswer(String username,String question,String answer);
    ServerResponse<String> forgetResetPassword(String username,String password,String forgetToken);
    ServerResponse<String> updateLoginPassword(User user, String passwordOld, String passwordNew);
    ServerResponse<User> updateInformation(User user);
    ServerResponse<User> getInformation(Integer userId);
}
