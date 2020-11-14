package dev.kingkongcode.edtube.util


class ConvertDurationIsoToString {
    data class EdTubeDuration(var time: String) {
    }

    companion object {
        fun convert(time: String) : String {
            var tempStr = Constants.instance.EMPTY_STRING
            var hourStr = EdTubeDuration(Constants.instance.EMPTY_STRING)
            var minStr = EdTubeDuration("00")
            var secStr = EdTubeDuration("00")
            var finalStr: String

            for (index in 2 until time.length) {
                var char = time[index]
                when (char) {
                    'H' -> {
                        hourStr = EdTubeDuration(tempStr).copy()
                        tempStr = Constants.instance.EMPTY_STRING
                    }

                    'M' -> {
                        tempStr = if (tempStr.length == 1) {
                            tempStr.replace(tempStr,"0$tempStr")
                        } else tempStr
                        minStr = EdTubeDuration(tempStr).copy()
                        tempStr = Constants.instance.EMPTY_STRING
                    }

                    'S' -> {
                        tempStr = if (tempStr.length == 1) {
                            tempStr.replace(tempStr,"0$tempStr")
                        } else tempStr
                        secStr = EdTubeDuration(tempStr).copy()
                        tempStr = Constants.instance.EMPTY_STRING
                    }

                    else -> tempStr += char
                }
            }

            finalStr = if (hourStr.time != Constants.instance.EMPTY_STRING) {
                "${hourStr.time}:${minStr.time}:${secStr.time}"
            } else {
                "${minStr.time}:${secStr.time}"
            }

            return finalStr
        }
    }
}