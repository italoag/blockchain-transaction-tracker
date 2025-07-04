package dev.bloco.wallet.handler;

import dev.bloco.wallet.model.TrackingRequest;
import dev.bloco.wallet.service.TransactionTrackingService;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Component
public class TrackingWebSocketHandler implements WebSocketHandler {

    private final TransactionTrackingService trackingService;

    public TrackingWebSocketHandler(TransactionTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(msg -> msg.getPayloadAsText())
                .map(json -> new TrackingRequest("address", json, java.util.List.of()))
                .flatMapMany(trackingService::track)
                .map(tx -> session.textMessage(tx.hash()))
                .as(session::send);
    }
}
