package org.redhat.integration.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProducerRouteBuilder extends RouteBuilder {

	@Value("${amqp.producer.address}")
	private String address;
	
	@Value("${amqp.producer.enabled}")
	private boolean enabled;
	
	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.transform(simple("${exception.message}"))
				.log("Exception: ${body}");

		from("timer://foo?fixedRate=true&period=1000")
				.routeId("producer")
				.autoStartup(enabled)
				.setBody(simple("Test Message #${exchangeProperty.CamelTimerCounter} at ${date:now}"))
				.log("${body}")
				.to("producer-amqp:" + address);

	}

}
