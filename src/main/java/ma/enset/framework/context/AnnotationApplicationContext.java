package ma.enset.framework.context;

import ma.enset.framework.annotation.Autowired;
import ma.enset.framework.annotation.Component;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AnnotationApplicationContext implements ApplicationContext {

    private Map<String, Object> beansMap = new HashMap<>();

    public AnnotationApplicationContext(String packageName) throws Exception {
        // 1. Scanner le package pour trouver les @Component
        String path = packageName.replace(".", "/");
        URL url = ClassLoader.getSystemResource(path);
        File dir = new File(url.getFile());

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                        + file.getName().replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (cls.isAnnotationPresent(Component.class)) {
                    Component comp = cls.getAnnotation(Component.class);
                    String beanId = comp.value().isEmpty()
                            ? cls.getSimpleName() : comp.value();
                    Object instance = cls.getDeclaredConstructor().newInstance();
                    beansMap.put(beanId, instance);
                }
            }
        }

        // 2. Injection des dépendances
        for (Object instance : beansMap.values()) {

            // Via Setter
            for (Method method : instance.getClass().getMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object dep = findBeanByType(paramType);
                    if (dep != null) method.invoke(instance, dep);
                }
            }

            // Via Field
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dep = findBeanByType(field.getType());
                    if (dep != null) {
                        field.setAccessible(true);
                        field.set(instance, dep);
                    }
                }
            }
        }
    }

    private Object findBeanByType(Class<?> type) {
        for (Object bean : beansMap.values()) {
            if (type.isAssignableFrom(bean.getClass())) return bean;
        }
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return beansMap.get(beanName);
    }
}