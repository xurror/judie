package cm.ubuea.judie

class Info {
    private lateinit var city: String
    private lateinit var temp: String
    private lateinit var text: String
    private lateinit var humid: String
    private lateinit var week: Array<Array<String>>

    fun setCity(city: String) {
        this.city = city
    }

    fun setTemp(temp: String) {
        this.temp = temp
    }

    fun setHumid(humid: String) {
        this.humid = humid
    }

    fun setText(text: String) {
        this.text = text
    }

    fun setWeek(week: Array<Array<String>>) {
        this.week = week
    }

    fun getTemp(): String {
        return """${week[0][3]}
        $temp
         Highest${week[0][0]}
         Lowest${week[0][1]}
        $text
        $humid"""
    }

    fun getWeek(): String? {
        var resullt: String? = null
        for (i in 0..6) {
            resullt = """$resullt${week[i][3]}
             Highest${week[i][0]}
             Lowest${week[i][1]}
            ${week[i][2]}
            """
        }
        return resullt
    }
}