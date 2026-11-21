package com.studio23webdezign.mp3hunter.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.net.Uri

data class LocalAudioTrack(
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val albumArtUri: Uri?
)

object MediaStoreAudioLibrary {
    fun load(context: Context): List<LocalAudioTrack> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val tracks = mutableListOf<LocalAudioTrack>()
        context.contentResolver.query(
            collection,
            projection,
            "${MediaStore.Audio.Media.IS_MUSIC} != 0",
            null,
            "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                tracks += LocalAudioTrack(
                    uri = ContentUris.withAppendedId(collection, id),
                    title = cursor.getString(titleColumn).orEmpty().ifBlank { "Untitled track" },
                    artist = cursor.getString(artistColumn).orEmpty(),
                    album = cursor.getString(albumColumn).orEmpty(),
                    durationMs = cursor.getLong(durationColumn),
                    albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, cursor.getLong(albumIdColumn))
                )
            }
        }
        return tracks
    }
}
