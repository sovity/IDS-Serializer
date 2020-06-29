package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;

import de.fraunhofer.iais.eis.*;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.TypeNamePreprocessor;

public class ParserTest {

	Logger logger = LoggerFactory.getLogger(ParserTest.class);


	/**
	 * Main purpose: test for ids and idsc in the context
	 * 
	 * @throws IOException
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
	 * @throws IOException
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
		Connector connector = serializer.deserialize(catalogAsString, Connector.class);
		String serialisedJsonLd = serializer.serialize(connector);
//		logger.info(serialisedJsonLd);
		assertFalse(serialisedJsonLd.isEmpty());
	}
	
	
	
	/**
	 * Main purpose: test for RDF Objects at JSON value position (reference by URI, not by xsd:anyURI or xsd:string Literals):
	 * for instance "ids:correlationMessage" : {"@id": "https://52d2c3e4-88de-42ee-9261-dfd239ccb863"} vs. 
	 * "ids:correlationMessage" : "https://52d2c3e4-88de-42ee-9261-dfd239ccb863"
	 * @throws IOException 
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
	public void testHeavyBaseConnector() throws IOException {
		
		String connectorString = SerializerUtil.readResourceToString("Connector3.jsonld");
		
		Serializer serializer = new Serializer();
		
		BaseConnector connector = serializer.deserialize(connectorString, BaseConnector.class);
		
	}

}
