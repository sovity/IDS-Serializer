package de.fraunhofer.iais.eis.ids;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonLDModule extends SimpleModule {

    public JsonLDModule() {
        setSerializerModifier(new JsonLDSerializerModifier());
    }

}
