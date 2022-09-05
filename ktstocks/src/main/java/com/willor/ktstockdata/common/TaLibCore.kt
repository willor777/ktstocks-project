package com.willor.ktstockdata.common

import com.tictactec.ta.lib.Core


internal class TaLibCore {



    companion object {
        private var taLibCore: Core? = null

        fun getTaLibCore(): Core{
            return buildTaLibCore()
        }

        private fun buildTaLibCore(): Core{
            if (taLibCore == null){
                taLibCore = Core()
            }
            return taLibCore!!
        }
    }
}