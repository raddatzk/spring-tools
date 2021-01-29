package com.raddatzk.spring.boot;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This {@link org.springframework.boot.env.EnvironmentPostProcessor} enables you to inject
 * additional config files into the environment.
 * <p>
 * This processor gets loaded right after the {@link ConfigDataEnvironmentPostProcessor} ({@value DEFAULT_ORDER})
 * and injects it's own {@link org.springframework.core.env.PropertySource} at the begin of the environments property
 * sources. It then behaves like {@link ConfigDataEnvironmentPostProcessor}, which basically searches for
 * CONFIG_NAME_PROPERTY, resolves the corresponding files and adds the specified properties to the property
 * sources of the environment in
 * {@link #postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application)}.
 * In the end the injected {@link org.springframework.core.env.PropertySource} will be removed, as it might cause
 * following PostProcessors to read the wrong CONFIG_NAME_PROPERTY.
 * <p>
 * The resolved {@link AdditionalConfigDataLoader} will be ordered by
 * {@link org.springframework.core.annotation.AnnotationAwareOrderComparator} which means you can change the order of
 * the added configs by implementing {@link Ordered}.
 *
 * To use this {@link org.springframework.boot.env.EnvironmentPostProcessor} you first need to implement
 * {@link AdditionalConfigDataLoader}:
 * <pre class="code">
 * class ExampleConfigDataLoader implements AdditionalConfigDataLoader, Ordered {
 *
 *     &#064;Override
 *     public String useAdditionalConfig() {
 *         return "example";
 *     }
 *
 *     &#064;Override
 *     public int getOrder() {
 *         return 0;
 *     }
 * }
 * </pre>
 * <p>
 * Then you should create a file `spring.factories` in your `resources/META-INF` directory and register your
 * {@link AdditionalConfigDataLoader} like this:
 * <pre class="code">
 * com.raddatzk.spring.boot.AdditionalConfigDataLoader=com.example.ExampleConfigDataLoader
 * </pre>
 */
public class AdditionalConfigDataEnvironmentProcessor extends ConfigDataEnvironmentPostProcessor implements Ordered {

    public static final int DEFAULT_ORDER = ConfigDataEnvironmentPostProcessor.ORDER + 1;
    static final String CONFIG_NAME_PROPERTY = "spring.config.name";
    static final String ADDITIONAL_CONFIG_PROPERTY_SOURCE = "AdditionalConfigPropertySource";

    public AdditionalConfigDataEnvironmentProcessor(DeferredLogFactory logFactory, ConfigurableBootstrapContext bootstrapContext) {
        super(logFactory, bootstrapContext);
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        resolveAndInjectConfigNames(environment, application);
        cleanup(environment);
    }

    private void resolveAndInjectConfigNames(ConfigurableEnvironment environment, SpringApplication application) {
        List<AdditionalConfigDataLoader> additionalConfigDataLoaders = getAdditionalConfigDataLoaders();
        String additionalConfigs = buildConfigString(additionalConfigDataLoaders);
        environment.getPropertySources().addFirst(new MapPropertySource(ADDITIONAL_CONFIG_PROPERTY_SOURCE, Collections.singletonMap(CONFIG_NAME_PROPERTY, additionalConfigs)));
        super.postProcessEnvironment(environment, application);
    }

    private List<AdditionalConfigDataLoader> getAdditionalConfigDataLoaders() {
        return SpringFactoriesLoader.loadFactories(AdditionalConfigDataLoader.class, null);
    }

    private String buildConfigString(List<AdditionalConfigDataLoader> additionalConfigDataLoaders) {
        return additionalConfigDataLoaders.stream()
                .map(AdditionalConfigDataLoader::useAdditionalConfig)
                .collect(Collectors.joining(","));
    }

    private void cleanup(ConfigurableEnvironment environment) {
        environment.getPropertySources().remove(ADDITIONAL_CONFIG_PROPERTY_SOURCE);
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }
}
