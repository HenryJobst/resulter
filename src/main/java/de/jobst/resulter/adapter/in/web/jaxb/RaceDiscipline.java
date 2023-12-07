//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.07 um 10:23:25 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für RaceDiscipline.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <pre>
 * &lt;simpleType name="RaceDiscipline"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="Sprint"/&gt;
 *     &lt;enumeration value="Middle"/&gt;
 *     &lt;enumeration value="Long"/&gt;
 *     &lt;enumeration value="Ultralong"/&gt;
 *     &lt;enumeration value="Other"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "RaceDiscipline")
@XmlEnum
public enum RaceDiscipline {

    @XmlEnumValue("Sprint")
    SPRINT("Sprint"),
    @XmlEnumValue("Middle")
    MIDDLE("Middle"),
    @XmlEnumValue("Long")
    LONG("Long"),
    @XmlEnumValue("Ultralong")
    ULTRALONG("Ultralong"),
    @XmlEnumValue("Other")
    OTHER("Other");
    private final String value;

    RaceDiscipline(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RaceDiscipline fromValue(String v) {
        for (RaceDiscipline c: RaceDiscipline.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
