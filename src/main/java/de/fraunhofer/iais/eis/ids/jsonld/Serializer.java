package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.ids.jsonld.preprocessing.JsonPreprocessor;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Serializer {

    private static ObjectMapper mapper;
    private final List<JsonPreprocessor> preprocessors;

    public Serializer() {
        mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        preprocessors = new ArrayList<>();
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
        if (format != RDFFormat.JSONLD && format != RDFFormat.TURTLE && format != RDFFormat.RDFXML) {
            throw new IOException("RDFFormat " + format + " is currently not supported by the serializer.");
        }
        mapper.registerModule(new JsonLDModule());
        String lineSep = System.lineSeparator();
        StringBuilder jsonLDBuilder = new StringBuilder();
        if (instance instanceof Collection) {
            jsonLDBuilder.append("[");
            jsonLDBuilder.append(lineSep);
            for (Object item : (Collection) instance) {
                jsonLDBuilder.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(item));
                jsonLDBuilder.append(",");
                jsonLDBuilder.append(lineSep);
            }
            int lastComma = jsonLDBuilder.lastIndexOf(",");
            jsonLDBuilder.replace(lastComma, lastComma + 1, "");
            jsonLDBuilder.append("]");
            jsonLDBuilder.append(lineSep);
        } else {
            jsonLDBuilder.append(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance));
        }
        String jsonLD = jsonLDBuilder.toString();
        if (format == RDFFormat.JSONLD) return jsonLD;
        else return convertJsonLdToOtherRdfFormat(jsonLD, format);
    }

    public String convertJsonLdToOtherRdfFormat(String jsonLd, RDFFormat format) throws IOException {
        Model model = Rio.parse(new StringReader(jsonLd), null, RDFFormat.JSONLD);

        StringWriter rdfOutput = new StringWriter();
        RDFWriter writer = Rio.createWriter(format, rdfOutput);
        writer.startRDF();
        model.forEach(writer::handleStatement);
        writer.endRDF();
        return rdfOutput.toString();
    }

    public String serializePlainJson(Object instance) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
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
        mapper.registerModule(new JsonLDModule());
        for (JsonPreprocessor preprocessor : preprocessors) {
            serialization = preprocessor.preprocess(serialization);
        }
        return mapper.readValue(serialization, valueType);
    }

    /**
     * Method to add a preprocessor for deserialization.
     * <p>
     * Important note: The preprocessors are executed in the same order they were added.
     *
     * @param preprocessor the preprocessor to add
     */
    public void addPreprocessor(JsonPreprocessor preprocessor) {
        preprocessors.add(preprocessor);
    }

    /**
     * Method to add a preprocessor for deserialization.
     * <p>
     * Important note: The preprocessors are executed in the same order they were added.
     *
     * @param preprocessor the preprocessor to add
     * @param validate     set wether the preprocessors output should be checked by RDF4j
     */
    public void addPreprocessor(JsonPreprocessor preprocessor, boolean validate) {
        preprocessor.enableRDFValidation(validate);
        addPreprocessor(preprocessor);
    }

    /**
     * remove a preprocessor if no longer needed
     *
     * @param preprocessor the preprocessor to remove
     */
    public void removePreprocessor(JsonPreprocessor preprocessor) {
        preprocessors.remove(preprocessor);
    }
}
