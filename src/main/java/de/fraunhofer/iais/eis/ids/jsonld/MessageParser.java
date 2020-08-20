package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.spin.util.JenaUtil;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.*;


//TODO: To create TypedLiterals (and PlainLiterals), we are creating a dependency to the whole java libraries. Can we improve on that?
public class MessageParser {

    //private static Model ontologyModel = null;

    //public static boolean downloadOntology = false;

    private static MessageParser instance;

    private MessageParser()
    { }

    public static MessageParser getInstance()
    {
        if(instance == null)
        {
            instance = new MessageParser();
        }
        return instance;
    }

    private <T> T handleObject(Model inputModel, String objectUri, Class<T> targetClass) throws IOException {
        try {

            if(!targetClass.getSimpleName().endsWith("Impl"))
            {
                //We don't know the desired class yet. This is only known for the root object
                ArrayList<Class<?>> implementingClasses = MessageParser.getImplementingClasses(targetClass);

                String queryString = "SELECT ?type { BIND(<" + objectUri + "> AS ?s). ?s a ?type . }";
                Query query = QueryFactory.create(queryString);
                QueryExecution queryExecution = QueryExecutionFactory.create(query, inputModel);
                ResultSet resultSet = queryExecution.execSelect();

                if(!resultSet.hasNext())
                {
                    throw new IOException("Could not extract class of child object. ID: " + objectUri);
                }


                while (resultSet.hasNext()) {
                    QuerySolution solution = resultSet.nextSolution();
                    String fullName = solution.get("type").toString();
                    String className = fullName.substring(fullName.lastIndexOf('/') + 1);

                    for (Class<?> currentClass : implementingClasses) {
                        if (currentClass.getSimpleName().equals(className + "Impl")) {
                            targetClass = (Class<T>) currentClass;
                            System.out.println("Found implementing class: " + currentClass.getSimpleName() + " (of child object: " + objectUri + ")");
                            break;
                        }
                    }
                }
            }

            //T returnObject = (T) targetClass.getConstructor().setAccessible(true).newInstance();

            Constructor<T> constructor = targetClass.getDeclaredConstructor();
            constructor.setAccessible(true);

            T returnObject = constructor.newInstance();

/*            Field[] fields = returnObject.getClass().getDeclaredFields();
            System.out.println("FIELDS:");
            for(Field field : fields)
            {
                System.out.println(field.getName());
            }
 */

            //Get methods
            Method[] methods = returnObject.getClass().getDeclaredMethods();

            //Store methods in map. Key is the name of the RDF property without ids prefix
            Map<String, Method> methodMap = new HashMap<>();

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

            List<String> groupByKeys = new ArrayList<>();

            StringBuilder queryStringBuilder = new StringBuilder();
            queryStringBuilder.append("PREFIX ids: <https://w3id.org/idsa/core/>\nSELECT");
            methodMap.forEach((key1, value) -> {
                //Is the return type some sort of List?
                if(Collection.class.isAssignableFrom(value.getParameterTypes()[0]))
                {
                    //Yes, it is assignable multiple times. Concatenate multiple values together using some delimiter
                    //TODO: What kind of delimiter would be appropriate here?
                    queryStringBuilder.append(" (GROUP_CONCAT(?").append(key1).append(";separator=\"|\") AS ?").append(key1).append("s) ");

                }
                else {
                    //System.out.println("Collection is not assignable from " + value.getParameterTypes()[0]);
                    //No, it's not a list. No need to aggregate
                    queryStringBuilder.append(" ?").append(key1);
                    //We will have to GROUP BY this variable though...
                    groupByKeys.add(key1);
                }
            });
            queryStringBuilder.append(" { ");


            for(Map.Entry<String, Method> entry : methodMap.entrySet())
            {
                //Is this a field which is annotated by NOT NULL?
                boolean nullable = !targetClass.getDeclaredField("_" + entry.getKey()).isAnnotationPresent(NotNull.class);

                //If it is "nullable", we need to make this optional
                if(nullable)
                {
                    queryStringBuilder.append(" OPTIONAL {");
                }
                queryStringBuilder.append(" <").append(objectUri).append("> ") //subject, as passed to the function
                        .append("ids:").append(entry.getKey()) //predicate
                        .append(" ?").append(entry.getKey()).append(" ."); //object
                if(nullable)
                {
                    queryStringBuilder.append("} ");
                }
            }


            queryStringBuilder.append(" } ");

            //Do we need to group? We do, if there is at least one property which can occur multiple times
            //We added all those properties, which may only occur once, to the groupByKeys list
            if(groupByKeys.size() < methodMap.size())
            {
                queryStringBuilder.append("GROUP BY");
                for(String key : groupByKeys)
                {
                    queryStringBuilder.append(" ?").append(key);
                }
            }

            String queryString = queryStringBuilder.toString();

            System.out.println(queryString);

            //Copy the ontology inputModel
            //Model combinedModel = ontologyModel;

            //Add the message inputModel to it - prevents additional parsing of the message
            //TODO: Is this even needed?! Maybe we can save some time here. We probably don't need the ontology at all, as we extract subclasses from Jackson
            //combinedModel.add(inputModel);

            Query query = QueryFactory.create(queryString);

            //Evaluate query on combined model
            QueryExecution queryExecution = QueryExecutionFactory.create(query, inputModel);
            ResultSet resultSet = queryExecution.execSelect();


            if(!resultSet.hasNext())
            {
                return returnObject;
            }


            while(resultSet.hasNext())
            {
                QuerySolution querySolution = resultSet.next();

                if(resultSet.hasNext())
                {
                    throw new IOException("Multiple bindings for SPARQL query which should only have one binding!");
                }

                for(Map.Entry<String, Method> entry : methodMap.entrySet())
                {

                    Class<?> currentType = entry.getValue().getParameterTypes()[0];
                    //Is this a field which is annotated by NOT NULL?
                    //boolean nullable = !targetClass.getDeclaredField("_" + entry.getKey()).isAnnotationPresent(NotNull.class);

                    String sparqlParameterName = entry.getKey();

                    if(Collection.class.isAssignableFrom(currentType)) {
                        sparqlParameterName += "s"; //plural form for the concatenated values
                    }
                    if(querySolution.contains(sparqlParameterName))
                    {
                        String currentSparqlBinding = querySolution.get(sparqlParameterName).toString();

                        if(currentType.isEnum())
                        {
                            entry.getValue().invoke(returnObject, handleEnum(currentType, currentSparqlBinding));
                            continue;
                        }


                        //There is a binding. If it is a complex sub-object, we need to recursively call this function
                        if(Collection.class.isAssignableFrom(currentType))
                        {
                            //We are working with ArrayLists.
                            //Here, we need to work with the GenericParameterTypes instead to find out what kind of ArrayList we are dealing with
                            String typeName = extractTypeNameFromArrayList(entry.getValue().getGenericParameterTypes()[0]);
                            if(isArrayListTypePrimitive(entry.getValue().getGenericParameterTypes()[0]))
                            {
                                ArrayList<Object> list = new ArrayList<>();
                                for(String s : currentSparqlBinding.split("\\|"))
                                {
                                    Literal literal = ResourceFactory.createPlainLiteral(s);

                                    //Is the type of the ArrayList some built in Java primitive?

                                    if(builtInMap.containsKey(typeName))
                                    {
                                        //Yes, it is. We MUST NOT call Class.forName(name)!
                                        list.add(handlePrimitive(builtInMap.get(typeName), literal, null));
                                    }
                                    else
                                    {
                                        //Not a Java primitive, we may call Class.forName(name)
                                        list.add(handlePrimitive(Class.forName(typeName), literal, s));
                                    }
                                }
                                entry.getValue().invoke(returnObject, list);
                            }
                            else
                            {
                                //List of complex sub-objects, such as a list of Resources in a ResourceCatalog
                                ArrayList<Object> list = new ArrayList<>();
                                for(String s : currentSparqlBinding.split("\\|"))
                                {
                                    if(Class.forName(typeName).isEnum())
                                    {
                                        list.add(handleEnum(Class.forName(typeName), s));
                                    }
                                    else {
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
                                }
                                catch (Exception ignored) {}

                                entry.getValue().invoke(returnObject, handlePrimitive(currentType, literal, currentSparqlBinding));

                            } else {
                                System.out.println(entry.getValue().getParameterTypes()[0].getName() + " is not primitive");

                                entry.getValue().invoke(returnObject, handleObject(inputModel, currentSparqlBinding, entry.getValue().getParameterTypes()[0]));
                            }
                        }
                    }

                }
            }

            return returnObject;
        }
        catch (NoSuchMethodException | NullPointerException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException | URISyntaxException | DatatypeConfigurationException | ClassNotFoundException e)
        {
            throw new IOException("Failed to instantiate desired class", e);
        }
    }

    private <T> T handleEnum(Class<T> enumClass, String url) throws IOException {
        if (!enumClass.isEnum()) {
            throw new RuntimeException("Non-Enum class passed to handleEnum function.");
        }
        T[] constants = (T[]) enumClass.getEnumConstants();
        boolean found = false;
        for (T constant : constants) {
            if (url.equals(constant.toString())) {
                return constant;
            }
        }
        throw new IOException("Failed to find matching enum value for " + url);
    }

    //TODO for performance: Don't pass the full querySolution here, but just one node...
    private Object handlePrimitive(Class<?> currentType, Literal literal, String currentSparqlBinding) throws URISyntaxException, DatatypeConfigurationException, IOException {
        //Java way of checking for primitives, i.e. int, char, float, double, ...
        if(currentType.isPrimitive())
        {
            System.out.println(currentType.getName() + " is a Java primitive");
            if(literal == null)
            {
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

        System.out.println(currentType.getName() + " is some other (rather) primitive value");

        //Check for the more complex literals

        //URI
        if(URI.class.isAssignableFrom(currentType))
        {
            return new URI(currentSparqlBinding);
        }

        //String
        if(String.class.isAssignableFrom(currentType))
        {
            return currentSparqlBinding;
        }

        //XMLGregorianCalendar
        if(XMLGregorianCalendar.class.isAssignableFrom(currentType))
        {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(ZonedDateTime.parse(literal.getValue().toString())));
        }

        //TypedLiteral
        if(TypedLiteral.class.isAssignableFrom(currentType))
        {
            return new TypedLiteral(currentSparqlBinding);
        }

        //BigInteger
        if(BigInteger.class.isAssignableFrom(currentType))
        {
            return new BigInteger(currentSparqlBinding);
        }

        //byte[]
        if(byte[].class.isAssignableFrom(currentType))
        {
            return currentSparqlBinding.getBytes();
        }

        //Duration
        if(Duration.class.isAssignableFrom(currentType))
        {
            return DatatypeFactory.newInstance().newDuration(currentSparqlBinding);
        }

        //RdfResource
        if(RdfResource.class.isAssignableFrom(currentType))
        {
            return new RdfResource(currentSparqlBinding);
        }

        throw new IOException("Unrecognized primitive type: " + currentType.getName());
    }

    private final Map<String,Class<?>> builtInMap = new HashMap<>();{
        builtInMap.put("int", Integer.TYPE );
        builtInMap.put("long", Long.TYPE );
        builtInMap.put("double", Double.TYPE );
        builtInMap.put("float", Float.TYPE );
        builtInMap.put("bool", Boolean.TYPE );
        builtInMap.put("char", Character.TYPE );
        builtInMap.put("byte", Byte.TYPE );
        builtInMap.put("void", Void.TYPE );
        builtInMap.put("short", Short.TYPE );
    }

    private boolean isArrayListTypePrimitive(Type t) throws IOException {
        String typeName = extractTypeNameFromArrayList(t);
        System.out.println("Extracted type name from ArrayList: " + typeName);

        try {
            //Do not try to call Class.forName(primitive) -- that would throw an exception
            if(builtInMap.containsKey(typeName)) return true;
            return isPrimitive(Class.forName(typeName));
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException("Unable to retrieve class from generic", e);
        }
    }

    private String extractTypeNameFromArrayList(Type t) throws IOException {
        String typeName = t.getTypeName();
        if(!typeName.startsWith("java.util.ArrayList<? extends "))
        {
            throw new IOException("Illegal argument encountered while interpreting type parameter");
        }
        //last space is where we want to cut off (right after the "extends"), as well as removing the last closing braces
        return typeName.substring(typeName.lastIndexOf(" ") + 1, typeName.length() - 1);
    }

    private boolean isPrimitive(Class<?> input)
    {
        //TODO: collection, (but not Map? May not matter, as we excluded the "properties" method)

        //Collections are not simple
        if(Collection.class.isAssignableFrom(input))
        {
            System.out.println("Encountered collection in isPrimitive. Use isArrayListTypePrimitive instead");
            return false;
        }

        //check for: plain/typed literal, XMLGregorianCalendar, byte[], RdfResource
        //covers int, long, short, float, double, boolean, byte
        if(input.isPrimitive()) return true;

        return (URI.class.isAssignableFrom(input) ||
                String.class.isAssignableFrom(input) ||
                XMLGregorianCalendar.class.isAssignableFrom(input) ||
                TypedLiteral.class.isAssignableFrom(input) ||
                BigInteger.class.isAssignableFrom(input) ||
                byte[].class.isAssignableFrom(input) ||
                Duration.class.isAssignableFrom(input) ||
                RdfResource.class.isAssignableFrom(input));
    }

    public <T> T parseMessage(String message, Class<T> targetClass) throws IOException {
        Model model = MessageParser.readMessage(message);

        ArrayList<Class<?>> implementingClasses = MessageParser.getImplementingClasses(targetClass);

        String queryString = "SELECT ?id ?type { ?id a ?type . }";
        Query query = QueryFactory.create(queryString);
        QueryExecution queryExecution = QueryExecutionFactory.create(query, model);
        ResultSet resultSet = queryExecution.execSelect();

        if(!resultSet.hasNext())
        {
            throw new IOException("Could not extract class from input message");
        }

        Class<?> returnClass = null;
        String returnId = null;

        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.nextSolution();
            String fullName = solution.get("type").toString();
            String className = fullName.substring(fullName.lastIndexOf('/') + 1);

            for(Class<?> currentClass : implementingClasses)
            {
                if(currentClass.getSimpleName().equals(className + "Impl"))
                {
                    returnId = solution.get("id").toString();
                    returnClass = currentClass;
                    System.out.println("Found implementing class: " + currentClass.getSimpleName());
                    break;
                }
            }
            if(returnClass != null) break;
        }

        if(returnClass == null)
        {
            throw new NullPointerException("Could not determine an appropriate implementing class for " + targetClass.getName());
        }

        //At this point, we parsed the model and know to which implementing class we want to parse


        return (T)handleObject(model, returnId, returnClass);

    }

    /**
     * Reads a message into an Apache Jena model.
     * If the class was not previously initialized, it will automatically initialize upon this function call.
     * @param message Message to be read
     * @return The model of the message plus ontology
     */
    public static Model readMessage(String message) {

        Model targetModel = JenaUtil.createMemoryModel();

        //Read incoming message to the same model

        RDFDataMgr.read(targetModel, new ByteArrayInputStream(message.getBytes()), RDFLanguages.JSONLD);

        return targetModel;
    }

    public static Model readMessageAndOntology(String message) throws IOException {

        //Copy ontology model
        Model messageModel = JenaUtil.createMemoryModel();

        //Read incoming message to the same model
        RDFParser.create()
                .source(new ByteArrayInputStream(message.getBytes()))
                .lang(Lang.JSONLD)
                .errorHandler(ErrorHandlerFactory.getDefaultErrorHandler())
                .parse(messageModel.getGraph());
        return messageModel;
    }

    public static ArrayList<Class<?>> getImplementingClasses(Class<?> someClass)
    {
        ArrayList<Class<?>> result = new ArrayList<>();
        JsonSubTypes subTypeAnnotation = someClass.getAnnotation(JsonSubTypes.class);
        if(subTypeAnnotation != null) {
            JsonSubTypes.Type[] types = subTypeAnnotation.value();
            for(JsonSubTypes.Type type : types)
            {
                result.addAll(getImplementingClasses(type.value()));
            }
        }
        if(!someClass.isInterface())
            result.add(someClass);
        return result;
    }

}
