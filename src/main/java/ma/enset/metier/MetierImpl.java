package ma.enset.metier;

import ma.enset.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MetierImpl implements IMetier {
    @Autowired
    private IDao dao;// injection via field

    // Injection par constructeur
    public MetierImpl(IDao dao) {
        this.dao = dao;
    }
    @Autowired  // OU via setter
    public void setDao(IDao dao) { this.dao = dao; }
    public MetierImpl() {
        // Optionnel : initialisation par défaut
    }
    public double calcul() {
        double data = dao.getData();
        return data * 2;
    }
}
