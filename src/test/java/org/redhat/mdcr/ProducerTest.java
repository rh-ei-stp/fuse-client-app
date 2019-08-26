package org.redhat.mdcr;

import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.CamelSpringBootRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.Assert.assertEquals;

@RunWith(CamelSpringBootRunner.class)
@SpringBootTest
@EnableAutoConfiguration
@ComponentScan
@ContextConfiguration()
public class ProducerTest {

    @Autowired
    CamelContext context;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    ProducerTemplate producerTemplate;

    @Produce(uri = "direct:input")
    protected ProducerTemplate in;

    @EndpointInject(uri = "mock:produceramqp:queue.test")
    private MockEndpoint out;

    @Before
    public void configureMocks() throws Exception {
        context.getRouteDefinition("producer")
            .adviceWith(context, new AdviceWithRouteBuilder() {
                @Override
                public void configure() throws Exception {
                    replaceFromWith("direct:input");
                    mockEndpointsAndSkip("produceramq*");
                }
            });
    }

    @Test
    public void testSampleRoute () throws Exception {

        // expectations
        out.expectedMessageCount(1);

        // exercise the route
        in.sendBodyAndProperty("hello", "TIMERCOUNTER", "1");

        // make assertions
        out.assertIsSatisfied(1);
        Exchange exchange = out.getReceivedExchanges().get(0);
        assertEquals(256, exchange.getIn().getBody().toString().getBytes().length);
        assertEquals("1", exchange.getProperty("TIMERCOUNTER", String.class));


    }


}