package com.mrtripop.aspect;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class GlobalAspect {

  @Around("execution(public * com.mrtripop..*(..))")
  public Object handleLogMessageEntireApplication(ProceedingJoinPoint joinPoint) throws Throwable {
    if (!log.isDebugEnabled()) {
      return joinPoint.proceed();
    }

    Object[] args = joinPoint.getArgs();
    String className = joinPoint.getTarget().getClass().getSimpleName();
    String methodName = joinPoint.getSignature().getName();
    log.debug("In: {}.{}() | args={}", className, methodName, Arrays.toString(args));
    try {
      Object result = joinPoint.proceed();
      log.debug("Out: {}.{}() | result={}", className, methodName, result);
      return result;
    } catch (Throwable ex) {
      log.debug(
          "Out: {}.{}() | exception={}", className, methodName, ex.getClass().getSimpleName());
      throw ex;
    }
  }
}
