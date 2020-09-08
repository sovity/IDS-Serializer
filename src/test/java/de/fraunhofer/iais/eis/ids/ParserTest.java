package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;

import de.fraunhofer.iais.eis.*;
import org.junit.Test;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

public class ParserTest {

	//Logger logger = LoggerFactory.getLogger(ParserTest.class);


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



	/**
	 * This one tests whether the serializer can work with relative URIs.
	 *
	 * @throws IOException
	 */
	@Test
	public void relativeUriTest() throws IOException {

		String baseConnector = SerializerUtil.readResourceToString("Connector1.jsonld");
		baseConnector = baseConnector.replace("\"curator\" : \"http://companyA.com/ids/participant\"","\"curator\" : \"./ids/participant\"");
		BaseConnector connector = new Serializer().deserialize(baseConnector, BaseConnector.class);

		String serializedConnector = new Serializer().serialize(connector);
		assertTrue(serializedConnector.contains("/ids/participant"));
	}
}
