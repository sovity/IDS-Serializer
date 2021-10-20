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

    Logger logger = LoggerFactory.getLogger(ParserTest.class);

    @Ignore
    @Test
    public void UriOrModelClassCorrectTranslationTest() throws IOException, DatatypeConfigurationException {
        BaseConnector baseConnector = new BaseConnectorBuilder()
                //._curatorAsUri_(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/1"))
                //._curator_(new ParticipantBuilder()
                //        ._version_("1")
                //        ._legalForm_("Very legal")
                //        .build())
                ._hasAgent_(new ArrayList<>(Arrays.asList(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/2"))))
                //._maintainerAsUri_(URI.create("http://example.com/participant/uriormodelclasscorrecttranslation/2"))
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()
                        ._accessURL_(URI.create("http://example.com/endpoint/uriormodelclasscorrecttranslation/1"))
                        .build()
                )
                ._inboundModelVersion_("4.4.4")
                ._outboundModelVersion_("4.4.4")
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                .build();
        String baseConnectorAsString = new Serializer().serialize(baseConnector);
        logger.info(baseConnectorAsString);

        Assert.assertTrue(baseConnectorAsString.contains("Very legal"));
        Assert.assertFalse(baseConnectorAsString.contains("http://example.com/participant/uriormodelclasscorrecttranslation/1"));

        BaseConnector recreated = new Serializer().deserialize(baseConnectorAsString, BaseConnector.class);
        String recreatedBaseConnectorAsString = new Serializer().serialize(recreated);

        //logger.info(recreatedBaseConnectorAsString);

        Assert.assertTrue(recreatedBaseConnectorAsString.contains("Very legal"));
        Assert.assertFalse(recreatedBaseConnectorAsString.contains("http://example.com/participant/uriormodelclasscorrecttranslation/1"));
    }


}
