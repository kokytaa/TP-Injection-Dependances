package ma.enset.metier;

import ma.enset.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetierImpl implements IMetier {
    @Autowired
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
