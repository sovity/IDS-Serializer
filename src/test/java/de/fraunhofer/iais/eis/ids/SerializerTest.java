package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SerializerTest {

    private static ConnectorAvailableMessage basicInstance;
    private static Connector nestedInstance;
    private static RejectionMessage enumInstance;
    private static Serializer serializer;

    @BeforeClass
    public static void setUp() throws ConstraintViolationException, DatatypeConfigurationException, MalformedURLException {
        serializer = new Serializer();

        // object with only basic types
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(new Date());
        XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        basicInstance = new ConnectorAvailableMessageBuilder()
                ._issued_(now)
                ._modelVersion_("1.0.0")
                ._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer"))
                .build();

        ArrayList<Resource> resources = new ArrayList<>();
        resources.add(new ResourceBuilder()._version_("1.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard1")).build());
        resources.add(new ResourceBuilder()._version_("2.0.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard2")).build());

        // connector -> object with nested types
        Catalog catalog = new CatalogBuilder()
                ._offers_(resources)
                .build();

        nestedInstance = new BaseConnectorBuilder()
                ._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer"))
                ._version_("1.0.0")
                ._catalog_(catalog)
                .build();

        // object with enum
        enumInstance = new RejectionMessageBuilder()
                ._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer"))
                ._modelVersion_("1.0.0")
                ._rejectionReason_(RejectionReason.METHOD_NOT_SUPPORTED)
                .build();
    }

    @Test
    public void jsonldSerialize_Basic() throws IOException {
        String connectorAvailableMessage = serializer.serialize(basicInstance);
        Model model;
        try {
            model = Rio.parse(new StringReader(connectorAvailableMessage), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
            model = null;
        }
        Assert.assertNotNull(model);

        ConnectorAvailableMessage deserializedConnectorAvailableMessage = serializer.deserialize(connectorAvailableMessage, ConnectorAvailableMessageImpl.class);
        Assert.assertNotNull(connectorAvailableMessage);
        //TODO assert object equals
    }

    @Test
    public void jsonldSerialize_Nested() throws IOException {
        String connector = serializer.serialize(nestedInstance, RDFFormat.JSONLD);
        Model model;
        try {
            model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
            model = null;
        }
        Assert.assertNotNull(model);

        Connector deserializedTransfer = serializer.deserialize(connector, BaseConnectorImpl.class);
        //TODO assert object equals
    }

    @Test
    public void jsonldSerialize_Enum() throws IOException {
        String rejectionMessage = serializer.serialize(enumInstance, RDFFormat.JSONLD);
        System.out.println(rejectionMessage);
        Model model;
        try {
            model = Rio.parse(new StringReader(rejectionMessage), null, RDFFormat.JSONLD);
        } catch (Exception e) {
            e.printStackTrace();
            model = null;
        }
        Assert.assertNotNull(model);

        RejectionMessage deserialized = serializer.deserialize(rejectionMessage, RejectionMessage.class);
        //TODO equality check
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
}
