package ma.enset.framework.context;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Bean {

    @XmlAttribute
    private String id;

    @XmlAttribute(name = "class")
    private String className;
    @XmlElement(name="property")
    private Property property;
    public Property getProperty() {
        return property;
    }

    public String getId() { return id; }
    public String getClassName() { return className; }
}