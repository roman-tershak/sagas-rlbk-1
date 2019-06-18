package rt.sagas.cart.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.cart.entities.Transaction;
import rt.sagas.cart.entities.TransactionStatus;
import rt.sagas.cart.repositories.TransactionRepository;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.CartDeclinedEvent;
import rt.sagas.events.QueueNames;
import rt.sagas.events.services.EventService;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.cart.entities.TransactionStatus.AUTHORIZED;
import static rt.sagas.cart.entities.TransactionStatus.DECLINED;
import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.CART_DECLINED_EVENT_QUEUE;

@Service
public class TransactionService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private EventService eventService;

    @Transactional(REQUIRES_NEW)
    public void authorizeCart(String reservationId, Long orderId, Long userId, String cartNumber)
            throws Exception {

        Optional<Transaction> mayAlreadyExist = transactionRepository.findByOrderId(orderId);
        if (!mayAlreadyExist.isPresent()) {

            String cartSuffix = cartNumber.substring(cartNumber.length() - 1, cartNumber.length());

            if (cartSuffix.equals("1") || cartSuffix.equals("7") || cartSuffix.equals("9")) {

                declineTransaction(reservationId, orderId, userId, cartNumber,
                        "Cart number ends with " + cartSuffix);
            } else {
                authorizeTransaction(reservationId, orderId, userId, cartNumber);
            }
        } else {
            LOGGER.warn("Transaction {} has already been created", mayAlreadyExist.get());
        }
    }

    private void authorizeTransaction(String reservationId, Long orderId, Long userId, String cartNumber)
            throws Exception {

        Transaction transaction = new Transaction(cartNumber, orderId, userId, AUTHORIZED);
        transactionRepository.save(transaction);

        eventService.storeOutgoingEvent(
                CART_AUTHORIZED_EVENT_QUEUE,
                new CartAuthorizedEvent(reservationId, cartNumber, orderId, userId));

        LOGGER.info("Transaction {} authorized", transaction);
    }

    private void declineTransaction(String reservationId, Long orderId, Long userId, String cartNumber, String reason)
            throws Exception {

        Transaction transaction = new Transaction(cartNumber, orderId, userId, DECLINED, reason);
        transactionRepository.save(transaction);

        eventService.storeOutgoingEvent(
                CART_DECLINED_EVENT_QUEUE,
                new CartDeclinedEvent(reservationId, cartNumber, orderId, userId, reason));

        LOGGER.info("Transaction {} declined", transaction);
    }

}
