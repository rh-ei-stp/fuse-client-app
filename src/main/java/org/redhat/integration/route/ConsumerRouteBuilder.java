package org.redhat.integration.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRouteBuilder extends RouteBuilder {

	@Value("${amqp.consumer.address}")
	private String address;
	
	@Value("${amqp.consumer.enabled}")
	private boolean enabled;
	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.transform(simple("${exception.message}"))
				.log("Exception: ${body}");

		from("consumer-amqp:" + address)
				.routeId("consumer")
				.autoStartup(enabled)
				.log("${body}");

	}

}
