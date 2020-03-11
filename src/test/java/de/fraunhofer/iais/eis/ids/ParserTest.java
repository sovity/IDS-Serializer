package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.TypeNamePreprocessor;

public class ParserTest {

	Logger logger = LoggerFactory.getLogger(ParserTest.class);
	
	private String baseConnector = "{\n" + 
			"  \"@context\" : \"https://w3id.org/idsa/contexts/3.0.0/context.jsonld\",\n" + 
			" \"@id\" : \"https://broker.ids.isst.fraunhofer.de/\",\n" + 
			"  \"@type\" : \"ids:BaseConnector\",\n" + 
			"  \"ids:catalog\" : {\n" + 
			"    \"@id\" : \"https://w3id.org/idsa/autogen/catalog/3039fcc6-0571-436d-86ef-5c51f78ce84a\",\n" + 
			"    \"@type\" : \"ids:Catalog\"\n" + 
			"  },\n" + 
			"  \"curator\" : \"https://example.com/curator\",\n" + 
			"  \"description\" : \"This is a dummy description from the Interaction Library\",\n" + 
			"  \"inboundModelVersion\" : \"2.0.1\",\n" + 
			"  \"maintainer\" : \"https://example.com/maintainer\",\n" + 
			"  \"outboundModelVersion\" : \"2.0.1\",\n" + 
			"  \"securityProfile\" : {\n" + 
			"    \"@id\" : \"idsc:BASE_CONNECTOR_SECURITY_PROFILE\"\n" + 
			"  },\n" + 
			"  \"title\" : \"This is a dummy title from the Interaction Library\"\n" + 
			"}";
	
	@Test
	public void test() throws IOException {
		Serializer serializer = new Serializer();
		serializer.addPreprocessor(new TypeNamePreprocessor());
		BaseConnector base = serializer.deserialize(baseConnector, BaseConnector.class);
		String serialisedJsonLd = serializer.serialize(base);
		assertTrue(!serialisedJsonLd.isEmpty());
	}

}
