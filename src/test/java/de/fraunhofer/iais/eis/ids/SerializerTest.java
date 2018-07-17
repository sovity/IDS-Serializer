package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
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
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SerializerTest {

    private static BrokerDataRequest basicInstance;
    private static DataTransfer nestedInstance;
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

        nestedInstance = new DataTransferBuilder()
                .authToken(new AuthTokenBuilder().tokenValue("dummyToken").build())
                .customAttributes(Arrays.asList(transferAttribute))
                .build();

        // todo: object with plain, language and typed literals
    }

    @Test
    public void plainJsonSerialize_Basic() throws IOException {
        String brokerDataRequest = serializer.serialize(basicInstance);
        BrokerDataRequestImpl deserializedDataRequest = serializer.deserialize(brokerDataRequest, BrokerDataRequestImpl.class);
        Assert.assertNotNull(deserializedDataRequest);
    }

    @Test
    public void plainJsonSerialize_Nested() throws IOException {
        String dataTransfer = serializer.serialize(nestedInstance);
        DataTransfer deserializedTransfer = serializer.deserialize(dataTransfer, DataTransferImpl.class);
        Assert.assertNotNull(deserializedTransfer);
    }

    @Test
    public void deserialzeFromJsonLD_Basic() throws IOException {
        String serializiedJsonLD = serializer.serialize(ObjectType.BASIC);
        BrokerDataRequest deserialized = serializer.deserialize(serializiedJsonLD, BrokerDataRequestImpl.class);

        Assert.assertEquals(basicInstance.getDataRequestAction(), deserialized.getDataRequestAction());
        Assert.assertEquals(basicInstance.getCoveredEntity(), deserialized.getCoveredEntity());
        Assert.assertEquals(basicInstance.getMessageContent(), deserialized.getMessageContent());
    }

    @Test
    public void deserialzeFromJsonLD_Nested() throws IOException {
        String serializiedJsonLD = serializer.serialize(ObjectType.NESTED);
        DataTransfer deserialized = serializer.deserialize(serializiedJsonLD, DataTransfer.class);

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
        String serializiedJsonLD = serializer.serialize(ObjectType.BASIC);

        InputStream brokerJsonLDstream = getClass().getClassLoader().getResourceAsStream("BrokerDataRequestJsonLD.txt");
        String expectedJsonLD = IOUtils.toString(brokerJsonLDstream, Charset.defaultCharset());

        JSONAssert.assertEquals(serializiedJsonLD, expectedJsonLD, true);
    }

    @Test
    public void jsonLDisValidRDF_Basic() throws IOException {
        String serializiedJsonLD = serializer.serialize(ObjectType.BASIC);
        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);

        Assert.assertEquals(4, model.size());

        ValueFactory factory = SimpleValueFactory.getInstance();
        Model subModel;

        subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/coveredEntity"),null);
        subModel.forEach(triple -> Assert.assertTrue(triple.getObject() instanceof Resource));

        subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/dataRequestAction"),null);
        subModel.forEach(triple -> Assert.assertTrue(triple.getObject() instanceof Resource));
    }


    @Test
    public void testIntSerialization() throws ConstraintViolationException, IOException {
        URL instantId = new URL("http://industrialdataspace.org/instant/8d43422f-30a2-401e-bcf3-bc2bae97b73c");
        Instant instant = new InstantBuilder(instantId)
                .namedValue(42)
                .build();

        // in future this will work
        //String serializiedJsonLD = serializer.serialize(instant);

        // emulated behaviour
        String serializiedJsonLD = serializer.serialize(ObjectType.INT_LIT);
        Instant deserInstant = serializer.deserialize(serializiedJsonLD, InstantImpl.class);
        Assert.assertNotNull(deserInstant);

        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);

        // instant URL match
        Model subModel = model.filter(null, RDF.TYPE,null);
        subModel.forEach(triple -> Assert.assertEquals(instant.getId().toString(), triple.getSubject().stringValue()));

        // integer value ends up as integer-typed literal
        ValueFactory factory = SimpleValueFactory.getInstance();
        subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/namedValue"),null);
        subModel.forEach(triple -> Assert.assertEquals(XMLSchema.INTEGER, ((Literal) triple.getObject()).getDatatype()));
    }


    @Test
    public void testMultiLangLiteralSerialization() throws ConstraintViolationException, IOException {
        DataAsset asset = new DataAssetBuilder()
                .entityNames(Arrays.asList(new PlainLiteral("literal no langtag"), new PlainLiteral("english literal", "en")))
                .build();

        //System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(asset));

        // emulated behaviour
        String serializiedJsonLD = serializer.serialize(ObjectType.LANG_LIT);

        DataAsset deserAsset = serializer.deserialize(serializiedJsonLD, DataAssetImpl.class);
        Assert.assertNotNull(deserAsset);

        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);
        ValueFactory factory = SimpleValueFactory.getInstance();
        Model subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/entityNames"),null);

        List<Statement> list = subModel.stream().collect(Collectors.toList());
        Assert.assertEquals(2, list.size());

        Iterator<Statement> namesIt = list.iterator();
        Literal firstLiteral = (Literal) namesIt.next().getObject();
        Literal secondLiteral = (Literal) namesIt.next().getObject();

        Assert.assertTrue(!firstLiteral.getLabel().isEmpty() && !firstLiteral.getLanguage().isPresent());
        Assert.assertTrue(!secondLiteral.getLabel().isEmpty() && firstLiteral.getLanguage().isPresent());
    }




}
