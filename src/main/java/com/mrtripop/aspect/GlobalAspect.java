package com.mrtripop.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class GlobalAspect {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private static final int MAX_RESULT_LENGTH = 500;

  @Around("execution(public * com.mrtripop..*(..))")
  public Object handleLogMessageEntireApplication(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!log.isDebugEnabled()) {
      return joinPoint.proceed();
    }

    Object[] args = joinPoint.getArgs();
    String className = joinPoint.getSignature().getDeclaringTypeName();
    className = className.substring(className.lastIndexOf('.') + 1);
    String methodName = joinPoint.getSignature().getName();
    String readableArgs = Arrays.stream(args)
        .map(this::toJsonString)
        .collect(Collectors.joining(", "));
    log.debug(">> {}.{}() | args=[{}]", className, methodName, readableArgs);
    long start = System.currentTimeMillis();
    try {
      Object result = joinPoint.proceed();
      long elapsed = System.currentTimeMillis() - start;
      log.debug("<< {}.{}() | {}ms | result={}", className, methodName, elapsed, truncate(toJsonString(result)));
      return result;
    } catch (Throwable ex) {
      long elapsed = System.currentTimeMillis() - start;
      log.debug("<< {}.{}() | {}ms | exception={}", className, methodName, elapsed, ex.getClass().getSimpleName());
      throw ex;
    }
  }

  private String truncate(String value) {
    if (value.length() <= MAX_RESULT_LENGTH) {
      return value;
    }
    return value.substring(0, MAX_RESULT_LENGTH) + "...(truncated)";
  }

  private String toJsonString(Object obj) {
    if (obj == null) {
      return "null";
    }
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      return obj.toString();
    }
  }
}
