package ma.enset.pres;



import ma.enset.dao.IDao;
import ma.enset.metier.IMetier;

import java.io.InputStream;
import java.util.Scanner;

public class presentation2Dynamique {

    public static void main(String[] args) throws Exception {

        InputStream input =
                presentation2Dynamique.class.getClassLoader()
                        .getResourceAsStream("config.txt");

        Scanner scanner = new Scanner(input);

        String daoClassName = scanner.nextLine();
        Class<?> cDao = Class.forName(daoClassName);
        IDao dao = (IDao) cDao.getDeclaredConstructor().newInstance();

        String metierClassName = scanner.nextLine();
        Class<?> cMetier = Class.forName(metierClassName);
        IMetier metier = (IMetier)
                cMetier.getConstructor(IDao.class)
                        .newInstance(dao);

        System.out.println("Résultat = " + metier.calcul());
    }
}