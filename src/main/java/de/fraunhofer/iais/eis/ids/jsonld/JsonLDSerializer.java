package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import de.fraunhofer.iais.eis.annotation.RdfId;
import de.fraunhofer.iais.eis.annotation.RdfProperty;
import de.fraunhofer.iais.eis.annotation.RdfType;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JsonLDSerializer extends BeanSerializer {

    private Usage usage;
    private static int currentRecursionDepth = 0;


    JsonLDSerializer(BeanSerializerBase src, Usage usage) {
        super(src);
        this.usage = usage;
    }

    @Override
    public void serializeWithType(Object bean, JsonGenerator gen, SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        if (usage.equals(Usage.STANDALONE)) {
            super.serializeWithType(bean, gen, provider, typeSer);
        }
        else if (usage.equals(Usage.LIB)) {
            gen.setCurrentValue(bean);

            currentRecursionDepth++;
            gen.writeStartObject();
            if(currentRecursionDepth == 1) {
                gen.writeStringField("@context", "http://industrialdataspace.org/jsonld/context"); // only add @context on top level
            }
            gen.writeStringField("@class", "." + bean.getClass().getSimpleName());
            if (_propertyFilterId != null) {
                serializeFieldsFiltered(bean, gen, provider);
            } else {
                serializeFields(bean, gen, provider);
            }
            gen.writeEndObject();
            currentRecursionDepth--;
        }
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator gen, SerializerProvider provider) throws IOException {
        // add @id
        Method rdfIdMethod = getRdfIdMethod(bean);
        if (rdfIdMethod != null) {
            rdfIdMethod.setAccessible(true);
            gen.writeFieldName("@id");
            try {
                gen.writeString(rdfIdMethod.invoke(bean).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (usage.equals(Usage.STANDALONE)) {
            // add @RdfProperty's to context if context should be generated on-the-fly
            JsonLDContext context = JsonLDContext.getInstance();
            List<Method> methods = new ArrayList<>();
            methods.addAll(Arrays.asList(bean.getClass().getMethods()));
            for (Class iface : bean.getClass().getInterfaces()) {
                methods.addAll(Arrays.asList(iface.getMethods()));
            }

            methods.stream()
                    .filter(method -> method.isAnnotationPresent(RdfProperty.class))
                    .forEach(method -> {
                        String k = method.getName().replace("is", "").replace("get", ""); // remove getter part of method name
                        k = Character.toLowerCase(k.charAt(0)) + k.substring(1); // decapitalize the first char
                        String v = method.getAnnotation(RdfProperty.class).value();
                        context.addProperty(k, v);
                    });
        }
        //do the normal serialization work
        super.serializeFields(bean, gen, provider);
    }


    private RdfType getRdfTypeAnnotation(Object bean) {
        RdfType rdfType = bean.getClass().getAnnotation(RdfType.class);
        if (rdfType == null) {
            rdfType = Arrays.stream(bean.getClass().getInterfaces())
                    .map(iface -> iface.getAnnotation(RdfType.class))
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
        }
        return rdfType;
    }

    private Method getRdfIdMethod(Object bean) {
        Method rdfIdMethod = Arrays.stream(bean.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(RdfId.class))
                .findFirst().orElse(null);
        if (rdfIdMethod == null) {
            rdfIdMethod = Arrays.stream(bean.getClass().getInterfaces())
                    .map(Class::getMethods)
                    .flatMap(methods -> Arrays.stream(methods)
                            .filter(method -> method.isAnnotationPresent(RdfId.class)))
                    .findFirst().orElse(null);
        }
        return rdfIdMethod;
    }
}
