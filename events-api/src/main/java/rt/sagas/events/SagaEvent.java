package rt.sagas.events;

import java.io.Serializable;

public abstract class SagaEvent implements Serializable {

    @Override
    public String toString() {
        return new StringBuilder("SagaEvent{}").toString();
    }
}
