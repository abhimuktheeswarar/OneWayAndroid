package com.msa.oneway

/**
 * Created by Abhi Muktheeswarar on 21-August-2020
 */

object Utils {

    fun getStringFromFile(fileName: String): String {
        return ClassLoader.getSystemResource(fileName).readText()
    }

}