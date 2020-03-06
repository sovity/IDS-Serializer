package de.fraunhofer.iais.eis.ids.jsonld.preprocessing;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TypeNamePreprocessor extends BasePreprocessor {

    private static final Map<String, String> prefixes;

    static {
        prefixes = new HashMap<>();
        prefixes.put("ids:", "https://w3id.org/idsa/core/");
        prefixes.put("idsc:", "https://w3id.org/idsa/code/");
        prefixes.put("info:", "http://www.fraunhofer.de/fraunhofer-digital/infomodell#");
        prefixes.put("kdsf:", "http://kerndatensatz-forschung.de/version1/technisches_datenmodell/owl/Basis#");
    }

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
                AtomicReference<String> modifiableValue = new AtomicReference<>((String) v);
                prefixes.forEach((p, u) -> modifiableValue.set(modifiableValue.get().replace(u, p))); // replace full URI with prefix
                if(! (modifiableValue.get().startsWith("ids:")
                        || modifiableValue.get().startsWith("info:")
                        || modifiableValue.get().startsWith("kdsf:"))) {
                    modifiableValue.set("ids:".concat(modifiableValue.get())); // default to ids prefix for backwards compatibility
                }
                out.put(k, modifiableValue.get());
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
