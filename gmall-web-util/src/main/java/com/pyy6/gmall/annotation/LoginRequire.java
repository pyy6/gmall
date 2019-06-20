package com.pyy6.gmall.annotation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequire {

    boolean ifNeedSuccess() default true;//这个方法的作用是版主判断是否可以不登录（购物车）
}
