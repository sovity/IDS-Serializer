package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.MessageParser;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

public class ParserTest {

	Logger logger = LoggerFactory.getLogger(ParserTest.class);


	/**
	 * Main purpose: test for ids and idsc in the context
	 * 
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
		
		serializer.deserialize(connectorString, BaseConnector.class);
		
	}

	@Test
	public void ontologyDownloadTest() throws IOException {
		MessageParser.downloadOntology = true;
		MessageParser.init();
		String messageString = SerializerUtil.readResourceToString("MessageProcessedNotificationMessage.jsonld");

		Model model = MessageParser.readMessageAndOntology(messageString);
		logger.info("Model contains " + model.size() + " triples.");
		Assert.assertTrue(model.size() > 300);
	}

	@Test
	public void extractClassesFromMessageString() throws IOException {
		String messageString = SerializerUtil.readResourceToString("MessageProcessedNotificationMessage.jsonld");

		Model model = MessageParser.readMessage(messageString);

		//For incoming messages, we just tell the serializer that it is a Message, but not what kind of message
		//So we use the same input here and require to compute the correct subclass.
		Class<?> targetClass = Message.class;

		ArrayList<Class<?>> implementingClasses = MessageParser.getImplementingClasses(targetClass);


		String queryString = "SELECT ?type { ?s a ?type . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
		ResultSet resultSet = queryExecution.execSelect();
		Assert.assertTrue(resultSet.hasNext());


		while (resultSet.hasNext()) {
			QuerySolution solution = resultSet.nextSolution();
			String fullName = solution.get("type").toString();
			String className = fullName.substring(fullName.lastIndexOf('/') + 1);

			for(Class<?> currentClass : implementingClasses)
			{
				if(currentClass.getSimpleName().equals(className + "Impl"))
				{
					System.out.println("Found correct class: " + currentClass.getSimpleName());
				}
			}
		}
	}


	@Test
	public void reflectionTest() {
		new MessageParser().getDeclaredFields(InfrastructureComponent.class);
	}

}
