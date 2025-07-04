package dev.bloco.wallet.handler;

import dev.bloco.wallet.model.Transaction;
import dev.bloco.wallet.model.TrackingRequest;
import dev.bloco.wallet.service.TransactionTrackingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final TransactionTrackingService trackingService;

    public TrackingController(TransactionTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/transactions")
    public Flux<Transaction> track(@RequestBody TrackingRequest request) {
        return trackingService.track(request);
    }
}
