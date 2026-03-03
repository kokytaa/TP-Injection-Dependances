package ma.enset.pres;
import ma.enset.framework.context.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import ma.enset.framework.context.ClassPathXmlApplicationContext;
import ma.enset.metier.IMetier;
import org.springframework.context.annotation.Bean;


import java.io.File;

public class testXml {
        public static void main(String[] args) throws Exception {
            ApplicationContext ctx = new ClassPathXmlApplicationContext("config.xml");
            IMetier metier = (IMetier) ctx.getBean("metier");
            System.out.println("Résultat = " + metier.calcul());

        }
    }
