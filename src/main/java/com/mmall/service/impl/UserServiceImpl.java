package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.sun.xml.internal.bind.v2.TODO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.util.Password;

import java.util.UUID;

/**
 * @author liuzhiliang
 * @create 2018-09-06 10:00
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 登录的功能
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int usernameCount = userMapper.checkUsername(username);
        if (usernameCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //TODO 密码MD5加密
        String passwordNew = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, passwordNew);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码不正确");
        }
        //程序走到这里说明密码和用户名都是正确的，然后将密码置空
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
       /* int resultCount = userMapper.checkUsername(user.getUsername());
        if (resultCount >0) {
            return ServerResponse.createByErrorMessage("用户名已注册");
        }
        int emailCount = userMapper.checkEmail(user.getEmail());
        if (emailCount >0) {
            return ServerResponse.createByErrorMessage("邮箱已注册");
        }*/
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        //这里判断的原因就是如果校验成功就通过，如果失败就直接return，程序不在往下执行
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        user.setRole(Const.Role.ROLE_CUSTOMER);
        int insertCount = userMapper.insert(user);
        if (insertCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 校验username和email，防止在注册的时候被恶意调用接口注册
     *
     * @param str  对应的value值
     * @param type type代表是username或者是email。根据判断来调用哪个sql语句
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int emailCount = userMapper.checkEmail(str);
                if (emailCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已注册");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> forgetGetQuestion(String username) {
        //校验一下username是否存在
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在的");
        }
        String question = userMapper.selectQuestion(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码提示问题为空");
    }

    @Override
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题及问题的答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            //然后将forgetToken的值放进本地的缓存guavaCache中存储
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案不正确");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String password, String forgetToken) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户名不存在的");
        }
        String passwordNew = MD5Util.MD5EncodeUtf8(password);
        if (StringUtils.equals(TokenCache.TOKEN_PREFIX + username, forgetToken)) {
            int rowCount = userMapper.updateForgetResetPassword(username, passwordNew);
            if (rowCount > 0) {
                return ServerResponse.createBySuccess("忘记密码重置成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token过期失效");
        }
        return ServerResponse.createByErrorMessage("忘记密码重置失败");
    }
}
