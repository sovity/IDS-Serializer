package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonLDModule extends SimpleModule {

    public JsonLDModule() {
        setSerializerModifier(new JsonLDSerializerModifier());
    }

}
