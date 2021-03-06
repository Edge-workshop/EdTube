package dev.kingkongcode.edtube.app

class ConvertDurationIsoToString {

    companion object {
        fun convert(time: String) : String {
            var tempStr = Constants.EMPTY_STRING
            var hourStr = Constants.EMPTY_STRING
            var minStr = Constants.EMPTY_STRING
            var secStr = Constants.EMPTY_STRING
            val finalStr: String

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

            finalStr = if (hourStr.isNotEmpty()) {
                "$hourStr:$minStr:$secStr"
            } else {
                "$minStr:$secStr"
            }

            return finalStr
        }
    }
}