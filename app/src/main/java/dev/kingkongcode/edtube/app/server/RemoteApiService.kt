package dev.kingkongcode.edtube.app.server

import retrofit2.http.POST

interface RemoteApiService {
    @POST("/oauth2/v4/token")
    fun requestAccessToken()
}