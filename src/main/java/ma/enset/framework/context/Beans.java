package ma.enset.framework.context;

import jakarta.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "beans")
@XmlAccessorType(XmlAccessType.FIELD)
public class Beans {

    @XmlElement(name = "bean")
    private List<Bean> beans;

    public List<Bean> getBeans() { return beans; }
}