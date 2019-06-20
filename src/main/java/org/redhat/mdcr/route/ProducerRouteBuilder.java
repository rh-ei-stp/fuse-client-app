package org.redhat.mdcr.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProducerRouteBuilder extends RouteBuilder {

	@Value("${queue.name}")
	private String queueName;
	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.transform(simple("${exception.message}"))
				.log("Exception: ${body}");

		from("timer://foo?fixedRate=true&period=1000").routeId("producer")
				.setBody(simple("Test Message at ->" + "${date:now}")).log("${body}")
				.to("amqp:" + queueName);

	}

}
