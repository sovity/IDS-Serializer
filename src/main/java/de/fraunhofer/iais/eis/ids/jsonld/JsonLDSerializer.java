package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class JsonLDSerializer extends BeanSerializer {

    private static int currentRecursionDepth = 0;

    private static final Map<String, String> contextItems;

    static {
        contextItems = new HashMap<>();
        contextItems.put("ids", "https://w3id.org/idsa/core/");
        contextItems.put("info", "http://www.fraunhofer.de/fraunhofer-digital/infomodell#");
        contextItems.put("kdsf", "http://kerndatensatz-forschung.de/version1/technisches_datenmodell/owl/Basis#");
    }


    JsonLDSerializer(BeanSerializerBase src) {
        super(src);
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        gen.setCurrentValue(bean);

        currentRecursionDepth++;
        gen.writeStartObject();
        if (currentRecursionDepth == 1) {
            Map<String, String> filteredContext = new HashMap<>();
            filterContextWrtBean(bean, filteredContext);
            gen.writeObjectField("@context", filteredContext);
        }
        WritableTypeId typeIdDef = _typeIdDef(typeSer, bean, JsonToken.START_OBJECT);
        String resolvedTypeId = typeIdDef.id != null ? typeIdDef.id.toString() : typeSer.getTypeIdResolver().idFromValue(bean);
        if (resolvedTypeId != null) {
            gen.writeStringField(typeIdDef.asProperty, resolvedTypeId);
        }
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, gen, provider);
        } else {
            serializeFields(bean, gen, provider);
        }
        gen.writeEndObject();
        currentRecursionDepth--;
    }

    private void filterContextWrtBean(Object bean, Map<String, String> filteredContext) {
        if(bean == null || bean.getClass() == XMLGregorianCalendarImpl.class) return; // XMLGregorianCalendarImpl causes infinite recursion
        contextItems.forEach((p, u) -> {
            JsonTypeName typeNameAnnotation = bean.getClass().getAnnotation(JsonTypeName.class);
            if(typeNameAnnotation != null && typeNameAnnotation.value().contains(p)) {
                filteredContext.put(p, u);
            }
            Stream.of(bean.getClass().getMethods()).forEach(m -> {
                JsonProperty propertyAnnotation = m.getAnnotation(JsonProperty.class);
                if(propertyAnnotation != null && propertyAnnotation.value().contains(p)) {
                    filteredContext.put(p, u);
                }
            });
        });
        // run through fields recursively
        Stream.of(bean.getClass().getDeclaredFields()).forEach(f -> {
            f.setAccessible(true);
            if(! (f.getType().isPrimitive() || f.getType().isEnum())) {
                try {
                    filterContextWrtBean(f.get(bean), filteredContext);
                } catch (IllegalAccessException e) {
                    System.err.println("setting accessible failed");
                }
            }
        });
    }
}
