package com.quarto.server

import com.google.gson.annotations.SerializedName
import com.quarto.Base
import com.quarto.Quarto
import com.quarto.Room
import com.quarto.Type

data class Query(
        @SerializedName(value = "rooms") internal var rooms: Array<Array<Room?>>,
        @SerializedName(value = "quartos") var quartos: Array<Quarto?>,
        @SerializedName(value = "queryType") var queryType: QueryType,
        @SerializedName(value = "picked") var picked: Int = -1
): Base(Type.QUERY) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Query

        if (!rooms.contentDeepEquals(other.rooms)) return false
        if (!quartos.contentEquals(other.quartos)) return false
        if (queryType != other.queryType) return false
        if (picked != other.picked) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rooms.contentDeepHashCode()
        result = 31 * result + quartos.contentHashCode()
        result = 31 * result + queryType.hashCode()
        result = 31 * result + picked
        return result
    }

}