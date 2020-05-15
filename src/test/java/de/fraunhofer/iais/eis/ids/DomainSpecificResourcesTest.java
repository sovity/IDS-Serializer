package de.fraunhofer.iais.eis.ids;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import de.fraunhofer.iais.eis.DataResource;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

public class DomainSpecificResourcesTest {

	@Test
	public void test() throws IOException {
		
		byte[] encoded = Files.readAllBytes(Paths.get("src/test/resources/Life-Expectancy.jsonld"));
		
		String resource =  new String(encoded);
		
		Serializer serializer = new Serializer();
		Resource res = serializer.deserialize(resource, DataResource.class);
		
		System.out.println(serializer.serialize(res));
	}

}
