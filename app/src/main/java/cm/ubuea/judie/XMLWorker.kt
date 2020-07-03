package cm.ubuea.judie

import org.xml.sax.Attributes
import org.xml.sax.SAXException
import org.xml.sax.helpers.DefaultHandler

class XMLWorker : DefaultHandler() {
    private var info = Info()
    private var count = 0
    private var data = ""
    private var temp: String = ""
    private var text: String = ""
    private var humid = ""
    private var speed: String = ""
    private var chill: String = ""
    private var pressure: String = ""

    fun getTemp(): String {
        return """
                Temperature $temp degree Farenhites
                $text
                Humidity $humid
                Wind Temperature $chill degree Farenhites
                Wind Speed ${speed}kilometers per hour
                Atmospheric Pressure $pressure
            """
    }

    @Throws(SAXException::class)
    override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
        val u = uri
        if (localName == "location") {
            val city = attributes.getValue("city")
            info.setCity(city)
            data = city
        } else if (localName == "condition") {
            temp = attributes.getValue("temp")
            text = attributes.getValue("text")
            info.setTemp(temp)
            info.setText(text)
        } else if (localName == "wind") {
            speed = attributes.getValue("speed")
            chill = attributes.getValue("chill")
        } else if (localName == "atmosphere") {
            humid = attributes.getValue("humidity")
            pressure = attributes.getValue("pressure")
        }
    }
}