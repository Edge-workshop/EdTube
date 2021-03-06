package dev.kingkongcode.edtube.model.dataserver.request

data class AccessTokenDataRequest(
    val idToken: String?,
    val deviceCode: String?
)
