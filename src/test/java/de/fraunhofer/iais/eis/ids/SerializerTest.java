package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;

public class SerializerTest {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static BrokerDataRequest basicInstance;
    private static DataTransfer complexInstance;
    private static Serializer serializer;

    @BeforeClass
    public static void setUp() throws ConstraintViolationException {
        serializer = new Serializer();

        // object with only basic types and enums
        basicInstance = new BrokerDataRequestBuilder()
                .dataRequestAction(BrokerDataRequestAction.REGISTER)
                .coveredEntity(EntityCoveredByDataRequest.CONNECTOR)
                .messageContent("Hello world")
                .build();

        // object with nested types
        TransferAttribute transferAttribute = new TransferAttributeBuilder()
                .transferAttributeKey("key")
                .transferAttributeValue("value")
                .build();

        complexInstance = new DataTransferBuilder()
                .authToken(new AuthTokenBuilder().tokenValue("dummyToken").build())
                .customAttributes(Arrays.asList(transferAttribute))
                .build();

        // todo: object with plain, language and typed literals
    }

    @Test
    public void plainJsonSerialize_Basic() throws IOException {
        String brokerDataRequest = mapper.writeValueAsString(this.basicInstance);
        BrokerDataRequestImpl deserializedDataRequest = mapper.readValue(brokerDataRequest, BrokerDataRequestImpl.class);
        Assert.assertNotNull(deserializedDataRequest);
    }

    @Test
    public void plainJsonSerialize_Complex() throws IOException {
        String dataTransfer = mapper.writeValueAsString(this.complexInstance);
        DataTransfer deserializedTransfer = mapper.readValue(dataTransfer, DataTransferImpl.class);
        Assert.assertNotNull(deserializedTransfer);
    }

    @Test
    public void deserialzeFromJsonLD_Basic() throws IOException {
        String serializiedJsonLD = serializer.toJsonLD(basicInstance);
        BrokerDataRequest deserialized = mapper.readValue(serializiedJsonLD, BrokerDataRequestImpl.class);

        Assert.assertEquals(basicInstance.getDataRequestAction(), deserialized.getDataRequestAction());
        Assert.assertEquals(basicInstance.getCoveredEntity(), deserialized.getCoveredEntity());
        Assert.assertEquals(basicInstance.getMessageContent(), deserialized.getMessageContent());
    }

    @Test
    public void serializeToJsonLD_Basic() throws IOException, JSONException {
        String serializiedJsonLD = serializer.toJsonLD(basicInstance);

        InputStream brokerJsonLDstream = getClass().getClassLoader().getResourceAsStream("BrokerDataRequestJsonLD.txt");
        String expectedJsonLD = IOUtils.toString(brokerJsonLDstream, Charset.defaultCharset());

        JSONAssert.assertEquals(serializiedJsonLD, expectedJsonLD, true);
    }

    @Test
    public void jsonLDisValidRDF_Basic() throws IOException {
        String serializiedJsonLD = serializer.toJsonLD(basicInstance);
        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);
        Assert.assertEquals(model.size(), 4);

        // todo Benedikt: add a test to make sure that objects of the enum property (datarequestaction and coveredentity) are resources
    }

    //todo: add tests for complex instance (deserialize, serialze, validRdf)

    //todo: add tests for instance with different literal types
}
