package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class Serializer {

    private static ObjectMapper mapper;

    public Serializer() {
        mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.registerModule(new JsonLDModule());
    }

    /**
     * Serializes an object to JSON-LD representation. In order to support JSON-LD, the input instance must be
     * annotated using IDS Infomodel annotations
     *
     * @param instance the instance to be serialized
     * @return RDF serialization of the provided object graph
     */
    public String serialize(Object instance) throws IOException {
        return serialize(instance, RDFFormat.JSONLD);
    }

    public String serialize(Object instance, RDFFormat format) throws IOException {
        String jsonLD = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
        if (format == RDFFormat.JSONLD) return jsonLD;
        else return convertJsonLdToOtherRdfFormat(jsonLD, format);
    }

    private String convertJsonLdToOtherRdfFormat(String jsonLd, RDFFormat format) throws IOException {
        Model model = Rio.parse(new StringReader(jsonLd), null, RDFFormat.JSONLD);

        StringWriter rdfOutput = new StringWriter();
        RDFWriter writer = Rio.createWriter(format, rdfOutput);
        writer.startRDF();
        model.forEach(writer::handleStatement);
        writer.endRDF();
        return rdfOutput.toString();
    }

    /**
     * Inverse method of "serialize"
     *
     * @param serialization JSON(-LD) string
     * @param valueType     class of top level type
     * @param <T>           deserialized type
     * @return an object representing the provided JSON(-LD) structure
     */
    public <T> T deserialize(String serialization, Class<T> valueType) throws IOException {
        return mapper.readValue(serialization, valueType);
    }

}
