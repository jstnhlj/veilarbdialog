package no.nav.fo.veilarbdialog;

import no.nav.sbl.dialogarena.types.Pingable;
import no.nav.sbl.dialogarena.types.Pingable.Ping;
import no.nav.sbl.dialogarena.types.Pingable.Ping.PingMetadata;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;


@Configuration
@EnableJms
public class MessageQueueContext {

    @Bean
    public Pingable varselQueuePingable(JmsTemplate varselQueue) {
        final PingMetadata metadata = new PingMetadata(
                "VarselQueue via " + System.getProperty("mqGateway03.hostname"),
                "Brukes for å sende varsler til bruker om nye dialoger.",
                true
        );
        return () -> {
            try {
                varselQueue.getConnectionFactory().createConnection().close();
            } catch (JMSException e) {
                return Ping.feilet(metadata, "Kunne ikke opprette connection", e);
            }
            return Ping.lyktes(metadata);
        };
    }

    @Bean
    public JmsTemplate varselQueue() throws NamingException {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory());
        jmsTemplate.setDefaultDestination(varselDestination());
        return jmsTemplate;
    }

    private ConnectionFactory connectionFactory() throws NamingException {
        return (ConnectionFactory) new InitialContext().lookup("java:jboss/mqConnectionFactory");
    }

    private Destination varselDestination() throws NamingException {
        return (Destination) new InitialContext().lookup("java:/jboss/jms/VARSELPRODUKSJON.VARSLINGER");
    }

}
