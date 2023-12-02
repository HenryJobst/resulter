//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import java.math.BigInteger;
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
 *         Defines an event entry for a team.
 *       
 * 
 * <p>Java-Klasse für TeamEntry complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TeamEntry"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{http://www.orienteering.org/datastandard/3.0}Id" minOccurs="0"/&gt;
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="Organisation" type="{http://www.orienteering.org/datastandard/3.0}Organisation" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TeamEntryPerson" type="{http://www.orienteering.org/datastandard/3.0}TeamEntryPerson" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Class" type="{http://www.orienteering.org/datastandard/3.0}Class" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Race" type="{http://www.w3.org/2001/XMLSchema}integer" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="AssignedFee" type="{http://www.orienteering.org/datastandard/3.0}AssignedFee" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ServiceRequest" type="{http://www.orienteering.org/datastandard/3.0}ServiceRequest" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="StartTimeAllocationRequest" type="{http://www.orienteering.org/datastandard/3.0}StartTimeAllocationRequest" minOccurs="0"/&gt;
 *         &lt;element name="ContactInformation" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="EntryTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
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
@XmlType(name = "TeamEntry", propOrder = {
    "id",
    "name",
    "organisations",
    "teamEntryPersons",
    "clazzs",
    "races",
    "assignedFees",
    "serviceRequests",
    "startTimeAllocationRequest",
    "contactInformation",
    "entryTime",
    "extensions"
})
public class TeamEntry
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Id")
    protected Id id;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "Organisation")
    protected List<Organisation> organisations;
    @XmlElement(name = "TeamEntryPerson")
    protected List<TeamEntryPerson> teamEntryPersons;
    @XmlElement(name = "Class")
    protected List<Class> clazzs;
    @XmlElement(name = "Race")
    protected List<BigInteger> races;
    @XmlElement(name = "AssignedFee")
    protected List<AssignedFee> assignedFees;
    @XmlElement(name = "ServiceRequest")
    protected List<ServiceRequest> serviceRequests;
    @XmlElement(name = "StartTimeAllocationRequest")
    protected StartTimeAllocationRequest startTimeAllocationRequest;
    @XmlElement(name = "ContactInformation")
    protected String contactInformation;
    @XmlElement(name = "EntryTime", type = String.class)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar entryTime;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "modifyTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar modifyTime;

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Id }
     *     
     */
    public Id getId() {
        return id;
    }

    /**
     * Legt den Wert der id-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Id }
     *     
     */
    public void setId(Id value) {
        this.id = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the organisations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the organisations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOrganisations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Organisation }
     * 
     * 
     */
    public List<Organisation> getOrganisations() {
        if (organisations == null) {
            organisations = new ArrayList<Organisation>();
        }
        return this.organisations;
    }

    /**
     * Gets the value of the teamEntryPersons property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the teamEntryPersons property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeamEntryPersons().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TeamEntryPerson }
     * 
     * 
     */
    public List<TeamEntryPerson> getTeamEntryPersons() {
        if (teamEntryPersons == null) {
            teamEntryPersons = new ArrayList<TeamEntryPerson>();
        }
        return this.teamEntryPersons;
    }

    /**
     * Gets the value of the clazzs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the clazzs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClazzs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Class }
     * 
     * 
     */
    public List<Class> getClazzs() {
        if (clazzs == null) {
            clazzs = new ArrayList<Class>();
        }
        return this.clazzs;
    }

    /**
     * Gets the value of the races property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the races property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRaces().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BigInteger }
     * 
     * 
     */
    public List<BigInteger> getRaces() {
        if (races == null) {
            races = new ArrayList<BigInteger>();
        }
        return this.races;
    }

    /**
     * Gets the value of the assignedFees property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the assignedFees property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssignedFees().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssignedFee }
     * 
     * 
     */
    public List<AssignedFee> getAssignedFees() {
        if (assignedFees == null) {
            assignedFees = new ArrayList<AssignedFee>();
        }
        return this.assignedFees;
    }

    /**
     * Gets the value of the serviceRequests property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the serviceRequests property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getServiceRequests().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ServiceRequest }
     * 
     * 
     */
    public List<ServiceRequest> getServiceRequests() {
        if (serviceRequests == null) {
            serviceRequests = new ArrayList<ServiceRequest>();
        }
        return this.serviceRequests;
    }

    /**
     * Ruft den Wert der startTimeAllocationRequest-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link StartTimeAllocationRequest }
     *     
     */
    public StartTimeAllocationRequest getStartTimeAllocationRequest() {
        return startTimeAllocationRequest;
    }

    /**
     * Legt den Wert der startTimeAllocationRequest-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link StartTimeAllocationRequest }
     *     
     */
    public void setStartTimeAllocationRequest(StartTimeAllocationRequest value) {
        this.startTimeAllocationRequest = value;
    }

    /**
     * Ruft den Wert der contactInformation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContactInformation() {
        return contactInformation;
    }

    /**
     * Legt den Wert der contactInformation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContactInformation(String value) {
        this.contactInformation = value;
    }

    /**
     * Ruft den Wert der entryTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getEntryTime() {
        return entryTime;
    }

    /**
     * Legt den Wert der entryTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEntryTime(Calendar value) {
        this.entryTime = value;
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
