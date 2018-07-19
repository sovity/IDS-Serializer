package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.BrokerDataRequestImpl;
import de.fraunhofer.iais.eis.util.PlainLiteral;

import java.io.IOException;

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
            "\t    \"DataAsset\": \"ids:DataAsset\",\n"+
            "\t    \"entityNames\": \"ids:entityName\"\n"+
            "\t},\n"+

            "\t\"@type\": \"DataAsset\",\n"+
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
        mapper.enableDefaultTyping();
    }

    /**
     * Serializes an object to JSON(-LD) representation. In order to support JSON-LD, the input instance must be
     * annotated using IDS Infomodel annotations (todo CM: add link here)
     * @param instance the instance to be serialized
     * @return JSON string that is optionally interpretable as JSON-LD (and therefore a RDF serialization)
     * @throws JsonProcessingException
     */
    public String serialize(Object instance) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(instance);
    }

    /**
     * Inverse method of "serialize"
     * @param serialization JSON(-LD) string
     * @param valueType class of top level type (todo CM: investigate if needed)
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
