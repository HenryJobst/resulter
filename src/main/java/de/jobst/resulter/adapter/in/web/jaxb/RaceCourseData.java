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
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *         This element defines all the control and course information for a race.
 *       
 * 
 * <p>Java-Klasse für RaceCourseData complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="RaceCourseData"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Map" type="{http://www.orienteering.org/datastandard/3.0}Map" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Control" type="{http://www.orienteering.org/datastandard/3.0}Control" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Course" type="{http://www.orienteering.org/datastandard/3.0}Course" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="ClassCourseAssignment" type="{http://www.orienteering.org/datastandard/3.0}ClassCourseAssignment" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="PersonCourseAssignment" type="{http://www.orienteering.org/datastandard/3.0}PersonCourseAssignment" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="TeamCourseAssignment" type="{http://www.orienteering.org/datastandard/3.0}TeamCourseAssignment" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="raceNumber" type="{http://www.w3.org/2001/XMLSchema}integer" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RaceCourseData", propOrder = {
    "maps",
    "controls",
    "courses",
    "classCourseAssignments",
    "personCourseAssignments",
    "teamCourseAssignments",
    "extensions"
})
public class RaceCourseData
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Map")
    protected List<Map> maps;
    @XmlElement(name = "Control")
    protected List<Control> controls;
    @XmlElement(name = "Course")
    protected List<Course> courses;
    @XmlElement(name = "ClassCourseAssignment")
    protected List<ClassCourseAssignment> classCourseAssignments;
    @XmlElement(name = "PersonCourseAssignment")
    protected List<PersonCourseAssignment> personCourseAssignments;
    @XmlElement(name = "TeamCourseAssignment")
    protected List<TeamCourseAssignment> teamCourseAssignments;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "raceNumber")
    protected BigInteger raceNumber;

    /**
     * Gets the value of the maps property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the maps property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMaps().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Map }
     * 
     * 
     */
    public List<Map> getMaps() {
        if (maps == null) {
            maps = new ArrayList<Map>();
        }
        return this.maps;
    }

    /**
     * Gets the value of the controls property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the controls property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getControls().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Control }
     * 
     * 
     */
    public List<Control> getControls() {
        if (controls == null) {
            controls = new ArrayList<Control>();
        }
        return this.controls;
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
     * {@link Course }
     * 
     * 
     */
    public List<Course> getCourses() {
        if (courses == null) {
            courses = new ArrayList<Course>();
        }
        return this.courses;
    }

    /**
     * Gets the value of the classCourseAssignments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the classCourseAssignments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getClassCourseAssignments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ClassCourseAssignment }
     * 
     * 
     */
    public List<ClassCourseAssignment> getClassCourseAssignments() {
        if (classCourseAssignments == null) {
            classCourseAssignments = new ArrayList<ClassCourseAssignment>();
        }
        return this.classCourseAssignments;
    }

    /**
     * Gets the value of the personCourseAssignments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the personCourseAssignments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPersonCourseAssignments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersonCourseAssignment }
     * 
     * 
     */
    public List<PersonCourseAssignment> getPersonCourseAssignments() {
        if (personCourseAssignments == null) {
            personCourseAssignments = new ArrayList<PersonCourseAssignment>();
        }
        return this.personCourseAssignments;
    }

    /**
     * Gets the value of the teamCourseAssignments property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the Jakarta XML Binding object.
     * This is why there is not a <CODE>set</CODE> method for the teamCourseAssignments property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTeamCourseAssignments().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TeamCourseAssignment }
     * 
     * 
     */
    public List<TeamCourseAssignment> getTeamCourseAssignments() {
        if (teamCourseAssignments == null) {
            teamCourseAssignments = new ArrayList<TeamCourseAssignment>();
        }
        return this.teamCourseAssignments;
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
     * Ruft den Wert der raceNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRaceNumber() {
        return raceNumber;
    }

    /**
     * Legt den Wert der raceNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRaceNumber(BigInteger value) {
        this.raceNumber = value;
    }

}
