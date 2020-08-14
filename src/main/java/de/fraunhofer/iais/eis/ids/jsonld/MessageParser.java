package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.spin.util.JenaUtil;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class MessageParser {

    private static Model ontologyModel = null;

    public static boolean downloadOntology = false;

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

    public <T> T parseMessage(String message, Class<T> targetClass) throws IOException {
        init();
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
            returnId = solution.get("id").toString();
            String className = fullName.substring(fullName.lastIndexOf('/') + 1);

            for(Class<?> currentClass : implementingClasses)
            {
                if(currentClass.getSimpleName().equals(className + "Impl"))
                {
                    returnClass = currentClass;
                    System.out.println("Found implementing class: " + currentClass.getSimpleName());
                }
            }
        }
        try {
            //T returnObject = (T) returnClass.getConstructor().setAccessible(true).newInstance();

            Constructor<T> constructor = (Constructor<T>) returnClass.getDeclaredConstructor();
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

            StringBuilder queryStringBuilder = new StringBuilder();
            queryStringBuilder.append("PREFIX ids: <https://w3id.org/idsa/core/>\nSELECT");
            methodMap.forEach((key1, value) -> queryStringBuilder.append(" ?").append(key1));
            queryStringBuilder.append(" { ");


            for(Map.Entry<String, Method> entry : methodMap.entrySet())
            {
                //Is this a field which is annotated by NOT NULL?
                boolean notNull = returnClass.getDeclaredField("_" + entry.getKey()).isAnnotationPresent(NotNull.class);

                //If not, we need to make this optional
                if(!notNull)
                {
                    queryStringBuilder.append(" OPTIONAL { ");
                }
                queryStringBuilder.append("<").append(returnId).append("> ") //subject
                        .append("ids:").append(entry.getKey()) //predicate
                        .append(" ?").append(entry.getKey()).append(" . "); //object
                if(!notNull)
                {
                    queryStringBuilder.append("} ");
                }
            }


            queryStringBuilder.append(" } ");
            //TODO: GROUP BY and GROUP_CONCAT
            //TODO: If a "complex object" is expected, do something similar as this, just without parsing
            queryString = queryStringBuilder.toString();

            System.out.println(queryString);

            //Copy the ontology model
            Model combinedModel = ontologyModel;

            query = QueryFactory.create(queryString);
            queryExecution = QueryExecutionFactory.create(query, combinedModel);
            resultSet = queryExecution.execSelect();
            //Add the message model to it - prevents additional parsing of the message
            ontologyModel.add(model);

            return returnObject;
        }
        catch (NoSuchMethodException | NullPointerException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchFieldException e)
        {
            throw new IOException("Failed to instantiate desired class", e);
        }
    }


    /**
     * This function initialized the MessageParser class. It will be called automatically upon being requested to read a message.
     * You can manually call this function on startup to avoid a delay later on.
     * Note that you can choose whether to download the ontology or from resources via the downloadOntology static variable.
     * @throws IOException if the ontology cannot be loaded
     */
    public static void init() throws IOException {
        //Check if ontology model exists yet
        if(ontologyModel != null)
        {
            return;
        }

        //Does not exist. Initialize
        ontologyModel = JenaUtil.createMemoryModel();

        //Load ontology
        InputStream inputStream;

        //From web or from local file?
        if(downloadOntology) {
            //try to download from GitHub
            URL url;
            try {

                //TODO
                url = new URL("https://github.com/International-Data-Spaces-Association/InformationModel/releases/download/v4.0.0/IDS-InformationModel-v4.0.0.ttl");
            }
            catch (MalformedURLException e)
            {
                throw new IOException("Failed to download ontology.", e);
            }
            inputStream = url.openConnection().getInputStream();
        }
        else {
            //Load from local file
            inputStream = MessageParser.class.getResourceAsStream("ontology.ttl");
        }
        //Pipe the input stream (whether it is from web or local file) into Apache Jena model
        ontologyModel.read(inputStream, null, FileUtils.langTurtle);
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

        //Make sure ontology is initialized (does nothing if already initialized)
        init();

        //Copy ontology model
        Model combinedModel = ontologyModel;

        //Read incoming message to the same model
        RDFParser.create()
                .source(new ByteArrayInputStream(message.getBytes()))
                .lang(Lang.JSONLD)
                .errorHandler(ErrorHandlerFactory.getDefaultErrorHandler())
                .parse(combinedModel.getGraph());
        return combinedModel;
    }

    //TODO to be deleted
    public <T> void getDeclaredFields(Class<T> bean)
    {
        Field[] fields = bean.getDeclaredFields();
        if(bean.isInterface())
        {
            System.out.println("Note that interfaces have no fields.");
        }
        else
        {
            System.out.println("Num fields: " + fields.length);
        }
        for(Field field : fields)
        {
            System.out.println(field.getName());
        }

        Method[] methods = bean.getMethods();
        for(Method method : methods) {
            System.out.println(method.getReturnType().getName() + " " + method.getName());
        }

        List<String> methodNames = Arrays.stream(methods).filter(method -> {
            String name = method.getName();
            //Filter out irrelevant methods
            return name.startsWith("get") && !name.equals("getProperties") && !name.equals("getComment") && !name.equals("getLabel");
        }).map(method -> {
            //Remove "get" part
            String reducedName = method.getName().substring(3);

            //Turn first character to lower case
            char[] c = reducedName.toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            return new String(c);
        }).collect(Collectors.toList());

        System.out.println("METHOD NAMES");
        methodNames.forEach(System.out::println);


        System.out.println("IMPLEMENTING CLASSES");
        ArrayList<Class<?>> implementingClasses = getImplementingClasses(bean);
        for(Class<?> impl : implementingClasses)
        {
            System.out.println(impl.getName());
        }

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
