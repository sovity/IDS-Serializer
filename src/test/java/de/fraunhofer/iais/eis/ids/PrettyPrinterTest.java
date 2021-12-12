package de.fraunhofer.iais.eis.ids;

import de.fraunhofer.iais.eis.Person;
import de.fraunhofer.iais.eis.PersonBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


/**
 * This test class aims to demonstrate current problems with the Infomodel serializer
 *
 * First relating issue: https://github.com/International-Data-Spaces-Association/Java-Representation-of-IDS-Information-Model/issues/12
 *
 * While the ObjectMapper from jackson itself is thread-safe, pretty print seems to have problems with extended children.
 * 3 year old Thread: https://groups.google.com/g/jackson-user/c/TTBmwZ_2HaM
 *
 * Jackson Doc mentioning the Instantiatable Interface to avoid corruption: https://fasterxml.github.io/jackson-core/javadoc/2.12/com/fasterxml/jackson/core/PrettyPrinter.html
 * This might be the case with JsonLDSerializer extending BeanSerializer.
 *
 * @author j.schneider@isst.fraunhofer.de
 */
public class PrettyPrinterTest {

    static Serializer serializer = new Serializer();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * This test will fail in most cases.
     * Demonstrating, that the "@context" field necessary for compact json-ld deserialization is missing (sometimes).
     *
     * On some machines a higher loop count is needed, dependent on the scheduler.
     */
    @Test
    public void parallelContextMissing() throws ExecutionException, InterruptedException {
        Person infoModelObject = new PersonBuilder()._familyName_("Datenschmidt").build();

        Runnable serFunction = () -> {
            try {
                for (int i = 0; i < 50; i++) {
                    String idsJson = serializer.serialize(infoModelObject);
                    Assert.assertTrue(idsJson.contains("@context")); // "If failed, Context is missing!"
                }
            } catch (IOException e) {
                logger.error("Ser error in test:", e);
            }
        };

        Runnable mapperFunction = () -> {
            for (int i = 0; i < 50; i++) {
                logger.info("Test {}", infoModelObject);
            }

        };

        CompletableFuture future1 = CompletableFuture.runAsync(serFunction);
        CompletableFuture future2 = CompletableFuture.runAsync(mapperFunction);

        // gather the threads so we get the output
        CompletableFuture combined = CompletableFuture.allOf(future1, future2);
        combined.get();
        Assert.assertTrue(combined.isDone());
    }
}