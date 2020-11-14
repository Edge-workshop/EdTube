package dev.kingkongcode.edtube.server

class Config {
    companion object{
        val current = Config()
    }

    val CLIENT_ID = "CLIENT_ID"
    val CLIENT_SECRET = "CLIENT_SECRET"
    val API_KEY = "API_KEY"
    val OAUTH2_URL = "OAUTH2_URL"

    /**
     * Youtube
     * */
    val YOUTUBE_PLAYLIST_REQUEST = "https://www.googleapis.com/youtube/v3/playlists" // missing psrt =     ?part=snippet%2CcontentDetails%2Cplayer%2Cid&maxResults=25&mine=true&key=[YOUR_API_KEY] HTTP/1.1
    val YOUTUBE_PLAYLIST_ITEMS_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems"
    val YOUTUBE_PLAYLIST_ITEMS_VIDEO_REQ = "https://www.googleapis.com/youtube/v3/videos"
    val YOUTUBE_AUTH_READONLY = "https://www.googleapis.com/auth/youtube.readonly"
    val YOUTUBE_AUTH_UPLOAD = "https://www.googleapis.com/auth/youtube.upload"
}