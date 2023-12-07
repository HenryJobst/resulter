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
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         Defines a class in an event.
 *       
 * 
 * <p>Java-Klasse für Class complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType value="Class"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element value="Id" type="{http://www.orienteering.org/datastandard/3.0}Id" minOccurs="0"/&gt;
 *         &lt;element value="Name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element value="ShortName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element value="ClassType" type="{http://www.orienteering.org/datastandard/3.0}ClassType" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="Leg" type="{http://www.orienteering.org/datastandard/3.0}Leg" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="TeamFee" type="{http://www.orienteering.org/datastandard/3.0}Fee" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="Fee" type="{http://www.orienteering.org/datastandard/3.0}Fee" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="Status" type="{http://www.orienteering.org/datastandard/3.0}EventClassStatus" minOccurs="0"/&gt;
 *         &lt;element value="RaceClass" type="{http://www.orienteering.org/datastandard/3.0}RaceClass" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element value="TooFewEntriesSubstituteClass" type="{http://www.orienteering.org/datastandard/3.0}Class" minOccurs="0"/&gt;
 *         &lt;element value="TooManyEntriesSubstituteClass" type="{http://www.orienteering.org/datastandard/3.0}Class" minOccurs="0"/&gt;
 *         &lt;element value="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute value="minAge" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="maxAge" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="sex" default="B"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="B"/&gt;
 *             &lt;enumeration value="F"/&gt;
 *             &lt;enumeration value="M"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute value="minNumberOfTeamMembers" type="{http://www.w3.org/2001/XMLSchema}integer" default="1" /&gt;
 *       &lt;attribute value="maxNumberOfTeamMembers" type="{http://www.w3.org/2001/XMLSchema}integer" default="1" /&gt;
 *       &lt;attribute value="minTeamAge" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="maxTeamAge" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="numberOfCompetitors" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="maxNumberOfCompetitors" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *       &lt;attribute value="resultListMode" default="Default"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="Default"/&gt;
 *             &lt;enumeration value="Unordered"/&gt;
 *             &lt;enumeration value="UnorderedNoTimes"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute value="modifyTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Class", propOrder = {
    "id",
    "value",
    "shortName",
    "classTypes",
    "legs",
    "teamFees",
    "fees",
    "status",
    "raceClasses",
    "tooFewEntriesSubstituteClass",
    "tooManyEntriesSubstituteClass",
    "extensions"
})
public class Class
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Id")
    protected Id id;
    @XmlElement(name = "Name", required = true)
    protected String name;
    @XmlElement(name = "ShortName")
    protected String shortName;
    @XmlElement(name = "ClassType")
    protected List<ClassType> classTypes;
    @XmlElement(name = "Leg")
    protected List<Leg> legs;
    @XmlElement(name = "TeamFee")
    protected List<Fee> teamFees;
    @XmlElement(name = "Fee")
    protected List<Fee> fees;
    @XmlElement(name = "Status", defaultValue = "Normal")
    @XmlSchemaType(name = "NMTOKEN")
    protected EventClassStatus status;
    @XmlElement(name = "RaceClass")
    protected List<RaceClass> raceClasses;
    @XmlElement(name = "TooFewEntriesSubstituteClass")
    protected Class tooFewEntriesSubstituteClass;
    @XmlElement(name = "TooManyEntriesSubstituteClass")
    protected Class tooManyEntriesSubstituteClass;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "minAge")
    protected BigInteger minAge;
    @XmlAttribute(name = "maxAge")
    protected BigInteger maxAge;
    @XmlAttribute(name = "sex")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String sex;
    @XmlAttribute(name = "minNumberOfTeamMembers")
    protected BigInteger minNumberOfTeamMembers;
    @XmlAttribute(name = "maxNumberOfTeamMembers")
    protected BigInteger maxNumberOfTeamMembers;
    @XmlAttribute(name = "minTeamAge")
    protected BigInteger minTeamAge;
    @XmlAttribute(name = "maxTeamAge")
    protected BigInteger maxTeamAge;
    @XmlAttribute(name = "numberOfCompetitors")
    protected BigInteger numberOfCompetitors;
    @XmlAttribute(name = "maxNumberOfCompetitors")
    protected BigInteger maxNumberOfCompetitors;
    @XmlAttribute(name = "resultListMode")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String resultListMode;
    @XmlAttribute(name = "modifyTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar modifyTime;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
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
     * Legt den Wert der value-Eigenschaft fest.
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
     * Ruft den Wert der value-Eigenschaft ab.
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
     * Legt den Wert der value-Eigenschaft fest.
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
     * Ruft den Wert der shortName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Legt den Wert der shortName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortName(String value) {
        this.shortName = value;
    }

    /**
     * Gets the value of the classTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the classTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClassTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClassType }
     * 
     * 
     */
    public List<ClassType> getClassTypes() {
        if (classTypes == null) {
            classTypes = new ArrayList<ClassType>();
        }
        return this.classTypes;
    }

    /**
     * Gets the value of the legs property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the legs property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLegs().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Leg }
     * 
     * 
     */
    public List<Leg> getLegs() {
        if (legs == null) {
            legs = new ArrayList<Leg>();
        }
        return this.legs;
    }

    /**
     * Gets the value of the teamFees property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the teamFees property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeamFees().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Fee }
     * 
     * 
     */
    public List<Fee> getTeamFees() {
        if (teamFees == null) {
            teamFees = new ArrayList<Fee>();
        }
        return this.teamFees;
    }

    /**
     * Gets the value of the fees property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the fees property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFees().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Fee }
     * 
     * 
     */
    public List<Fee> getFees() {
        if (fees == null) {
            fees = new ArrayList<Fee>();
        }
        return this.fees;
    }

    /**
     * Ruft den Wert der status-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link EventClassStatus }
     *     
     */
    public EventClassStatus getStatus() {
        return status;
    }

    /**
     * Legt den Wert der status-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link EventClassStatus }
     *     
     */
    public void setStatus(EventClassStatus value) {
        this.status = value;
    }

    /**
     * Gets the value of the raceClasses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the raceClasses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRaceClasses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RaceClass }
     * 
     * 
     */
    public List<RaceClass> getRaceClasses() {
        if (raceClasses == null) {
            raceClasses = new ArrayList<RaceClass>();
        }
        return this.raceClasses;
    }

    /**
     * Ruft den Wert der tooFewEntriesSubstituteClass-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Class }
     *     
     */
    public Class getTooFewEntriesSubstituteClass() {
        return tooFewEntriesSubstituteClass;
    }

    /**
     * Legt den Wert der tooFewEntriesSubstituteClass-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Class }
     *     
     */
    public void setTooFewEntriesSubstituteClass(Class value) {
        this.tooFewEntriesSubstituteClass = value;
    }

    /**
     * Ruft den Wert der tooManyEntriesSubstituteClass-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Class }
     *     
     */
    public Class getTooManyEntriesSubstituteClass() {
        return tooManyEntriesSubstituteClass;
    }

    /**
     * Legt den Wert der tooManyEntriesSubstituteClass-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Class }
     *     
     */
    public void setTooManyEntriesSubstituteClass(Class value) {
        this.tooManyEntriesSubstituteClass = value;
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
     * Ruft den Wert der minAge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMinAge() {
        return minAge;
    }

    /**
     * Legt den Wert der minAge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMinAge(BigInteger value) {
        this.minAge = value;
    }

    /**
     * Ruft den Wert der maxAge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxAge() {
        return maxAge;
    }

    /**
     * Legt den Wert der maxAge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxAge(BigInteger value) {
        this.maxAge = value;
    }

    /**
     * Ruft den Wert der sex-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSex() {
        if (sex == null) {
            return "B";
        } else {
            return sex;
        }
    }

    /**
     * Legt den Wert der sex-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSex(String value) {
        this.sex = value;
    }

    /**
     * Ruft den Wert der minNumberOfTeamMembers-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMinNumberOfTeamMembers() {
        if (minNumberOfTeamMembers == null) {
            return new BigInteger("1");
        } else {
            return minNumberOfTeamMembers;
        }
    }

    /**
     * Legt den Wert der minNumberOfTeamMembers-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMinNumberOfTeamMembers(BigInteger value) {
        this.minNumberOfTeamMembers = value;
    }

    /**
     * Ruft den Wert der maxNumberOfTeamMembers-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxNumberOfTeamMembers() {
        if (maxNumberOfTeamMembers == null) {
            return new BigInteger("1");
        } else {
            return maxNumberOfTeamMembers;
        }
    }

    /**
     * Legt den Wert der maxNumberOfTeamMembers-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxNumberOfTeamMembers(BigInteger value) {
        this.maxNumberOfTeamMembers = value;
    }

    /**
     * Ruft den Wert der minTeamAge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMinTeamAge() {
        return minTeamAge;
    }

    /**
     * Legt den Wert der minTeamAge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMinTeamAge(BigInteger value) {
        this.minTeamAge = value;
    }

    /**
     * Ruft den Wert der maxTeamAge-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxTeamAge() {
        return maxTeamAge;
    }

    /**
     * Legt den Wert der maxTeamAge-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxTeamAge(BigInteger value) {
        this.maxTeamAge = value;
    }

    /**
     * Ruft den Wert der numberOfCompetitors-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberOfCompetitors() {
        return numberOfCompetitors;
    }

    /**
     * Legt den Wert der numberOfCompetitors-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberOfCompetitors(BigInteger value) {
        this.numberOfCompetitors = value;
    }

    /**
     * Ruft den Wert der maxNumberOfCompetitors-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getMaxNumberOfCompetitors() {
        return maxNumberOfCompetitors;
    }

    /**
     * Legt den Wert der maxNumberOfCompetitors-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setMaxNumberOfCompetitors(BigInteger value) {
        this.maxNumberOfCompetitors = value;
    }

    /**
     * Ruft den Wert der resultListMode-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultListMode() {
        if (resultListMode == null) {
            return "Default";
        } else {
            return resultListMode;
        }
    }

    /**
     * Legt den Wert der resultListMode-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultListMode(String value) {
        this.resultListMode = value;
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
