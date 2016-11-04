//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2016.11.01 at 01:00:49 PM EDT 
//


package gov.nist.healthcare.cds.domain.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DateType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DateType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice>
 *         &lt;element name="Fixed" type="{http://www.example.org/testCase/}FixedDateType"/>
 *         &lt;element name="Relative" type="{http://www.example.org/testCase/}RelativeDateType"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DateType", propOrder = {
    "fixed",
    "relative"
})
public class DateType {

    @XmlElement(name = "Fixed")
    protected FixedDateType fixed;
    @XmlElement(name = "Relative")
    protected RelativeDateType relative;

    /**
     * Gets the value of the fixed property.
     * 
     * @return
     *     possible object is
     *     {@link FixedDateType }
     *     
     */
    public FixedDateType getFixed() {
        return fixed;
    }

    /**
     * Sets the value of the fixed property.
     * 
     * @param value
     *     allowed object is
     *     {@link FixedDateType }
     *     
     */
    public void setFixed(FixedDateType value) {
        this.fixed = value;
    }

    /**
     * Gets the value of the relative property.
     * 
     * @return
     *     possible object is
     *     {@link RelativeDateType }
     *     
     */
    public RelativeDateType getRelative() {
        return relative;
    }

    /**
     * Sets the value of the relative property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelativeDateType }
     *     
     */
    public void setRelative(RelativeDateType value) {
        this.relative = value;
    }

}
