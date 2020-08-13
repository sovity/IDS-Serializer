package de.fraunhofer.iais.eis.ids.jsonld;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.system.ErrorHandlerFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.spin.util.JenaUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MessageParser {

    private static Model ontologyModel = null;

    public static boolean downloadOntology = false;


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
     * @throws IOException if the ontology could not be retrieved
     */
    //TODO: Slow!
    public static Model readMessage(String message) throws IOException {

        Model targetModel = JenaUtil.createMemoryModel();

        //Read incoming message to the same model
        RDFParser.create()
                .source(new ByteArrayInputStream(message.getBytes()))
                .lang(Lang.JSONLD)
                .errorHandler(ErrorHandlerFactory.getDefaultErrorHandler())
                .parse(targetModel.getGraph());
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
