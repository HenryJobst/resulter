//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{http://www.orienteering.org/datastandard/3.0}BaseMessageElement"&gt;
 *       &lt;sequence&gt;
 *         &lt;element value="Event" type="{http://www.orienteering.org/datastandard/3.0}Event"/&gt;
 *         &lt;element value="TeamEntry" type="{http://www.orienteering.org/datastandard/3.0}TeamEntry" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="PersonEntry" type="{http://www.orienteering.org/datastandard/3.0}PersonEntry" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "event",
    "teamEntries",
    "personEntries",
    "extensions"
})
@XmlRootElement(name = "EntryList")
public class EntryList
    extends BaseMessageElement
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Event", required = true)
    protected Event event;
    @XmlElement(name = "TeamEntry")
    protected List<TeamEntry> teamEntries;
    @XmlElement(name = "PersonEntry")
    protected List<PersonEntry> personEntries;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;

    /**
     * Ruft den Wert der event-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Event }
     *     
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Legt den Wert der event-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Event }
     *     
     */
    public void setEvent(Event value) {
        this.event = value;
    }

    /**
     * Gets the value of the teamEntries property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the teamEntries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeamEntries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TeamEntry }
     * 
     * 
     */
    public List<TeamEntry> getTeamEntries() {
        if (teamEntries == null) {
            teamEntries = new ArrayList<TeamEntry>();
        }
        return this.teamEntries;
    }

    /**
     * Gets the value of the personEntries property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the personEntries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonEntries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersonEntry }
     * 
     * 
     */
    public List<PersonEntry> getPersonEntries() {
        if (personEntries == null) {
            personEntries = new ArrayList<PersonEntry>();
        }
        return this.personEntries;
    }

    /**
     * Ruft den Wert der extensions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Extensions }
     *     
     */
    public Extensions getExtensions() {
        return extensions;
    }

    /**
     * Legt den Wert der extensions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Extensions }
     *     
     */
    public void setExtensions(Extensions value) {
        this.extensions = value;
    }

}
