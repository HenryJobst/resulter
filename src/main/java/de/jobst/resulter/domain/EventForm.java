package de.jobst.resulter.domain;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für EventForm.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <pre>
 * &lt;simpleType name="EventForm"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="Individual"/&gt;
 *     &lt;enumeration value="Team"/&gt;
 *     &lt;enumeration value="Relay"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 */
@XmlType(name = "EventForm")
@XmlEnum
public enum EventForm {

    @XmlEnumValue("Individual")
    INDIVIDUAL("Individual"),
    @XmlEnumValue("Team")
    TEAM("Team"),
    @XmlEnumValue("Relay")
    RELAY("Relay");
    private final String value;

    EventForm(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventForm fromValue(String v) {
        for (EventForm c : EventForm.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
