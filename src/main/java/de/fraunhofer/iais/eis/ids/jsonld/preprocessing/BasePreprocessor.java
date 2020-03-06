package de.fraunhofer.iais.eis.ids.jsonld.preprocessing;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.IOException;
import java.io.StringReader;

/**
 * basic implementation of {@code JsonPreprocessor} that encapsulates validation.
 * By default, validation is disabled for performance reasons (@context has to be downloaded each time).
 */
public abstract class BasePreprocessor implements JsonPreprocessor {

    private boolean validate = false;


    @Override
    public final String preprocess(String input) throws IOException {
        String result = preprocess_impl(input);
        if(validate) {
            Rio.parse(new StringReader(result), null, RDFFormat.JSONLD); // try to parse transformed result
        }
        return result;
    }

    abstract String preprocess_impl(String input) throws IOException;

    @Override
    public void enableRDFValidation(boolean validate) {
        this.validate = validate;
    }
}
