package dev.kingkongcode.edtube.app.server

class Config {
    companion object{
//        const val CLIENT_ID = ""
//        const val CLIENT_SECRET = ""
//        const val API_KEY = ""

        const val CLIENT_ID = "1033471762786-mps5q8anslk0aj5ojbujgc3183jgon8j.apps.googleusercontent.com"
        const val CLIENT_SECRET = "3YT2hRfVA8U7pSXWCGyc6LZv"
        const val API_KEY = "AIzaSyA6okESac-Fhdlx6yqRG5QkSKswXxBgh5Y"


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