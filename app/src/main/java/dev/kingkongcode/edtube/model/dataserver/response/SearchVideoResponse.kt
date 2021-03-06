package dev.kingkongcode.edtube.model.dataserver.response

import com.google.api.services.youtube.model.PlaylistItem

data class SearchVideoResponse(
    val searchResultList: ArrayList<PlaylistItem>
)
