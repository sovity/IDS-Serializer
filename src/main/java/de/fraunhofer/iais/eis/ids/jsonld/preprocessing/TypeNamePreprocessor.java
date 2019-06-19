package de.fraunhofer.iais.eis.ids.jsonld.preprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class TypeNamePreprocessor extends BasePreprocessor {

    @Override
    String preprocess_impl(String input) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<Object, Object> inMap = mapper.readValue(input, Map.class);
        Map<Object, Object> outMap = unifyTypeURIPrefix(inMap);
        return mapper.writeValueAsString(outMap);
    }


    private Map unifyTypeURIPrefix(Map in) {
        Map<Object, Object> out = new LinkedHashMap<>();
        in.forEach((k,v) -> {
            if(v instanceof String && k instanceof String && k.equals("@type")) {
                String newValue = ((String) v).replace("https://w3id.org/idsa/core/", "ids:");
                if(!newValue.contains("ids:")) newValue = "ids:".concat(newValue);
                out.put(k, newValue);
            } else if(v instanceof Map) {
                out.put(k, unifyTypeURIPrefix((Map) v));
            }
            else {
                out.put(k, v); // modify nothing if not @type or a map
            }
        });
        return out;
    }




}
