package de.fraunhofer.iais.eis.ids;

public class Serializer {

    public String toJsonLD(Object instance) {
        return "{\n" +
                "\t\"@context\": {\n" +
                "\t    \"ids\" : \"https://w3id.org/ids/core/\",\n" +
                "\t    \"dataRequestAction\": \"ids:dataRequestAction\",\n" +
                "\t    \"messageContent\": \"ids:messageContent\",\n" +
                "\t    \"coveredEntity\": \"ids:coveredEntity\"\n" +
                "\t},\n" +
                "\t\"@type\": \"ids:BrokerDataRequest\",\n" +
                "\t\"@id\":\"http://industrialdataspace.org/brokerDataRequest/8e5b8e67-e7a0-45a1-8910-9b75e00882ec\",\n" +
                "\n" +
                "\t\"id\":\"http://industrialdataspace.org/brokerDataRequest/8e5b8e67-e7a0-45a1-8910-9b75e00882ec\",\n" +
                "\t\"dataRequestAction\":\"https://w3id.org/ids/core/BrokerDataRegisterAction\",\n" +
                "\t\"messageContent\":\"Hello world\",\n" +
                "\t\"coveredEntity\":\"https://w3id.org/ids/core/CoveredConnector\"\n" +
                "}";
    }

}
