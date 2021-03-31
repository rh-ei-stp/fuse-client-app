package org.redhat.integration.config;

import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
public class AMQPConfiguration {

	@Value("${amqp.producer.uri}")
	private String producerUrl;
	@Value("${amqp.producer.user}")
	private String producerUsername;
	@Value("${amqp.producer.password}")
	private String producerPassword;
	
	@Value("${amqp.consumer.uri}")
	private String consumerUrl;
	@Value("${amqp.consumer.user}")
	private String consumerUsername;
	@Value("${amqp.consumer.password}")
	private String consumerPassword;
	
	private JmsConnectionFactory jmsConnectionFactory(String type) throws Exception {

		JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
		try {
			if (type.equals("producer")) {
				jmsConnectionFactory.setRemoteURI(producerUrl);
				jmsConnectionFactory.setUsername(producerUsername);
				jmsConnectionFactory.setPassword(producerPassword);
			} else if (type.equals("consumer")) {
				jmsConnectionFactory.setRemoteURI(consumerUrl);
				jmsConnectionFactory.setUsername(consumerUsername);
				jmsConnectionFactory.setPassword(consumerPassword);
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

	@Bean(name = "producer-amqp")
	public AMQPComponent producerAmqpComponent() throws Exception {
		AMQPComponent component = new AMQPComponent();
		component.setConfiguration(jmsConfiguration("producer"));
		return component;
	}
	
	@Bean(name = "consumer-amqp")
	public AMQPComponent consumerAmqpComponent() throws Exception {
		AMQPComponent component = new AMQPComponent();
		component.setConfiguration(jmsConfiguration("consumer"));
		return component;
	}
}
