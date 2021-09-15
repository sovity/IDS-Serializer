package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.junit.Assert;
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

    @Test
    public void UriOrModelClassCorrectTranslationTest() throws IOException, DatatypeConfigurationException {
        Rule rule = new PermissionBuilder()
                ._target_(URI.create("http://SchwachkopfSchwabbelspeckKrimskramsQuiek"))
                ._targetAsAsset_(new ArtifactBuilder()._checkSum_("4711").build())
                ._assigner_(new ArrayList<>(Arrays.asList(URI.create("http://Dumbledore"))))
                ._assignerAsParticipant_(new ParticipantBuilder()
                        ._version_("FreshDumbledore")
                        ._legalForm_("Almost legal")
                        .build())
                ._assigneeAsParticipant_(new ParticipantBuilder()
                        ._version_("SeverusSnape")
                        ._legalForm_("Not so legal")
                        .build())
                ._assignee_(new ArrayList<>(Arrays.asList(URI.create("http://severus.snape.org"))))
                ._postDuty_(new ArrayList<>(Arrays.asList(new DutyBuilder()
                        ._title_(new ArrayList<>(Arrays.asList(new TypedLiteral("Clean owls"))))
                        .build())))
                .build();
        String ruleAsString = new Serializer().serialize(rule);
        logger.info(ruleAsString);

        Assert.assertFalse(ruleAsString.contains("http://SchwachkopfSchwabbelspeckKrimskramsQuiek"));
        Assert.assertTrue(ruleAsString.contains("4711"));

        Assert.assertFalse(ruleAsString.contains("http://Dumbledore"));
        Assert.assertTrue(ruleAsString.contains("FreshDumbledore"));

        Assert.assertFalse(ruleAsString.contains("SeverusSnape"));
        Assert.assertTrue(ruleAsString.contains("http://severus.snape.org"));

        Rule recreated = new Serializer().deserialize(ruleAsString, Rule.class);
        String recreatedRuleAsString = new Serializer().serialize(recreated);

        logger.info(recreatedRuleAsString);

        Assert.assertTrue(recreatedRuleAsString.contains("4711"));
        Assert.assertTrue(recreatedRuleAsString.contains("FreshDumbledore"));


    }


}
