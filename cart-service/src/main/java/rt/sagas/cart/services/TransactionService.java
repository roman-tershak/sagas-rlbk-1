package rt.sagas.cart.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rt.sagas.cart.entities.Transaction;
import rt.sagas.cart.repositories.TransactionRepository;
import rt.sagas.events.CartAuthorizedEvent;
import rt.sagas.events.CartEvent;
import rt.sagas.events.CartRejectedEvent;

import javax.transaction.Transactional;
import java.util.Optional;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;
import static rt.sagas.cart.entities.TransactionStatus.AUTHORIZED;
import static rt.sagas.cart.entities.TransactionStatus.REJECTED;
import static rt.sagas.events.QueueNames.CART_AUTHORIZED_EVENT_QUEUE;
import static rt.sagas.events.QueueNames.CART_REJECTED_EVENT_QUEUE;

@Service
public class TransactionService {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(REQUIRES_NEW)
    public CartEvent authorizeCart(String reservationId, Long orderId, Long userId, String cartNumber) {

        Optional<Transaction> mayAlreadyExist = transactionRepository.findByOrderId(orderId);
        if (!mayAlreadyExist.isPresent()) {

            String cartSuffix = cartNumber.substring(cartNumber.length() - 1, cartNumber.length());
            if (cartSuffix.equals("1") || cartSuffix.equals("7") || cartSuffix.equals("9")) {

                return rejectTransaction(reservationId, orderId, userId, cartNumber,
                        "Cart number ends with " + cartSuffix);
            } else {
                return authorizeTransaction(reservationId, orderId, userId, cartNumber);
            }
        } else {
            Transaction transaction = mayAlreadyExist.get();
            LOGGER.warn("Transaction {} has already been created", transaction);

            if (transaction.getStatus() == AUTHORIZED) {
                return new CartAuthorizedEvent(reservationId, cartNumber, orderId, userId);
            } else {
                return new CartRejectedEvent(reservationId, cartNumber, orderId, userId, transaction.getReason());
            }
        }
    }

    private CartEvent authorizeTransaction(String reservationId, Long orderId, Long userId, String cartNumber) {

        Transaction transaction = transactionRepository.save(
                new Transaction(cartNumber, orderId, userId, AUTHORIZED));

        LOGGER.info("Transaction {} authorized", transaction);

        return new CartAuthorizedEvent(reservationId, cartNumber, orderId, userId);
    }

    private CartEvent rejectTransaction(
            String reservationId, Long orderId, Long userId, String cartNumber, String reason) {

        Transaction transaction = transactionRepository.save(
                new Transaction(cartNumber, orderId, userId, REJECTED, reason));

        LOGGER.info("Transaction {} rejected", transaction);

        return new CartRejectedEvent(reservationId, cartNumber, orderId, userId, reason);
    }

    public String getQueueName(CartEvent cartEvent) {
        if (CartAuthorizedEvent.class.isInstance(cartEvent)) {
            return CART_AUTHORIZED_EVENT_QUEUE;
        } else {
            return CART_REJECTED_EVENT_QUEUE;
        }
    }
}
