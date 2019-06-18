package rt.sagas.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import static javax.jms.Session.SESSION_TRANSACTED;

@Configuration
public class JMSConfiguration {

    @Value("${spring.activemq.broker-url}")
    private String brokerUrl;

    @Value("${spring.jms.redelivery-policy.maximum-redeliveries:100}")
    private Integer maximumRedeliveries;
    @Value("${spring.jms.redelivery-policy.redelivery-delay:1000}")
    private Long redeliveryDelay;
    @Value("${spring.jms.redelivery-policy.initial-redelivery-delay:5000}")
    private Long initialRedeliveryDelay;

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(cachingConnectionFactory());
        jmsTemplate.setMessageConverter(messageConverter());
        jmsTemplate.setSessionTransacted(true);
        return jmsTemplate;
    }

    @Bean("jmsListenerContainerFactory")
    public JmsListenerContainerFactory queueListenerFactory() {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setSessionTransacted(true);
        factory.setSessionAcknowledgeMode(SESSION_TRANSACTED);
        factory.setMessageConverter(messageConverter());
        factory.setConnectionFactory(activeMQConnectionFactory());
        return factory;
    }

    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(activeMQConnectionFactory());
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();

        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(maximumRedeliveries);
        redeliveryPolicy.setInitialRedeliveryDelay(initialRedeliveryDelay);
        redeliveryPolicy.setRedeliveryDelay(redeliveryDelay);
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy);

        activeMQConnectionFactory.setBrokerURL(brokerUrl);
        return activeMQConnectionFactory;
    }

    protected MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    protected ObjectMapper objectMapper(){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

}
