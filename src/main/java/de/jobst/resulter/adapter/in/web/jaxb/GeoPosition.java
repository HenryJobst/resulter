//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *         Defines a geographical position, e.g. of a control.
 *       
 * 
 * <p>Java-Klasse für GeoPosition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType value="GeoPosition"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute value="lng" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute value="lat" use="required" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *       &lt;attribute value="alt" type="{http://www.w3.org/2001/XMLSchema}double" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GeoPosition")
public class GeoPosition
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlAttribute(name = "lng", required = true)
    protected double lng;
    @XmlAttribute(name = "lat", required = true)
    protected double lat;
    @XmlAttribute(name = "alt")
    protected Double alt;

    /**
     * Ruft den Wert der lng-Eigenschaft ab.
     * 
     */
    public double getLng() {
        return lng;
    }

    /**
     * Legt den Wert der lng-Eigenschaft fest.
     * 
     */
    public void setLng(double value) {
        this.lng = value;
    }

    /**
     * Ruft den Wert der lat-Eigenschaft ab.
     * 
     */
    public double getLat() {
        return lat;
    }

    /**
     * Legt den Wert der lat-Eigenschaft fest.
     * 
     */
    public void setLat(double value) {
        this.lat = value;
    }

    /**
     * Ruft den Wert der alt-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAlt() {
        return alt;
    }

    /**
     * Legt den Wert der alt-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAlt(Double value) {
        this.alt = value;
    }

}
