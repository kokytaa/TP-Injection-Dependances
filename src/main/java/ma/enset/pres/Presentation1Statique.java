package ma.enset.pres;

import ma.enset.dao.DaoImpl;
import ma.enset.dao.IDao;
import ma.enset.metier.IMetier;
import ma.enset.metier.MetierImpl;

public class Presentation1Statique {
    public static void main(String[] args) {
        IDao dao = new DaoImpl();
        IMetier metier = new MetierImpl(dao);
        System.out.println("Résultat = " + metier.calcul());
    }
}
