package de.fraunhofer.iais.eis.ids.jacksonmodule.jsonld;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iais.eis.util.PlainLiteral;

import java.io.IOException;

public class PlainLiteralDeserializer extends JsonDeserializer<PlainLiteral> {
    @Override
    public PlainLiteral deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec objectCodec = p.getCodec();
        JsonNode node = objectCodec.readTree(p);
        if(node.textValue() == null) {
            // more complex literal, literal with specified language
            String literal = node.get("@literal").textValue();
            String language = node.get("@language").textValue();
            return new PlainLiteral(literal, language);
        } else {
            return new PlainLiteral(node.textValue()); // plain literal was supplied
        }
    }
}