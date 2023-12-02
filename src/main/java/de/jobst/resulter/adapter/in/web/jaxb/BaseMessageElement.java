//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v3.0.0 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.12.02 um 06:53:24 PM CET 
//


package de.jobst.resulter.adapter.in.web.jaxb;

import java.io.Serializable;
import java.util.Calendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 *         The base message element that all message elements extend.
 *       
 * 
 * <p>Java-Klasse für BaseMessageElement complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BaseMessageElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attribute name="iofVersion" use="required" type="{http://www.w3.org/2001/XMLSchema}string" fixed="3.0" /&gt;
 *       &lt;attribute name="createTime" type="{http://www.w3.org/2001/XMLSchema}dateTime" /&gt;
 *       &lt;attribute name="creator" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseMessageElement")
@XmlSeeAlso({
    ControlCardList.class,
    ServiceRequestList.class,
    ResultList.class,
    StartList.class,
    CourseData.class,
    EntryList.class,
    ClassList.class,
    EventList.class,
    OrganisationList.class,
    CompetitorList.class
})
public abstract class BaseMessageElement
    implements Serializable
{

    private final static long serialVersionUID = -1L;
    @XmlAttribute(name = "iofVersion", required = true)
    protected String iofVersion;
    @XmlAttribute(name = "createTime")
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "dateTime")
    protected Calendar createTime;
    @XmlAttribute(name = "creator")
    protected String creator;

    /**
     * Ruft den Wert der iofVersion-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIofVersion() {
        if (iofVersion == null) {
            return "3.0";
        } else {
            return iofVersion;
        }
    }

    /**
     * Legt den Wert der iofVersion-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIofVersion(String value) {
        this.iofVersion = value;
    }

    /**
     * Ruft den Wert der createTime-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public Calendar getCreateTime() {
        return createTime;
    }

    /**
     * Legt den Wert der createTime-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreateTime(Calendar value) {
        this.createTime = value;
    }

    /**
     * Ruft den Wert der creator-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCreator() {
        return creator;
    }

    /**
     * Legt den Wert der creator-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCreator(String value) {
        this.creator = value;
    }

}
