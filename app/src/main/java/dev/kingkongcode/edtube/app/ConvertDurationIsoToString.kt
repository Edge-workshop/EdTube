package dev.kingkongcode.edtube.app

class ConvertDurationIsoToString {
    // TODO fix issue when it is an even number ex:  GOOD=02:00 BAD=02: and GOOD=00:32 BAD=:32
    companion object {
        fun convert(time: String) : String {
            var tempStr = Constants.EMPTY_STRING
            var hourStr = Constants.EMPTY_STRING
            var minStr = Constants.EMPTY_STRING
            var secStr = Constants.EMPTY_STRING

            for (index in 2 until time.length) {
                when (val char = time[index]) {
                    'H' -> {
                        hourStr = tempStr
                        tempStr = Constants.EMPTY_STRING
                    }

                    'M' -> {
                        tempStr = if (tempStr.length == 1) {
                            tempStr.replace(tempStr,"0$tempStr")
                        } else tempStr
                        minStr = tempStr
                        tempStr = Constants.EMPTY_STRING
                    }

                    'S' -> {
                        tempStr = if (tempStr.length == 1) {
                            tempStr.replace(tempStr,"0$tempStr")
                        } else tempStr
                        secStr = tempStr
                        tempStr = Constants.EMPTY_STRING
                    }

                    else -> tempStr += char
                }
            }

            return if (hourStr.isNotEmpty()) {
                "$hourStr:$minStr:$secStr"
            } else {
                "$minStr:$secStr"
            }
        }
    }
}