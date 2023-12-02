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
 *         &lt;element name="Event" type="{http://www.orienteering.org/datastandard/3.0}Event"/&gt;
 *         &lt;element name="OrganisationServiceRequest" type="{http://www.orienteering.org/datastandard/3.0}OrganisationServiceRequest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="PersonServiceRequest" type="{http://www.orienteering.org/datastandard/3.0}PersonServiceRequest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
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
    "organisationServiceRequests",
    "personServiceRequests",
    "extensions"
})
@XmlRootElement(name = "ServiceRequestList")
public class ServiceRequestList
    extends BaseMessageElement
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Event", required = true)
    protected Event event;
    @XmlElement(name = "OrganisationServiceRequest")
    protected List<OrganisationServiceRequest> organisationServiceRequests;
    @XmlElement(name = "PersonServiceRequest")
    protected List<PersonServiceRequest> personServiceRequests;
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
     * Gets the value of the organisationServiceRequests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the organisationServiceRequests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganisationServiceRequests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OrganisationServiceRequest }
     * 
     * 
     */
    public List<OrganisationServiceRequest> getOrganisationServiceRequests() {
        if (organisationServiceRequests == null) {
            organisationServiceRequests = new ArrayList<OrganisationServiceRequest>();
        }
        return this.organisationServiceRequests;
    }

    /**
     * Gets the value of the personServiceRequests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the personServiceRequests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonServiceRequests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersonServiceRequest }
     * 
     * 
     */
    public List<PersonServiceRequest> getPersonServiceRequests() {
        if (personServiceRequests == null) {
            personServiceRequests = new ArrayList<PersonServiceRequest>();
        }
        return this.personServiceRequests;
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
