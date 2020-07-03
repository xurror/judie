package cm.ubuea.judie

object Commands {
    //Main Activity Commands
    const val mailModule = "mail"
    const val callModule = "phone"
    const val locModule = "location"
    const val remmodule = "reminder"
    const val musicModule = "play music"
    const val emergencyModule = "emergency"
    const val noteModule = "note"
    const val helpModule = "help"
    const val EMERGENCY = "emergency activity"
    const val TIME = "time"

    //Gmail Module Commands
    //search base commands
    const val MAIL_FETCH_MAILS = "get mails"
    const val MAIL_FEtCH_LABELS = "get labels"
    const val MAIL_SEARCH_SUBJECT = "search subject"
    const val MAIL_SEARCH_LABELS = "search labels"
    const val MAIL_NEXT = "next"
    const val MAIL_CONFIRM = "do you want to change any field?"

    //compose based commands
    const val MAIL_COMPOSE_MAIL = "compose mail"

    //Weather Module
    const val TEMP = "s"
    const val WEEK = "w"
    const val weather = "how is the weather"

    //Support commands
    const val MAIL_SEND = "send"
    const val MAIL_SUBJECT = "subject"
    const val MAIL_TO = "send to"
    const val MAIL_BODY = "body"

    //Phone Module Commands
    const val CALL_LOG = "call logs"
    const val SMS = "messages"
    const val CALL = "call"
    const val CONTACTS = "search contact"
    const val SMS_SEND = "send message"
    const val PHONE_HELP = "help"

    //Reminder Module Commands
    const val DATE = "date"
    const val MONTH = "month"
    const val YEAR = "year"
    const val HOUR = "hour"
    const val MINUTES = "minutes"
    const val TITLE = "title"
    const val SET = "setting"

    //Notes Module in Utility
    const val TITLEn = "title"
    const val BODY = "body"
    const val PRIORITY = "priority"
    const val SAVE = "saving"
    const val MAKE = "make"
    const val READ = "read"
    const val YES = "yes"
    const val NO = "no"
    @JvmStatic
    fun filterCommands(commandToFilter: String): Array<String?> {
        val words: Array<String?> = commandToFilter.split(" ".toRegex()).toTypedArray()
        return switchCommands(words)
    }

    fun switchCommands(commandToSwitch: Array<String?>): Array<String?> {
        val a = arrayOfNulls<String>(4)
        var `as` = ""
        if (commandToSwitch[0] == CALL) {
            if (commandToSwitch.size == 4) {
                when (commandToSwitch[0]) {
                    CALL -> if (commandToSwitch[1] != null) {
                        a[0] = commandToSwitch[0]
                        a[1] = commandToSwitch[1] + commandToSwitch[2] + commandToSwitch[3]
                        return a
                    }
                }
            } else if (commandToSwitch[1] == "logs") {
                a[0] = commandToSwitch[0].toString() + " " + commandToSwitch[1]
            } else {
                a[0] = commandToSwitch[0]
                a[1] = commandToSwitch[1]
                a[2] = "check"
            }
        } else if (commandToSwitch[0] == SMS) {
            a[0] = commandToSwitch[0]
        } else if (commandToSwitch[0] == helpModule) {
            a[0] = commandToSwitch[0]
        } else if (commandToSwitch[0] == PHONE_HELP) {
            a[0] = commandToSwitch[0]
            a[1] = ""
        } else if (commandToSwitch.size == 1) {
            a[0] = ""
        } else {
            when (commandToSwitch[0].toString() + " " + commandToSwitch[1]) {
                MAIL_SEARCH_SUBJECT -> {
                    a[0] = commandToSwitch[0].toString() + " " + commandToSwitch[1]
                    if (commandToSwitch[2] != null) {
                        a[1] = commandToSwitch[2]
                    }
                }
                MAIL_FETCH_MAILS -> {
                    a[0] = commandToSwitch[0].toString() + " " + commandToSwitch[1]
                    try {
                        a[1] = commandToSwitch[2]
                    } catch (e: IndexOutOfBoundsException) {
                        a[1] = ""
                    }
                }
                CONTACTS -> {
                    a[0] = commandToSwitch[0].toString() + " " + commandToSwitch[1]
                    if (commandToSwitch[2] != null) {
                        a[1] = commandToSwitch[2]
                    }
                }
                else -> {
                    for (ab in commandToSwitch) `as` = "$`as`$ab "
                    a[0] = `as`.trim { it <= ' ' }
                }
            }
        }
        return a
    }
}