package org.redhat.mdcr.config;

import org.apache.camel.component.amqp.AMQPComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.qpid.jms.JmsConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.connection.CachingConnectionFactory;

@Configuration
public class QPIDRouterConfiguration {

	@Value("${router.url}")
	private String url;
	@Value("${router.user}")
	private String username;
	@Value("${router.password}")
	private String password;

	//@Bean(name = "jmsConnectionFactory")
	private JmsConnectionFactory jmsConnectionFactory() throws Exception {

		JmsConnectionFactory jmsConnectionFactory = new JmsConnectionFactory();
		try {
			jmsConnectionFactory.setRemoteURI(url);
			jmsConnectionFactory.setUsername(username);
			jmsConnectionFactory.setPassword(password);
		} catch (Exception e) {
			throw new Exception(e);
		}
		return jmsConnectionFactory;
	}

	@Bean(name = "jmsCachingConnectionFactory")
	public CachingConnectionFactory cachingConnectionFactory() throws Exception {
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		cachingConnectionFactory.setTargetConnectionFactory(jmsConnectionFactory());
		return cachingConnectionFactory;
	}

	@Bean(name = "jmsConfig")
	public JmsConfiguration jmsConfiguration(
			@Qualifier("jmsCachingConnectionFactory") CachingConnectionFactory cachingConnectionFactory) {
		JmsConfiguration jmsConfiguration = new JmsConfiguration();
		jmsConfiguration.setConnectionFactory(cachingConnectionFactory);
		return jmsConfiguration;
	}

	@Bean(name = "amqp")
	public AMQPComponent amqpComponent(
			@Qualifier("jmsConfig") JmsConfiguration jmsConfiguration) {
		AMQPComponent component = new AMQPComponent();
		component.setConfiguration(jmsConfiguration);
		return component;
	}
}
