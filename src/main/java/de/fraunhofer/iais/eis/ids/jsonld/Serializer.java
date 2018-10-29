package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
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
        mapper.registerModule(new JsonLDModule(Usage.STANDALONE));
    }

    /**
     * Serializes an object to JSON-LD representation. In order to support JSON-LD, the input instance must be
     * annotated using IDS Infomodel annotations
     *
     * @param instance the instance to be serialized
     * @return RDF serialization of the provided object graph
     * @throws JsonProcessingException
     */
    public String serialize(Object instance) throws IOException {
        return serialize(instance, RDFFormat.JSONLD);
    }

    public String serialize(Object instance, RDFFormat format) throws IOException {
        /*
        TODO: add the @context block to the JSON, containing:
        1) the IDS namespace prefix, which is always constant: "ids" : "https://w3id.org/ids/core/"

        TODO: configure jackson so that for each JSON object it adds 2 key value pairs:
        1) "@type" : the value for this key is taken from the Object's @RdfType annotation
        2) "@id" : the value for this key is taken from the Object's member value that is annotated by the @RdfId annotation

        TODO: add property mappings to the @context block to the JSON, containing:
        1) for each member the object instance has, collect the value of the @RdfProperty annotation and add it
        in the form "<property>" : "<value provided in the parenthesis of the @RdfProperty(...) annotation of each member method>"
         */
        String jsonLD = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
        jsonLD = "{" + JsonLDContext.getInstance().toString() + jsonLD.substring(1); // insert @context at the beginning (after "{")
        JsonLDContext.getInstance().clear();

        if (format == RDFFormat.JSONLD) return jsonLD;
        else return convertJsonLdToOtherRdfFormat(jsonLD, format);
    }

    private String convertJsonLdToOtherRdfFormat(String jsonLd, RDFFormat format) throws IOException {
        Model model = Rio.parse(new StringReader(jsonLd), null, RDFFormat.JSONLD);

        StringWriter rdfOutput = new StringWriter();
        RDFWriter writer = Rio.createWriter(format, rdfOutput);
        writer.startRDF();
        model.stream().forEach(statement -> writer.handleStatement(statement));
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
     * @throws IOException
     */
    public <T> T deserialize(String serialization, Class<T> valueType) throws IOException {
        return mapper.readValue(serialization, valueType);
    }

}
