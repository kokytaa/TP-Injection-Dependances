package ma.enset.framework.context;


import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Property {

    @XmlAttribute(name="name")
    private String name;

    @XmlAttribute(name="ref")
    private String ref;

    public String getName() { return name; }
    public String getRef() { return ref; }
}
