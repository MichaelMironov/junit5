package org.example.extensions.injector;

import org.example.service.Inject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import java.lang.reflect.Field;
public class InjectExtension implements TestInstancePostProcessor {
    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        System.out.println("Test Instance Post Processor");
        Field[] fields = testInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if(field.isAnnotationPresent(Inject.class)){
                field.setAccessible(true);
                field.set(testInstance, field.getType().newInstance());
            }
        }
    }
}
