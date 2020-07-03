package cm.ubuea.judie

object Util {
    fun mail(add: String): String {
        return add.replace("dot".toRegex(), ".").replace("dash".toRegex(), "-").replace("underscore".toRegex(), "_")
                .replace("at the rate".toRegex(), "@").replace(" ".toRegex(), "")
    }

    fun number(num: String): String? {
        var num1: String
        for (i in 0..9) num1 = num.replace(num, "$num ")
        return null
    }
}