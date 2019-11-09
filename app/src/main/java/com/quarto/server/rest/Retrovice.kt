package com.quarto.server.rest

import com.quarto.server.Action
import com.quarto.server.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Retrovice {

    @POST(value = "/GetAction")
    suspend fun getAction(@Body query: Query): Response<Action>?

}