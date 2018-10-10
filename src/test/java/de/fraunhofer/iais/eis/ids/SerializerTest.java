package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.ObjectType;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.apache.commons.io.IOUtils;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class SerializerTest {

    private static ConnectorAvailableMessage basicInstance;
    private static Connector nestedInstance;
    private static Serializer serializer;
//    private static DataAsset polymorphic;

    @BeforeClass
    public static void setUp() throws ConstraintViolationException, DatatypeConfigurationException, MalformedURLException {
        serializer = new Serializer();

        // object with only basic types and enums
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        basicInstance = new ConnectorAvailableMessageBuilder()
                ._issued_(now)
                ._modelVersion_("1.0.0")
                ._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssues"))
                .build();

        ArrayList<Resource> resources = new ArrayList<>();
        resources.add(new ResourceBuilder()._version_("1.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard1")).build());
        resources.add(new ResourceBuilder()._version_("2.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard2")).build());

        // connector -> nested
        // object with nested types
        Catalog catalog = new CatalogBuilder()
                ._offers_(resources)
                .build();

        nestedInstance = new BaseConnectorBuilder()
                ._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer"))
                ._version_("1.0.0")
                ._catalog_(catalog)
                .build();

/*        Instant instant = new InstantBuilder().named(NamedInstant.TODAY).build();
        polymorphic = new DataAssetBuilder().coversTemporal(Util.asList(instant)).build();*/
    }

    @Test
    public void plainJsonSerialize_Basic() throws IOException {
        String connectorAvailableMessage = serializer.serialize(basicInstance);
        System.out.println(connectorAvailableMessage);
        ConnectorAvailableMessage deserializedDataRequest = serializer.deserialize(connectorAvailableMessage, ConnectorAvailableMessageImpl.class);
        Assert.assertNotNull(deserializedDataRequest);
    }

    @Test
    public void plainJsonSerialize_Nested() throws IOException {
        String connector = serializer.serialize(nestedInstance, RDFFormat.JSONLD);
        System.out.println(connector);
        Model model;
        try {
            model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
            model = null;
        }
        Assert.assertNotNull(model);
        Connector deserializedTransfer = serializer.deserialize(connector, BaseConnectorImpl.class);
        Assert.assertNotNull(deserializedTransfer);
    }

/*    @Test
    public void plainJsonSerialize_Polymorphic() throws IOException {
        String dataAsset = serializer.serialize(polymorphic);
        DataAsset deserializedDataAsset = serializer.deserialize(dataAsset, DataAssetImpl.class);
        Assert.assertNotNull(deserializedDataAsset);
        Assert.assertTrue(deserializedDataAsset.getCoversTemporal().iterator().next() instanceof Instant);
    }*/

    @Test
    public void plainJsonSerialize_Literal() throws ConstraintViolationException, IOException {
        Resource resource = new ResourceBuilder()
                ._descriptions_(Util.asList(new PlainLiteral("literal no langtag"), new PlainLiteral("english literal", "en")))
                .build();

        String serialized = serializer.serialize(resource);
        System.out.println(serialized);
        Resource deserializedResource = serializer.deserialize(serialized, ResourceImpl.class);

        Assert.assertEquals(2, deserializedResource.getDescriptions().size());
        Iterator<? extends PlainLiteral> names = deserializedResource.getDescriptions().iterator();
        Assert.assertTrue(names.next().getLanguage().isEmpty());
        Assert.assertFalse(names.next().getLanguage().isEmpty());
    }

/*    @Test
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
        DataTransfer deserialized = serializer.deserialize(serializiedJsonLD, DataTransferImpl.class);

        Assert.assertNotNull(deserialized.getAuthToken());
        Assert.assertNotNull(deserialized.getCustomAttributes());

        Assert.assertEquals(nestedInstance.getAuthToken().getTokenValue(), deserialized.getAuthToken().getTokenValue());

        TransferAttribute expectedTransferAttribute = nestedInstance.getCustomAttributes().iterator().next();
        TransferAttribute actualTransferAttribute = deserialized.getCustomAttributes().iterator().next();

        Assert.assertEquals(expectedTransferAttribute.getTransferAttributeKey(), actualTransferAttribute.getTransferAttributeKey());
        Assert.assertEquals(expectedTransferAttribute.getTransferAttributeValue(), actualTransferAttribute.getTransferAttributeValue());
    }*/

    @Test
    public void serializeToJsonLD_Basic() throws IOException, JSONException {
        String serializiedJsonLD = serializer.serialize(ObjectType.BASIC);

        InputStream brokerJsonLDstream = getClass().getClassLoader().getResourceAsStream("BrokerDataRequestJsonLD.txt");
        String expectedJsonLD = IOUtils.toString(brokerJsonLDstream, Charset.defaultCharset());

        JSONAssert.assertEquals(serializiedJsonLD, expectedJsonLD, true);
    }

    /*@Test
    public void jsonLDisValidRDF_Basic() throws IOException {
        String serializiedJsonLD = serializer.serialize(ObjectType.BASIC);
        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);

        Assert.assertEquals(4, model.size());

        ValueFactory factory = SimpleValueFactory.getInstance();
        Model subModel;

        subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/coveredEntity"), null);
        subModel.forEach(triple -> Assert.assertTrue(triple.getObject() instanceof Resource));

        subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/dataRequestAction"), null);
        subModel.forEach(triple -> Assert.assertTrue(triple.getObject() instanceof Resource));
    }*/


/*    @Test
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
    public void testMultiLangLiteralSerialization() throws IOException {
        String serializiedJsonLD = serializer.serialize(ObjectType.LANG_LIT);

        DataAsset deserAsset = serializer.deserialize(serializiedJsonLD, DataAssetImpl.class);
        Assert.assertNotNull(deserAsset);

        Model model = Rio.parse(new StringReader(serializiedJsonLD), null, RDFFormat.JSONLD);
        ValueFactory factory = SimpleValueFactory.getInstance();
        Model subModel = model.filter(null, factory.createIRI("https://w3id.org/ids/core/entityName"),null);

        List<Statement> list = subModel.stream().collect(Collectors.toList());
        Assert.assertEquals(2, list.size());

        Iterator<Statement> namesIt = list.iterator();
        Literal firstLiteral = (Literal) namesIt.next().getObject();
        Literal secondLiteral = (Literal) namesIt.next().getObject();

        Assert.assertTrue(!firstLiteral.getLabel().isEmpty() && !firstLiteral.getLanguage().isPresent());
        Assert.assertTrue(!secondLiteral.getLabel().isEmpty() && secondLiteral.getLanguage().isPresent());
    }

*/


}
