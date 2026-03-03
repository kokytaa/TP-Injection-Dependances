package ma.enset.framework.context;

public interface ApplicationContext {
    Object getBean(String beanName) throws Exception;
}