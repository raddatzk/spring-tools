package com.raddatzk.spring.boot;

/**
 * To use {@link AdditionalConfigDataEnvironmentProcessor} you first need to implement
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
public interface AdditionalConfigDataLoader {

    String useAdditionalConfig();
}
