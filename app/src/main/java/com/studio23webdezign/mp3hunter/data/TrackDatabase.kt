package com.studio23webdezign.mp3hunter.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Entity(tableName = "tracks")
data class SavedTrack(
    @PrimaryKey val pageUrl: String,
    val title: String,
    val artist: String?,
    val sourceName: String,
    val directAudioUrl: String?,
    val isPermittedDownload: Boolean,
    val isFavorite: Boolean = false,
    val lastSeenAt: Long = System.currentTimeMillis()
)

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(track: SavedTrack)
    @Query("SELECT * FROM tracks WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE") suspend fun favorites(): List<SavedTrack>
    @Query("SELECT * FROM tracks ORDER BY lastSeenAt DESC LIMIT 50") suspend fun history(): List<SavedTrack>
    @Query("UPDATE tracks SET isFavorite = :favorite WHERE pageUrl = :url") suspend fun setFavorite(url: String, favorite: Boolean)
}

@Database(entities = [SavedTrack::class], version = 1, exportSchema = false)
abstract class TrackDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
}
