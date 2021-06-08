package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.ids.jsonld.custom.BigDecimalSerializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Jackson module which provides support for JSON-LD serialization
 */
public class JsonLDModule extends SimpleModule {

    public JsonLDModule() {
        super();
        
        setSerializerModifier(new JsonLDSerializerModifier());
        
        addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
        addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
        addSerializer(BigDecimal.class, new BigDecimalSerializer());
        
        addSerializer(URI.class, new UriSerializer());
    }

}
