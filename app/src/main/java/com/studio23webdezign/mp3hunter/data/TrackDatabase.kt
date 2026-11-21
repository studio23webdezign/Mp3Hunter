package com.studio23webdezign.mp3hunter.data

import androidx.room.Dao
import androidx.room.ColumnInfo
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tracks")
data class SavedTrack(
    @PrimaryKey val pageUrl: String,
    val title: String,
    val artist: String?,
    val sourceName: String,
    val directAudioUrl: String?,
    @ColumnInfo(name = "isPermittedDownload") val isDownloadAvailable: Boolean,
    val isFavorite: Boolean = false,
    val lastSeenAt: Long = System.currentTimeMillis()
)

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(track: SavedTrack)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfMissing(track: SavedTrack)

    @Query("UPDATE tracks SET title = :title, artist = :artist, sourceName = :sourceName, directAudioUrl = :audioUrl, isPermittedDownload = :available, lastSeenAt = :seenAt WHERE pageUrl = :url")
    suspend fun updateSearchMetadata(url: String, title: String, artist: String?, sourceName: String, audioUrl: String?, available: Boolean, seenAt: Long)

    @Transaction
    suspend fun recordSearch(track: SavedTrack) {
        insertIfMissing(track)
        updateSearchMetadata(track.pageUrl, track.title, track.artist, track.sourceName, track.directAudioUrl, track.isDownloadAvailable, track.lastSeenAt)
    }

    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    suspend fun favorites(): List<SavedTrack>

    @Query("SELECT pageUrl FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun favoriteUrls(): Flow<List<String>>

    @Query("SELECT isFavorite FROM tracks WHERE pageUrl = :url LIMIT 1")
    suspend fun isFavorite(url: String): Boolean?

    @Query("SELECT * FROM tracks ORDER BY lastSeenAt DESC LIMIT 50")
    suspend fun history(): List<SavedTrack>

    @Query("UPDATE tracks SET isFavorite = :favorite WHERE pageUrl = :url")
    suspend fun setFavorite(url: String, favorite: Boolean)
}

@Database(entities = [SavedTrack::class], version = 1, exportSchema = false)
abstract class TrackDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
