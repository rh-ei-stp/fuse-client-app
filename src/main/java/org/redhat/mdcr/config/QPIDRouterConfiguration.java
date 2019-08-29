package org.redhat.mdcr.config;

import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
public class QPIDRouterConfiguration {

    @Value("${producer.router.url}")
    private String p_url;
    @Value("${producer.router.user}")
    private String p_username;
    @Value("${producer.router.password}")
    private String p_password;
    @Value("${producer.client.id}")
    private String p_id;

    @Value("${consumer.router.url}")
    private String c_url;
    @Value("${consumer.router.user}")
    private String c_username;
    @Value("${consumer.router.password}")
    private String c_password;
    @Value("${consumer.client.id}")
    private String c_id;

    private JmsConnectionFactory jmsConnectionFactory(String type) throws Exception {

        JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
        try {
            if (type.equals("producer")) {
                jmsConnectionFactory.setRemoteURI(p_url);
                jmsConnectionFactory.setUsername(p_username);
                jmsConnectionFactory.setPassword(p_password);
                jmsConnectionFactory.setClientID(p_id);
            } else if (type.equals("consumer")) {
                jmsConnectionFactory.setRemoteURI(c_url);
                jmsConnectionFactory.setUsername(c_username);
                jmsConnectionFactory.setPassword(c_password);
                jmsConnectionFactory.setClientID(c_id);
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
        return jmsConnectionFactory;
    }

    private CachingConnectionFactory cachingConnectionFactory(String type) throws Exception {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
        cachingConnectionFactory.setTargetConnectionFactory(jmsConnectionFactory(type));
        return cachingConnectionFactory;
    }

    private JmsConfiguration jmsConfiguration(String type) throws Exception {
        JmsConfiguration jmsConfiguration = new JmsConfiguration();
        jmsConfiguration.setConnectionFactory(cachingConnectionFactory(type));
        return jmsConfiguration;
    }

    @Bean(name = "produceramqp")
    public AMQPComponent producerAmqpComponent() throws Exception {
        AMQPComponent component = new AMQPComponent();
        component.setConfiguration(jmsConfiguration("producer"));
        return component;
    }

    @Bean(name = "consumeramqp")
    public AMQPComponent consumerAmqpComponent() throws Exception {
        AMQPComponent component = new AMQPComponent();
        component.setConfiguration(jmsConfiguration("consumer"));
        return component;
    }
}
