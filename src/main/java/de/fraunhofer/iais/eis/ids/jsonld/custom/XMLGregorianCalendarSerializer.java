package de.fraunhofer.iais.eis.ids.jsonld.custom;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;

public class XMLGregorianCalendarSerializer extends StdSerializer<XMLGregorianCalendar> {

    public XMLGregorianCalendarSerializer() {
        this(null);
    }

    public XMLGregorianCalendarSerializer(Class clazz) {
        super(clazz);
    }

    @Override
    public void serialize(XMLGregorianCalendar value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String serializedCalendar = value.toGregorianCalendar().toZonedDateTime().toString();
        serializedCalendar = serializedCalendar.substring(0, serializedCalendar.indexOf("[")); // remove [GMT+...] appendix
        gen.writeStartObject();
        gen.writeStringField("@value", serializedCalendar);
        gen.writeStringField("@type", "xsd:dateTimeStamp");
        gen.writeEndObject();

    }
}
