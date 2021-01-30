package dev.kingkongcode.edtube.server

class Config {
    companion object{
        const val CLIENT_ID = ""
        const val CLIENT_SECRET = ""
        const val API_KEY = ""
        

        /**
         * Youtube
         * */
        //const val YOUTUBE_PLAYLIST_REQUEST = "https://www.googleapis.com/youtube/v3/playlists" // missing psrt =     ?part=snippet%2CcontentDetails%2Cplayer%2Cid&maxResults=25&mine=true&key=[YOUR_API_KEY] HTTP/1.1
        //const val YOUTUBE_PLAYLIST_ITEMS_REQUEST = "https://www.googleapis.com/youtube/v3/playlistItems"
        //const val YOUTUBE_PLAYLIST_ITEMS_VIDEO_REQ = "https://www.googleapis.com/youtube/v3/videos"
        const val YOUTUBE_AUTH_READONLY = "https://www.googleapis.com/auth/youtube.readonly"
        const val YOUTUBE_AUTH_UPLOAD = "https://www.googleapis.com/auth/youtube.upload"
    }

}