package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;

import de.fraunhofer.iais.eis.*;
import org.junit.Assert;
import org.junit.Test;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParserTest {

	Logger logger = LoggerFactory.getLogger(ParserTest.class);


	/**
	 * Main purpose: test for ids and idsc in the context
	 *
	 * @throws IOException if source file cannot be read or if parsing fails
	 */
	@Test
	public void testBaseConnector() throws IOException {
		String baseConnector = SerializerUtil.readResourceToString("Connector1.jsonld");
//		logger.info(baseConnector);

		Serializer serializer = new Serializer();
		//serializer.addPreprocessor(new TypeNamePreprocessor());
		BaseConnector base = serializer.deserialize(baseConnector, BaseConnector.class);
		String serialisedJsonLd = serializer.serialize(base);
//		logger.info(serialisedJsonLd);
		assertFalse(serialisedJsonLd.isEmpty());
	}


	/**
	 * Main purpose: test for JSON Arrays
	 *
	 * @throws IOException if source file cannot be read or if parsing fails
	 */
	@Test
	public void testCatalog() throws IOException {
		String catalogAsString = SerializerUtil.readResourceToString("Catalog1.jsonld");
		Serializer serializer = new Serializer();
		//serializer.addPreprocessor(new TypeNamePreprocessor());
		Catalog catalog = serializer.deserialize(catalogAsString, Catalog.class);
		String serialisedJsonLd = serializer.serialize(catalog);
//		logger.info(serialisedJsonLd);
		assertFalse(serialisedJsonLd.isEmpty());

	}


	@Test
	public void testConnectorWithComplexCatalog() throws IOException {
		String catalogAsString = SerializerUtil.readResourceToString("Catalog2.jsonld");
		Serializer serializer = new Serializer();
		//serializer.addPreprocessor(new TypeNamePreprocessor());
		ResourceCatalog catalog = serializer.deserialize(catalogAsString, ResourceCatalog.class);
		String serialisedJsonLd = serializer.serialize(catalog);
//		logger.info(serialisedJsonLd);
		assertFalse(serialisedJsonLd.isEmpty());
	}



	/**
	 * Main purpose: test for RDF Objects at JSON value position (reference by URI, not by xsd:anyURI or xsd:string Literals):
	 * for instance "ids:correlationMessage" : {"@id": "https://52d2c3e4-88de-42ee-9261-dfd239ccb863"} vs.
	 * "ids:correlationMessage" : "https://52d2c3e4-88de-42ee-9261-dfd239ccb863"
	 * @throws IOException if source file cannot be read or if parsing fails
	 *
	 */
	@Test
	public void testMessage() throws IOException {

		String messageString  = SerializerUtil.readResourceToString("MessageProcessedNotificationMessage.jsonld");

		Serializer serializer = new Serializer();
		//serializer.addPreprocessor(new TypeNamePreprocessor());

		MessageProcessedNotificationMessage message = (MessageProcessedNotificationMessage) serializer.deserialize(messageString, Message.class);
		assertNotNull(message.getCorrelationMessage());

		//logger.info(serializer.serialize(message));
		serializer.serialize(message);
	}

	@Test
	public void testArtifactRequestMessage() throws IOException {

		String messageString  = SerializerUtil.readResourceToString("ArtifactRequestMessage.jsonld");

		Serializer serializer = new Serializer();
		//serializer.addPreprocessor(new TypeNamePreprocessor());

		ArtifactRequestMessage message = (ArtifactRequestMessage) serializer.deserialize(messageString, Message.class);
		assertNotNull(message.getRequestedArtifact());

		//logger.info(serializer.serialize(message));
		serializer.serialize(message);
	}


	/**
	 * Test deserialize of ContracRejectionMessage
	 * Created an example of ContracRejectionMessage in String, deserialize it and check two properties of it.
	 * @throws IOException if source file cannot be read or if parsing fails
	 */
	@Test
	public void testContractRejectionMessage() throws IOException {

		String messageString  = SerializerUtil.readResourceToString("ContractRejectionMessage.jsonld");

		Serializer serializer = new Serializer();

		ContractRejectionMessage message = (ContractRejectionMessage) serializer.deserialize(messageString, Message.class);

		assertNotNull(message.getCorrelationMessage());
		assertNotNull(message.getSecurityToken());
		assertNotNull(message.getContractRejectionReason());

		serializer.serialize(message);
	}


	@Test
	public void testHeavyBaseConnector() throws IOException {

		String connectorString = SerializerUtil.readResourceToString("Connector3.jsonld");

		Serializer serializer = new Serializer();

		serializer.deserialize(connectorString, BaseConnector.class);

	}


	@Test
	public void parseMessageTest() throws IOException {
		String messageString = SerializerUtil.readResourceToString("MessageProcessedNotificationMessage.jsonld");
		Message message = new Serializer().deserialize(messageString, Message.class);
		System.out.println(message.toRdf()); //at this stage, it does nothing. Debug to look into variables
	}

	@Test
	public void avoidDuplicates() throws IOException {
		String infrastructureComponentString = SerializerUtil.readResourceToString("Connector2.jsonld");
		Connector connector = new Serializer().deserialize(infrastructureComponentString, Connector.class);
		assertNotNull(connector.getResourceCatalog());
		assertEquals(1, connector.getResourceCatalog().size());
		assertTrue(connector.getResourceCatalog().get(0).getOfferedResource().get(0).getKeyword().size() < 3);

	}



	/**
	 * This one tests whether the serializer can work with relative URIs.
	 *
	 * @throws IOException if parsing fails
	 */
	@Test
	public void relativeUriTest() throws IOException {

		String baseConnector = SerializerUtil.readResourceToString("Connector1.jsonld");
		baseConnector = baseConnector.replace("\"curator\" : \"http://companyA.com/ids/participant\"","\"curator\" : \"./ids/participant\"");
		BaseConnector connector = new Serializer().deserialize(baseConnector, BaseConnector.class);

		String serializedConnector = new Serializer().serialize(connector);
		assertTrue(serializedConnector.contains("/ids/participant"));
	}

	/**
	 * This test consumes a connector which has a loop inside the RDF (a connector with owl:sameAs reference to itself)
	 * @throws IOException if parsing fails
	 */
	@Test
	public void avoidStackOverflowTest() throws IOException {
		String illegalBaseConnector = SerializerUtil.readResourceToString("Connector5.jsonld");
		new Serializer().deserialize(illegalBaseConnector, BaseConnector.class);
	}

	@Test
	public void parseResourceTest() throws IOException {
		String resourceString = SerializerUtil.readResourceToString("Resource1.jsonld");
		new Serializer().deserialize(resourceString, Resource.class);
	}

	@Test(expected = IOException.class)
	public void parseUtterRubbishResourceTest() throws IOException {
		String connectorAsString = SerializerUtil.readResourceToString("ConnectorWithRubbishResource.jsonld");
		//This MUST throw an exception. The "resource" in the catalog is NOT an ids Resource, but some AAS stuff
		Connector c = new Serializer().deserialize(connectorAsString, Connector.class);
		logger.info(new Serializer().serialize(c));
	}

	@Test
	public void parsePermissionTest() throws IOException
	{
		String permissionAsString = SerializerUtil.readResourceToString("Permission.jsonld");
		Permission p = new Serializer().deserialize(permissionAsString, Permission.class);
		assertFalse(p.getTitle().isEmpty());
	}


	//https://github.com/International-Data-Spaces-Association/Java-Representation-of-IDS-Information-Model/issues/6
	@Test
	public void giveParserMultipleOptionsTest() throws IOException {
		/* ARRANGE */
		final Serializer serializer = new Serializer();
		final String input = "{\n"
				+ "        \"@context\" : {\n"
				+ "            \"ids\" : \"https://w3id.org/idsa/core/\",\n"
				+ "            \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
				+ "        },\n"
				+ "      \"@type\": \"ids:Permission\",\n"
				+ "      \"@id\": \"https://w3id"
				+ ".org/idsa/autogen/permission/c0bdb9d5-e86a-4bb3-86d2-2b1dc9d226f5\",\n"
				+ "      \"ids:description\": [\n"
				+ "        {\n"
				+ "          \"@value\": \"usage-notification\",\n"
				+ "          \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n"
				+ "        }\n"
				+ "      ],\n"
				+ "      \"ids:title\": [\n"
				+ "        {\n"
				+ "          \"@value\": \"Example Usage Policy\",\n"
				+ "          \"@type\": \"http://www.w3.org/2001/XMLSchema#string\"\n"
				+ "        }\n"
				+ "      ],\n"
				+ "      \"ids:action\": [\n"
				+ "        {\n"
				+ "          \"@id\": \"idsc:USE\"\n"
				+ "        }\n"
				+ "      ],\n"
				+ "      \"ids:postDuty\": [\n"
				+ "        {\n"
				+ "          \"@type\": \"ids:Duty\",\n"
				+ "          \"@id\": \"https://w3id"
				+ ".org/idsa/autogen/duty/863d2fac-1072-476d-b504-9d6347fe4b6f\",\n"
				+ "          \"ids:action\": [\n"
				+ "            {\n"
				+ "              \"@id\": \"idsc:NOTIFY\"\n"
				+ "            }\n"
				+ "          ],\n"
				+ "          \"ids:constraint\": [\n"
				+ "            {\n"
				+ "              \"@type\": \"ids:Constraint\",\n"
				+ "              \"@id\": \"https://w3id"
				+ ".org/idsa/autogen/constraint/c91e64ce-1fc1-44fd-bec1-6c6778603919\",\n"
				+ "              \"ids:rightOperand\": {\n"
				+ "                \"@value\": \"https://localhost:8080/api/ids/data\",\n"
				+ "                \"@type\": \"xsd:anyURI\"\n"
				+ "              },\n"
				+ "              \"ids:leftOperand\": {\n"
				+ "                \"@id\": \"idsc:ENDPOINT\"\n"
				+ "              },\n"
				+ "              \"ids:operator\": {\n"
				+ "                \"@id\": \"idsc:DEFINES_AS\"\n"
				+ "              }\n"
				+ "            }\n"
				+ "          ]\n"
				+ "        }\n"
				+ "      ]\n"
				+ "    }";

		/* ACT */
		final Rule result = serializer.deserialize(input, Rule.class);

		/* ASSERT */
		assertEquals(Action.USE, result.getAction().get(0));
	}

	@Test
	public void parseDatabaseBackupTest() throws IOException {
		String catalogString = SerializerUtil.readResourceToString("IdsLabDatabaseBackup.jsonld");
		ConnectorCatalog connectorCatalog = new Serializer().deserialize(catalogString, ConnectorCatalog.class);
		Assert.assertTrue(connectorCatalog.getListedConnector().size() > 8);
	}

	@Test
	public void should_be_equals() throws IOException {
		final String input = "{\n"
				+ "  \"@context\" : {\n"
				+ "    \"ids\" : \"https://w3id.org/idsa/core/\",\n"
				+ "    \"idsc\" : \"https://w3id.org/idsa/code/\"\n"
				+ "  },\n"
				+ "  \"@type\" : \"ids:Constraint\",\n"
				+ "  \"@id\" : \"https://w3id.org/idsa/autogen/constraint/4ae656d1-2a73"
				+ "-44e3-a168-b1cbe49d4622\",\n"
				+ "  \"ids:leftOperand\" : {\n"
				+ "    \"@id\" : \"https://w3id.org/idsa/code/COUNT\"\n"
				+ "  },\n"
				+ "  \"ids:rightOperand\" : {\n"
				+ "    \"@value\" : \"5\",\n"
				+ "    \"@type\" : \"http://www.w3.org/2001/XMLSchema#double\"\n"
				+ "  },\n"
				+ "  \"ids:operator\" : {\n"
				+ "    \"@id\" : \"https://w3id.org/idsa/code/LTEQ\"\n"
				+ "  }\n"
				+ "}";

		final Serializer deserializer = new Serializer();
		final Constraint obj1 = deserializer.deserialize(input, Constraint.class);
		final Constraint obj2 = deserializer.deserialize(input, Constraint.class);

		Assert.assertEquals(obj1, obj2);
	}

}
