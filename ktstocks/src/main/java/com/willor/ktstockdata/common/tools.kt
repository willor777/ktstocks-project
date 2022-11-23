package com.willor.ktstockdata.common

import com.google.gson.Gson
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.pow
import kotlin.math.sqrt


internal val gson: Gson = Gson()


internal val Log: Logger = Logger.getGlobal()


internal fun Logger.w(tag: String, msg: String) {
    Logger.getGlobal().warning("$tag\t$msg")
}


internal fun Logger.d(tag: String, msg: String) {
    Logger.getGlobal().log(Level.parse("DEBUG"), "$tag\t$msg")
}


internal fun calculateStandardDeviationForDoubles(l: List<Double>): Double {
    var sum = 0.0
    var standardDeviation = 0.0

    for (num in l) {
        sum += num
    }

    val mean = sum / 10

    for (num in l) {
        standardDeviation += (num - mean).pow(2.0)
    }

    return sqrt(standardDeviation / 10)
}


/**
 * Attempts to parse Double from a String, Will remove all commas and spaces. On failure will
 * return 0 as default
 */
internal fun parseLongFromBigAbbreviatedNumbers(str: String): Long {

    val spltNonDecimal = { s: String ->
        var indexOne = ""
        var indexTwo = ""

        for (n in 0..s.lastIndex) {
            if (s[n].isDigit()) {
                indexOne += s[n]
            } else {
                indexTwo += s[n]
            }
        }

        mutableListOf(indexOne, indexTwo)
    }

    try {
        // Remove spaces
        val s = str.replace(" ", "").replace(",", "")

        // Check for non abbreviated number
        if (!s[s.lastIndex].isLetter()) {
            return s.replace(",", "").toLong()
        }

        val splitString: MutableList<String>

        // Check for non decimal abbreviated number
        splitString = if (!s.contains(".")) {
            spltNonDecimal(s)
        } else {
            s.split(".").toMutableList()

        }

        val previxValue = splitString[0]

        when {
            s.contains("M") -> {
                return (
                        previxValue + splitString[1]
                            .replace("M", "")
                            .padEnd(3, '0') + "000"
                        ).toLong()
            }

            s.contains("B") -> {
                return (
                        previxValue + splitString[1]
                            .replace("B", "")
                            .padEnd(3, '0') + "000000"
                        ).toLong()
            }

            s.contains("T") -> {
                return (
                        previxValue + splitString[1]
                            .replace("T", "")
                            .padEnd(3, '0') + "000000000"
                        ).toLong()
            }

            s.contains("k") -> {
                return (
                        previxValue + splitString[1]
                            .replace("k", "")
                            .padEnd(3, '0')
                        ).toLong()
            }
        }

        return 0
    } catch (e: Exception) {
        Log.w(
            "EXCEPTION", "tools.parseLongFromBigAbbreviatedNumbers() Failed to parse" +
                    " Long. Returning 0.0 by default. Cause: $str\n" + e.stackTraceToString()
        )
        return 0
    }
}


/**
 * Attempts to parse Int from a String. Will remove all commas and spaces. On failure will
 * return 1 as default
 */
internal fun parseInt(s: String): Int {
    return try {
        s.replace(" ", "").replace(",", "").toInt()
    } catch (e: Exception) {
        Log.w(
            "EXCEPTION", "tools.parseInt() Failed to parse Int. Returning 0\n"
                    + e.stackTraceToString()
        )
        return 0
    }
}


/**
 * Attempts to parse Double from a String. Will remove all commas and spaces. On failure will
 * return 0.0 as default
 */
internal fun parseDouble(str: String): Double {

    try {
        if (str.isNullOrEmpty()) {
            return 0.0
        }
        val s = str.replace(" ", "").replace("$", "").replace("%", "")

        // Check for N/A
        if (str.contains("N/A")) {
            return 0.0
        }

        if (!(s[s.lastIndex].isDigit()) && s.contains("-")) {
            return 0.0
        }
        return s
            .replace(",", "")
            .toDouble()
    } catch (e: Exception) {
        Log.w(
            "EXCEPTION", "tools.parseDouble() Failed due to argument '$str'...Returning Default 0.0\n"
                    + e.stackTraceToString()
        )
        return 0.0
    }
}


