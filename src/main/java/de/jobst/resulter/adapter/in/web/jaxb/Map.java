//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.07 um 10:23:25 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *         Map information, used in course setting software with regard to the "real" map.
 *       
 * 
 * <p>Java-Klasse für Map complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Map"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Id" type="{http://www.orienteering.org/datastandard/3.0}Id" minOccurs="0"/&gt;
 *         &lt;element name="Image" type="{http://www.orienteering.org/datastandard/3.0}Image" minOccurs="0"/&gt;
 *         &lt;element name="Scale" type="{http://www.w3.org/2001/XMLSchema}double"/&gt;
 *         &lt;element name="MapPositionTopLeft" type="{http://www.orienteering.org/datastandard/3.0}MapPosition"/&gt;
 *         &lt;element name="MapPositionBottomRight" type="{http://www.orienteering.org/datastandard/3.0}MapPosition"/&gt;
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
@XmlType(name = "Map", propOrder = {
    "id",
    "image",
    "scale",
    "mapPositionTopLeft",
    "mapPositionBottomRight",
    "extensions"
})
public class Map
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlElement(name = "Id")
    protected Id id;
    @XmlElement(name = "Image")
    protected Image image;
    @XmlElement(name = "Scale")
    protected double scale;
    @XmlElement(name = "MapPositionTopLeft", required = true)
    protected MapPosition mapPositionTopLeft;
    @XmlElement(name = "MapPositionBottomRight", required = true)
    protected MapPosition mapPositionBottomRight;
    @XmlElement(name = "Extensions")
    protected Extensions extensions;

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
     * Ruft den Wert der image-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Image }
     *     
     */
    public Image getImage() {
        return image;
    }

    /**
     * Legt den Wert der image-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Image }
     *     
     */
    public void setImage(Image value) {
        this.image = value;
    }

    /**
     * Ruft den Wert der scale-Eigenschaft ab.
     * 
     */
    public double getScale() {
        return scale;
    }

    /**
     * Legt den Wert der scale-Eigenschaft fest.
     * 
     */
    public void setScale(double value) {
        this.scale = value;
    }

    /**
     * Ruft den Wert der mapPositionTopLeft-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MapPosition }
     *     
     */
    public MapPosition getMapPositionTopLeft() {
        return mapPositionTopLeft;
    }

    /**
     * Legt den Wert der mapPositionTopLeft-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MapPosition }
     *     
     */
    public void setMapPositionTopLeft(MapPosition value) {
        this.mapPositionTopLeft = value;
    }

    /**
     * Ruft den Wert der mapPositionBottomRight-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MapPosition }
     *     
     */
    public MapPosition getMapPositionBottomRight() {
        return mapPositionBottomRight;
    }

    /**
     * Legt den Wert der mapPositionBottomRight-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MapPosition }
     *     
     */
    public void setMapPositionBottomRight(MapPosition value) {
        this.mapPositionBottomRight = value;
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
