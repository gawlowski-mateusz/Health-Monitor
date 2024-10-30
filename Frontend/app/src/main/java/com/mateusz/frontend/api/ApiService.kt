package com.mateusz.frontend.api

import com.mateusz.frontend.model.WalkingSession
import retrofit2.http.GET

interface ApiService {
    @GET("walking")
    suspend fun getWalkingSessions(): List<WalkingSession>
}
