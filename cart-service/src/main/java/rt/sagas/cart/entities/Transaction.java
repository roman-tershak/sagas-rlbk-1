package rt.sagas.cart.entities;

import javax.persistence.*;

@Entity(name = "TRANSACTIONS")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String cartNumber;
    @Column(unique = true)
    private Long orderId;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public Transaction() {
    }

    public Transaction(String cartNumber, Long orderId, Long userId, TransactionStatus status) {
        this.cartNumber = cartNumber;
        this.orderId = orderId;
        this.userId = userId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getCartNumber() {
        return cartNumber;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Transaction{");
        sb.append("id=").append(id);
        sb.append(", cartNumber='").append(cartNumber).append('\'');
        sb.append(", orderId=").append(orderId);
        sb.append(", userId=").append(userId);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
