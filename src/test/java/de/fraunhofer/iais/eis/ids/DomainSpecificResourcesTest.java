package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import de.fraunhofer.iais.eis.DataResource;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.junit.runner.notification.RunListener;

import javax.validation.constraints.AssertTrue;

public class DomainSpecificResourcesTest {

	@Test
	/**
	 * This test loads a JSON-LD with unknown (neither ids, idsc, fhg digital etc.) namespaces and checks whether some
	 * of them survived the parsing/serialization
	 */
	public void test() throws IOException {
		
		byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/Life-Expectancy.jsonld"));
		
		String resource =  new String(encoded);
		
		Serializer serializer = new Serializer();
		Resource res = serializer.deserialize(resource, DataResource.class);

		String serialized_resource = serializer.serialize(res);
		System.out.println(serialized_resource);
		assertTrue(serialized_resource.contains("\"http://rdfs.org/ns/void#distinctObjects\" : 3"));
		assertTrue(serialized_resource.contains("\"http://rdfs.org/ns/void#triples\" : 1"));
		assertTrue(serialized_resource.contains("\"http://rdfs.org/ns/void#property\" :"));
		assertTrue(serialized_resource.contains("\"@id\" : \"http://dbpedia.org/resource/Year\""));

	}

	/**
	 * This test parses, serializes, parses, and serializes a data resource in order to see if things change.
	 */
	@Test
	public void testSerializationChain() throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/Life-Expectancy.jsonld"));
		String source =  new String(encoded);

		Serializer serializer = new Serializer();

		Resource res1 = serializer.deserialize(source, DataResource.class);
		String serialized1 = serializer.serialize(res1);

		Resource res2 = serializer.deserialize(serialized1, DataResource.class);
		String serialized2 = serializer.serialize(res2);

		assertTrue(serialized1.equalsIgnoreCase(serialized2));
	}
}
