package com.hfdy.hfdypan.aspect;

import com.hfdy.hfdypan.annotation.VerifyParam;
import com.hfdy.hfdypan.annotation.VerifyParamMethod;
import com.hfdy.hfdypan.domain.enums.HttpCodeEnum;
import com.hfdy.hfdypan.exception.BusinessException;
import com.hfdy.hfdypan.utils.StringUtil;
import com.hfdy.hfdypan.utils.VerifyUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * @author hf-dy
 * @date 2024/10/21 21:50
 */
@Aspect
@Component
@Slf4j
public class VerifyParamsAspect {
    private static final String TYPE_STRING = "java.lang.String";
    private static final String TYPE_INTEGER = "java.lang.Integer";
    private static final String TYPE_LONG = "java.lang.Long";

    /**
     * 判断类型字符串是否是，String/Integer?Long
     *
     * @param typeName
     * @return
     */
    private boolean isPrimitiveParamType(String typeName) {
        return List.of(TYPE_INTEGER, TYPE_STRING, TYPE_LONG).contains(typeName);
    }


    /**
     * 切点，需要校验参数的方法
     */
    @Pointcut("@annotation(com.hfdy.hfdypan.annotation.VerifyParamMethod)")
    private void verifyParamsMethod() {
    }

    @Before("verifyParamsMethod()")
    public void before(JoinPoint joinPoint) throws BusinessException {
        Object[] args = joinPoint.getArgs(); // 参数
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod(); // 方法
        VerifyParamMethod verifyParamMethod = method.getAnnotation(VerifyParamMethod.class);
        // 如果没有注解
        if (verifyParamMethod == null) return;
        // 校验方法
        if (verifyParamMethod.checkParams()) {
            validateParams(method, args);
        }
    }

    private void validateParams(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i]; // 参数名
            Object value = args[i]; // 参数值
            // 判断是否有校验参数的注解
            VerifyParam verifyParam = parameter.getAnnotation(VerifyParam.class);
            if (verifyParam == null) continue;
            // 基本数据类型
            if (isPrimitiveParamType(parameter.getParameterizedType().getTypeName())) {
                checkValue(parameter.getName(), value, verifyParam);
            } else {
                checkObjValue(value);
            }
        }
    }

    /**
     * 校验基础类型的参数
     *
     * @param name        字段名
     * @param value       字段值
     * @param verifyParam 字段的校验注解
     */
    private void checkValue(String name, Object value, VerifyParam verifyParam) {
        boolean isEmpty = value == null || StringUtil.isEmpty(value.toString());
        int length = value == null ? 0 : value.toString().length();

        // 校验空
        if (isEmpty && verifyParam.required()) {
            throw new BusinessException(HttpCodeEnum.AOP_VALIDATE_EMPTY_FAIL, name + "不能为空");
        }
        // 校验长度
        if (!isEmpty && verifyParam.max() != -1 && length > verifyParam.max() || verifyParam.min() != -1 && length < verifyParam.min()) {
            throw new BusinessException(HttpCodeEnum.AOP_VALIDATE_LENGTH_FAIL, name + "长度不符合要求");
        }
        // 校验正则
        if (!isEmpty && !StringUtil.isEmpty(verifyParam.regex().getRegex()) && !VerifyUtil.verify(verifyParam.regex(), String.valueOf(value))) {
            throw new BusinessException(HttpCodeEnum.AOP_VALIDATE_REGEX_FAIL, name + "格式不符合要求");
        }
    }

    /**
     * 校验对象参数
     *
     * @param value
     */
    private void checkObjValue(Object value) {
        try {
            Class<?> clazz1 = value.getClass();
            Field[] fields = clazz1.getDeclaredFields();
            for (Field field : fields) {
                VerifyParam verifyParam = field.getAnnotation(VerifyParam.class);
                if (verifyParam == null) continue;
                field.setAccessible(true);
                Object resultValue = field.get(value);
                checkValue(field.getName(), resultValue, verifyParam);
            }
        } catch (Exception e) {
            throw new BusinessException(HttpCodeEnum.AOP_VALIDATE_TYPE_FAIL, "参数不符合要求");
        }
    }
}
