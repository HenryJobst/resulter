//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import java.math.BigInteger;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *         Element that connects a course with a relay team member. Courses should be present in the RaceCourseData element and are matched on course name and/or course family. Team members are matched by 1) BibNumber, 2) Leg and LegOrder, 3) EntryId.
 *       
 * 
 * <p>Java-Klasse für TeamMemberCourseAssignment complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="TeamMemberCourseAssignment"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="EntryId" type="{http://www.orienteering.org/datastandard/3.0}Id" minOccurs="0"/&gt;
 *         &lt;element name="BibNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Leg" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="LegOrder" type="{http://www.w3.org/2001/XMLSchema}integer" minOccurs="0"/&gt;
 *         &lt;element name="TeamMemberName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CourseName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="CourseFamily" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TeamMemberCourseAssignment", propOrder = {
    "entryId",
    "bibNumber",
    "leg",
    "legOrder",
    "teamMemberName",
    "courseName",
    "courseFamily",
    "extensions"
})
public class TeamMemberCourseAssignment
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "EntryId")
    protected Id entryId;
    @XmlElement(name = "BibNumber")
    protected String bibNumber;
    @XmlElement(name = "Leg")
    protected BigInteger leg;
    @XmlElement(name = "LegOrder")
    protected BigInteger legOrder;
    @XmlElement(name = "TeamMemberName")
    protected String teamMemberName;
    @XmlElement(name = "CourseName")
    protected String courseName;
    @XmlElement(name = "CourseFamily")
    protected String courseFamily;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;

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
     * Ruft den Wert der bibNumber-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBibNumber() {
        return bibNumber;
    }

    /**
     * Legt den Wert der bibNumber-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBibNumber(String value) {
        this.bibNumber = value;
    }

    /**
     * Ruft den Wert der leg-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLeg() {
        return leg;
    }

    /**
     * Legt den Wert der leg-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLeg(BigInteger value) {
        this.leg = value;
    }

    /**
     * Ruft den Wert der legOrder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getLegOrder() {
        return legOrder;
    }

    /**
     * Legt den Wert der legOrder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setLegOrder(BigInteger value) {
        this.legOrder = value;
    }

    /**
     * Ruft den Wert der teamMemberName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTeamMemberName() {
        return teamMemberName;
    }

    /**
     * Legt den Wert der teamMemberName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTeamMemberName(String value) {
        this.teamMemberName = value;
    }

    /**
     * Ruft den Wert der courseName-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Legt den Wert der courseName-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCourseName(String value) {
        this.courseName = value;
    }

    /**
     * Ruft den Wert der courseFamily-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCourseFamily() {
        return courseFamily;
    }

    /**
     * Legt den Wert der courseFamily-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCourseFamily(String value) {
        this.courseFamily = value;
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
