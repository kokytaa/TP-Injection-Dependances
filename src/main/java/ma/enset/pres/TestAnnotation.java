package ma.enset.pres;

import ma.enset.framework.context.AnnotationApplicationContext;
import ma.enset.metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class TestAnnotation {
    public static void main(String[] args) throws Exception {
        ApplicationContext context =
                new AnnotationConfigApplicationContext("ma.enset.dao", "ma.enset.metier");
        IMetier metier = context.getBean(IMetier.class);

        System.out.println("Résultat Annotation = " + metier.calcul());

    }}
