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
 * <p>Java-Klasse für EventStatus.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <pre>
 * &lt;simpleType name="EventStatus"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *     &lt;enumeration value="Planned"/&gt;
 *     &lt;enumeration value="Applied"/&gt;
 *     &lt;enumeration value="Proposed"/&gt;
 *     &lt;enumeration value="Sanctioned"/&gt;
 *     &lt;enumeration value="Canceled"/&gt;
 *     &lt;enumeration value="Rescheduled"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "EventStatus")
@XmlEnum
public enum EventStatus {


    /**
     * 
     *             The event or race is on a planning stadium and has not been submitted to any sanctioning body.
     *           
     * 
     */
    @XmlEnumValue("Planned")
    PLANNED("Planned"),

    /**
     * 
     *             The organiser has submitted the event to the relevant sanctioning body.
     *           
     * 
     */
    @XmlEnumValue("Applied")
    APPLIED("Applied"),

    /**
     * 
     *             The organiser has bid on hosting the event or race as e.g. a championship.
     *           
     * 
     */
    @XmlEnumValue("Proposed")
    PROPOSED("Proposed"),

    /**
     * 
     *             The event oc race meets the relevant requirements and will happen.
     *           
     * 
     */
    @XmlEnumValue("Sanctioned")
    SANCTIONED("Sanctioned"),

    /**
     * 
     *             The event or race has been canceled, e.g. due to weather conditions.
     *           
     * 
     */
    @XmlEnumValue("Canceled")
    CANCELED("Canceled"),

    /**
     * 
     *             The date of the event or race has changed. A new Event or Race element should be created in addition to the already existing element.
     *           
     * 
     */
    @XmlEnumValue("Rescheduled")
    RESCHEDULED("Rescheduled");
    private final String value;

    EventStatus(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventStatus fromValue(String v) {
        for (EventStatus c: EventStatus.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
