package com.zyh432.server.modules.user.converter;

import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.user.context.*;
import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.zyh432.server.modules.user.po.*;
import com.zyh432.server.modules.user.vo.UserInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 用户模块实体转化工具类
 */
@Mapper(componentModel = "spring")
public interface UserConverter {
    /**
     * UserRegisterPO转化成UserRegisterContext
     *
     * @param userRegisterPO
     * @return
     */
    UserRegisterContext userRegisterPO2UserRegisterContext(UserRegisterPO userRegisterPO);

    /**
     * 传过来的明文密码先忽视，需要加密
     * @param userRegisterContext
     * @return
     */
    @Mapping(target = "password", ignore = true)
    DatastorageplatformUser userRegisterContext2DatastorageplatformUser(UserRegisterContext userRegisterContext);

    /**
     * userLoginPO转userLoginContext
     * @param userLoginPO
     * @return
     */
    UserLoginContext userLoginPO2UserLoginContext(UserLoginPO userLoginPO);

    /**
     * CheckUsernamePO转CheckUsernameContext
     *
     * @param checkUsernamePO
     * @return
     */
    CheckUsernameContext checkUsernamePO2CheckUsernameContext(CheckUsernamePO checkUsernamePO);

    /**
     * CheckAnswerPO转CheckAnswerContext
     *
     * @param checkAnswerPO
     * @return
     */
    CheckAnswerContext checkAnswerPO2CheckAnswerContext(CheckAnswerPO checkAnswerPO);

    /**
     * ResetPasswordPO转ResetPasswordContext
     *
     * @param resetPasswordPO
     * @return
     */
    ResetPasswordContext resetPasswordPO2ResetPasswordContext(ResetPasswordPO resetPasswordPO);

    /**
     * ChangePasswordPO转ChangePasswordContext
     *
     * @param changePasswordPO
     * @return
     */
    ChangePasswordContext changePasswordPO2ChangePasswordContext(ChangePasswordPO changePasswordPO);

    /**
     * 拼装用户基本信息返回实体
     * @param datastorageplatformUser
     * @param datastorageplatformUserFile
     * @return
     */
    @Mapping(source = "datastorageplatformUser.username", target = "username")
    @Mapping(source = "datastorageplatformUserFile.fileId", target = "rootFileId")
    @Mapping(source = "datastorageplatformUserFile.filename", target = "rootFilename")
    UserInfoVO assembleUserInfoVO(DatastorageplatformUser datastorageplatformUser, DatastorageplatformUserFile datastorageplatformUserFile);
}
