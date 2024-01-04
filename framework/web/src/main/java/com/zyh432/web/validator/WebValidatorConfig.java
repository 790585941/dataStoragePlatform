package com.zyh432.web.validator;

import com.zyh432.core.constants.DataStoragePlatformConstants;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.HibernateValidator;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * 统一的参数校验器
 */
@SpringBootConfiguration
@Slf4j
public class WebValidatorConfig {

    private static final String FAIL_FAST_KEY = "hibernate.validator.fail_fast";

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.setValidator(dataStoragePlatformValidator());
        log.info("The hibernate validator is loaded successfully!");
        return postProcessor;
    }

    /**
     * 构造项目的方法参数校验器
     *
     * @return
     */
    private Validator dataStoragePlatformValidator() {
        //使用 Hibernate Validator 提供者,
        // 配置参数，设置 Hibernate Validator 的校验模式为快速失败（fail fast）
        ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .addProperty(FAIL_FAST_KEY, DataStoragePlatformConstants.TRUE_STR)
                .buildValidatorFactory();
        Validator validator = validatorFactory.getValidator();
        return validator;
    }


}
