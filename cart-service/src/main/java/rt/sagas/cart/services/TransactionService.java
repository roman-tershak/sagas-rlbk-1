package rt.sagas.cart.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.cart.entities.Transaction;
import rt.sagas.cart.entities.TransactionStatus;
import rt.sagas.cart.repositories.TransactionRepository;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.QueueNames;
import rt.sagas.events.services.EventService;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;

@Service
public class TransactionService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EventService eventService;

    @Transactional(REQUIRES_NEW)
    public void authorizeTransaction(String reservationId, Long orderId, Long userId, String cartNumber)
            throws Exception {

        Optional<Transaction> mayAlreadyExist = transactionRepository.findByOrderId(orderId);
        if (!mayAlreadyExist.isPresent()) {

            Transaction transaction = new Transaction(cartNumber, orderId, userId, TransactionStatus.AUTHORIZED);
            transactionRepository.save(transaction);

            eventService.storeOutgoingEvent(
                    CART_AUTHORIZED_EVENT_QUEUE,
                    new CartAuthorizedEvent(reservationId, cartNumber, orderId, userId));

            LOGGER.info("Transaction {} authorized", transaction);
        } else {
            LOGGER.warn("Transaction {} has already been created", mayAlreadyExist.get());
        }
    }

}
