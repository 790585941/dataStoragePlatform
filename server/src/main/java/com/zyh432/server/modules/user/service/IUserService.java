package com.zyh432.server.modules.user.service;

import com.zyh432.server.modules.user.context.*;
import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zyh432.server.modules.user.vo.UserInfoVO;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user(用户信息表)】的数据库操作Service
* @createDate 2024-01-08 22:22:00
*/
public interface IUserService extends IService<DatastorageplatformUser> {
    /**
     * 用户注册
     *
     * @param userRegisterContext
     * @return
     */
    Long register(UserRegisterContext userRegisterContext);

    /**
     * 用户登录业务
     * @param userLoginContext
     * @return
     */
    String login(UserLoginContext userLoginContext);

    /**
     * 用户退出登录
     * @param userId
     */
    void exit(Long userId);

    /**
     * 用户忘记密码-检验用户名
     * @param checkUsernameContext
     * @return
     */
    String checkUsername(CheckUsernameContext checkUsernameContext);

    /**
     * 用户忘记密码-校验密保答案
     * @param checkAnswerContext
     * @return
     */
    String checkAnswer(CheckAnswerContext checkAnswerContext);

    /**
     * 重置用户密码
     * @param resetPasswordContext
     */
    void resetPassword(ResetPasswordContext resetPasswordContext);

    /**
     * 在线修改密码
     * @param changePasswordContext
     */
    void changePassword(ChangePasswordContext changePasswordContext);

    /**
     * 查询在线用户的基本信息
     * @param userId
     * @return
     */
    UserInfoVO info(Long userId);
}
