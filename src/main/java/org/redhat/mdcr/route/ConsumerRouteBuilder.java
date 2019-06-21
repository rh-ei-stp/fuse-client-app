package org.redhat.mdcr.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConsumerRouteBuilder extends RouteBuilder {

	@Value("${consumer.queue.name}")
	private String queueName;
	
	@Value("${consumer.route.switch}")
	private boolean runRoute;
	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.transform(simple("${exception.message}"))
				.log("Exception: ${body}");


		from("consumeramqp:" + queueName).routeId("consumer").autoStartup(runRoute)
				.log("${body}");

	}

}
