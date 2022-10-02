package com.willor.ktstockdata.common

import java.io.File

class FileLoader {

    private val locTAG = FileLoader::class.java.name

    private val sep = File.separator


    private fun buildFilePath(vararg dirs: String): String {

        if (dirs.isEmpty()) {
            return ""
        }

        var base = dirs[0]

        if (dirs.size < 2) {
            return base
        }

        for (n in 1..dirs.lastIndex) {
            base += "$sep${dirs[n]}"
        }

        return base
    }


    private fun buildFilePath(dirs: List<String>): String {
        if (dirs.isEmpty()) {
            return ""
        }

        var base = dirs[0]

        if (dirs.size < 2) {
            return base
        }

        for (n in 1..dirs.lastIndex) {
            base += "$sep${dirs[n]}"
        }

        return base
    }


    fun loadFile(vararg pathStrings: String): File? {
        val p = buildFilePath()

        return try {
            File(p)
        } catch (e: Exception) {
            Log.d(
                "INFO", "$locTAG.loadFile triggered an Exception...\n" +
                        " ${e.stackTraceToString()}"
            )
            null
        }
    }
}


fun main() {
    val loader = FileLoader()


}
