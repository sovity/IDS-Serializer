package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.BrokerDataRequest;
import de.fraunhofer.iais.eis.BrokerDataRequestAction;
import de.fraunhofer.iais.eis.BrokerDataRequestBuilder;
import de.fraunhofer.iais.eis.EntityCoveredByDataRequest;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import org.junit.Before;

public class TestSerialization {

    private BrokerDataRequest request;

    @Before
    private void setUp() throws ConstraintViolationException {
        request = new BrokerDataRequestBuilder()
                .dataRequestAction(BrokerDataRequestAction.REGISTER)
                .coveredEntity(EntityCoveredByDataRequest.CONNECTOR)
                .messageContent("Hello world")
                .build();
    }

}
