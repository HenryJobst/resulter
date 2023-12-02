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
 *         The start list of a single class containing either individual start times or team start times.
 *       
 * 
 * <p>Java-Klasse für ClassStart complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ClassStart"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Class" type="{http://www.orienteering.org/datastandard/3.0}Class"/&gt;
 *         &lt;element name="Course" type="{http://www.orienteering.org/datastandard/3.0}SimpleRaceCourse" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="StartName" type="{http://www.orienteering.org/datastandard/3.0}StartName" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="PersonStart" type="{http://www.orienteering.org/datastandard/3.0}PersonStart" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TeamStart" type="{http://www.orienteering.org/datastandard/3.0}TeamStart" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="timeResolution" type="{http://www.w3.org/2001/XMLSchema}double" default="1" /&gt;
 *       &lt;attribute name="modifyTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassStart", propOrder = {
    "clazz",
    "courses",
    "startNames",
    "personStarts",
    "teamStarts",
    "extensions"
})
public class ClassStart
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Class", required = true)
    protected Class clazz;
    @XmlElement(name = "Course")
    protected List<SimpleRaceCourse> courses;
    @XmlElement(name = "StartName")
    protected List<StartName> startNames;
    @XmlElement(name = "PersonStart")
    protected List<PersonStart> personStarts;
    @XmlElement(name = "TeamStart")
    protected List<TeamStart> teamStarts;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "timeResolution")
    protected Double timeResolution;
    @XmlAttribute(name = "modifyTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar modifyTime;

    /**
     * Ruft den Wert der clazz-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Class }
     *     
     */
    public Class getClazz() {
        return clazz;
    }

    /**
     * Legt den Wert der clazz-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Class }
     *     
     */
    public void setClazz(Class value) {
        this.clazz = value;
    }

    /**
     * Gets the value of the courses property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the courses property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCourses().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SimpleRaceCourse }
     * 
     * 
     */
    public List<SimpleRaceCourse> getCourses() {
        if (courses == null) {
            courses = new ArrayList<SimpleRaceCourse>();
        }
        return this.courses;
    }

    /**
     * Gets the value of the startNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the startNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStartNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StartName }
     * 
     * 
     */
    public List<StartName> getStartNames() {
        if (startNames == null) {
            startNames = new ArrayList<StartName>();
        }
        return this.startNames;
    }

    /**
     * Gets the value of the personStarts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the personStarts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonStarts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersonStart }
     * 
     * 
     */
    public List<PersonStart> getPersonStarts() {
        if (personStarts == null) {
            personStarts = new ArrayList<PersonStart>();
        }
        return this.personStarts;
    }

    /**
     * Gets the value of the teamStarts property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the teamStarts property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeamStarts().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TeamStart }
     * 
     * 
     */
    public List<TeamStart> getTeamStarts() {
        if (teamStarts == null) {
            teamStarts = new ArrayList<TeamStart>();
        }
        return this.teamStarts;
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
     * Ruft den Wert der timeResolution-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getTimeResolution() {
        if (timeResolution == null) {
            return  1.0D;
        } else {
            return timeResolution;
        }
    }

    /**
     * Legt den Wert der timeResolution-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTimeResolution(Double value) {
        this.timeResolution = value;
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
