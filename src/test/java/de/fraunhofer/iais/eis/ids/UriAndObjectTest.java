package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

public class UriAndObjectTest {

    Logger logger = LoggerFactory.getLogger(UriAndObjectTest.class);

    @Ignore
    @Test
    public void UriOrModelClassBaseConnectorCorrectTranslationTest() throws IOException {
        BaseConnector baseConnector = new BaseConnectorBuilder()
                ._curator_(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/1"))
//                ._curatorAsParticipant_(new ParticipantBuilder()
//                        ._version_("1")
//                        ._legalForm_("Very legal")
//                        .build())
                ._hasAgent_(new ArrayList<>(Arrays.asList(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/2"))))
//                ._maintainerAsParticipant_(new ParticipantBuilder()
//                        ._version_("2")
//                        ._legalForm_("illegal")
//                        .build())
                ._maintainer_(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/2"))
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()
                        ._accessURL_(URI.create("http://example.com/endpoint/uriormodelclasscorrecttranslation/1"))
                        .build()
                )
                ._inboundModelVersion_("4.4.4")
                ._outboundModelVersion_("4.4.4")
                //._securityProfile_(DefaultSecurityProfile.BASE_SECURITY_PROFILE)
                .build();
        String baseConnectorAsString = new Serializer().serialize(baseConnector);
        logger.info(baseConnectorAsString);

        Assert.assertTrue(baseConnectorAsString.contains("Very legal"));
        Assert.assertFalse(baseConnectorAsString.contains("http://example.com/participant/uriormodelclasscorrecttranslation/1"));
        Assert.assertFalse(baseConnectorAsString.contains("illegal"));

        BaseConnector recreated = new Serializer().deserialize(baseConnectorAsString, BaseConnector.class);
        String recreatedBaseConnectorAsString = new Serializer().serialize(recreated);

        //logger.info(recreatedBaseConnectorAsString);

        Assert.assertTrue(recreatedBaseConnectorAsString.contains("Very legal"));
        Assert.assertFalse(recreatedBaseConnectorAsString.contains("http://example.com/participant/uriormodelclasscorrecttranslation/1"));
        Assert.assertFalse(recreatedBaseConnectorAsString.contains("illegal"));
    }

    @Ignore
    @Test
    public void UriOrModelClassResourceCaralogTranslationTest() throws IOException {
        ResourceCatalog resourceCatalog = new ResourceCatalogBuilder()
//                ._offeredResourceAsUri_(new ArrayList<>(Arrays.asList(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/1"))))
//                ._offeredResourceAsUri_(new ArrayList<>(Arrays.asList(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/2"))))
                ._offeredResource_(new ArrayList<>(Arrays.asList(new ResourceBuilder()
                        ._version_("Resource V1")
                        .build()
                )))
//                ._requestedResourceAsUri_(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/1"))
//                ._requestedResourceAsUri_(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/2"))
                ._requestedResource_(new ResourceBuilder()
                        ._version_("Resource V2")
                        .build()
                )
                .build();
        String resourceCatalogAsString = new Serializer().serialize(resourceCatalog);
        logger.info(resourceCatalogAsString);

        Assert.assertTrue(resourceCatalogAsString.contains("Resource V1"));
        Assert.assertTrue(resourceCatalogAsString.contains("Resource V2"));
        Assert.assertFalse(resourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/1"));

        ResourceCatalog recreated = new Serializer().deserialize(resourceCatalogAsString, ResourceCatalog.class);
        String recreatedResourceCatalogAsString = new Serializer().serialize(recreated);

        Assert.assertTrue(recreatedResourceCatalogAsString.contains("Resource V1"));
        Assert.assertTrue(recreatedResourceCatalogAsString.contains("Resource V2"));
        Assert.assertFalse(recreatedResourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/1"));
    }

    @Ignore
    @Test
    public void UriOrModelClassResourceCaralogTranslationTestViceVersa() throws IOException {
        ResourceCatalog resourceCatalog = new ResourceCatalogBuilder()
                ._offeredResource_(new ArrayList<>(Arrays.asList(new ResourceBuilder()
                        ._version_("Resource V1")
                        .build()
                )))
//                ._offeredResourceAsUri_(new ArrayList<>(Arrays.asList(
//                        URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/1"),
//                        URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/2")))
//                )
                ._requestedResource_(new ResourceBuilder()
                        ._version_("Resource V2")
                        .build()
                )
//                ._requestedResourceAsUri_(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/3"))
//                ._requestedResourceAsUri_(URI.create("http://example.com/resource/uriormodelclasscorrecttranslation/4"))
                .build();
        String resourceCatalogAsString = new Serializer().serialize(resourceCatalog);
        logger.info(resourceCatalogAsString);


        Assert.assertFalse(resourceCatalogAsString.contains("Resource V1"));
        Assert.assertFalse(resourceCatalogAsString.contains("Resource V2"));
        Assert.assertTrue(resourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/1"));
        Assert.assertTrue(resourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/4"));

        ResourceCatalog recreated = new Serializer().deserialize(resourceCatalogAsString, ResourceCatalog.class);
        String recreatedResourceCatalogAsString = new Serializer().serialize(recreated);

        Assert.assertFalse(recreatedResourceCatalogAsString.contains("Resource V1"));
        Assert.assertFalse(recreatedResourceCatalogAsString.contains("Resource V2"));
        Assert.assertTrue(recreatedResourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/1"));
        Assert.assertTrue(recreatedResourceCatalogAsString.contains("http://example.com/resource/uriormodelclasscorrecttranslation/4"));
    }

}
