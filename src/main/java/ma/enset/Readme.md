# TP1 JEE — Injection des Dépendances & Mini Framework IOC

## Table des Matières
1. [Introduction](#introduction)
2. [Partie 1 — Injection des Dépendances avec Spring](#partie-1)
    - [IDao & DaoImpl](#idao)
    - [IMetier & MetierImpl](#imetier)
    - [Injection Statique](#statique)
    - [Injection Dynamique](#dynamique)
    - [Spring XML](#spring-xml)
    - [Spring Annotations](#spring-annotations)
3. [Partie 2 — Mini Framework IOC](#partie-2)
    - [Architecture](#architecture)
    - [Annotations](#annotations)
    - [Interface ApplicationContext](#interface)
    - [Version XML (JAXB)](#version-xml)
    - [Version Annotations](#version-annotations)
    - [Explication Détaillée du Code](#explication)
4. [Configuration pom.xml](#pom)
5. [Tests & Résultats](#tests)

---

## Introduction <a name="introduction"></a>

Ce projet implémente le principe d'**Injection des Dépendances (IoC)** en Java JEE.

> **Couplage faible** = une classe ne connaît ses dépendances qu'à travers des **interfaces**, jamais des classes concrètes. Cela permet de changer l'implémentation sans toucher au code métier.

**Problème sans IoC :**
```java
// Couplage FORT — difficile à changer, tester ou maintenir
public class MetierImpl {
    private DaoImpl dao = new DaoImpl(); // dépend de la classe concrète
}
```

**Solution avec IoC :**
```java
// Couplage FAIBLE — dépend uniquement de l'interface
public class MetierImpl implements IMetier {
    private IDao dao; // on ne sait pas quelle implémentation sera injectée
}
```

---

## Partie 1 — Injection des Dépendances avec Spring <a name="partie-1"></a>

### 1. IDao & DaoImpl <a name="idao"></a>

**`IDao.java`** — Interface de la couche d'accès aux données :
```java
package ma.enset.dao;

public interface IDao {
    double getData();
}
```

**`DaoImpl.java`** — Implémentation qui simule une source de données :
```java
package ma.enset.dao;

import org.springframework.stereotype.Component;

@Component("dao")
public class DaoImpl implements IDao {
    @Override
    public double getData() {
        // Simule une lecture depuis une base de données
        return 42.0;
    }
}
```

---

### 2. IMetier & MetierImpl <a name="imetier"></a>

**`IMetier.java`** — Interface métier :
```java
package ma.enset.metier;

public interface IMetier {
    double calcul();
}
```

**`MetierImpl.java`** — Implémentation avec couplage faible :
```java
package ma.enset.metier;

import ma.enset.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("metier")
public class MetierImpl implements IMetier {

    @Autowired
    private IDao dao; // couplage faible : dépend de l'interface, pas de DaoImpl

    @Override
    public double calcul() {
        double data = dao.getData();
        return data * 2; // logique métier
    }

    public void setDao(IDao dao) {
        this.dao = dao;
    }
}
```

---

### 3. Injection Statique <a name="statique"></a>

L'injection est faite **manuellement dans le code** :
```java
package ma.enset.pres;

import ma.enset.dao.DaoImpl;
import ma.enset.metier.MetierImpl;

public class Presentation1Static {
    public static void main(String[] args) {
        // On crée manuellement les objets et on les lie
        DaoImpl dao = new DaoImpl();
        MetierImpl metier = new MetierImpl();
        metier.setDao(dao); // injection via setter
        System.out.println("Résultat static = " + metier.calcul());
    }
}
```
> **Inconvénient** : Si on veut changer `DaoImpl`, il faut modifier le code source et recompiler.

---

### 4. Injection Dynamique <a name="dynamique"></a>

L'injection se fait via un **fichier de configuration texte** et la **réflexion Java** :

**`config.txt`** :
```
ma.enset.dao.DaoImpl
ma.enset.metier.MetierImpl
```

```java
package ma.enset.pres;

import java.io.*;
import java.lang.reflect.Method;

public class Presentation2Dynamic {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("config.txt"));

        // Chargement dynamique des classes sans les connaître à la compilation
        String daoClassName = br.readLine();
        Class<?> daoClass = Class.forName(daoClassName);
        Object dao = daoClass.getDeclaredConstructor().newInstance();

        String metierClassName = br.readLine();
        Class<?> metierClass = Class.forName(metierClassName);
        Object metier = metierClass.getDeclaredConstructor().newInstance();

        // Injection via réflexion
        Method setDao = metierClass.getMethod("setDao", IDao.class);
        setDao.invoke(metier, dao);

        Method calcul = metierClass.getMethod("calcul");
        System.out.println("Résultat dynamique = " + calcul.invoke(metier));
    }
}
```
> **Avantage** : On peut changer la classe DAO dans `config.txt` sans recompiler.

---

### 5. Spring XML <a name="spring-xml"></a>

**`ApplicationContext.xml`** (dans `src/main/resources/`) :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="dao" class="ma.enset.dao.DaoImpl"/>

    <bean id="metier" class="ma.enset.metier.MetierImpl">
        <property name="dao" ref="dao"/> <!-- injection via setter -->
    </bean>
</beans>
```

```java
package ma.enset.pres;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Presentation3SpringXml {
    public static void main(String[] args) {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        IMetier metier = ctx.getBean("metier", IMetier.class);
        System.out.println("Résultat Spring XML = " + metier.calcul());
    }
}
```

---

### 6. Spring Annotations <a name="spring-annotations"></a>

```java
package ma.enset.pres;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Presentation4Annotations {
    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext("ma.enset");
        IMetier metier = ctx.getBean("metier", IMetier.class);
        System.out.println("Résultat Spring Annotations = " + metier.calcul());
    }
}
```
> Spring scanne automatiquement les classes annotées `@Component` et injecte les dépendances `@Autowired`.

---

## Partie 2 — Mini Framework IOC <a name="partie-2"></a>

### Architecture <a name="architecture"></a>

```
ma.enset.framework/
├── annotations/
│   ├── Component.java        ← Marque une classe comme bean géré
│   └── Autowired.java        ← Marque un point d'injection
├── context/
│   ├── ApplicationContext.java              ← Interface commune
│   ├── Bean.java                            ← Modèle JAXB pour <bean>
│   ├── Beans.java                           ← Modèle JAXB pour <beans>
│   ├── ClassPathXmlApplicationContext.java  ← Version XML
│   └── AnnotationApplicationContext.java    ← Version Annotations
```

---

### Annotations <a name="annotations"></a>

**`Component.java`** — Identifie une classe comme composant géré par le framework :
```java
package ma.enset.framework.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)   // visible à l'exécution (pas seulement compilation)
@Target(ElementType.TYPE)             // applicable sur une classe uniquement
public @interface Component {
    String value() default "";        // nom optionnel du bean
}
```

**`Autowired.java`** — Marque un champ, setter ou constructeur pour injection automatique :
```java
package ma.enset.framework.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface Autowired {
}
```

---

### Interface ApplicationContext <a name="interface"></a>

```java
package ma.enset.framework.context;

public interface ApplicationContext {
    Object getBean(String beanName) throws Exception;
}
```
> Interface commune aux deux versions (XML et Annotations). Le code client n'a pas besoin de savoir laquelle est utilisée.

---

### Classes JAXB (Modèles XML) <a name="version-xml"></a>

**`Bean.java`** — Représente un `<bean id="..." class="..."/>` dans le XML :
```java
package ma.enset.framework.context;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Bean {
    @XmlAttribute
    private String id;

    @XmlAttribute(name = "class")
    private String className;

    public String getId() { return id; }
    public String getClassName() { return className; }
}
```

**`Beans.java`** — Représente la balise racine `<beans>` :
```java
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
```

**`config.xml`** (dans `src/main/resources/`) :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans>
    <bean id="dao"    class="ma.enset.dao.DaoImpl"/>
    <bean id="metier" class="ma.enset.metier.MetierImpl"/>
</beans>
```

---

**`ClassPathXmlApplicationContext.java`** — Lit le XML et injecte les dépendances :
```java
package ma.enset.framework.context;

import jakarta.xml.bind.*;
import ma.enset.framework.annotations.Autowired;
import java.io.File;
import java.lang.reflect.*;
import java.util.*;

public class ClassPathXmlApplicationContext implements ApplicationContext {

    private Map<String, Object> beansMap = new HashMap<>();

    public ClassPathXmlApplicationContext(String xmlFile) throws Exception {

        // ÉTAPE 1 : Parser le fichier XML avec JAXB
        JAXBContext jaxbContext = JAXBContext.newInstance(Beans.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Beans beans = (Beans) unmarshaller.unmarshal(
            new File(ClassLoader.getSystemResource(xmlFile).getFile())
        );

        // ÉTAPE 2 : Instancier tous les beans via réflexion
        for (Bean bean : beans.getBeans()) {
            Object instance = Class.forName(bean.getClassName())
                                   .getDeclaredConstructor()
                                   .newInstance();
            beansMap.put(bean.getId(), instance);
        }

        // ÉTAPE 3 : Injection via Setter (@Autowired sur méthode set...)
        for (Bean bean : beans.getBeans()) {
            Object instance = beansMap.get(bean.getId());
            for (Method method : instance.getClass().getMethods()) {
                if (method.isAnnotationPresent(Autowired.class)
                        && method.getName().startsWith("set")) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object dep = findBeanByType(paramType);
                    if (dep != null) method.invoke(instance, dep);
                }
            }

            // ÉTAPE 4 : Injection via Field (@Autowired sur attribut)
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dep = findBeanByType(field.getType());
                    if (dep != null) {
                        field.setAccessible(true); // permet d'accéder aux champs private
                        field.set(instance, dep);
                    }
                }
            }
        }
    }

    // Cherche un bean dont le type est compatible avec le type demandé
    private Object findBeanByType(Class<?> type) {
        for (Object bean : beansMap.values()) {
            if (type.isAssignableFrom(bean.getClass())) return bean;
        }
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return beansMap.get(beanName);
    }
}
```

---

**`AnnotationApplicationContext.java`** — Scanne un package et injecte les dépendances :
```java
package ma.enset.framework.context;

import ma.enset.framework.annotations.*;
import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

public class AnnotationApplicationContext implements ApplicationContext {

    private Map<String, Object> beansMap = new HashMap<>();

    public AnnotationApplicationContext(String packageName) throws Exception {

        // ÉTAPE 1 : Scanner le package pour trouver les classes @Component
        String path = packageName.replace(".", "/");
        URL url = ClassLoader.getSystemResource(path);
        File dir = new File(url.getFile());

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "."
                    + file.getName().replace(".class", "");
                Class<?> cls = Class.forName(className);

                if (cls.isAnnotationPresent(Component.class)) {
                    Component comp = cls.getAnnotation(Component.class);
                    // Si @Component("monNom") → utilise "monNom", sinon prend le nom de classe
                    String beanId = comp.value().isEmpty()
                        ? cls.getSimpleName() : comp.value();
                    Object instance = cls.getDeclaredConstructor().newInstance();
                    beansMap.put(beanId, instance);
                }
            }
        }

        // ÉTAPE 2 : Injection des dépendances pour chaque bean trouvé
        for (Object instance : beansMap.values()) {

            // Injection via Setter
            for (Method method : instance.getClass().getMethods()) {
                if (method.isAnnotationPresent(Autowired.class)) {
                    Class<?> paramType = method.getParameterTypes()[0];
                    Object dep = findBeanByType(paramType);
                    if (dep != null) method.invoke(instance, dep);
                }
            }

            // Injection via Field
            for (Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object dep = findBeanByType(field.getType());
                    if (dep != null) {
                        field.setAccessible(true);
                        field.set(instance, dep);
                    }
                }
            }
        }
    }

    private Object findBeanByType(Class<?> type) {
        for (Object bean : beansMap.values()) {
            if (type.isAssignableFrom(bean.getClass())) return bean;
        }
        return null;
    }

    @Override
    public Object getBean(String beanName) {
        return beansMap.get(beanName);
    }
}
```

---

### Explication Détaillée du Code <a name="explication"></a>

#### Version XML — Les 4 étapes

```
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 1 : JAXB lit config.xml et crée des objets Java (Beans)  │
│                                                                  │
│  config.xml ──JAXB──► Beans { List<Bean> }                      │
│  <bean id="dao" class="DaoImpl"/>  ──►  Bean{id="dao",          │
│                                              className="DaoImpl"}│
└─────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 2 : Réflexion Java instancie chaque bean                  │
│                                                                  │
│  Class.forName("ma.enset.dao.DaoImpl")                          │
│       .getDeclaredConstructor().newInstance()                    │
│  → Crée un objet DaoImpl sans écrire new DaoImpl()              │
│                                                                  │
│  Résultat : beansMap = { "dao" → DaoImpl@x1,                    │
│                          "metier" → MetierImpl@x2 }             │
└─────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 3 : Injection via Setter                                  │
│                                                                  │
│  Pour MetierImpl, on cherche les méthodes annotées @Autowired   │
│  On trouve setDao(IDao dao)                                      │
│  On cherche dans beansMap un objet de type IDao → trouve DaoImpl│
│  On appelle : metier.setDao(dao) via réflexion                  │
└─────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 4 : Injection via Field (attribut direct)                 │
│                                                                  │
│  Pour MetierImpl, on cherche les champs annotés @Autowired      │
│  On trouve private IDao dao                                      │
│  field.setAccessible(true) → autorise l'accès au champ private  │
│  field.set(metier, dao) → injecte directement l'attribut        │
└─────────────────────────────────────────────────────────────────┘
```

#### Version Annotations — Les 2 étapes

```
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 1 : Scanner le package                                    │
│                                                                  │
│  On liste tous les fichiers .class du package donné             │
│  Pour chaque classe, on vérifie si @Component est présent       │
│  Si oui → on instancie et on stocke dans beansMap               │
│                                                                  │
│  @Component("dao") DaoImpl → beansMap.put("dao", new DaoImpl()) │
└─────────────────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────────────────┐
│  ÉTAPE 2 : Injection (même logique que version XML)              │
│                                                                  │
│  Pour chaque bean dans beansMap :                                │
│    → Cherche @Autowired sur les setters  → injecte              │
│    → Cherche @Autowired sur les fields   → injecte              │
└─────────────────────────────────────────────────────────────────┘
```

#### Rôle de `findBeanByType()`

```java
// Cette méthode cherche un bean compatible avec un type donné
// Exemple : on cherche un bean de type IDao
// DaoImpl implements IDao → isAssignableFrom retourne true
// → on retourne l'instance de DaoImpl

private Object findBeanByType(Class<?> type) {
    for (Object bean : beansMap.values()) {
        if (type.isAssignableFrom(bean.getClass())) return bean;
    }
    return null;
}
```

---

## Configuration pom.xml <a name="pom"></a>

```xml
<dependencies>

    <!-- Spring Context (Partie 1) -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-context</artifactId>
        <version>6.1.0</version>
    </dependency>

    <!-- JAXB API (Partie 2 — Version XML) -->
    <dependency>
        <groupId>jakarta.xml.bind</groupId>
        <artifactId>jakarta.xml.bind-api</artifactId>
        <version>3.0.1</version>
    </dependency>

    <!-- JAXB Runtime -->
    <dependency>
        <groupId>com.sun.xml.bind</groupId>
        <artifactId>jaxb-impl</artifactId>
        <version>3.0.1</version>
    </dependency>

</dependencies>
```

---

## Tests & Résultats <a name="tests"></a>

### Test Version XML
```java
ApplicationContext ctx = new ClassPathXmlApplicationContext("config.xml");
IMetier metier = (IMetier) ctx.getBean("metier");
System.out.println("Résultat XML = " + metier.calcul());
// Output : Résultat XML = 84.0
```

### Test Version Annotations
```java
ApplicationContext ctx = new AnnotationApplicationContext("ma.enset.metier");
IMetier metier = (IMetier) ctx.getBean("metier");
System.out.println("Résultat Annotations = " + metier.calcul());
// Output : Résultat Annotations = 84.0
```

---

## Tableau Comparatif

| Critère | Statique | Dynamique | Spring XML | Spring Annotations | Mini Framework |
|---|---|---|---|---|---|
| Recompilation si changement | ✅ Oui | ❌ Non | ❌ Non | ❌ Non | ❌ Non |
| Fichier de config | ❌ | `.txt` | `.xml` | ❌ | `.xml` ou annotations |
| Réflexion Java | ❌ | ✅ | ✅ (interne) | ✅ (interne) | ✅ |
| Couplage | Fort | Faible | Faible | Faible | Faible |
| Complexité | Faible | Moyenne | Faible | Faible | Moyenne |

---

*TP réalisé dans le cadre du cours JEE — ENSET*