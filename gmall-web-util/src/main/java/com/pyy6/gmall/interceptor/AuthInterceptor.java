package com.pyy6.gmall.interceptor;

import com.pyy6.gmall.annotation.LoginRequire;
import com.pyy6.gmall.util.CookieUtil;
import com.pyy6.gmall.util.HttpClientUtil;
import com.pyy6.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {//系统级别的拦截器，需要配置拦截器以及拦截方式，拦截所有访问web端的方法

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //判断当前访问的方法是否需要认证拦截：贴标签：注解，可以通过反射方式获得，然后获取这个标签的说明
        HandlerMethod method = (HandlerMethod)handler;
        LoginRequire methodAnnotation = method.getMethodAnnotation(LoginRequire.class);

        if(methodAnnotation == null){//方法没有注解，不需要sso认证的，直接放行
            return true;
        }

        StringBuffer returnUrl = request.getRequestURL();
        String oldToken = CookieUtil.getCookieValue(request, "token", true);
        String newToken = request.getParameter("newToken");

        String token = "";
            /*oldToken : 老token
            newToken：新token
            oldToken不空，新token空，用户登陆过
            oldToken空，新token不空，用户第一次登陆
            oldToken空，新token空，用户从没登陆
            oldToken不空，新token不空，用户登录过期*/
        if(StringUtils.isNotBlank(oldToken) && StringUtils.isBlank(newToken)){token = oldToken;}
        if(StringUtils.isBlank(oldToken) && StringUtils.isNotBlank(newToken)){token = newToken;}
        if(StringUtils.isNotBlank(oldToken) && StringUtils.isNotBlank(newToken)){token = newToken;}

        if(methodAnnotation.ifNeedSuccess()){//必须登录
            if(StringUtils.isBlank(token)){//没有token
                //拦截到sso 且 登录成功之后，需要重新访问之前的页面
                response.sendRedirect("http://passport.gmall.com:8087/index?returnUrl="+returnUrl);
                return false;
            }else{//token不为空，就要远程访问passport（webService远程过程调用rpc）sso验证身份
                String s = HttpClientUtil.doGet("http://passport.gmall.com:8087/verify?token="+token+"&currentip="+getMyIp(request));//不受拦截的
                if(s.equals("success")){//验证成功,刷新过期时间
                    CookieUtil.setCookie(request,response,"token",token,60*60*2,true);
                    Map userMap = JwtUtil.decode("pyy6", token, getMyIp(request));
                    request.setAttribute("userId",userMap.get("userId"));//给购物车用，决定有没有用户走哪条分支
                    return true;
                }else{//验证失败，无论是否已经登录或者登录过期，都跳转登录页面
                    response.sendRedirect("http://passport.gmall.com:8087/index?returnUrl="+returnUrl);
                    return false;
                }
            }
        }
        if(!methodAnnotation.ifNeedSuccess()){//不一定需要登录

            if(StringUtils.isBlank(token)){//没有token
                return true;//?????
            }else{//token不为空，就要远程访问passport（webService远程过程调用rpc）sso验证身份
                String s = HttpClientUtil.doGet("http://passport.gmall.com:8087/verify?token="+token+"&currentip="+getMyIp(request));//不受拦截的
                if(s.equals("success")){//验证成功
                    CookieUtil.setCookie(request,response,"token",token,60*60*2,true);
                    Map userMap = JwtUtil.decode("pyy6", token, getMyIp(request));
                    request.setAttribute("userId",userMap.get("userId"));//给购物车用，决定有没有用户走哪条分支
                    return true;
                }else{//验证失败，无论是否已经登录或者登录过期，都跳转登录页面
                    response.sendRedirect("http://passport.gmall.com:8087/index?returnUrl="+returnUrl);
                    return false;
                }
            }
        }
        return true;
    }
    private String getMyIp(HttpServletRequest request) {
        String ip = "";
        ip = request.getHeader("x-forwarded-for");//经过负载均衡的ip
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();//直接获取ip
        }
        if(StringUtils.isBlank(ip)){
            ip = "127.0.0.1";
        }
        return ip;
    }
}
