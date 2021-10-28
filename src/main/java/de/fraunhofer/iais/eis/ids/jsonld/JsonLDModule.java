package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.ids.jsonld.custom.BigDecimalSerializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.UriOrModelClassSerializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;
import de.fraunhofer.iais.eis.util.UriOrModelClass;

import java.math.BigDecimal;
import java.net.URI;

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

        addSerializer(UriOrModelClass.class, new UriOrModelClassSerializer());

        addSerializer(URI.class, new UriSerializer());
    }

}
