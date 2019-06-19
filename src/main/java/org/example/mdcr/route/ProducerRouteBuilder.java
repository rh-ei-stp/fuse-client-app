package org.example.mdcr.route;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;


@Component
public class ProducerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer://foo?period=5000")
                .setBody().constant("Hello World")
                .to(">>> ${body}");

    }

}
