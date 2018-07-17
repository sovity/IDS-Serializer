package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.BrokerDataRequestImpl;
import de.fraunhofer.iais.eis.ids.jacksonmodule.jsonld.PlainLiteralDeserializer;
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
            "  \"@class\" : \"de.fraunhofer.iais.eis.DataTransferImpl\",\n"+
            "  \"id\" : \"http://industrialdataspace.org/dataTransfer/214ac663-d60f-4b9b-b1d0-35ccd41e661c\",\n"+
            "  \"customAttributes\" : [ \"java.util.ArrayList\", [ {\n"+
            "    \"@class\" : \"de.fraunhofer.iais.eis.TransferAttributeImpl\",\n"+
            "    \"id\" : \"http://industrialdataspace.org/transferAttribute/9847a645-8be7-4900-8e9b-7cd01074749a\",\n"+
            "    \"transferAttributeKey\" : \"key\",\n"+
            "    \"transferAttributeValue\" : \"value\"\n"+
            "  } ] ],\n"+
            "  \"sender\" : null,\n"+
            "  \"receiver\" : null,\n"+
            "  \"transferCreatedAt\" : null,\n"+
            "  \"sendingParticipant\" : null,\n"+
            "  \"receivingParticipant\" : null,\n"+
            "  \"sourceOperation\" : null,\n"+
            "  \"targetOperation\" : null,\n"+
            "  \"authToken\" : {\n"+
            "    \"@class\" : \"de.fraunhofer.iais.eis.AuthTokenImpl\",\n"+
            "    \"id\" : \"http://industrialdataspace.org/authToken/aca629a3-9b47-42d9-ac39-5201cf10a52e\",\n"+
            "    \"tokenValue\" : \"dummyToken\"\n"+
            "  },\n"+
            "  \"transferContract\" : null,\n"+
            "  \"payloadDigest\" : null,\n"+
            "  \"hashFunction\" : null\n"+
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
            "\t    \"entityNames\": \"ids:entityNames\"\n"+
            "\t},\n"+

            "\t\"@type\": \"DataAsset\",\n"+
            "\t\"@id\":\"http://industrialdataspace.org/dataAsset/f7608b8b-d60a-4476-a45a-bd4ca204d61b\",\n"+
            "\n" +

            "  \"@class\" : \"de.fraunhofer.iais.eis.DataAssetImpl\",\n" +
            "  \"id\" : \"http://industrialdataspace.org/dataAsset/f7608b8b-d60a-4476-a45a-bd4ca204d61b\",\n" +
            "  \"entityNames\" : [ \"java.util.ArrayList\", [ [ \"de.fraunhofer.iais.eis.util.PlainLiteral\", \"literal no langtag\" ], [ \"de.fraunhofer.iais.eis.util.PlainLiteral\", { \"@literal\": \"english literal\", \"@language\": \"en\" } ] ] ]\n" +
            "}\n";

    private static ObjectMapper mapper;

    public Serializer() {
        mapper = new ObjectMapper();
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(PlainLiteral.class, new PlainLiteralDeserializer());
        mapper.registerModule(module);
    }

    /**
     * Serializes an object to JSON(-LD) representation. In order to support JSON-LD, the input instance must be
     * annotated using IDS Infomodel annotations (todo CM: add link here)
     * @param instance the instance to be serialized
     * @return JSON string that is optionally interpretable as JSON-LD (and therefore a RDF serialization)
     * @throws JsonProcessingException
     */
    public String serialize(Object instance) throws JsonProcessingException {
        return mapper.writeValueAsString(instance);
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
