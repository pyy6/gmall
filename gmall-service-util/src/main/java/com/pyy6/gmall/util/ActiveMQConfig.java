package com.pyy6.gmall.util;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.Session;

/**
 * @param
 * @return
 */

@Configuration//如果在这没有任何配置文件路径的配置，就说明是访问各个系统中的application。properties
public class ActiveMQConfig {


    @Value("${spring.activemq.broker-url:disabled}")
    String brokerURL;

    @Value("${activemq.listener.enable:disabled}")
    String listenerEnable;

    @Bean
    public ActiveMQUtil getActiveMQUtil() {
        if("disabled".equals(brokerURL)){
            return null;
        }
        ActiveMQUtil activeMQUtil = new ActiveMQUtil();
        activeMQUtil.init(brokerURL);
        return activeMQUtil;
    }

    @Bean(name = "jmsQueueListener")
    public DefaultJmsListenerContainerFactory jmsQueueListenerContainerFactory(ActiveMQConnectionFactory activeMQConnectionFactory) {

        if("disabled".equals(listenerEnable)){
            return null;
        }

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(activeMQConnectionFactory);

        factory.setSessionTransacted(false);

        factory.setSessionAcknowledgeMode(Session.AUTO_ACKNOWLEDGE);
        factory.setConcurrency("5");
        factory.setRecoveryInterval(5000L);

        return factory;

    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory ( ){

        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(  brokerURL);
        return activeMQConnectionFactory;
    }

}