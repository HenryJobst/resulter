//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ControlType.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <pre>
 * &lt;simpleType name="ControlType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="Control"/&gt;
 *     &lt;enumeration value="Start"/&gt;
 *     &lt;enumeration value="Finish"/&gt;
 *     &lt;enumeration value="CrossingPoint"/&gt;
 *     &lt;enumeration value="EndOfMarkedRoute"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ControlType")
@XmlEnum
public enum ControlType {

    @XmlEnumValue("Control")
    CONTROL("Control"),
    @XmlEnumValue("Start")
    START("Start"),
    @XmlEnumValue("Finish")
    FINISH("Finish"),
    @XmlEnumValue("CrossingPoint")
    CROSSING_POINT("CrossingPoint"),
    @XmlEnumValue("EndOfMarkedRoute")
    END_OF_MARKED_ROUTE("EndOfMarkedRoute");
    private final String value;

    ControlType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ControlType fromValue(String v) {
        for (ControlType c: ControlType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
