package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.HashMap;
import java.util.Map;

public class JsonLDContext {

    private static final String prefix = "\"@context\": {\n";
    private static final String postfix = "},";

    private static final String idsPrefix = "ids";
    private static final String idsNamespace = "https://w3id.org/idsa/core/";

    private static final String idsEnumPrefix = "idsEnums";
    private static final String idsEnumNamespace = "https://w3id.org/idsa/code/";

    private final Map<String, String> properties = new HashMap<>();

    private static JsonLDContext instance;


    public static JsonLDContext getInstance() {
        if (instance == null) {
            instance = new JsonLDContext();
        }
        return instance;
    }


    private JsonLDContext() {
        properties.put(idsPrefix, idsNamespace);
        properties.put(idsEnumPrefix, idsEnumNamespace);
    }

    public void addProperty(String key, String value) {
        // do namespace replacing
       value = replaceIdsNamespace(value);

        // insert new property
        if (properties.containsKey(key) && !properties.get(key).equals(value)) {
            throw new IllegalArgumentException("ambiguous value for key: " + key + " " +
                    "old value: " + properties.get(key) + " " +
                    "new value: " + value);
        } else {
            properties.put(key, value);
            //System.out.println("added property: " + key + " with value: " + value);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        properties.forEach((k, v) -> sb.append("\"").append(k).append("\": \"").append(v).append("\",\n"));
        int lastComma = sb.lastIndexOf(",");
        sb.replace(lastComma, lastComma+1, "");
        sb.append(postfix);
        return sb.toString();
    }

    public void clear() {
        properties.clear();
        properties.put(idsPrefix, idsNamespace);
        properties.put(idsEnumPrefix, idsEnumNamespace);
    }


    public String replaceIdsNamespace(String value) {
        value = value.replace(idsNamespace, idsPrefix + ":");
        if (value.contains(idsEnumNamespace)) {
            if (!properties.containsKey(idsEnumPrefix)) {
                properties.put(idsEnumPrefix, idsEnumNamespace);
            }
            value = value.replace(idsEnumNamespace, idsEnumPrefix + ":");
        }
        return value;
    }
}