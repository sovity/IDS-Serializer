package de.fraunhofer.iais.eis.ids.jsonld.util;

import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MixinHelper {

    private static final Logger logger = LoggerFactory.getLogger(MixinHelper.class);

    private static final String MIXIN_SUFFIX = "Mixin";

    private static final String PACKAGE_NAME = "de.fraunhofer.iais.eis";

    private static final String MIXIN_PACKAGE_SUFFIX = ".mixins";

    public static Map<Class<?>, Class<?>> scanMixins() {
        ScanResult modelScan = new ClassGraph()
                .enableClassInfo()
                .acceptPackagesNonRecursive(PACKAGE_NAME)
                .scan();

        ScanResult mixinScan = new ClassGraph()
                .enableClassInfo()
                .acceptPackagesNonRecursive(PACKAGE_NAME + MIXIN_PACKAGE_SUFFIX)
                .scan();
        Map<Class<?>, Class<?>> mixins = new HashMap<>();
        mixinScan.getAllClasses()
                .filter(x -> x.getSimpleName().endsWith(MIXIN_SUFFIX))
                .loadClasses()
                .forEach(x -> {
                    String modelClassName = x.getSimpleName().substring(0, x.getSimpleName().length() - MIXIN_SUFFIX.length());
                    ClassInfoList modelClassInfos = modelScan.getAllClasses().filter(y -> Objects.equals(y.getSimpleName(), modelClassName));

                    if (modelClassInfos.isEmpty()) {
                        logger.warn("could not auto-resolve target class for mixin '{}'", x.getSimpleName());
                    } else {
                        mixins.put(modelClassInfos.get(0).loadClass(), x);
//                        logger.info("using mixin '{}' for class '{}'",
//                                x.getSimpleName(),
//                                modelClassInfos.get(0).getName());
                    }
                });
        return mixins;
    }

    private MixinHelper(){}
}
