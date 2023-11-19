import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.podcastapi.PodcastModel
import android.util.Log

class DBHandler(context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createPodcastsTableQuery = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, "
                + DESCRIPTION_COL + " TEXT,"
                + URL_COL + " TEXT,"
                + AUTHOR_COL + " TEXT,"
                + EPISODES_COL + " INT,"
                + LANGUAGE_COL + " TEXT,"
                + LATEST_RELEASE_DATE_COL + " TEXT,"
                + PUBLISHED_DATE_COL + " TEXT,"
                + GENRE_COL + " TEXT,"
                + IS_COMPLETE_COL + " TEXT,"
                + IS_EXPLICIT_COL + " TEXT)")

        val createSearchTableQuery = "CREATE TABLE $SEARCH_TABLE_NAME (" +
                "$SEARCH_ID_COL INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$SEARCH_QUERY_COL TEXT)"

        db.execSQL(createPodcastsTableQuery)
        db.execSQL(createSearchTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $SEARCH_TABLE_NAME")
        onCreate(db)
    }

    fun countSearchQueries(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $SEARCH_TABLE_NAME", null)
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        return count
    }

    fun addSearchQuery(query: String) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(SEARCH_QUERY_COL, query)
        }
        db.insert(SEARCH_TABLE_NAME, null, values)
        db.close()
    }

    fun readSearchQueries(): List<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $SEARCH_TABLE_NAME", null)
        val queries = mutableListOf<String>()

        if (cursor.moveToFirst()) {
            do {
                queries.add(cursor.getString(cursor.getColumnIndex(SEARCH_QUERY_COL)))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return queries
    }

    fun savePodcasts(podcasts: List<PodcastModel>) {
        val db = this.writableDatabase
        for (podcast in podcasts) {
            val values = ContentValues().apply {
                put(ID_COL, podcast.id)
                put(DESCRIPTION_COL, podcast.podcastDescription)
                put(URL_COL, podcast.podcastUrl)
                put(AUTHOR_COL, podcast.authorName)
                put(EPISODES_COL, podcast.episodeCount)
                put(LANGUAGE_COL, podcast.podcastLanguage)
                put(LATEST_RELEASE_DATE_COL, podcast.latestReleaseDate)
                put(PUBLISHED_DATE_COL, podcast.publishedDate)
                put(GENRE_COL, podcast.genre)
                put(IS_COMPLETE_COL, podcast.isComplete)
                put(IS_EXPLICIT_COL, podcast.isExplicit)
            }
            db.insert(TABLE_NAME, null, values)
        }
        db.close()
    }

    fun readPodcasts(): List<PodcastModel> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        val podcasts = mutableListOf<PodcastModel>()

        if (cursor.moveToFirst()) {
            do {
                val podcast = PodcastModel(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL)),
                    podcastDescription = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL)),
                    podcastUrl = cursor.getString(cursor.getColumnIndexOrThrow(URL_COL)),
                    authorName = cursor.getString(cursor.getColumnIndexOrThrow(AUTHOR_COL)),
                    episodeCount = cursor.getInt(cursor.getColumnIndexOrThrow(EPISODES_COL)),
                    podcastLanguage = cursor.getString(cursor.getColumnIndexOrThrow(LANGUAGE_COL)),
                    latestReleaseDate = cursor.getString(cursor.getColumnIndexOrThrow(LATEST_RELEASE_DATE_COL)),
                    publishedDate = cursor.getString(cursor.getColumnIndexOrThrow(PUBLISHED_DATE_COL)),
                    genre = cursor.getString(cursor.getColumnIndexOrThrow(GENRE_COL)),
                    isComplete = cursor.getString(cursor.getColumnIndexOrThrow(IS_COMPLETE_COL)),
                    isExplicit = cursor.getString(cursor.getColumnIndexOrThrow(IS_EXPLICIT_COL))
                )
                podcasts.add(podcast)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return podcasts
    }

    companion object {
        private const val DB_NAME = "podcast"
        private const val DB_VERSION = 2
        private const val TABLE_NAME = "podcasts"
        private const val ID_COL = "id"
        private const val DESCRIPTION_COL = "description"
        private const val URL_COL = "url"
        private const val AUTHOR_COL = "author_name"
        private const val EPISODES_COL = "episodes_count"
        private const val LANGUAGE_COL = "podcast_language"
        private const val LATEST_RELEASE_DATE_COL = "latest_release_date"
        private const val PUBLISHED_DATE_COL = "published_date"
        private const val GENRE_COL = "genre"
        private const val IS_COMPLETE_COL = "is_complete"
        private const val IS_EXPLICIT_COL = "is_explicit"
        private const val SEARCH_TABLE_NAME = "searches"
        private const val SEARCH_ID_COL = "id"
        private const val SEARCH_QUERY_COL = "query"
    }
}
