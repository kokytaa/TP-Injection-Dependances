package ma.enset.metier;

import ma.enset.dao.IDao;

public class MetierImpl {
    private IDao dao;

    // Injection par constructeur
    public MetierImpl(IDao dao) {
        this.dao = dao;
    }
    public double calcul() {
        double data = dao.getData();
        return data * 2;
    }
}
