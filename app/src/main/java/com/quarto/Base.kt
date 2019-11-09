package com.quarto

import com.google.gson.annotations.SerializedName
import java.io.Serializable

open class Base(@SerializedName(value = "type") var type: Type = Type.UNKNOWN): Serializable