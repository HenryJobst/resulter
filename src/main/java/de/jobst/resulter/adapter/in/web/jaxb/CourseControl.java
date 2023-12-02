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
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         A control included in a particular course.
 *       
 * 
 * <p>Java-Klasse für CourseControl complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CourseControl"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Control" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/&gt;
 *         &lt;element name="MapText" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="MapTextPosition" type="{http://www.orienteering.org/datastandard/3.0}MapPosition" minOccurs="0"/&gt;
 *         &lt;element name="LegLength" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="Score" type="{http://www.w3.org/2001/XMLSchema}double" minOccurs="0"/&gt;
 *         &lt;element name="Extensions" type="{http://www.orienteering.org/datastandard/3.0}Extensions" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type" type="{http://www.orienteering.org/datastandard/3.0}ControlType" /&gt;
 *       &lt;attribute name="randomOrder" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *       &lt;attribute name="specialInstruction" default="None"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *             &lt;enumeration value="None"/&gt;
 *             &lt;enumeration value="TapedRoute"/&gt;
 *             &lt;enumeration value="FunnelTapedRoute"/&gt;
 *             &lt;enumeration value="MandatoryCrossingPoint"/&gt;
 *             &lt;enumeration value="MandatoryOutOfBoundsAreaPassage"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="tapedRouteLength" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute name="modifyTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CourseControl", propOrder = {
    "controls",
    "mapText",
    "mapTextPosition",
    "legLength",
    "score",
    "extensions"
})
public class CourseControl
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Control", required = true)
    protected List<String> controls;
    @XmlElement(name = "MapText")
    protected String mapText;
    @XmlElement(name = "MapTextPosition")
    protected MapPosition mapTextPosition;
    @XmlElement(name = "LegLength")
    protected Double legLength;
    @XmlElement(name = "Score")
    protected Double score;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;
    @XmlAttribute(name = "type")
    protected ControlType type;
    @XmlAttribute(name = "randomOrder")
    protected Boolean randomOrder;
    @XmlAttribute(name = "specialInstruction")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String specialInstruction;
    @XmlAttribute(name = "tapedRouteLength")
    protected Double tapedRouteLength;
    @XmlAttribute(name = "modifyTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar modifyTime;

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
     * {@link String }
     * 
     * 
     */
    public List<String> getControls() {
        if (controls == null) {
            controls = new ArrayList<String>();
        }
        return this.controls;
    }

    /**
     * Ruft den Wert der mapText-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMapText() {
        return mapText;
    }

    /**
     * Legt den Wert der mapText-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMapText(String value) {
        this.mapText = value;
    }

    /**
     * Ruft den Wert der mapTextPosition-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MapPosition }
     *     
     */
    public MapPosition getMapTextPosition() {
        return mapTextPosition;
    }

    /**
     * Legt den Wert der mapTextPosition-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MapPosition }
     *     
     */
    public void setMapTextPosition(MapPosition value) {
        this.mapTextPosition = value;
    }

    /**
     * Ruft den Wert der legLength-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getLegLength() {
        return legLength;
    }

    /**
     * Legt den Wert der legLength-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setLegLength(Double value) {
        this.legLength = value;
    }

    /**
     * Ruft den Wert der score-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getScore() {
        return score;
    }

    /**
     * Legt den Wert der score-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setScore(Double value) {
        this.score = value;
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
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ControlType }
     *     
     */
    public ControlType getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ControlType }
     *     
     */
    public void setType(ControlType value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der randomOrder-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isRandomOrder() {
        if (randomOrder == null) {
            return false;
        } else {
            return randomOrder;
        }
    }

    /**
     * Legt den Wert der randomOrder-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRandomOrder(Boolean value) {
        this.randomOrder = value;
    }

    /**
     * Ruft den Wert der specialInstruction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecialInstruction() {
        if (specialInstruction == null) {
            return "None";
        } else {
            return specialInstruction;
        }
    }

    /**
     * Legt den Wert der specialInstruction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecialInstruction(String value) {
        this.specialInstruction = value;
    }

    /**
     * Ruft den Wert der tapedRouteLength-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getTapedRouteLength() {
        return tapedRouteLength;
    }

    /**
     * Legt den Wert der tapedRouteLength-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setTapedRouteLength(Double value) {
        this.tapedRouteLength = value;
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
