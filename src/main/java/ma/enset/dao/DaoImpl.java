package ma.enset.dao;

public class DaoImpl implements IDao {
    public double getData() {
        return Math.random() * 100;
    }
}
