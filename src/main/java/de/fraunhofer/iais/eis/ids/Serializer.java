package de.fraunhofer.iais.eis.ids;

public class Serializer {

    // simple object
    private String brokerDataRequest = "{\n"+
            "\t\"@context\": {\n"+
            "\t    \"ids\" : \"https://w3id.org/ids/core/\",\n"+
            "\t    \"BrokerDataRequest\": \"ids:BrokerDataRequest\",\n"+
            "\t    \"messageContent\": \"ids:messageContent\",\n"+
            "\t    \"dataRequestAction\": { \"@id\": \"ids:dataRequestAction\", \"@type\": \"@id\" },\n"+
            "\t    \"coveredEntity\": { \"@id\": \"ids:coveredEntity\", \"@type\": \"@id\" }\n"+
            "\t},\n"+
            "\t\"@type\": \"BrokerDataRequest\",\n"+
            "\t\"@id\":\"http://industrialdataspace.org/brokerDataRequest/8e5b8e67-e7a0-45a1-8910-9b75e00882ec\",\n"+
            "\n" +
            "    \"@class\" : \"de.fraunhofer.iais.eis.BrokerDataRequestImpl\",\n"+
            "    \"id\" : \"http://industrialdataspace.org/brokerDataRequest/983c018c-9914-4008-8e1e-12b3a2d3feb4\",\n"+
            "    \"dataRequestAction\" : \"https://w3id.org/ids/core/BrokerDataRegisterAction\",\n"+
            "    \"messageContent\": \"Hello world\",\n"+
            "    \"coveredEntity\": \"https://w3id.org/ids/core/CoveredConnector\"\n"+
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

    public String toJsonLD(Object instance) {
        // todo: implement me :)
        return "";
    }

    // for testing only
    public String toJsonLD(ObjectType objectType) {
        switch (objectType) {
            case BASIC:
                return brokerDataRequest;
            case NESTED:
                return dataTransfer;
        }
        return "";
    }

}
