package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.eis.BaseConnector;
import de.fraunhofer.iais.eis.Catalog;
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

//	@Test
	public void testBaseConnector() throws IOException {
//		logger.info(baseConnector);
		Serializer serializer = new Serializer();
		serializer.addPreprocessor(new TypeNamePreprocessor());
		BaseConnector base = serializer.deserialize(baseConnector, BaseConnector.class);
		String serialisedJsonLd = serializer.serialize(base);
//		logger.info(serialisedJsonLd);
		assertTrue(!serialisedJsonLd.isEmpty());
	}


	@Test
	public void testCatalog() throws IOException {
		String catalogAsString = "{\r\n" + 
				"  \"@context\" : \"https://w3id.org/idsa/contexts/3.0.0/context.jsonld\",\r\n" + 
				" \"@type\" : \"ids:Catalog\",\r\n" + 
				"  \"@id\" : \"https://iais.fraunhofer.de/eis/ids/someBroker/catalog\",\r\n" + 
				"   \"offer\" : [ {\r\n" + 
				"     \"@id\" : \"https://w3id.org/idsa/autogen/resource/8bc5b952-1376-4356-93cd-7be0e669c587\",\r\n" + 
				"     \"@type\" : \"ids:Resource\",\r\n" + 
				"     \"contentType\" : {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/SCHEMA_DEFINITION\"\r\n" + 
				"     },\r\n" + 
				"     \"contractOffer\" : {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/autogen/contractOffer/f979e468-5bd3-4be6-8e8c-bfc73c61caaf\",\r\n" + 
				"       \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"       \"provider\" : \"https://example.com/provider\"\r\n" + 
				"     },\r\n" + 
				"     \"description\" : \"This is the description of the resource from a test\",\r\n" + 
				"     \"language\" : [ {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/JA\"\r\n" + 
				"     }, {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/PT\"\r\n" + 
				"     } ],\r\n" + 
				"     \"title\" : \"This is the title of a resource from a broker test\",\r\n" + 
				"     \"version\" : \"1.1\"\r\n" + 
				"   }, {\r\n" + 
				"     \"@id\" : \"https://w3id.org/idsa/autogen/resource/4db59158-0e18-4326-85ed-e8bc1b7f8b6c\",\r\n" + 
				"     \"@type\" : \"ids:Resource\",\r\n" + 
				"     \"contentType\" : {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/SCHEMA_DEFINITION\"\r\n" + 
				"     },\r\n" + 
				"     \"contractOffer\" : {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/autogen/contractOffer/d92ea163-7d50-438e-b9b2-33ea653f7b25\",\r\n" + 
				"       \"@type\" : \"ids:ContractOffer\",\r\n" + 
				"       \"provider\" : \"https://example.com/provider\"\r\n" + 
				"     },\r\n" + 
				"     \"description\" : \"This is the description of the resource from a test\",\r\n" + 
				"     \"language\" : [ {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/JA\"\r\n" + 
				"     }, {\r\n" + 
				"       \"@id\" : \"https://w3id.org/idsa/code/PT\"\r\n" + 
				"     } ],\r\n" + 
				"     \"title\" : \"This is the title of a resource from a broker test\",\r\n" + 
				"     \"version\" : \"1.1\"\r\n" + 
				"   } ]\r\n" + 
				" }}";
		Serializer serializer = new Serializer();
		serializer.addPreprocessor(new TypeNamePreprocessor());
		Catalog catalog = serializer.deserialize(catalogAsString, Catalog.class);
		String serialisedJsonLd = serializer.serialize(catalog);
		logger.info(serialisedJsonLd);
		assertTrue(!serialisedJsonLd.isEmpty());

	}

}
