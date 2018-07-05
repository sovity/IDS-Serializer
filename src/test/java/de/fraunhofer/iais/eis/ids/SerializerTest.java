package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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

    private static ObjectMapper mapper;
    private static BrokerDataRequest basicInstance;
    private static DataTransfer nestedInstance;
    private static Serializer serializer;

    @BeforeClass
    public static void setUp() throws ConstraintViolationException {
        serializer = new Serializer();
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

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

        nestedInstance = new DataTransferBuilder()
                .authToken(new AuthTokenBuilder().tokenValue("dummyToken").build())
                .customAttributes(Arrays.asList(transferAttribute))
                .build();

        // todo: object with plain, language and typed literals
    }

    @Test
    public void plainJsonSerialize_Basic() throws IOException {
        String brokerDataRequest = mapper.writeValueAsString(basicInstance);
        BrokerDataRequestImpl deserializedDataRequest = mapper.readValue(brokerDataRequest, BrokerDataRequestImpl.class);
        Assert.assertNotNull(deserializedDataRequest);
    }

    @Test
    public void plainJsonSerialize_Nested() throws IOException {
        String dataTransfer = mapper.writeValueAsString(nestedInstance);
        DataTransfer deserializedTransfer = mapper.readValue(dataTransfer, DataTransferImpl.class);
        Assert.assertNotNull(deserializedTransfer);
    }

    @Test
    public void deserialzeFromJsonLD_Basic() throws IOException {
        String serializiedJsonLD = serializer.toJsonLD(ObjectType.BASIC);
        BrokerDataRequest deserialized = mapper.readValue(serializiedJsonLD, BrokerDataRequestImpl.class);

        Assert.assertEquals(basicInstance.getDataRequestAction(), deserialized.getDataRequestAction());
        Assert.assertEquals(basicInstance.getCoveredEntity(), deserialized.getCoveredEntity());
        Assert.assertEquals(basicInstance.getMessageContent(), deserialized.getMessageContent());
    }

    @Test
    public void deserialzeFromJsonLD_Nested() throws IOException {
        String serializiedJsonLD = serializer.toJsonLD(ObjectType.NESTED);
        DataTransfer deserialized = mapper.readValue(serializiedJsonLD, DataTransfer.class);

        Assert.assertNotNull(deserialized.getAuthToken());
        Assert.assertNotNull(deserialized.getCustomAttributes());

        Assert.assertEquals(nestedInstance.getAuthToken().getTokenValue(), deserialized.getAuthToken().getTokenValue());

        TransferAttribute expectedTransferAttribute = nestedInstance.getCustomAttributes().iterator().next();
        TransferAttribute actualTransferAttribute = deserialized.getCustomAttributes().iterator().next();

        Assert.assertEquals(expectedTransferAttribute.getTransferAttributeKey(), actualTransferAttribute.getTransferAttributeKey());
        Assert.assertEquals(expectedTransferAttribute.getTransferAttributeValue(), actualTransferAttribute.getTransferAttributeValue());
    }

    @Test
    public void serializeToJsonLD_Basic() throws IOException, JSONException {
        String serializiedJsonLD = serializer.toJsonLD(ObjectType.BASIC);

        InputStream brokerJsonLDstream = getClass().getClassLoader().getResourceAsStream("BrokerDataRequestJsonLD.txt");
        String expectedJsonLD = IOUtils.toString(brokerJsonLDstream, Charset.defaultCharset());

        JSONAssert.assertEquals(serializiedJsonLD, expectedJsonLD, true);
    }

    @Test
    public void jsonLDisValidRDF_Basic() throws IOException {
        String serializiedJsonLD = serializer.toJsonLD(ObjectType.BASIC);
        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);
        Assert.assertEquals(model.size(), 4);

        // todo Benedikt: add a test to make sure that objects of the enum property (datarequestaction and coveredentity) are resources
    }

    //todo: add tests for complex instance (serialize, validRdf)

    //todo: add tests for instance with different literal types
}
