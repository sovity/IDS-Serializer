package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarDeserializer;
import de.fraunhofer.iais.eis.ids.jsonld.custom.XMLGregorianCalendarSerializer;

import javax.xml.datatype.XMLGregorianCalendar;


public class JsonLDModule extends SimpleModule {

    /**
     * constructor for the Jackson module which provides support for JSON-LD serialization
     * @param usage modifier for context generation,
     *              if used as library choose Usage.LIB to use the static IDS @context
     *              if used as standalone app choose Usage.STANDALONE to generate @context dynamically on-the-fly (has to be added manually, see {@link JsonLDContext}
     *
     */
    public JsonLDModule(Usage usage) {
        setSerializerModifier(new JsonLDSerializerModifier(usage));
        addSerializer(XMLGregorianCalendar.class, new XMLGregorianCalendarSerializer());
        addDeserializer(XMLGregorianCalendar.class, new XMLGregorianCalendarDeserializer());
    }

}
