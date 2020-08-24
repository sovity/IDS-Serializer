package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.topbraid.spin.util.JenaUtil;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.*;


//TODO: To create TypedLiterals (and PlainLiterals), we are creating a dependency to the whole java libraries. Can we improve on that?
//TODO: We still need to look into unknown properties and add them to the "properties" map
//TODO: Change the type of Exceptions being thrown?

/**
 * Internal class to handle the parsing of JSON-LD into java objects
 * @author mboeckmann
 */
class MessageParser {

    Logger logger = LoggerFactory.getLogger(MessageParser.class);

    static Map<String, String> knownNamespaces = new HashMap<>();

    /**
     * Main internal method for creating a java object from a given RDF graph and a URI of the object to handle
     * @param inputModel Model on which queries are to be evaluated from which information can be retrieved
     * @param objectUri URI of the object to be handled
     * @param targetClass Variable containing the class which should be returned
     * @param <T> Class which should be returned
     * @return Object of desired class, filled with the values extracted from inputModel
     * @throws IOException thrown if the parsing fails
     */
    private <T> T handleObject(Model inputModel, String objectUri, Class<T> targetClass) throws IOException {
        try {

            //if(!targetClass.getSimpleName().endsWith("Impl")) //This would not work for "TypedLiteral", "RdfResource" and so on
            //Check whether we are dealing with an instantiable class (i.e. no interface and no abstract class)
            if (targetClass.isInterface() || Modifier.isAbstract(targetClass.getModifiers())) {
                //We don't know the desired class yet (current targetClass is not instantiable). This is only known for the root object
                ArrayList<Class<?>> implementingClasses = getImplementingClasses(targetClass);

                //Get a list of all "rdf:type" statements in our model
                String queryString = "SELECT ?type { BIND(<" + objectUri + "> AS ?s). ?s a ?type . }";
                Query query = QueryFactory.create(queryString);
                QueryExecution queryExecution = QueryExecutionFactory.create(query, inputModel);
                ResultSet resultSet = queryExecution.execSelect();

                if (!resultSet.hasNext()) {
                    throw new IOException("Could not extract class of child object. ID: " + objectUri);
                }

                Class<?> candidateClass = null;


                while (resultSet.hasNext()) {
                    QuerySolution solution = resultSet.nextSolution();
                    String fullName = solution.get("type").toString();

                    //Expected URI is something like https://w3id.org/idsa/core/ClassName (and we want ClassName)
                    String className = fullName.substring(fullName.lastIndexOf('/') + 1);

                    //Some namespaces use "#" instead of "/"
                    if (className.contains("#")) {
                        className = className.substring(className.lastIndexOf("#"));
                    }

                    for (Class<?> currentClass : implementingClasses) {
                        //Is this class instantiable?
                        if (!currentClass.isInterface() && !Modifier.isAbstract(currentClass.getModifiers())) {
                            candidateClass = currentClass;
                            if (currentClass.getSimpleName().equals(className) || candidateClass.getSimpleName().equals(className + "Impl")) {
                                targetClass = (Class<T>) currentClass;
                                break;
                            }
                        }
                    }
                }
                //Did we find "the" class, i.e. instantiable and name matches?
                if (targetClass.isInterface() || Modifier.isAbstract(targetClass.getModifiers())) {
                    //No, the current targetClass cannot be instantiated. Do we have a candidate class?
                    if (candidateClass != null) {
                        logger.warn("Did not find an instantiable class for " + objectUri + " matching expected class name. Guessing " + candidateClass.getSimpleName());
                        targetClass = (Class<T>) candidateClass;
                    }
                }
            }


            //Get constructor (which is package private for our classes) and make it accessible
            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);

            //Instantiate new object, which will be returned at the end
            T returnObject = constructor.newInstance();

            //Get methods
            Method[] methods = returnObject.getClass().getDeclaredMethods();

            //Store methods in map. Key is the name of the RDF property without ids prefix
            Map<String, Method> methodMap = new HashMap<>();

            //Get all relevant methods (setters, but not for label, comment or external properties)
            Arrays.stream(methods).filter(method -> {
                String name = method.getName();
                //Filter out irrelevant methods
                return name.startsWith("set") && !name.equals("setProperty") && !name.equals("setComment") && !name.equals("setLabel") && !name.equals("setId");
            }).forEach(method -> {
                //Remove "set" part
                String reducedName = method.getName().substring(3);

                //Turn first character to lower case
                char[] c = reducedName.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                String finalName = new String(c);
                methodMap.put(finalName, method);

            });

            //There is no "setId" method in our CodeGen generated classes, so we get the field
            Field idField = returnObject.getClass().getDeclaredField("id");

            //Store whether or not it was accessible, so that we can undo making it accessible
            boolean wasAccessible = idField.isAccessible();
            idField.setAccessible(true);

            //Set the ID of the object to be identical with the objectUri parameter
            idField.set(returnObject, new URI(objectUri));
            idField.setAccessible(wasAccessible);

            //A list which stores all those parameter names which may occur only once (i.e. those occurring in the GROUP BY clause)
            List<String> groupByKeys = new ArrayList<>();

            StringBuilder queryStringBuilder = new StringBuilder();

            queryStringBuilder.append("PREFIX ids: <https://w3id.org/idsa/core/>\n");
            for(Map.Entry<String, String> entry : knownNamespaces.entrySet())
            {
                queryStringBuilder.append("PREFIX ").append(entry.getKey()).append(" <").append(entry.getValue()).append(">\n");
            }
            queryStringBuilder.append("SELECT");
            methodMap.forEach((key1, value) -> {
                //Is the return type some sort of List?
                if (Collection.class.isAssignableFrom(value.getParameterTypes()[0])) {
                    boolean isTypedLiteral = false;
                    //Yes, it is assignable multiple times. Concatenate multiple values together using some delimiter
                    //TODO: What kind of delimiter would be appropriate here?
                    try {
                        //ArrayLists are generics. We need to extract the name of the generic parameter as string and interpret that
                        String typeName = extractTypeNameFromArrayList(value.getGenericParameterTypes()[0]);

                        //TODO: This needs to be documented at the TypedLiteral in the CodeGen so that we don't rename it by accident
                        if (typeName.endsWith("TypedLiteral")) isTypedLiteral = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (isTypedLiteral) {
                        queryStringBuilder.append(" (GROUP_CONCAT(CONCAT('\"',?").append(key1).append(",'\"@', lang(?").append(key1).append("));separator=\"|\") AS ?").append(key1).append("sLang) ");
                    }
                    queryStringBuilder.append(" (GROUP_CONCAT(?").append(key1).append(";separator=\"|\") AS ?").append(key1).append("s) ");

                } else {
                    //No, it's not a list. No need to aggregate
                    queryStringBuilder.append(" ?").append(key1);
                    //We will have to GROUP BY this variable though...
                    groupByKeys.add(key1);
                }
            });
            //Start the "WHERE" part - Fuseki does not expect the "WHERE" keyword, but just an "{"
            queryStringBuilder.append(" { ");

            //In case we get an empty result set, we want to know whether or not the query failed (i.e. mandatory field missing)
            boolean containsNotNullableField = false;

            for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
                //Is this a field which is annotated by NOT NULL?
                Field field;
                //Attempt to find a field matching the setter method name
                //E.g. for "setSomething", we search for a field with name "_something" (IDS way) and "something"
                try {
                    field = targetClass.getDeclaredField("_" + entry.getKey());
                } catch (NoSuchFieldException e) {
                    try {
                        field = targetClass.getDeclaredField(entry.getKey());
                    } catch (NoSuchFieldException e2) {
                        throw new NoSuchMethodException("Failed to find field which is set by method " + entry.getKey());
                    }
                }

                boolean nullable = !field.isAnnotationPresent(NotNull.class);

                //If it is "nullable", we need to make this optional
                if (nullable) {
                    queryStringBuilder.append(" OPTIONAL {");
                } else {
                    //Found at least one not nullable field
                    containsNotNullableField = true;
                }
                queryStringBuilder.append(" <").append(objectUri).append("> "); //subject, as passed to the function
                        //For the field, get the JsonAlias annotation (present for all classes generated by the CodeGen tool)
                        //Find the annotation value containing a colon and interpret this as "prefix:predicate"
                        //TODO: Apply this to TypedLiteral and so on
                Optional<String> currentAnnotation = Arrays.stream(field.getAnnotation(JsonAlias.class).value()).filter(annotation -> annotation.contains(":")).findFirst();
                if(currentAnnotation.isPresent())
                {
                    queryStringBuilder.append(currentAnnotation.get());
                }
                else
                {
                    logger.warn("Failed to retrieve JsonAlias for field " + field + ". Assuming ids:" + entry.getKey());
                    queryStringBuilder.append("ids:").append(entry.getKey());
                }
                queryStringBuilder.append(" ?").append(entry.getKey()).append(" ."); //object
                if (nullable) {
                    queryStringBuilder.append("} ");
                }
            }


            queryStringBuilder.append(" } ");

            //Do we need to group? We do, if there is at least one property which can occur multiple times
            //We added all those properties, which may only occur once, to the groupByKeys list
            if (!groupByKeys.isEmpty()) {
                queryStringBuilder.append("GROUP BY");
                for (String key : groupByKeys) {
                    queryStringBuilder.append(" ?").append(key);
                }
            }

            String queryString = queryStringBuilder.toString();

            StringBuilder queryForOtherProperties = new StringBuilder();
            //Query for all unknown properties and their values
            //Select properties and values only

            //TODO: SBA grab complex foreign sub objects
            //CONSTRUCT { ?s ?p ?o } { ?s ?p ?o. FILTER(?p NOT IN (liste der ids: properties)) }
            queryForOtherProperties.append("PREFIX ids: <https://w3id.org/idsa/core/>\n PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n SELECT ?p ?o { ");

            //Respect ALL properties and values
            queryForOtherProperties.append(" <").append(objectUri).append("> ?p ?o .\n");

            //Exclude known properties
            queryForOtherProperties.append("FILTER (?p NOT IN (rdf:type");

            //Predicates usually look like: .append("ids:").append(entry.getKey())
            for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
                queryForOtherProperties.append(", ");
                queryForOtherProperties.append("ids:").append(entry.getKey());
            }

            queryForOtherProperties.append(")). } ");


            //Now that we searched for all "known properties", let's search for all unrecognized content and append it to a generic properties map
            Query externalPropertiesQuery = QueryFactory.create(queryForOtherProperties.toString());
            QueryExecution externalPropertiesQueryExecution = QueryExecutionFactory.create(externalPropertiesQuery, inputModel);
            ResultSet externalPropertiesResultSet = externalPropertiesQueryExecution.execSelect();


            logger.info(queryString);
            Query query = QueryFactory.create(queryString);

            //Evaluate query
            QueryExecution queryExecution = QueryExecutionFactory.create(query, inputModel);
            ResultSet resultSet = queryExecution.execSelect();


            if (!resultSet.hasNext()) {
                //no content... ONLY allowed, if the class has optional fields only (i.e. no mandatory fields)!
                if (containsNotNullableField) {
                    StringBuilder notNullableFieldNames = new StringBuilder();
                    for (Map.Entry<String, Method> entry : methodMap.entrySet()) {
                        //Is this a field which is annotated by NOT NULL?
                        Field field;
                        //Attempt to find a field matching the setter method name
                        //E.g. for "setSomething", we search for a field with name "_something" (IDS way) and "something"
                        try {
                            field = targetClass.getDeclaredField("_" + entry.getKey());
                        } catch (NoSuchFieldException e) {
                            try {
                                field = targetClass.getDeclaredField(entry.getKey());
                            } catch (NoSuchFieldException e2) {
                                throw new NoSuchMethodException("Failed to find field which is set by method " + entry.getKey());
                            }
                        }
                        if (field.isAnnotationPresent(NotNull.class)) {
                            if (notNullableFieldNames.length() > 0) {
                                notNullableFieldNames.append(", ");
                            }
                            notNullableFieldNames.append(entry.getKey());
                        }
                    }
                    logger.info("Executed query: " + queryString);
                    throw new IOException("Mandatory field of " + returnObject.getClass().getSimpleName().replace("Impl", "") + " not filled or invalid. Note that the value of \"@id\" fields MUST be a valid URI. Mandatory fields are: " + notNullableFieldNames.toString());
                }

                return returnObject;
            }

            //TODO SBA: This is rather flat so far. Nested external properties are not captured yet
            while (externalPropertiesResultSet.hasNext()) {
                QuerySolution externalPropertySolution = externalPropertiesResultSet.next();
                //System.out.println("Added external property: " + externalPropertySolution.get("p").toString());
                Method setProperty = returnObject.getClass().getDeclaredMethod("setProperty", String.class, Object.class);
                setProperty.invoke(returnObject, externalPropertySolution.get("p").toString(), externalPropertySolution.get("o"));
            }

            //SPARQL binding present, iterate over result and construct return object
            while (resultSet.hasNext()) {
                QuerySolution querySolution = resultSet.next();

                if (resultSet.hasNext()) {
                    throw new IOException("Multiple bindings for SPARQL query which should only have one binding. Input contains multiple values for a field which may occur only once.");
                }

                for (Map.Entry<String, Method> entry : methodMap.entrySet()) {

                    //What is this method setting? Get the expected parameter type and check whether it is some complex sub-object and whether this is a list
                    Class<?> currentType = entry.getValue().getParameterTypes()[0];
                    //Is this a field which is annotated by NOT NULL?
                    //boolean nullable = !targetClass.getDeclaredField("_" + entry.getKey()).isAnnotationPresent(NotNull.class);

                    String sparqlParameterName = entry.getKey();

                    if (Collection.class.isAssignableFrom(currentType)) {
                        sparqlParameterName += "s"; //plural form for the concatenated values
                    }
                    if (querySolution.contains(sparqlParameterName)) {
                        String currentSparqlBinding = querySolution.get(sparqlParameterName).toString();

                        if (currentType.isEnum()) {
                            entry.getValue().invoke(returnObject, handleEnum(currentType, currentSparqlBinding));
                            continue;
                        }


                        //There is a binding. If it is a complex sub-object, we need to recursively call this function
                        if (Collection.class.isAssignableFrom(currentType)) {
                            //We are working with ArrayLists.
                            //Here, we need to work with the GenericParameterTypes instead to find out what kind of ArrayList we are dealing with
                            String typeName = extractTypeNameFromArrayList(entry.getValue().getGenericParameterTypes()[0]);
                            if (isArrayListTypePrimitive(entry.getValue().getGenericParameterTypes()[0])) {
                                if (typeName.endsWith("TypedLiteral")) {
                                    try {
                                        currentSparqlBinding = querySolution.get(sparqlParameterName + "Lang").toString();
                                    } catch (NullPointerException e) {
                                        logger.warn("NullPointerException occurred upon trying to retrieve localized values of " + currentSparqlBinding, e);

                                        //TODO: Would it be wise to make the parsing fail at this point?
                                        // It happens when, for example, an unknown @type parameter is passed (e.g. "@type" : "xsd:string" with unknown namespace xsd)
                                    }
                                }
                                ArrayList<Object> list = new ArrayList<>();
                                for (String s : currentSparqlBinding.split("\\|")) {
                                    Literal literal;
                                    //querySolution.get(sparqlParameterName).
                                    if (s.endsWith("@")) {
                                        s = s.substring(2, s.length() - 3);
                                        literal = ResourceFactory.createStringLiteral(s);
                                    } else if (s.startsWith("\\")) {
                                        //turn something like \"my Desc 1\"@en to "my Desc 1"@en
                                        s = s.substring(1).replace("\\\"@", "\"@");
                                        literal = ResourceFactory.createLangLiteral(s.substring(1, s.lastIndexOf("@") - 1), s.substring(s.lastIndexOf("@") + 1));
                                    } else {
                                        literal = ResourceFactory.createPlainLiteral(s);
                                    }

                                    //Is the type of the ArrayList some built in Java primitive?

                                    if (builtInMap.containsKey(typeName)) {
                                        //Yes, it is. We MUST NOT call Class.forName(name)!
                                        list.add(handlePrimitive(builtInMap.get(typeName), literal, null));
                                    } else {
                                        //Not a Java primitive, we may call Class.forName(name)
                                        list.add(handlePrimitive(Class.forName(typeName), literal, s));
                                    }
                                }
                                entry.getValue().invoke(returnObject, list);
                            } else {
                                //List of complex sub-objects, such as a list of Resources in a ResourceCatalog
                                ArrayList<Object> list = new ArrayList<>();
                                for (String s : currentSparqlBinding.split("\\|")) {
                                    if (Class.forName(typeName).isEnum()) {
                                        list.add(handleEnum(Class.forName(typeName), s));
                                    } else {
                                        list.add(handleObject(inputModel, s, Class.forName(typeName)));
                                    }
                                }
                                entry.getValue().invoke(returnObject, list);
                            }
                        }

                        //Not an ArrayList of objects expected, but rather one object
                        else {
                            //Our implementation of checking for primitives (i.e. also includes URLs, Strings, XMLGregorianCalendars, ...)
                            if (isPrimitive(currentType)) {

                                Literal literal = null;
                                try {
                                    literal = querySolution.getLiteral(sparqlParameterName);
                                } catch (Exception ignored) {
                                }

                                entry.getValue().invoke(returnObject, handlePrimitive(currentType, literal, currentSparqlBinding));

                            } else {
                                //Not a primitive object, but a complex sub-object. Recursively call this function to handle it
                                entry.getValue().invoke(returnObject, handleObject(inputModel, currentSparqlBinding, entry.getValue().getParameterTypes()[0]));
                            }
                        }
                    }

                }
            }

            return returnObject;
        } catch (NoSuchMethodException | NullPointerException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException | URISyntaxException | DatatypeConfigurationException | ClassNotFoundException e) {
            throw new IOException("Failed to instantiate desired class", e);
        }
    }

    /**
     * Internal function to create a single enum object from a given desired class and a URL
     * @param enumClass The enum class
     * @param url The URL of the enum value
     * @param <T> Enum class
     * @return Value of enumClass matching the input URL
     * @throws IOException thrown if no matching enum value could be found
     */
    private <T> T handleEnum(Class<T> enumClass, String url) throws IOException {
        if (!enumClass.isEnum()) {
            throw new RuntimeException("Non-Enum class passed to handleEnum function.");
        }
        T[] constants = enumClass.getEnumConstants();
        for (T constant : constants) {
            if (url.equals(constant.toString())) {
                return constant;
            }
        }
        throw new IOException("Failed to find matching enum value for " + url);
    }

    /**
     * Function for handling a rather primitive object, i.e. not a complex sub-object (e.g. URI, TypedLiteral, GregorianCalendar values, ...)
     * @param currentType Input Class (or primitive)
     * @param literal Value as literal (can be null in some cases)
     * @param currentSparqlBinding Value as SPARQL Binding (can be null in some cases)
     * @return Object of type currentType
     * @throws URISyntaxException thrown, if currentType is URI, but the value cannot be parsed to a URI
     * @throws DatatypeConfigurationException thrown, if currentType is XMLGregorianCalendar or Duration, but parsing fails
     * @throws IOException thrown, if no matching "simple class" could be found
     */
    private Object handlePrimitive(Class<?> currentType, Literal literal, String currentSparqlBinding) throws URISyntaxException, DatatypeConfigurationException, IOException {
        //Java way of checking for primitives, i.e. int, char, float, double, ...
        if (currentType.isPrimitive()) {
            //System.out.println(currentType.getName() + " is a Java primitive");
            if (literal == null) {
                throw new NullPointerException("Trying to handle Java primitive, but got no literal value");
            }
            //If it is an actual primitive, there is no need to instantiate anything. Just give it to the function
            switch (currentType.getSimpleName()) {
                case "int":
                    return literal.getInt();
                case "boolean":
                    return literal.getBoolean();
                case "long":
                    return literal.getLong();
                case "short":
                    return literal.getShort();
                case "float":
                    return literal.getFloat();
                case "double":
                    return literal.getDouble();
                case "byte":
                    return literal.getByte();
            }
        }

        //System.out.println(currentType.getName() + " is some other (rather) primitive value");

        //Check for the more complex literals

        //URI
        if (URI.class.isAssignableFrom(currentType)) {
            return new URI(currentSparqlBinding);
        }

        //String
        if (String.class.isAssignableFrom(currentType)) {
            return currentSparqlBinding;
        }

        //XMLGregorianCalendar
        if (XMLGregorianCalendar.class.isAssignableFrom(currentType)) {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.parse(literal.getValue().toString())));
        }

        //TypedLiteral
        if (TypedLiteral.class.isAssignableFrom(currentType)) {
            if (!literal.getLanguage().equals("")) {
                //System.out.println("Creating language tagged typed literal");
                return new TypedLiteral(literal.getValue().toString(), literal.getLanguage());
            }
            if (literal.getDatatypeURI() != null) {
                //System.out.println("Creating literal with type");
                return new TypedLiteral(literal.getValue().toString(), new URI(literal.getDatatypeURI()));
            }
            return new TypedLiteral(currentSparqlBinding);
        }

        //BigInteger
        if (BigInteger.class.isAssignableFrom(currentType)) {
            return new BigInteger(literal.getString());
        }

        //byte[]
        if (byte[].class.isAssignableFrom(currentType)) {
            return currentSparqlBinding.getBytes();
        }

        //Duration
        if (Duration.class.isAssignableFrom(currentType)) {
            return DatatypeFactory.newInstance().newDuration(currentSparqlBinding);
        }

        //RdfResource
        if (RdfResource.class.isAssignableFrom(currentType)) {
            return new RdfResource(currentSparqlBinding);
        }

        throw new IOException("Unrecognized primitive type: " + currentType.getName());
    }

    /**
     * This list contains all primitive Java types
     */
    private final Map<String, Class<?>> builtInMap = new HashMap<>();
    {
        builtInMap.put("int", Integer.TYPE);
        builtInMap.put("long", Long.TYPE);
        builtInMap.put("double", Double.TYPE);
        builtInMap.put("float", Float.TYPE);
        builtInMap.put("bool", Boolean.TYPE);
        builtInMap.put("char", Character.TYPE);
        builtInMap.put("byte", Byte.TYPE);
        builtInMap.put("void", Void.TYPE);
        builtInMap.put("short", Short.TYPE);
    }

    private boolean isArrayListTypePrimitive(Type t) throws IOException {
        String typeName = extractTypeNameFromArrayList(t);
        //System.out.println("Extracted type name from ArrayList: " + typeName);

        try {
            //Do not try to call Class.forName(primitive) -- that would throw an exception
            if (builtInMap.containsKey(typeName)) return true;
            return isPrimitive(Class.forName(typeName));
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to retrieve class from generic", e);
        }
    }

    private String extractTypeNameFromArrayList(Type t) throws IOException {
        String typeName = t.getTypeName();
        if (!typeName.startsWith("java.util.ArrayList<? extends ")) {
            throw new IOException("Illegal argument encountered while interpreting type parameter");
        }
        //last space is where we want to cut off (right after the "extends"), as well as removing the last closing braces
        return typeName.substring(typeName.lastIndexOf(" ") + 1, typeName.length() - 1);
    }

    private boolean isPrimitive(Class<?> input) throws IOException {
        //Collections are not simple
        if (Collection.class.isAssignableFrom(input)) {
            throw new IOException("Encountered collection in isPrimitive. Use isArrayListTypePrimitive instead");
        }

        //check for: plain/typed literal, XMLGregorianCalendar, byte[], RdfResource
        //covers int, long, short, float, double, boolean, byte
        if (input.isPrimitive()) return true;

        return (URI.class.isAssignableFrom(input) ||
                String.class.isAssignableFrom(input) ||
                XMLGregorianCalendar.class.isAssignableFrom(input) ||
                TypedLiteral.class.isAssignableFrom(input) ||
                BigInteger.class.isAssignableFrom(input) ||
                byte[].class.isAssignableFrom(input) ||
                Duration.class.isAssignableFrom(input) ||
                RdfResource.class.isAssignableFrom(input));
    }

    /**
     * Entry point to this class. Takes a message and a desired target class (can be an interface)
     * @param message Message to be parsed
     * @param targetClass Desired target class (something as abstract as "Message.class" is allowed)
     * @param <T> Desired target class
     * @return Object of desired target class, representing the values contained in input message
     * @throws IOException if the parsing of the message fails
     */
    <T> T parseMessage(String message, Class<T> targetClass) throws IOException {
        Model model = readMessage(message);

        ArrayList<Class<?>> implementingClasses = getImplementingClasses(targetClass);
        /*System.out.println("Implementing classes of " + targetClass + " are: ");
        for(Class<?> c : implementingClasses)
        {
            System.out.println(c.getName());
        }
         */

        String queryString = "SELECT ?id ?type { ?id a ?type . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();

        if (!resultSet.hasNext()) {
            throw new IOException("Could not extract class from input message");
        }

        Class<?> returnClass = null;
        String returnId = null;

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String fullName = solution.get("type").toString();
            String className = fullName.substring(fullName.lastIndexOf('/') + 1);

            //For legacy purposes...
            if (className.startsWith("ids:")) {
                className = className.substring(4);
            }

            for (Class<?> currentClass : implementingClasses) {
                //System.out.println(className + " == " + currentClass.getSimpleName() + "Impl ? " +  currentClass.getSimpleName().equals(className + "Impl"));
                if (currentClass.getSimpleName().equals(className + "Impl")) {
                    returnId = solution.get("id").toString();
                    returnClass = currentClass;
                    //System.out.println("Found implementing class: " + currentClass.getSimpleName());
                    break;
                }
            }
            if (returnClass != null) break;
        }

        if (returnClass == null) {
            throw new NullPointerException("Could not determine an appropriate implementing class for " + targetClass.getName());
        }

        //At this point, we parsed the model and know to which implementing class we want to parse


        return (T) handleObject(model, returnId, returnClass);

    }

    /**
     * Reads a message into an Apache Jena model.
     * If the class was not previously initialized, it will automatically initialize upon this function call.
     *
     * @param message Message to be read
     * @return The model of the message plus ontology
     */
    private Model readMessage(String message) {

        Model targetModel = JenaUtil.createMemoryModel();

        //Read incoming message to the same model

        RDFDataMgr.read(targetModel, new ByteArrayInputStream(message.getBytes()), RDFLanguages.JSONLD);

        return targetModel;
    }


    /**
     * Get a list of all subclasses (by JsonSubTypes annotation) which can be instantiated
     * @param someClass Input class of which implementable subclasses need to be found
     * @return ArrayList of instantiable subclasses
     */
    ArrayList<Class<?>> getImplementingClasses(Class<?> someClass) {
        ArrayList<Class<?>> result = new ArrayList<>();
        JsonSubTypes subTypeAnnotation = someClass.getAnnotation(JsonSubTypes.class);
        if (subTypeAnnotation != null) {
            JsonSubTypes.Type[] types = subTypeAnnotation.value();
            for (JsonSubTypes.Type type : types) {
                result.addAll(getImplementingClasses(type.value()));
            }
        }
        if (!someClass.isInterface() && !Modifier.isAbstract(someClass.getModifiers()))
            result.add(someClass);
        return result;
    }

}
