package com.zyh432.server.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zyh432.cache.core.constants.CacheConstants;
import com.zyh432.core.exception.DataStoragePlatformBusinessException;
import com.zyh432.core.response.ResponseCode;
import com.zyh432.core.utils.IdUtil;
import com.zyh432.core.utils.JwtUtil;
import com.zyh432.core.utils.PasswordUtil;
import com.zyh432.server.common.cache.AnnotationCacheService;
import com.zyh432.server.modules.file.constants.FileConstants;
import com.zyh432.server.modules.file.context.CreateFolderContext;
import com.zyh432.server.modules.file.entity.DatastorageplatformUserFile;
import com.zyh432.server.modules.file.service.IUserFileService;
import com.zyh432.server.modules.user.constants.UserConstants;
import com.zyh432.server.modules.user.context.*;
import com.zyh432.server.modules.user.converter.UserConverter;
import com.zyh432.server.modules.user.entity.DatastorageplatformUser;
import com.zyh432.server.modules.user.service.IUserService;
import com.zyh432.server.modules.user.mapper.DatastorageplatformUserMapper;
import com.zyh432.server.modules.user.vo.UserInfoVO;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;

/**
* @author 790585941
* @description 针对表【datastorageplatform_user(用户信息表)】的数据库操作Service实现
* @createDate 2024-01-08 22:22:00
*/
@Service(value = "userService")
public class UserServiceImpl extends ServiceImpl<DatastorageplatformUserMapper, DatastorageplatformUser>
    implements IUserService {
    @Autowired
    private UserConverter userConverter;
    @Autowired
    private IUserFileService iUserFileService;
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    @Qualifier(value = "userAnnotationCacheService")
    private AnnotationCacheService<DatastorageplatformUser> cacheService;
    /**
     * 用户注册的业务实现
     * 需要实现的功能点：
     * 1、注册用户信息
     * 2、创建新用户的根本目录信息
     * <p>
     * 需要实现的技术难点：
     * 1、该业务是幂等的
     * 2、要保证用户名全局唯一
     * <p>
     * 实现技术难点的处理方案：
     * 1、幂等性通过数据库表对于用户名字段添加唯一索引，我们上有业务捕获对应的冲突异常，转化返回
     *
     * @param userRegisterContext
     * @return
     */
    @Override
    public Long register(UserRegisterContext userRegisterContext) {
        assembleUserEntity(userRegisterContext);
        doRegister(userRegisterContext);
        createUserRootFolder(userRegisterContext);

        return userRegisterContext.getEntity().getUserId();
    }

    /**
     * 用户登录的业务实现
     * 1、用户的登录信息校验
     * 2、生成一个具有时效性的access Token
     * 3、将accessToken缓存起来，去实现单机登录
     * @param userLoginContext
     * @return
     */
    @Override
    public String login(UserLoginContext userLoginContext) {
        checkLoginInfo(userLoginContext);
        generateAndSaveAccessToken(userLoginContext);
        return userLoginContext.getAccessToken();
    }

    /**
     * 用户退出登录
     * 清除用户的登录凭证缓存
     * @param userId
     */
    @Override
    public void exit(Long userId) {
        try {
            Cache cache = cacheManager.getCache(CacheConstants.dataStoragePlatform_CACHE_NAME);
            cache.evict(UserConstants.USER_LOGIN_PREFIX + userId);
        }catch (Exception e){
            throw new DataStoragePlatformBusinessException("用户退出登录失败");

        }
    }

    /**
     * 用户忘记密码-检验用户名称
     * @param checkUsernameContext
     * @return
     */
    @Override
    public String checkUsername(CheckUsernameContext checkUsernameContext) {
        String question=baseMapper.selectQuestionByUsername(checkUsernameContext.getUsername());
        if (StringUtils.isBlank(question)){
            throw new DataStoragePlatformBusinessException("没有此用户");
        }
        return question;
    }

    /**
     *用户忘记密码-校验密保答案
     * @param checkAnswerContext
     * @return
     */
    @Override
    public String checkAnswer(CheckAnswerContext checkAnswerContext) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("username",checkAnswerContext.getUsername());
        queryWrapper.eq("question",checkAnswerContext.getQuestion());
        queryWrapper.eq("answer",checkAnswerContext.getAnswer());
        int count = count(queryWrapper);
        if (count==0){
            throw new DataStoragePlatformBusinessException("密保答案不正确");
        }
        return generateCheckAnswerToken(checkAnswerContext);
    }

    /**
     * 重置用户密码
     * 1.校验token是否有效
     * 2.重置密码
     * @param resetPasswordContext
     */
    @Override
    public void resetPassword(ResetPasswordContext resetPasswordContext) {
        checkForgetPasswordToken(resetPasswordContext);
        checkAndResetUserPassword(resetPasswordContext);

    }

    /**
     * 在线修改密码
     * 1、检验旧密码
     * 2、重置新密码
     * 3、退出当前的的登录状态
     * @param changePasswordContext
     */
    @Override
    public void changePassword(ChangePasswordContext changePasswordContext) {
        checkOldPassword(changePasswordContext);
        doChangePassword(changePasswordContext);
        exitLoginStatus(changePasswordContext);
    }


    /**
     * 查询在线用户的基本信息
     * 1、查询用户的基本信息实体
     * 2、查询用户的根文件夹信息
     * 3、拼装VO对象返回
     *
     * @param userId
     * @return
     */
    @Override
    public UserInfoVO info(Long userId) {
        DatastorageplatformUser entity = getById(userId);
        if (Objects.isNull(entity)){
            throw new DataStoragePlatformBusinessException("用户信息查询失败");
        }
        DatastorageplatformUserFile datastorageplatformUserFile=getUserRootFileInfo(userId);

        if (Objects.isNull(datastorageplatformUserFile)){
            throw new DataStoragePlatformBusinessException("查询用户根文件夹信息失败");
        }
        return userConverter.assembleUserInfoVO(entity,datastorageplatformUserFile);
    }

    /**
     * 获取用户根文件夹信息实体
     * @param userId
     * @return
     */
    private DatastorageplatformUserFile getUserRootFileInfo(Long userId) {
        return iUserFileService.getUserRootFile(userId);
    }

    /**
     * 根据ID删除
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        //return super.removeById(id);
        return cacheService.removeById(id);
    }

    /**
     * 删除（根据ID批量删除）
     * @param idList
     * @return
     */
    @Override
    public boolean removeByIds(Collection<? extends Serializable> idList) {
        //return super.removeByIds(idList);
        throw new DataStoragePlatformBusinessException("请更换手动缓存");
    }


    @Override
    public boolean updateById(DatastorageplatformUser entity) {
        //return super.updateById(entity);
        return cacheService.updateById(entity.getUserId(),entity);
    }

    @Override
    public boolean updateBatchById(Collection<DatastorageplatformUser> entityList) {
        //return super.updateBatchById(entityList);
        throw new DataStoragePlatformBusinessException("请更换手动缓存");
    }

    @Override
    public DatastorageplatformUser getById(Serializable id) {
        //return super.getById(id);
        return cacheService.getById(id);
    }

    @Override
    public List<DatastorageplatformUser> listByIds(Collection<? extends Serializable> idList) {
        //return super.listByIds(idList);
        throw new DataStoragePlatformBusinessException("请更换手动缓存");
    }


    /***********************************************private************************************************/

    /**
     * 退出用户的登录状态
     * @param changePasswordContext
     */
    private void exitLoginStatus(ChangePasswordContext changePasswordContext) {
        exit(changePasswordContext.getUserId());
    }

    /**
     * 修改新密码
     * @param changePasswordContext
     */
    private void doChangePassword(ChangePasswordContext changePasswordContext) {
        String newPassword = changePasswordContext.getNewPassword();
        DatastorageplatformUser entity = changePasswordContext.getEntity();
        String salt = entity.getSalt();

        String encNewPassword = PasswordUtil.encryptPassword(salt, newPassword);
        entity.setPassword(encNewPassword);
        if (!updateById(entity)){
            throw new DataStoragePlatformBusinessException("修改用户密码失败");
        }
    }

    /**
     *校验用户的旧密码
     *该步骤会查询并封装用户的实体信息到上下文对象中
     * @param changePasswordContext
     */
    private void checkOldPassword(ChangePasswordContext changePasswordContext) {
        Long userId = changePasswordContext.getUserId();
        String oldPassword = changePasswordContext.getOldPassword();
        DatastorageplatformUser entity = getById(userId);
        if (Objects.isNull(entity)){
            throw new DataStoragePlatformBusinessException("用户信息不存在");
        }
        changePasswordContext.setEntity(entity);
        String encOldPassword = PasswordUtil.encryptPassword(entity.getSalt(), oldPassword);
        String dbOldPassword = entity.getPassword();
        if (!StringUtils.equals(encOldPassword,dbOldPassword)){
            throw new DataStoragePlatformBusinessException("旧密码不正确");
        }
    }

    /**
     *校验用户信息并重置用户密码
     * @param resetPasswordContext
     */
    private void checkAndResetUserPassword(ResetPasswordContext resetPasswordContext) {
        String username = resetPasswordContext.getUsername();
        String password = resetPasswordContext.getPassword();
        DatastorageplatformUser entity = getDatastorageplatformUserByUsername(username);
        if (Objects.isNull(entity)){
            throw new DataStoragePlatformBusinessException("用户信息不存在");
        }
        String newDbPassword=PasswordUtil.encryptPassword(entity.getSalt(),password);
        entity.setPassword(newDbPassword);
        entity.setUpdateTime(new Date());
        if (!updateById(entity)){
            throw new DataStoragePlatformBusinessException("重置密码失败");
        }

    }

    /**
     * 验证忘记用户密码的token是否有效
     * @param resetPasswordContext
     */
    private void checkForgetPasswordToken(ResetPasswordContext resetPasswordContext) {
        String token = resetPasswordContext.getToken();
        Object value = JwtUtil.analyzeToken(token, UserConstants.FORGET_USERNAME);
        if (Objects.isNull(value)){
            throw new DataStoragePlatformBusinessException(ResponseCode.TOKEN_EXPIRE);
        }
        String tokenUsername=String.valueOf(value);
        if (!StringUtils.equals(tokenUsername,resetPasswordContext.getUsername())){
            throw new DataStoragePlatformBusinessException("token错误");
        }
    }


/**
     * 生成用户忘记密码-校验密保答案通过的临时token
     * token的失效时间为5分钟过后
     * @param checkAnswerContext
     * @return
     */
    private String generateCheckAnswerToken(CheckAnswerContext checkAnswerContext) {
        String token = JwtUtil.generateToken(checkAnswerContext.getUsername(), UserConstants.FORGET_USERNAME, checkAnswerContext.getUsername(), UserConstants.FIVE_MINUTES_LONG);
        return token;
    }

/**
     * 生成并保存登录之后的凭证
     * @param userLoginContext
     */
    private void generateAndSaveAccessToken(UserLoginContext userLoginContext) {
        DatastorageplatformUser entity = userLoginContext.getEntity();
        String accessToken = JwtUtil.generateToken(entity.getUsername(), UserConstants.LOGIN_USER_ID, entity.getUserId(),
                UserConstants.ONE_DAY_LONG);
        Cache cache = cacheManager.getCache(CacheConstants.dataStoragePlatform_CACHE_NAME);
        cache.put(UserConstants.USER_LOGIN_PREFIX + entity.getUserId(), accessToken);
        userLoginContext.setAccessToken(accessToken);
    }

    /**
     * 检验用户名和密码
     * @param userLoginContext
     */
    private void checkLoginInfo(UserLoginContext userLoginContext) {
        String username = userLoginContext.getUsername();
        String password = userLoginContext.getPassword();

        DatastorageplatformUser entity= getDatastorageplatformUserByUsername(username);
        if (Objects.isNull(entity)){
            throw new DataStoragePlatformBusinessException("用户名称不存在");
        }
        String salt=entity.getSalt();
        String encPassword = PasswordUtil.encryptPassword(salt, password);
        String dbPassword= entity.getPassword();
        if(!Objects.equals(encPassword,dbPassword)){
            throw new DataStoragePlatformBusinessException("密码信息不正确");
        }

        userLoginContext.setEntity(entity);
    }

    /**
     * 通过用户名称获取用户实体信息
     * @param username
     * @return
     */
    private DatastorageplatformUser getDatastorageplatformUserByUsername(String username) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("username",username);
        return getOne(queryWrapper);
    }

    /**
     * 创建用户的根目录信息
     * @param userRegisterContext
     */
    private void createUserRootFolder(UserRegisterContext userRegisterContext) {
        CreateFolderContext createFolderContext=new CreateFolderContext();
        createFolderContext.setParentId(FileConstants.TOP_PARENT_ID);
        createFolderContext.setUserId(userRegisterContext.getEntity().getUserId());
        createFolderContext.setFolderName(FileConstants.ALL_FILE_CN_STR);
        iUserFileService.createFolder(createFolderContext);
    }

    /**
     * 实现注册用户的业务
     * 需要捕获数据库的唯一索引冲突异常，来实现全局用户名称唯一
     * @param userRegisterContext
     */
    private void doRegister(UserRegisterContext userRegisterContext) {
        DatastorageplatformUser entity = userRegisterContext.getEntity();
        if (Objects.nonNull(entity)){
            try{
                if (!save(entity)){
                    throw new DataStoragePlatformBusinessException("用户注册失败");
                }
            }catch (DuplicateKeyException duplicateKeyException){
                throw new DataStoragePlatformBusinessException("用户名已存在");
            }
            return;
        }
        throw new DataStoragePlatformBusinessException(ResponseCode.ERROR);

    }

    /**
     * 实体转化
     * 由上下文信息转化成用户实体，封装进上下文
     * @param userRegisterContext
     */
    private void assembleUserEntity(UserRegisterContext userRegisterContext) {
        DatastorageplatformUser entity=userConverter.userRegisterContext2DatastorageplatformUser(userRegisterContext);
        String salt= PasswordUtil.getSalt(),
                dbPassword=PasswordUtil.encryptPassword(salt,userRegisterContext.getPassword());
        entity.setUserId(IdUtil.get());
        entity.setSalt(salt);
        entity.setPassword(dbPassword);
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        userRegisterContext.setEntity(entity);
    }
}




