package dev.kingkongcode.edtube.model.dataserver.response

import com.google.api.services.youtube.model.PlaylistItem

data class SelectedPlaylistDetailsResponse(
    val selectedPList: ArrayList<PlaylistItem>?
)
