package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.JsonPreprocessor;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.TypeNamePreprocessor;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.UnsupportedRDFormatException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class SerializerTest { 

	private static ConnectorAvailableMessage basicInstance;
	private static Connector nestedInstance;
	private static RejectionMessage enumInstance;
	private static Connector securityProfileInstance;
	private static Serializer serializer;
	private static XMLGregorianCalendar now;

	@BeforeClass
	public static void setUp() throws ConstraintViolationException, DatatypeConfigurationException, URISyntaxException, MalformedURLException {
		serializer = new Serializer();

		// object with only basic types
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		basicInstance = new ConnectorAvailableMessageBuilder()
				._issued_(now)
				._modelVersion_("3.1.0")
				._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer").toURI())
				.build();

		ArrayList<Resource> resources = new ArrayList<>();
		resources.add(new ResourceBuilder()._version_("3.1.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard1").toURI()).build());
		resources.add(new ResourceBuilder()._version_("3.1.0")._contentStandard_(new URL("http://iais.fraunhofer.de/contentStandard2").toURI()).build());

		// connector -> object with nested types
		Catalog catalog = new CatalogBuilder()
				._offer_(resources)
				.build();

		nestedInstance = new BaseConnectorBuilder()
				._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer").toURI())
				._version_("3.1.0")
				._catalog_(catalog)
				.build();

		// object with enum
		enumInstance = new RejectionMessageBuilder()
				._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer").toURI())
				._modelVersion_("3.0.0")
				._rejectionReason_(RejectionReason.METHOD_NOT_SUPPORTED)
				.build();

		securityProfileInstance = new BaseConnectorBuilder()
				._maintainer_(new URL("http://iais.fraunhofer.de/connectorMaintainer").toURI())
				._version_("1.0.0")
				._catalog_(catalog)
				//                ._securityProfile_(SecurityProfile.BASE_CONNECTOR_SECURITY_PROFILE)
				.build();
		
	}

	@Test
	public void jsonldSerialize_Basic() throws IOException, NoSuchFieldException, IllegalAccessException {
		String connectorAvailableMessage = serializer.serialize(basicInstance);
		Assert.assertNotNull(connectorAvailableMessage);
		Model model = null;
		try {
			model = Rio.parse(new StringReader(connectorAvailableMessage), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);

		ConnectorAvailableMessage deserializedConnectorAvailableMessage = serializer.deserialize(connectorAvailableMessage, ConnectorAvailableMessageImpl.class);

		Assert.assertEquals(basicInstance.getId(), deserializedConnectorAvailableMessage.getId());
		Assert.assertNotNull(deserializedConnectorAvailableMessage);
		Assert.assertTrue(connectorAvailableMessage.equalsIgnoreCase(serializer.serialize(deserializedConnectorAvailableMessage)));

		Field properties = ConnectorAvailableMessageImpl.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(deserializedConnectorAvailableMessage, null); // Serialiser creates an empty HashMap, which kills the following equality check

		Assert.assertTrue(EqualsBuilder.reflectionEquals(basicInstance, deserializedConnectorAvailableMessage, true, Object.class, true));
	}

	@Test
	public void jsonldSerialize_Nested() throws IOException, NoSuchFieldException, IllegalAccessException {
		String connector = serializer.serialize(nestedInstance, RDFFormat.JSONLD);
		Assert.assertNotNull(connector);

		Model model = null;
		try {
			model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);

		Connector deserializedConnector = serializer.deserialize(connector, BaseConnectorImpl.class);
		Assert.assertNotNull(deserializedConnector);

		Field properties = BaseConnectorImpl.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(deserializedConnector, null); // Serialiser creates an empty HashMap, which kills the following equality check

		Assert.assertTrue(EqualsBuilder.reflectionEquals(nestedInstance, deserializedConnector, true, Object.class, true));
	}

	@Test
	public void jsonldSerialize_Enum() throws IOException, NoSuchFieldException, IllegalAccessException {
		String rejectionMessage = serializer.serialize(enumInstance, RDFFormat.JSONLD);
		Assert.assertNotNull(rejectionMessage);

		Model model = null;
		try {
			model = Rio.parse(new StringReader(rejectionMessage), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);

		RejectionMessage deserializedRejectionMessage = serializer.deserialize(rejectionMessage, RejectionMessage.class);
		Assert.assertNotNull(deserializedRejectionMessage);

		Field properties = RejectionMessageImpl.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(deserializedRejectionMessage, null); // Serialiser creates an empty HashMap, which kills the following equality check

		Assert.assertTrue(EqualsBuilder.reflectionEquals(enumInstance, deserializedRejectionMessage, true, Object.class, true));
	}

	@Test
	public void jsonldSerialize_SecurityProfile() throws IOException {
		String connector = serializer.serialize(securityProfileInstance, RDFFormat.JSONLD);
		Assert.assertNotNull(connector);

		Model model = null;
		try {
			model = Rio.parse(new StringReader(connector), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);
	}

	@Test
	public void jsonldSerialize_Literal() throws ConstraintViolationException, IOException {
		Resource resource = new ResourceBuilder()
				._description_(Util.asList(new TypedLiteral("literal no langtag"), new TypedLiteral("english literal", "en")))
				.build();

		String serialized = serializer.serialize(resource);
		Assert.assertNotNull(serialized);

		Model model = null;
		try {
			model = Rio.parse(new StringReader(serialized), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);

		// do not use reflective equals here as ArrayList comparison fails due to different modCount
		Resource deserializedResource = serializer.deserialize(serialized, ResourceImpl.class);
		Assert.assertEquals(2, deserializedResource.getDescription().size());
		Iterator<? extends TypedLiteral> names = deserializedResource.getDescription().iterator();

		Assert.assertNull(names.next().getLanguage());
		Assert.assertFalse(names.next().getLanguage().isEmpty());
	}

	@Test
	public void legacySerializationsJson_validate() {
		Connector connector = null;
		Connector connector_update = null;
		try {
			connector = serializer.deserialize(SerializerUtil.readResourceToString("Connector1.json"), Connector.class);
			connector_update = serializer.deserialize(SerializerUtil.readResourceToString("Connector1_update.json"), Connector.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(connector);
		Assert.assertNotNull(connector_update);
	}

	@Test
	public void legacySerializationsJsonld_validate() {
		Connector connector = null;
		Connector connector2 = null;
		try {
			serializer.addPreprocessor(new TypeNamePreprocessor());
			connector = serializer.deserialize(SerializerUtil.readResourceToString("Connector1.jsonld"), Connector.class);
			connector2 = serializer.deserialize(SerializerUtil.readResourceToString("Connector2.jsonld"), Connector.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(connector);
		Assert.assertNotNull(connector2);


		Model model = null;
		try {
			model = Rio.parse(new StringReader(SerializerUtil.readResourceToString("Connector1.jsonld")), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);

		model = null;
		try {
			model = Rio.parse(new StringReader(SerializerUtil.readResourceToString("Connector2.jsonld")), null, RDFFormat.JSONLD);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(model);
	}

	@Test
	@Ignore // TODO enable this test as soon as we can work with unknown namespaces
	public void serializeForeignProperties() throws Exception {
		serializer.addPreprocessor(new TypeNamePreprocessor());
		String serialized = "{\n" +
				"  \"@context\" : \"https://w3id.org/idsa/contexts/3.0.0/context.jsonld\",\n" +
				"  \"@type\" : \"ids:Broker\",\n" +
				"  \"inboundModelVersion\" : [ \"3.0.0\" ],\n" +
				"  \"@id\" : \"https://w3id.org/idsa/autogen/broker/5b9170a7-73fd-466e-89e4-83cedfe805aa\",\n" +
				"  \"http://xmlns.com/foaf/0.1/name\" : \"https://iais.fraunhofer.de/eis/ids/broker1/frontend\",\n" +
				"  \"http://xmlns.com/foaf/0.1/homepage\" : {\n  \"https://example.de/key\" : \"https://example.de/value\"\n}" +
				"}";
		Broker broker = serializer.deserialize(serialized, Broker.class);
		String originalSimplified = SerializerUtil.stripWhitespaces(serialized);
		String reserializedSimplified = SerializerUtil.stripWhitespaces(serializer.serialize(broker, RDFFormat.JSONLD));
		Assert.assertEquals(originalSimplified, reserializedSimplified);
	}

	@Test
	public void deserializeSingleValueAsArray() {
		ContractOffer contractOffer = null;
		try {
			contractOffer = serializer.deserialize(SerializerUtil.readResourceToString("ContractOfferValueForArray.jsonld"), ContractOffer.class);
		} catch(IOException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(contractOffer);
	}

	@Test
	public void deserializeThroughInheritanceChain() throws IOException, NoSuchFieldException, IllegalAccessException {

		DescriptionRequestMessage sdr = new DescriptionRequestMessageBuilder()
				._contentVersion_("test")
				.build();
		String serialized = serializer.serialize(sdr);
		Message m = serializer.deserialize(serialized, Message.class);
		Field properties = DescriptionRequestMessageImpl.class.getDeclaredField("properties");
		properties.setAccessible(true);
		properties.set(m, null); // Serialiser creates an empty HashMap, which kills the following equality check

		Assert.assertTrue(EqualsBuilder.reflectionEquals(sdr, m, true, Object.class, true));
	}

	@Test
	public void deserializeWithAndWithoutTypePrefix() {
		String withIdsPrefix = "{\n" +
				"  \"@context\" : \"https://w3id.org/idsa/contexts/3.0.0/context.jsonld\",\n" +
				"  \"@type\" : \"ids:TextResource\",\n" +
				"  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
				"}";
		String withAbsoluteURI = "{\n" +
				"  \"@type\" : \"https://w3id.org/idsa/core/TextResource\",\n" +
				"  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
				"}";

		String withoutExplicitPrefix = "{\n" +
				"  \"@context\" : \"https://w3id.org/idsa/contexts/3.0.0/context.jsonld\",\n" +
				"  \"@type\" : \"TextResource\",\n" +
				"  \"@id\" : \"https://creativecommons.org/licenses/by-nc/4.0/legalcode\"\n" +
				"}";

		try {

			Object defaultDeserialization = serializer.deserialize(withIdsPrefix, TextResource.class);

			JsonPreprocessor preprocessor = new TypeNamePreprocessor();
			serializer.addPreprocessor(preprocessor, true);

			Object deserializedWithIdsPrefix = serializer.deserialize(withIdsPrefix, TextResource.class);
			Object deserializedWithAbsoluteURI = serializer.deserialize(withAbsoluteURI, TextResource.class);
			Object deserializedWithoutExplicitPrefix = serializer.deserialize(withoutExplicitPrefix, TextResource.class);

			Field properties = TextResourceImpl.class.getDeclaredField("properties");
			properties.setAccessible(true);
			properties.set(defaultDeserialization, null); // Serialiser creates an empty HashMap, which kills the following equality check
			properties.set(deserializedWithIdsPrefix, null); // Serialiser creates an empty HashMap, which kills the following equality check
			properties.set(deserializedWithAbsoluteURI, null); // Serialiser creates an empty HashMap, which kills the following equality check
			properties.set(deserializedWithoutExplicitPrefix, null); // Serialiser creates an empty HashMap, which kills the following equality check


			serializer.removePreprocessor(preprocessor);

			Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithIdsPrefix, true, Object.class, true));
			Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithAbsoluteURI, true, Object.class, true));
			Assert.assertTrue(EqualsBuilder.reflectionEquals(defaultDeserialization, deserializedWithoutExplicitPrefix, true, Object.class, true));
		} catch (IOException | NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}


	@Ignore // This test intends to check a single date, not embedded in any enclosing JSON-LD. This does not make sense with the latest serializer version.
	@Test
	public void stableCalendarFormat() throws IOException {
		String serialized = "2019-07-24T17:29:18.908+02:00";

		XMLGregorianCalendar xgc = serializer.deserialize(serialized, XMLGregorianCalendar.class);
		String reserialized = serializer.serialize(xgc);
		Assert.assertEquals(serialized, reserialized);
	}

	/**
	 * lists have to be serialized with a @context element in each child
	 * otherwise, RDF4j does not correctly parse the data resulting in empty model and empty Turtle serialization
	 * @throws IOException on serialization failure
	 */
	@Test
	public void listWithContext() throws IOException {
		ContractOffer contractOffer1 = new ContractOfferBuilder()._refersTo_(PolicyTemplate.ACCESSAGREEMENTTOSECURECONSUMERTEMPLATE).build();
		ContractOffer contractOffer2 = new ContractOfferBuilder()._refersTo_(PolicyTemplate.ACCESSAGREEMENTTOSECURECONSUMERTEMPLATE).build();
		String serializedList = serializer.serialize(Util.asList(contractOffer1, contractOffer2));

		Model model = Rio.parse(new StringReader(serializedList), null, RDFFormat.JSONLD);
		Assert.assertEquals(4, model.size());

		String ttl = serializer.convertJsonLdToOtherRdfFormat(serializedList, RDFFormat.TURTLE);
		Assert.assertFalse(ttl.isEmpty());
	}


	@Test
	public void testJwtAttributesInContext() throws IOException {
		DatPayload datPayload = new DatPayloadBuilder()
				._exp_(new BigInteger(String.valueOf(12)))
				._aud_(Audience.IDS_CONNECTOR_ATTRIBUTES_ALL)
				.build();

		String serialized = serializer.serialize(datPayload);


		Rio.parse(new StringReader(serialized), "http://example.org/rdf#", RDFFormat.JSONLD); // ensure that valid JSON-LD is serialized
		Assert.assertTrue(serialized.contains("\"exp\" : \"ids:exp\"")); // ensure DatPayload fields are added to the context
	}



	@Test
	public void rightOperandTest() throws IOException, URISyntaxException {

		Constraint constraint = new ConstraintBuilder()
				._leftOperand_(LeftOperand.PAY_AMOUNT)
				._operator_(BinaryOperator.EQ)
				._rightOperand_(new TypedLiteral("5", new URI("http://www.w3.org/2001/XMLSchema#string")))
				.build();
		serializer.serialize(constraint);


		String constraintString = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Constraint\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/constraint/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:leftOperand\" : {\r\n" + 
				"    \"@id\" : \"idsc:PAY_AMOUNT\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:operator\" : {\r\n" + 
				"    \"@id\" : \"idsc:EQ\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:rightOperand\" : {\r\n" + 
				"    \"@value\" : \"5\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" +
				//"    \"@language\" : \"en\"\r\n" +
				"  }\r\n" + 
				"}";
		serializer.deserialize(constraintString, Constraint.class);

	}

	@Test
	public void serializingListOfUrisTest() throws IOException, DatatypeConfigurationException {

		DynamicAttributeToken token = new DynamicAttributeTokenBuilder()
				._tokenFormat_(TokenFormat.JWT)
				._tokenValue_("sampleToken")
				.build();

		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar now = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

		ResponseMessage message = new ResponseMessageBuilder()
				._securityToken_(token)
				._correlationMessage_(URI.create("example.com"))
				._issued_(now)
				._issuerConnector_(URI.create("example.com"))
				._modelVersion_("3.1.0")
				._senderAgent_(URI.create("example.com"))
				._recipientConnector_(Util.asList(URI.create("example.com"), URI.create("anotherExample.com")))
				//._recipientAgent_(Util.asList(URI.create("example.com")))
				.build();

		String s = serializer.serialize(message);
		ResponseMessage msg = serializer.deserialize(s, ResponseMessage.class);
	}


	@Test
	public void plainLiteralParseTest() throws IOException {

		Serializer localSerializer = new Serializer();

		// Prepare the test data
		String jsonLd1 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Resource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/resource/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:description\" : \"a description 1\",\r\n" + // plain 
				"  \"ids:keyword\" : \"keyword1\"\r\n" + // plain 
				"}";
		String jsonLd2 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Resource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/resource/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:description\" : {\r\n" + 
				"    \"@value\" : \"a description 2\"\r\n" + // with no tag
				"  },\r\n" + 
				"  \"ids:keyword\" : {\r\n" + 
				"    \"@value\" : \"keyword2\"\r\n"  + // with no tag
				"  }\r\n" + 
				"}";
		String jsonLd3 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Resource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/resource/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:description\" : {\r\n" + 
				"    \"@value\" : \"a description 3\",\r\n" + 
				"    \"@language\" : \"en\"\r\n" + // with language tag
				"  },\r\n" + 
				"  \"ids:keyword\" : {\r\n" + 
				"    \"@value\" : \"keyword3\",\r\n" + 
				"    \"@language\" : \"en\"\r\n" + // with language tag
				"  }\r\n" + 
				"}";
		String jsonLd4 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Resource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/resource/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:description\" : {\r\n" + 
				"    \"@value\" : \"a description 4\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + // with string type
				"  },\r\n" + 
				"  \"ids:keyword\" : {\r\n" + 
				"    \"@value\" : \"keyword4\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#string\"\r\n" + // with string type
				"  }\r\n" + 
				"}";
		String jsonLd5 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\",\r\n" + 
				"    \"idsc\" : \"https://w3id.org/idsa/code/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:Resource\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/resource/69755e0f-bf2f-4f62-b14d-6837a1cf1f6a\",\r\n" + 
				"  \"ids:description\" : {\r\n" + 
				"    \"@value\" : \"a description 5\",\r\n" + 
				"    \"@type\" : \"xsd:string\"\r\n" + // with string type  and xsd: prefix
				"  },\r\n" + 
				"  \"ids:keyword\" : {\r\n" + 
				"    \"@value\" : \"keyword5\",\r\n" + 
				"    \"@type\" : \"xsd:string\"\r\n" + // with string type and xsd: prefix
				"  }\r\n" + 
				"}";


		String[] jsonLds = new String[] {jsonLd1, jsonLd2, jsonLd3, jsonLd4, jsonLd5};


		for (int i = 0; i < jsonLds.length; i++) {
			String jsonLD = jsonLds[i];

			Resource fromJSONLD = localSerializer.deserialize(jsonLD, Resource.class);

			Resource resource = new ResourceBuilder()
					._description_(Util.asList(new TypedLiteral("a description " + (i+1), "en")))
					._keyword_(Util.asList(new TypedLiteral("keyword" + (i+1))))
					.build();
			serializer.serialize(resource);

			TypedLiteral descriptionJSONLD = fromJSONLD.getDescription().get(0);
			TypedLiteral description = resource.getDescription().get(0);

			assertTrue(descriptionJSONLD.getValue().equalsIgnoreCase(description.getValue()));
			if (i==2) assertTrue(descriptionJSONLD.getLanguage().equalsIgnoreCase(description.getLanguage()));
		}
	}

	@Test
	public void getLabelAndCommentsTest() throws IOException {
		SecurityProfile profile = SecurityProfile.BASE_CONNECTOR_SECURITY_PROFILE;
		Assert.assertFalse(profile.getComment().isEmpty());
		Assert.assertFalse(profile.getLabel().isEmpty());
		String rdfProfile = serializer.serialize(profile);
		if(rdfProfile.contains("label\"") || rdfProfile.contains("comment\""))
		{
			Assert.fail();
		}
	}

	

	@Test
	public void typedLiteralSerialiseTest() throws IOException, de.fraunhofer.iais.eis.util.ConstraintViolationException, URISyntaxException {
		
		Resource resource1 = new ResourceBuilder()
				._description_(Util.asList(new PlainLiteral("a description 1")))
				._keyword_(Util.asList(new PlainLiteral("keyword1")))
				.build();
		Resource resource2 = new ResourceBuilder()
				._description_(Util.asList(new PlainLiteral("\"a description 2\"^^http://www.w3.org/2001/XMLSchema#string")))
				._keyword_(Util.asList(new PlainLiteral("\"keyword2\"^^http://www.w3.org/2001/XMLSchema#string")))
				.build();
		Resource resource3 = new ResourceBuilder()
				._description_(Util.asList(new PlainLiteral("a description 3@en")))
				._keyword_(Util.asList(new PlainLiteral("keyword3@en")))
				.build();
		Resource resource4 = new ResourceBuilder()
				._description_(Util.asList(new PlainLiteral("a description 4", "en")))
				._keyword_(Util.asList(new PlainLiteral("keyword4", "en")))
				.build();
		Resource resource5 = new ResourceBuilder()
				._description_(Util.asList(new TypedLiteral("a description 5", "en")))
				._keyword_(Util.asList(new TypedLiteral("keyword5", "en")))
				.build();
		Resource resource6 = new ResourceBuilder()
				._description_(Util.asList(new TypedLiteral("a description 6", new URI("http://www.w3.org/2001/XMLSchema#string"))))
				._keyword_(Util.asList(new TypedLiteral("keyword6", new URI("http://www.w3.org/2001/XMLSchema#string"))))
				.build();
		Resource resource7 = new ResourceBuilder()
				._description_(Util.asList(new TypedLiteral("\"a description 7\"^^http://www.w3.org/2001/XMLSchema#string")))
				._keyword_(Util.asList(new TypedLiteral("\"keyword7\"^^http://www.w3.org/2001/XMLSchema#string")))
				.build();
		Resource resource8 = new ResourceBuilder()
				._description_(Util.asList(new TypedLiteral("\"a description 8\"^^xsd:string")))
				._keyword_(Util.asList(new TypedLiteral("\"keyword8\"^^xsd:string")))
				.build();
		
		Resource[] resources = new Resource[] { resource1, resource2, resource3, resource4, resource5, resource6, resource7, resource8};
		Serializer localSerializer = new Serializer();
		
		for (Resource resource : resources ) {
			String resourceAsJsonLD = localSerializer.serialize(resource);
			Resource parsedResource = localSerializer.deserialize(resourceAsJsonLD, Resource.class);
			assertEquals(resource.getDescription().get(0).getValue(), parsedResource.getDescription().get(0).getValue());
			assertEquals(resource.getDescription().get(0).getLanguage(), parsedResource.getDescription().get(0).getLanguage());
		}
	}


	@Test
	public void testRio() throws RDFParseException, UnsupportedRDFormatException, IOException {
		String jsonld = SerializerUtil.readResourceToString("ContractOfferValueForArray.jsonld");

		Serializer localSerializer = new Serializer();
		localSerializer.deserialize(jsonld, Contract.class);

		RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
		Model model = new LinkedHashModel();
		rdfParser.setRDFHandler(new StatementCollector(model));

		rdfParser.parse(new StringReader(jsonld), "http://example.org/rdf#");

	}
	
	/**
	 * This test is based on a ticket and bugfix received on 15.05.2020
	 * see Erik van den Akker's email (Infomodel Serializer: NullpointerException)
	 * @author sbader
	 * @throws IOException if serialization fails
	 * @throws ConstraintViolationException in case of mandatory fields missing (should not happen here, as all fields are hard coded)
	 */
	@Test 
	public void testArraysWithUris() throws IOException, de.fraunhofer.iais.eis.util.ConstraintViolationException {
		Serializer serializer = new Serializer();
		
	    DynamicAttributeToken token = new DynamicAttributeTokenBuilder()
	            ._tokenFormat_(TokenFormat.JWT)
	            ._tokenValue_("sampleToken")
	            .build();

	    ResponseMessage message = new ResponseMessageBuilder()
	            ._securityToken_(token)
	            ._correlationMessage_(URI.create("http://example.com"))
	            ._issued_(now)
	            ._issuerConnector_(URI.create("http://example.com"))
	            ._modelVersion_("3.1.0")
	            ._senderAgent_(URI.create("http://example.com"))
	            ._recipientAgent_(new ArrayList<>(Util.asList(URI.create("http://example.com"))))
	            ._recipientConnector_(new ArrayList<>(Util.asList(URI.create("http://example.com"))))
	            .build();

	    serializer.serialize(message);
	}


	/**
	 * This test checks whether different date formulations are treated accordingly
	 *
	 */
	@Test
	public void testDateTimeStamp() throws RDFParseException, UnsupportedRDFormatException, IOException, ConstraintViolationException, URISyntaxException {
		String jsonld1 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:ConnectorAvailableMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/connectorAvailableMessage/777e9303-a8f1-4f00-b1d0-2910c01b2d53\",\r\n" + 
				"  \"ids:issuerConnector\" : {\r\n" + 
				"    \"@id\" : \"http://iais.fraunhofer.de/connectorIssuer\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modelVersion\" : \"2.0.0\",\r\n" + 
				"  \"ids:issued\" : \"2020-03-31T01:01:01.001Z\"\r\n" + 
				"}";
		String jsonld2 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:ConnectorAvailableMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/connectorAvailableMessage/777e9303-a8f1-4f00-b1d0-2910c01b2d53\",\r\n" + 
				"  \"ids:issuerConnector\" : {\r\n" + 
				"    \"@id\" : \"http://iais.fraunhofer.de/connectorIssuer\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modelVersion\" : \"2.0.0\",\r\n" + 
				"  \"ids:issued\" : \"2020-03-31T02:02:02.002+02:00\"" +
				"}";
		/*	String jsonld3 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:ConnectorAvailableMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/connectorAvailableMessage/777e9303-a8f1-4f00-b1d0-2910c01b2d53\",\r\n" + 
				"  \"ids:issuerConnector\" : {\r\n" + 
				"    \"@id\" : \"http://iais.fraunhofer.de/connectorIssuer\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modelVersion\" : \"2.0.0\",\r\n" + 
				"  \"ids:issued\" : {\r\n" + 
				"    \"@value\" : \"2020-03-31T03:03:03.003+03:00\"\r\n" + 
				"  }" +
				"}"; */ // TODO ... "ids:issued" : {"@value" : "2020-03-31T03:03:03.003+03:00"} does not work
		String jsonld4 = "{\r\n" + 
				"  \"@context\" : {\r\n" + 
				"    \"ids\" : \"https://w3id.org/idsa/core/\"\r\n" + 
				"  },\r\n" + 
				"  \"@type\" : \"ids:ConnectorAvailableMessage\",\r\n" + 
				"  \"@id\" : \"https://w3id.org/idsa/autogen/connectorAvailableMessage/777e9303-a8f1-4f00-b1d0-2910c01b2d53\",\r\n" + 
				"  \"ids:issuerConnector\" : {\r\n" + 
				"    \"@id\" : \"http://iais.fraunhofer.de/connectorIssuer\"\r\n" + 
				"  },\r\n" + 
				"  \"ids:modelVersion\" : \"2.0.0\",\r\n" + 
				"  \"ids:issued\" : {\r\n" + 
				"    \"@value\" : \"2020-03-31T04:04:04.004+04:00\",\r\n" + 
				"    \"@type\" : \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\"\r\n" + 
				"  }" +
				"}";

		ConnectorAvailableMessage basicInstance = new ConnectorAvailableMessageBuilder()
				._issued_(now)
				._modelVersion_("2.0.0")
				._issuerConnector_(new URL("http://iais.fraunhofer.de/connectorIssuer").toURI())
				.build();
		String jsonld5 = serializer.serialize(basicInstance);

		String[] jsonlds = new String[]{ jsonld1, jsonld2, /*jsonld3,*/ jsonld4, jsonld5 };


		for (String jsonld : jsonlds) {
			// validate JSON-LD
			RDFParser rdfParser = Rio.createParser(RDFFormat.JSONLD);
			Model model = new LinkedHashModel();
			rdfParser.setRDFHandler(new StatementCollector(model));
			rdfParser.parse(new StringReader(jsonld), "http://example.org/rdf#");
			Rio.parse(new StringReader(jsonld), "http://example.org/rdf#", RDFFormat.JSONLD);


			// parse JSON-LD
			ConnectorAvailableMessage msg = serializer.deserialize(jsonld, ConnectorAvailableMessage.class);
			serializer.serialize(msg);
		}
	}




}
