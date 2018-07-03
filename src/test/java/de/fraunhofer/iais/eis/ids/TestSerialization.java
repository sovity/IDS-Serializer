package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

public class TestSerialization {

    private BrokerDataRequest request;
    private DataTransfer dataTransfer;

    @Before
    public void setUp() throws ConstraintViolationException {
        request = new BrokerDataRequestBuilder()
                .dataRequestAction(BrokerDataRequestAction.REGISTER)
                .coveredEntity(EntityCoveredByDataRequest.CONNECTOR)
                .messageContent("Hello world")
                .build();

        TransferAttribute transferAttribute = new TransferAttributeBuilder()
                .transferAttributeKey("key")
                .transferAttributeValue("value")
                .build();

        dataTransfer = new DataTransferBuilder()
                .authToken(new AuthTokenBuilder().tokenValue("dummyToken").build())
                .customAttributes(Arrays.asList(transferAttribute))
                .build();
    }

    @Test
    public void serializeDeserialize() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String brokerDataRequest = mapper.writeValueAsString(this.request);
        String dataTransfer = mapper.writeValueAsString(this.dataTransfer);

        DataTransfer deserializedTransfer = mapper.readValue(dataTransfer, DataTransferImpl.class);
        BrokerDataRequestImpl deserializedDataRequest = mapper.readValue(brokerDataRequest, BrokerDataRequestImpl.class);

        Assert.assertNotNull(deserializedTransfer);
        Assert.assertNotNull(deserializedDataRequest);
    }

}
