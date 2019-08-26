package org.redhat.mdcr.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProducerRouteBuilder extends RouteBuilder {

	@Value("${producer.queue.name}")
	private String queueName;
	
	@Value("${producer.route.switch}")
	private boolean runRoute;

	@Value("${producer.message.size.bytes}")
	private int producerMessageSizeBytes;

	@Value("${producer.message.period.millis}")
	private int producerMessagePeriodMillis;

	@Value("${producer.message.count}")
	private int producerMessageRepeatCount;

	@Override
	public void configure() throws Exception {

		onException(Exception.class)
				.handled(true)
				.transform(simple("${exception.message}"))
				.log("Exception: ${body}");

		from("timer://foo?fixedRate=true&repeatCount=" + producerMessageRepeatCount + "&period=" + producerMessagePeriodMillis)
				.routeId("producer").autoStartup(runRoute)
				.process(new Processor() {
					@Override
					public void process(Exchange exchange) throws Exception {
						byte[] r = new byte[producerMessageSizeBytes];
						exchange.getIn().setBody(new String(r));
					}
				})
				.log("Produced message #${property.TIMERCOUNTER}")
				.to("produceramqp:" + queueName);

	}

}
