package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class Serializer {

    // simple object
    private String brokerDataRequest = "{\n"+
            // context part is independent from the instance and only changes with the (information) model
            "\t\"@context\": {\n"+
            "\t    \"ids\" : \"https://w3id.org/ids/core/\",\n"+
            "\t    \"BrokerDataRequest\": \"ids:BrokerDataRequest\",\n"+
            "\t    \"messageContent\": \"ids:messageContent\",\n"+
            "\t    \"dataRequestAction\": { \"@id\": \"ids:dataRequestAction\", \"@type\": \"@id\" },\n"+
            "\t    \"coveredEntity\": { \"@id\": \"ids:coveredEntity\", \"@type\": \"@id\" }\n"+
            "\t},\n"+

            // the values of these annotations depend on the instance's type and id
            "\t\"@type\": \"BrokerDataRequest\",\n"+ // this is collected from the implemented interfaces of the instance's type
            "\t\"@id\":\"http://industrialdataspace.org/brokerDataRequest/8e5b8e67-e7a0-45a1-8910-9b75e00882ec\",\n"+
            "\n" +

            // this is the standard output of jackson for the instance
            "\"@class\" : \"de.fraunhofer.iais.eis.BrokerDataRequestImpl\",\n"+
            "\"id\" : \"http://industrialdataspace.org/brokerDataRequest/983c018c-9914-4008-8e1e-12b3a2d3feb4\",\n"+
            "\"dataRequestAction\" : \"https://w3id.org/ids/core/BrokerDataRegisterAction\",\n"+
            "\"messageContent\": \"Hello world\",\n"+
            "\"coveredEntity\": \"https://w3id.org/ids/core/CoveredConnector\"\n"+
            "}";

    // object with nested types
    private String dataTransfer = "{\n"+
            "\"@class\" : \"de.fraunhofer.iais.eis.DataTransferImpl\",\n"+
            "\"id\" : \"http://industrialdataspace.org/dataTransfer/7719b39c-38ea-4f0b-99b1-f214c1abac5d\",\n" +
            "  \"customAttributes\" : [ [ \"de.fraunhofer.iais.eis.TransferAttributeImpl\", {\n" +
            "    \"id\" : \"http://industrialdataspace.org/transferAttribute/005c38fa-c15e-484d-aae6-a4cb9086d103\",\n" +
            "    \"transferAttributeKey\" : \"key\",\n" +
            "    \"transferAttributeValue\" : \"value\"\n" +
            "  } ] ],\n" +
            "  \"authToken\" : [ \"de.fraunhofer.iais.eis.AuthTokenImpl\", {\n" +
            "    \"id\" : \"http://industrialdataspace.org/authToken/2ee4d630-ea5f-4425-9814-871bc0b0c159\",\n" +
            "    \"tokenValue\" : \"dummyToken\"\n" +
            "  } ]"+
            "}";

    // object with int literal
    private String instant = "{\n" +
            "\t\"@context\": {\n"+
            "\t    \"ids\" : \"https://w3id.org/ids/core/\",\n"+
            "\t    \"Instant\": \"ids:Instant\",\n"+
            "\t    \"namedValue\": \"ids:namedValue\"\n"+
            "\t},\n"+

            "\t\"@type\": \"Instant\",\n"+
            "\t\"@id\":\"http://industrialdataspace.org/instant/8d43422f-30a2-401e-bcf3-bc2bae97b73c\",\n"+
            "\n" +

            "  \"@class\" : \"de.fraunhofer.iais.eis.InstantImpl\",\n" +
            "  \"id\" : \"http://industrialdataspace.org/instant/8d43422f-30a2-401e-bcf3-bc2bae97b73c\",\n" +
            "  \"inXSDDateTime\" : null,\n" +
            "  \"named\" : null,\n" +
            "  \"namedValue\" : 42\n" +
            "}";

    private String dataasset = "{\n" +
            "\t\"@context\": {\n"+
            "\t    \"ids\" : \"https://w3id.org/ids/core/\",\n"+
            "\t    \"entityNames\": \"ids:entityName\"\n"+
            "\t},\n"+

            "\t\"@type\": \"ids:DataAsset\",\n"+
            "\t\"@id\":\"http://industrialdataspace.org/dataAsset/80b79546-7b50-480e-b397-c7fd6e865b3f\",\n"+
            "\n" +

            "  \"@class\" : \"de.fraunhofer.iais.eis.DataAssetImpl\",\n" +
            "  \"id\" : \"http://industrialdataspace.org/dataAsset/80b79546-7b50-480e-b397-c7fd6e865b3f\",\n" +
            "  \"entityNames\" : [ {\n" +
            "    \"@class\" : \"de.fraunhofer.iais.eis.util.PlainLiteral\",\n" +
            "    \"@value\" : \"literal no langtag\"\n" +
            "  }, {\n" +
            "    \"@class\" : \"de.fraunhofer.iais.eis.util.PlainLiteral\",\n" +
            "    \"@value\" : \"english literal\",\n" +
            "    \"@language\" : \"en\"\n" +
            "  } ]\n" +
            "}";


    private static ObjectMapper mapper;

    public Serializer() {
        mapper = new ObjectMapper();
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.setDateFormat(new ISO8601DateFormat());
      //  mapper.enableDefaultTyping();
       // mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        SimpleModule mod = new SimpleModule();
        mod.setSerializerModifier(new JsonLDSerializerModifier());
        mapper.registerModule(mod);
    }

    /**
     * Serializes an object to JSON-LD representation. In order to support JSON-LD, the input instance must be
     * annotated using IDS Infomodel annotations
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
     * @param serialization JSON(-LD) string
     * @param valueType class of top level type
     * @param <T> deserialized type
     * @return an object representing the provided JSON(-LD) structure
     * @throws IOException
     */
    public <T> T deserialize(String serialization, Class<T> valueType) throws IOException {
        return mapper.readValue(serialization, valueType);
    }

    // for testing only
    public String serialize(ObjectType objectType) {
        switch (objectType) {
            case BASIC:
                return brokerDataRequest;
            case NESTED:
                return dataTransfer;
            case INT_LIT:
                return instant;
            case LANG_LIT:
                return dataasset;
        }
        return "";
    }

}
