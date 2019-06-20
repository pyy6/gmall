package com.pyy6.gmall;

import com.pyy6.gmall.util.ActiveMQUtil;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan("com.pyy6.gmall.payment.mapper")
public class GmallPaymentApplication {

    @Autowired
    ActiveMQUtil activeMQUtil;
    public static void main(String[] args) {
        SpringApplication.run(GmallPaymentApplication.class, args);
    }

}
