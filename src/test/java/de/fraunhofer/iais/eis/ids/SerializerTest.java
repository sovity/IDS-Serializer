package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.JsonPreprocessor;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.TypeNamePreprocessor;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class SerializerTest { 

    private static ConnectorAvailableMessage basicInstance;
    private static Connector nestedInstance;
    private static RejectionMessage enumInstance;
    private static Connector securityProfileInstance;
    private static Serializer serializer;

    @BeforeClass
    public static void setUp() throws ConstraintViolationException, DatatypeConfigurationException, URISyntaxException, MalformedURLException {
        serializer = new Serializer();

        // object with only basic types
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        basicInstance = new ConnectorAvailableMessageBuilder()
                ._issued_(now)
                ._modelVersion_("2.0.0")
                ._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer").toURI())
                .build();

        ArrayList<Resource> resources = new ArrayList<>();
        resources.add(new ResourceBuilder()._version_("2.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard1").toURI()).build());
        resources.add(new ResourceBuilder()._version_("2.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard2").toURI()).build());

        // connector -> object with nested types
        Catalog catalog = new CatalogBuilder()
                ._offer_(resources)
                .build();

        nestedInstance = new BaseConnectorBuilder()
                ._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer").toURI())
                ._version_("2.0.0")
                ._catalog_(catalog)
                .build();

        // object with enum
        enumInstance = new RejectionMessageBuilder()
                ._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer").toURI())
                ._modelVersion_("2.0.0")
                ._rejectionReason_(RejectionReason.METHOD_NOT_SUPPORTED)
                .build();

        securityProfileInstance = new BaseConnectorBuilder()
                ._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer").toURI())
                ._version_("1.0.0")
                ._catalog_(catalog)
                ._securityProfile_(SecurityProfile.BASE_CONNECTOR_SECURITY_PROFILE)
                .build();
    }

    @Test
    public void jsonldSerialize_Basic() throws IOException {
        String connectorAvailableMessage = serializer.serialize(basicInstance);
        Assert.assertNotNull(connectorAvailableMessage);
        Model model = null;
        try {
            model = Rio.parse(new StringReader(connectorAvailableMessage), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);

        ConnectorAvailableMessage deserializedConnectorAvailableMessage = serializer.deserialize(connectorAvailableMessage, ConnectorAvailableMessageImpl.class);
        Assert.assertEquals(basicInstance.getId(), deserializedConnectorAvailableMessage.getId());
        Assert.assertNotNull(deserializedConnectorAvailableMessage);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(basicInstance, deserializedConnectorAvailableMessage, true, Object.class, true));
    }

    @Test
    public void jsonldSerialize_Nested() throws IOException {
        String connector = serializer.serialize(nestedInstance, RDFFormat.JSONLD);
        Assert.assertNotNull(connector);

        Model model = null;
        try {
            model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);

        Connector deserializedConnector = serializer.deserialize(connector, BaseConnectorImpl.class);
        Assert.assertNotNull(deserializedConnector);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(nestedInstance, deserializedConnector, true, Object.class, true));
    }

    @Test
    public void jsonldSerialize_Enum() throws IOException {
        String rejectionMessage = serializer.serialize(enumInstance, RDFFormat.JSONLD);
        Assert.assertNotNull(rejectionMessage);

        Model model = null;
        try {
            model = Rio.parse(new StringReader(rejectionMessage), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);

        RejectionMessage deserializedRejectionMessage = serializer.deserialize(rejectionMessage, RejectionMessage.class);
        Assert.assertNotNull(deserializedRejectionMessage);
        Assert.assertTrue(EqualsBuilder.reflectionEquals(enumInstance, deserializedRejectionMessage, true, Object.class, true));
    }

    @Test
    public void jsonldSerialize_SecurityProfile() throws IOException {
        String connector = serializer.serialize(securityProfileInstance, RDFFormat.JSONLD);
        Assert.assertNotNull(connector);

        Model model = null;
        try {
            model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);
    }

    @Test
    public void jsonldSerialize_Literal() throws ConstraintViolationException, IOException {
        Resource resource = new ResourceBuilder()
                ._description_(Util.asList(new PlainLiteral("literal no langtag"), new PlainLiteral("english literal", "en")))
                .build();

        String serialized = serializer.serialize(resource);
        Assert.assertNotNull(serialized);

        Model model = null;
        try {
            model = Rio.parse(new StringReader(serialized), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);

        // do not use reflective equals here as ArrayList comparison fails due to different modCount
        Resource deserializedResource = serializer.deserialize(serialized, ResourceImpl.class);
        Assert.assertEquals(2, deserializedResource.getDescription().size());
        Iterator<? extends PlainLiteral> names = deserializedResource.getDescription().iterator();

        Assert.assertTrue(names.next().getLanguage().isEmpty());
        Assert.assertFalse(names.next().getLanguage().isEmpty());
    }

    @Test
    public void legacySerializationsJson_validate() {
        Connector connector = null;
        Connector connector_update = null;
        try {
            connector = serializer.deserialize(readResourceToString("Connector1.json"), Connector.class);
            connector_update = serializer.deserialize(readResourceToString("Connector1_update.json"), Connector.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(connector);
        Assert.assertNotNull(connector_update);
    }

    @Test
    public void legacySerializationsJsonld_validate() {
        Connector connector = null;
        Connector connector2 = null;
        try {
            connector = serializer.deserialize(readResourceToString("Connector1.jsonld"), Connector.class);
            connector2 = serializer.deserialize(readResourceToString("Connector2.jsonld"), Connector.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(connector);
        Assert.assertNotNull(connector2);


        Model model = null;
        try {
            model = Rio.parse(new StringReader(readResourceToString("Connector1.jsonld")), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);

        model = null;
        try {
            model = Rio.parse(new StringReader(readResourceToString("Connector2.jsonld")), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(model);
    }

    @Test
    public void deserializeSingleValueAsArray() {
        ContractOffer contractOffer = null;
        try {
            contractOffer = serializer.deserialize(readResourceToString("ContractOfferValueForArray.jsonld"), ContractOffer.class);
        } catch(IOException e) {
            e.printStackTrace();
        }
        Assert.assertNotNull(contractOffer);
    }

    @Test
    public void deserializeWithAndWithoutTypePrefix() {
        String withIdsPrefix = "{\n" +
                "  \"@context\" : \"https://w3id.org/idsa/contexts/2.0.0/context.jsonld\",\n" +
                "  \"@type\" : \"ids:TextResource\",\n" +
                "  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
                "}";
        String withAbsoluteURI = "{\n" +
                "  \"@type\" : \"https://w3id.org/idsa/core/TextResource\",\n" +
                "  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
                "}";

        String withoutExplicitPrefix = "{\n" +
                "  \"@context\" : \"https://w3id.org/idsa/contexts/2.0.0/context.jsonld\",\n" +
                "  \"@type\" : \"TextResource\",\n" +
                "  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
                "}";

        try {

            Object defaultDeserialization = serializer.deserialize(withIdsPrefix, TextResource.class);

            JsonPreprocessor preprocessor = new TypeNamePreprocessor();
            serializer.addPreprocessor(preprocessor, true);

            Object deserializedWithIdsPrefix = serializer.deserialize(withIdsPrefix, TextResource.class);
            Object deserializedWithAbsoluteURI = serializer.deserialize(withAbsoluteURI, TextResource.class);
            Object deserializedWithoutExplicitPrefix = serializer.deserialize(withoutExplicitPrefix, TextResource.class);

            serializer.removePreprocessor(preprocessor);

            Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithIdsPrefix, true, Object.class, true));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithAbsoluteURI, true, Object.class, true));
            Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithoutExplicitPrefix, true, Object.class, true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deserializePluralForms() throws IOException {
        String serializedCatalog = readResourceToString("InstanceWithPluralFields.jsonld");
        Catalog deserialized = serializer.deserialize(serializedCatalog, Catalog.class);
        Assert.assertNotNull(deserialized);
        Assert.assertNotNull(deserialized.getOffer());
        Assert.assertFalse(deserialized.getOffer().isEmpty());
    }

    private String readResourceToString(String resourceName) throws IOException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream(resourceName);
        StringWriter writer = new StringWriter();
        IOUtils.copy(is, writer, "UTF-8");
        return writer.toString();
    }
}
