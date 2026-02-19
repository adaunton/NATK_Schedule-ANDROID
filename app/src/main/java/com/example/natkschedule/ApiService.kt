package com.example.natkschedule

import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/Schedule/groups")
    suspend fun getGroups(): List<Group>

    @GET("api/Schedule/lessons/{groupId}")
    suspend fun getSchedule(@Path("groupId") groupId: Int): List<ScheduleItem>
}