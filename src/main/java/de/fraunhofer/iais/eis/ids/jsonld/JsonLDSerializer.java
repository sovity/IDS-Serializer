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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class JsonLDSerializer extends BeanSerializer {

	Logger logger = LoggerFactory.getLogger(JsonLDSerializer.class);
	
    private static int currentRecursionDepth = 0;

    private static final Map<String, String> contextItems;

    static {
        contextItems = new HashMap<>();
        contextItems.put("ids", "https://w3id.org/idsa/core/");
        contextItems.put("idsc", "https://w3id.org/idsa/code/");
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
            addJwtFieldsToContext(bean, filteredContext);
            gen.writeObjectField("@context", filteredContext);
            //gen.writeStringField("@context", "https://jira.iais.fraunhofer.de/stash/projects/ICTSL/repos/ids-infomodel-commons/raw/jsonld-context/3.0.0/context.jsonld"); // only add @context on top level

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

    /**
     * We need to add the fields of DatPayload to the context manually (if DatPayload present)
     * as RFC 7519 requires the exact field names specified below without any prefix for JWTs.
     * @param bean
     * @param context
     */
    private void addJwtFieldsToContext(Object bean, Map<String, String> context) {
        if(bean == null || bean.getClass() == XMLGregorianCalendarImpl.class || bean.getClass() == BigInteger.class) return;
        if(bean.getClass().getSimpleName().contains("DatPayload")) {
            Stream.of("referringConnector", "aud", "iss", "sub", "nbf", "exp", "iat")
                    .forEach(k -> context.put(k, "ids:".concat(k)));
        } else {
            Stream.of(bean.getClass().getDeclaredFields()).forEach(f -> {
            	
                if(f.getType().isPrimitive() || f.getType().isEnum() 
                		|| f.getType().toString().contains("java.") 
                		|| f.getType().toString().contains("javax.")) return;
                
                boolean wasAccessible = f.isAccessible();
                f.setAccessible(true);
                try {
                    addJwtFieldsToContext(f.get(bean), context);
                } catch (IllegalAccessException e) {
                    logger.error("setting accessible failed"); //TODO can we really simply catch it here?
                }
                f.setAccessible(wasAccessible);
            });
        }
    }

    private void filterContextWrtBean(Object bean, Map<String, String> filteredContext) {
        if(bean == null || bean.getClass() == XMLGregorianCalendarImpl.class || bean.getClass() == BigInteger.class) return; // XMLGregorianCalendarImpl causes infinite recursion
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
        Stream.of(bean.getClass().getMethods()).forEach(m -> {
            // once more run through all properties to check if to add IDSC to context
            if(m.getReturnType().isEnum() && m.getReturnType().getCanonicalName().contains("fraunhofer")) { // TODO this query is really hacky and dangerous as implicit assumptions about the idsc usage are used.
                filteredContext.put("idsc", contextItems.get("idsc"));
            }
        });
        // run through fields recursively
        Stream.of(bean.getClass().getDeclaredFields()).forEach(f -> {
            if(f.getType().isPrimitive() || f.getType().isEnum() 
            		|| f.getType().toString().contains("java.") 
            		|| f.getType().toString().contains("javax.")) return;
            
            
            boolean wasAccessible = f.isAccessible();
            f.setAccessible(true);
            try {
                filterContextWrtBean(f.get(bean), filteredContext);
            } catch (IllegalAccessException e) {
                logger.error("setting accessible failed"); //TODO can we really simply catch it here?
            }
            
            f.setAccessible(wasAccessible);
            
        });
    }
}
