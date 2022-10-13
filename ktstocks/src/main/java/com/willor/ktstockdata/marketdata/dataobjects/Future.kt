package com.willor.ktstockdata.marketdata.dataobjects

import com.willor.ktstockdata.common.Log
import com.willor.ktstockdata.common.parseDouble
import com.willor.ktstockdata.common.parseLongFromBigAbbreviatedNumbers
import com.willor.ktstockdata.common.w
import java.lang.Exception


data class Future(
    val ticker: String,
    val name: String,
    val nameAndExpiration: String,
    val lastPrice: Double,
    val changeDollar: Double,
    val changePercent: Double,
    val volumeToday: Long,
    val volumeAvgThirtyDay: Long,
) {
    internal companion object {

        /**
         * Using the ticker, Determine the Future's simple name (No expiry included)
         */
        private fun getFutureName(ticker: String): String{

            return when (ticker) {
                "ES=F" -> {
                    "E-Mini S&P 500"
                }

                "YM=F" -> {
                    "Mini Dow Jones"
                }

                "NQ=F" -> {
                    "Nasdaq 100"
                }

                "RTY=F" -> {
                    "E-Mini Russell 2000"
                }

                "ZB=F" -> {
                    "U.S. Treasury Bond"
                }

                "ZN=F" -> {
                    "10-Year T-Note"
                }

                "ZF=F" -> {
                    "5-Year T-Note"
                }

                "ZT=F" -> {
                    "2-Year T-Note"
                }

                "GC=F" -> {
                    "Gold"
                }

                "MGC=F" -> {
                    "Micro Gold"
                }

                "SI=F" -> {
                    "Silver"
                }

                "SIL=F" -> {
                    "Micro Silver"
                }

                "CL=F" -> {
                    "Crude Oil"
                }

                "RB=F" -> {
                    "RBOB Gasoline"
                }

                "BZ=F" -> {
                    "Brent Crude Oil"
                }
                "PL=F" ->{
                    "Platinum"
                }
                "HG=F" ->{
                    "Copper"
                }
                "PA=F" ->{
                    "Palladium"
                }
                "HO=F" ->{
                    "Heating Oil"
                }
                "NG=F" ->{
                    "Natural Gas"
                }
                "B0=F" ->{      // Pretty sure this is meant to be a O, not a 0
                    "Mont Belvieu LDH Propane"
                }
                "BO=F" ->{      // So i'm registering it's ticker for both
                    "Mont Belvieu LDH Propane"
                }
                "ZC=F" ->{
                    "Corn"
                }
                "ZO=F" ->{
                    "Oat"
                }
                "KE=F" ->{
                    "KC HRW Wheat"
                }
                "ZR=F" ->{
                    "Rough Rice"
                }
                "ZM=F" ->{
                    "Soybean Meal"
                }
                "ZL=F" ->{
                    "Soybean Oil"
                }
                "ZS=F" ->{
                    "Soybean"
                }
                "GF=F" ->{
                    "Feeder Cattle"
                }
                "HE=F" ->{
                    "Lean Hogs"
                }
                "LE=F" ->{
                    "Live Cattle"
                }
                "CC=F" ->{
                    "Cocoa"
                }
                "KC=F" ->{
                    "Coffee"
                }
                "CT=F" ->{
                    "Cotton"
                }
                "OJ=F" ->{
                    "Orange Juice"
                }
                "LBS=F" ->{
                    "Lumber"
                }
                "SB=F" ->{
                    "Sugar"
                }
                else -> {
                    ""
                }
            }
        }

        fun createFromList(data: List<String>): Future? {
            try{
                val ticker = data[0].replace(" ", "")

                val name = getFutureName(ticker).takeIf {
                    it.isNotBlank()
                } ?: return null

                return Future(
                    ticker = ticker,
                    name = name,
                    nameAndExpiration = data[1],
                    lastPrice = parseDouble(data[2]),
                    changeDollar = parseDouble(data[4]),
                    changePercent = parseDouble(data[5].substringBefore("%")),
                    volumeToday = parseLongFromBigAbbreviatedNumbers(data[6]),
                    volumeAvgThirtyDay = parseLongFromBigAbbreviatedNumbers(data[7]),
                )
            }catch (e: Exception){
                Log.w("Future", "Failed to create data object 'Future' from list data." +
                        " List Data: $data. Exception: $e\nStacktrace: ${e.stackTraceToString()}")
                return null
            }
        }
    }
}


