package org.redhat.mdcr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class PropertyConfiguration {

	@Configuration
	@PropertySource({ "file:/deployments/config/configmap.properties" })
	//@PropertySource({ "file:/Users/vkonda/mdcr/client-consumer/src/main/resources/configmap.properties" })
	public class NonLocalhostConfiguration {

	}
}