//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         Start information for an individual competitor, including e.g. start time and bib number.
 *       
 * 
 * <p>Java-Klasse für TeamMemberStart complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TeamMemberStart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EntryId" type="{http://www.orienteering.org/datastandard/3.0}Id" minOccurs="0"/&gt;
 *         &lt;element name="Person" type="{http://www.orienteering.org/datastandard/3.0}Person" minOccurs="0"/&gt;
 *         &lt;element name="Organisation" type="{http://www.orienteering.org/datastandard/3.0}Organisation" minOccurs="0"/&gt;
 *         &lt;element name="Start" type="{http://www.orienteering.org/datastandard/3.0}TeamMemberRaceStart" maxOccurs="unbounded"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="modifyTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamMemberStart", propOrder = {
    "entryId",
    "person",
    "organisation",
    "starts",
    "extensions"
})
public class TeamMemberStart
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "EntryId")
    protected Id entryId;
    @XmlElement(name = "Person")
    protected Person person;
    @XmlElement(name = "Organisation")
    protected Organisation organisation;
    @XmlElement(name = "Start", required = true)
    protected List<TeamMemberRaceStart> starts;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "modifyTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar modifyTime;

    /**
     * Ruft den Wert der entryId-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Id }
     *     
     */
    public Id getEntryId() {
        return entryId;
    }

    /**
     * Legt den Wert der entryId-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Id }
     *     
     */
    public void setEntryId(Id value) {
        this.entryId = value;
    }

    /**
     * Ruft den Wert der person-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Person }
     *     
     */
    public Person getPerson() {
        return person;
    }

    /**
     * Legt den Wert der person-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Person }
     *     
     */
    public void setPerson(Person value) {
        this.person = value;
    }

    /**
     * Ruft den Wert der organisation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Organisation }
     *     
     */
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * Legt den Wert der organisation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Organisation }
     *     
     */
    public void setOrganisation(Organisation value) {
        this.organisation = value;
    }

    /**
     * Gets the value of the starts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the starts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStarts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TeamMemberRaceStart }
     * 
     * 
     */
    public List<TeamMemberRaceStart> getStarts() {
        if (starts == null) {
            starts = new ArrayList<TeamMemberRaceStart>();
        }
        return this.starts;
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

    /**
     * Ruft den Wert der modifyTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getModifyTime() {
        return modifyTime;
    }

    /**
     * Legt den Wert der modifyTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModifyTime(Calendar value) {
        this.modifyTime = value;
    }

}
