package com.pyy6.gmall.util;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * @param
 * @return
 */
public class JwtUtil {

    public static void main(String[] args){
        //生成token，颁发token
        String salt = "127.0.0.1";//有关访问浏览器的信息
        HashMap<String, String> hashMap = new HashMap<>();//用户登录成功后的用户信息
        hashMap.put("nickName","haha");
        String token = encode("haha", hashMap, salt);//key是服务端生成的由自己保管的跟这个用户相关的密钥
        System.out.println(token);

        //验证身份    获取浏览器携带过来的token以及salt。
        Map userMap = decode("haha", token, salt);
        System.out.println(userMap);
    }


    /***
     * jwt加密
     * @param key
     * @param map
     * @param salt
     * @return
     */
    public static String encode(String key,Map map,String salt){

        if(salt!=null){
            key+=salt;
        }
        JwtBuilder jwtBuilder = Jwts.builder().signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.addClaims(map);

        String token = jwtBuilder.compact();
        return token;
    }

    /***
     * jwt解密
     * @param key
     * @param token
     * @param salt
     * @return
     * @throws SignatureException
     */
    public static  Map decode(String key,String token,String salt)throws SignatureException{
        if(salt!=null){
            key+=salt;
        }
        Claims map = null;

        map = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();

        System.out.println("map.toString() = " + map.toString());

        return map;

    }

}
