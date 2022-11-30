package RequestProcessing;

import Entities.LoginRequest;
import Entities.Response;
import io.smallrye.mutiny.Multi;
import io.smallrye.reactive.messaging.kafka.Record;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RequestProcessor {

    @Inject
    @Channel("validate-login-requests")
    Multi<String> resultsTest;

    // (Kafka Topics)
    @Incoming("login-requests")     // Read from
    @Outgoing("responses")          // Write to
    public Record takeRequest(LoginRequest loginRequest) {
        System.out.println("Read from topic: login-requests! -- In backend");
        System.out.println("Got loginRequest: " + loginRequest.getRequestId() + "  Username: " + loginRequest.getUsername() + "  Password: " + loginRequest.getPassword());

        // Since this is a proof of concept we ALWAYS assume that the account exists and that the login was successful
        return Record.of(loginRequest.getRequestId(), new Response(loginRequest.getRequestId(), true, true));
    }
}