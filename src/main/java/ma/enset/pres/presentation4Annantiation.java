package ma.enset.pres;

import ma.enset.metier.IMetier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class presentation4Annantiation {
    public static void main(String[] args) {
        ApplicationContext context =
                new AnnotationConfigApplicationContext("ma.enset.dao", "ma.enset.metier");
        IMetier metier = context.getBean(IMetier.class);

        System.out.println("Résultat = " + metier.calcul());
    }
}