package org.redhat.mdcr.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRouteBuilder extends RouteBuilder {

	@Value("${queue.name}")
	private String queueName;
	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
			.handled(true)
			.log("******#####Exception occured. Body is ${body}");

		from("amqp:" + queueName).routeId("consumer")
			.log("${body}");

	}

}
