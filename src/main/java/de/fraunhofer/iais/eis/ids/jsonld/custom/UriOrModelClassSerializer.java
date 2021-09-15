package de.fraunhofer.iais.eis.ids.jsonld.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.fraunhofer.iais.eis.util.UriOrModelClass;


import java.io.IOException;

public class UriOrModelClassSerializer extends StdSerializer<UriOrModelClass> {
    public UriOrModelClassSerializer() {
        this(null);
    }

    protected UriOrModelClassSerializer(Class clazz) {
        super(clazz);
    }

    @Override
    public void serialize(UriOrModelClass uom, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        if (uom.hasObject()) {
            jsonGenerator.writeObject(uom.getObject());
        } else if (uom.hasObjectList()) {
            jsonGenerator.writeStartArray();
            for (Object o : uom.getObjectList()) {
                jsonGenerator.writeObject(o);
            }
            jsonGenerator.writeEndArray();
        } else {
            jsonGenerator.writeNull();
        }
    }
}
