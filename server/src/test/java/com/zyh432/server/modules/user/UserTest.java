package com.zyh432.server.modules.user;

import cn.hutool.core.lang.Assert;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.utils.JwtUtil;
import com.zyh432.server.DataStoragePlatformServerLauncher;
import com.zyh432.server.modules.user.constants.UserConstants;
import com.zyh432.server.modules.user.context.*;
import com.zyh432.server.modules.user.service.IUserService;
import com.zyh432.server.modules.user.vo.UserInfoVO;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = DataStoragePlatformServerLauncher.class)
@Transactional
public class UserTest {
    @Autowired
    private IUserService iUserService;

    /**
     * 测试成功用户注册信息
     */
    @Test
    public void
    testRegisterUser(){
        UserRegisterContext context=createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue()>0L);
    }

    /**
     * 测试重复用户名称注册幂等
     */
    @Test(expected = DataStoragePlatformBusinessException.class)
    public void testRegisterDuplicateUsername(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);
        iUserService.register(context);
    }

    /**
     * 测试登录成功
     */
    @Test
    public void loginSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext= createUserLoginContext();
        String accessToken = iUserService.login(userLoginContext);
        Assert.isTrue(StringUtils.isNotBlank(accessToken));

    }

    /**
     * 测试登录失败：用户名不正确
     */
    @Test(expected = DataStoragePlatformBusinessException.class)
    public void wrongUsername(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext= createUserLoginContext();
        userLoginContext.setUsername(userLoginContext.getUsername()+"_change");
        iUserService.login(userLoginContext);


    }
    /**
     * 测试登录失败：密码不正确
     */
    @Test(expected = DataStoragePlatformBusinessException.class)
    public void wrongPassword(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext= createUserLoginContext();
        userLoginContext.setPassword(userLoginContext.getPassword()+"_change");
        iUserService.login(userLoginContext);

    }

    /**
     * 用户成功登出
     */
    @Test
    public void exitSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserLoginContext userLoginContext= createUserLoginContext();
        String accessToken = iUserService.login(userLoginContext);
        Assert.isTrue(StringUtils.isNotBlank(accessToken));

        Long userId = (Long)JwtUtil.analyzeToken(accessToken, UserConstants.LOGIN_USER_ID);
        iUserService.exit(userId);
    }

    /**
     * 检验用户名称通过
     */
    @Test
    public void checkUsernameSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckUsernameContext checkUsernameContext=new CheckUsernameContext();
        checkUsernameContext.setUsername(USERNAME);
        String question = iUserService.checkUsername(checkUsernameContext);
        Assert.isTrue(StringUtils.isNotBlank(question));
    }
    /**
     * 检验用户名称失败-没有查询到该用户
     */
    @Test(expected= DataStoragePlatformBusinessException.class)
    public void checkUsernameNoExist(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckUsernameContext checkUsernameContext=new CheckUsernameContext();
        checkUsernameContext.setUsername(USERNAME+"_change");
        iUserService.checkUsername(checkUsernameContext);


    }

    /**
     * 检验用户密保问题答案成功
     */
    @Test
    public void checkAnswerSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername(USERNAME);
        checkAnswerContext.setQuestion(QUESTION);
        checkAnswerContext.setAnswer(ANSWER);
        String token = iUserService.checkAnswer(checkAnswerContext);
        Assert.isTrue(StringUtils.isNotBlank(token));
    }
    /**
     * 检验用户密保问题答案失败
     */
    @Test(expected= DataStoragePlatformBusinessException.class)
    public void checkAnswerFail(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername(USERNAME);
        checkAnswerContext.setQuestion(QUESTION);
        checkAnswerContext.setAnswer(ANSWER+"_change");
        iUserService.checkAnswer(checkAnswerContext);
    }

    /**
     * 正常重置用户密码
     */
    @Test
    public void resetPasswordSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername(USERNAME);
        checkAnswerContext.setQuestion(QUESTION);
        checkAnswerContext.setAnswer(ANSWER);
        String token = iUserService.checkAnswer(checkAnswerContext);
        Assert.isTrue(StringUtils.isNotBlank(token));

        ResetPasswordContext resetPasswordContext=new ResetPasswordContext();
        resetPasswordContext.setUsername(USERNAME);
        resetPasswordContext.setNewPassword(PASSWORD+"_change");
        resetPasswordContext.setToken(token);
        iUserService.resetPassword(resetPasswordContext);
    }

    /**
     * 用户重置密码失败-token异常
     */
    @Test(expected= DataStoragePlatformBusinessException.class)
    public void resetPasswordTokenError(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        CheckAnswerContext checkAnswerContext=new CheckAnswerContext();
        checkAnswerContext.setUsername(USERNAME);
        checkAnswerContext.setQuestion(QUESTION);
        checkAnswerContext.setAnswer(ANSWER);
        String token = iUserService.checkAnswer(checkAnswerContext);
        Assert.isTrue(StringUtils.isNotBlank(token));

        ResetPasswordContext resetPasswordContext=new ResetPasswordContext();
        resetPasswordContext.setUsername(USERNAME);
        resetPasswordContext.setNewPassword(PASSWORD+"_change");
        resetPasswordContext.setToken(token+"_change");
        iUserService.resetPassword(resetPasswordContext);
    }

    /**
     * 正常在线修改密码
     */
    @Test
    public void changePasswordSuccess(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        ChangePasswordContext changePasswordContext=new ChangePasswordContext();
        changePasswordContext.setUserId(register);
        changePasswordContext.setPassword(PASSWORD);
        changePasswordContext.setNewPassword(PASSWORD+"_change");
        iUserService.changePassword(changePasswordContext);
    }

    /**
     * 修改密码失败-旧密码错误
     */
    @Test(expected = DataStoragePlatformBusinessException.class)
    public void changePasswordFailByWrongOldPassword(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        ChangePasswordContext changePasswordContext=new ChangePasswordContext();
        changePasswordContext.setUserId(register);
        changePasswordContext.setPassword(PASSWORD+"_change");
        changePasswordContext.setNewPassword(PASSWORD+"_change");
        iUserService.changePassword(changePasswordContext);

    }

    @Test
    public void testQueryUserInfo(){
        UserRegisterContext context = createUserRegisterContext();
        Long register = iUserService.register(context);
        Assert.isTrue(register.longValue() > 0L);

        UserInfoVO userInfoVO = iUserService.info(register);
        Assert.notNull(userInfoVO);
    }

/************************************************private************************************************/
    private final static String USERNAME="panghu";
    private final static String PASSWORD="77";
    private final static String QUESTION = "question";
    private final static String ANSWER = "answer";

    /**
     * 构建注册用户上下文信息
     * @return
     */
    private UserRegisterContext createUserRegisterContext(){
        UserRegisterContext context=new UserRegisterContext();
        context.setUsername(USERNAME);
        context.setPassword(PASSWORD);
        context.setQuestion(QUESTION);
        context.setAnswer(ANSWER);
        return context;
    }

    /**
     * 构建用户登录上下文实体
     * @return
     */
    private UserLoginContext createUserLoginContext(){
        UserLoginContext userLoginContext=new UserLoginContext();
        userLoginContext.setUsername(USERNAME);
        userLoginContext.setPassword(PASSWORD);
        return userLoginContext;
    }
}
