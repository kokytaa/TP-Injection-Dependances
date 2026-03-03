package ma.enset.framework.context;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import ma.enset.framework.annotation.Autowired;

import java.io.File;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.Map;

public class ClassPathXmlApplicationContext implements ApplicationContext {

    private Map<String, Object> beansMap = new HashMap<>();

    public ClassPathXmlApplicationContext(String xmlFile) throws Exception {

        // 1️⃣ JAXB Parsing
        JAXBContext context = JAXBContext.newInstance(Beans.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        Beans beans = (Beans) unmarshaller.unmarshal(
                new File(ClassLoader.getSystemResource(xmlFile).getFile())
        );

        // 2️⃣ Instanciation des beans
        for (Bean bean : beans.getBeans()) {

            Class<?> clazz = Class.forName(bean.getClassName());

            Object instance = createInstanceWithConstructorInjection(clazz);

            beansMap.put(bean.getId(), instance);
        }

        // 3️⃣ Injection via <property> XML
        for (Bean bean : beans.getBeans()) {

            if (bean.getProperty() != null) {

                Object instance = beansMap.get(bean.getId());

                String propertyName = bean.getProperty().getName();
                String ref = bean.getProperty().getRef();

                Object dependency = beansMap.get(ref);

                Field field = instance.getClass().getDeclaredField(propertyName);
                field.setAccessible(true);
                field.set(instance, dependency);
            }
        }

        // 4️⃣ Injection via annotations
        for (Object bean : beansMap.values()) {

            // Field injection
            for (Field field : bean.getClass().getDeclaredFields()) {

                if (field.isAnnotationPresent(Autowired.class)) {

                    Object dependency = findBeanByType(field.getType());

                    field.setAccessible(true);
                    field.set(bean, dependency);
                }
            }

            // Setter injection
            for (Method method : bean.getClass().getMethods()) {

                if (method.isAnnotationPresent(Autowired.class)
                        && method.getName().startsWith("set")) {

                    Object dependency = findBeanByType(
                            method.getParameterTypes()[0]);

                    method.invoke(bean, dependency);
                }
            }
        }
    }

    // 🔥 Constructor injection
    private Object createInstanceWithConstructorInjection(Class<?> clazz) throws Exception {

        for (Constructor<?> constructor : clazz.getConstructors()) {

            if (constructor.isAnnotationPresent(Autowired.class)) {

                Class<?>[] paramTypes = constructor.getParameterTypes();
                Object[] params = new Object[paramTypes.length];

                for (int i = 0; i < paramTypes.length; i++) {
                    params[i] = findBeanByType(paramTypes[i]);
                }

                return constructor.newInstance(params);
            }
        }

        return clazz.getDeclaredConstructor().newInstance();
    }

    private Object findBeanByType(Class<?> type) {
        for (Object bean : beansMap.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return beansMap.get(beanName);
    }
}