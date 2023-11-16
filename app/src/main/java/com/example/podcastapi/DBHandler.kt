import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.podcastapi.PodcastModel
import android.util.Log

class DBHandler // creating a constructor for our database handler.
    (context: Context?) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    // below method is for creating a database by running a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // on below line we are creating an sqlite query and we are
        // setting our column names along with their data types.
        val query = ("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY, " // Id is grabbed from the api
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

        // at last we are calling a exec sql method to execute above sql query
        db.execSQL(query)
    }

    // this method is use to add new podcasts to our sqlite database.
    fun addNewPodcast(
        id: Int,
        podcastDescription: String,
        podcastUrl: String,
        authorName: String,
        episodeCount: Int,
        podcastLanguage: String,
        latestReleaseDate: String,
        publishedDate: String,
        genre: String,
        isComplete: String,
        isExplicit: String
    ) {
        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        val db = this.writableDatabase
        // on below line we are creating a
        // variable for content values.
        val values = ContentValues()
        // on below line we are passing all values
        // along with its key and value pair.
        values.put(ID_COL, id)
        values.put(DESCRIPTION_COL, podcastDescription)
        values.put(URL_COL, podcastUrl)
        values.put(AUTHOR_COL, authorName)
        values.put(EPISODES_COL, episodeCount)
        values.put(LANGUAGE_COL, podcastLanguage)
        values.put(LATEST_RELEASE_DATE_COL, latestReleaseDate)
        values.put(PUBLISHED_DATE_COL, publishedDate)
        values.put(GENRE_COL, genre)
        values.put(IS_COMPLETE_COL, isComplete)
        values.put(IS_EXPLICIT_COL, isExplicit)
        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values)
        // at last we are closing our
        // database after adding database.
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }
    fun deleteDatabase()
    {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db)
    }


    companion object {
        // creating a constant variables for our database.
        // below variable is for our database name.
        private const val DB_NAME = "podcast"

        // below int is our database version
        private const val DB_VERSION = 1

        // below variable is for our table name.
        private const val TABLE_NAME = "podcasts"

        // below variable is for our id column.
        private const val ID_COL = "id"

        // below variable for our podcast's description column.
        private const val DESCRIPTION_COL = "description"

        // below variable is for our podcast url column
        private const val URL_COL = "url"

        // below variable is for our podcast's author column
        private const val AUTHOR_COL = "author_name"

        // below variable is for our podcast's episode count
        private const val EPISODES_COL = "episodes_count"

        // below variable is for our language column
        private const val LANGUAGE_COL = "podcast_language"

        // below variable is for our podcasts latest release date column
        private const val LATEST_RELEASE_DATE_COL = "latest_release_date"

        // below variable is for our podcasts published date column
        private const val PUBLISHED_DATE_COL = "published_date"

        // below variable is for our podcasts genre(s) column
        private const val GENRE_COL = "genre"

        // below variable is for if our podcast is completed column
        private const val IS_COMPLETE_COL = "is_complete"

        // below variable is for if our podcast is explicit column
        private const val IS_EXPLICIT_COL = "is_explicit"
    }

    // gets data from db and outputs it as an ArrayList
    fun readPodcasts(): ArrayList<PodcastModel>? {
        // on below line we are creating a database for reading our database.
        val db = this.readableDatabase

        // on below line we are creating a cursor with query to read data from database.
        val cursorPodcasts: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        // on below line we are creating a new array list.
        val PodcastModelArrayList: ArrayList<PodcastModel> = ArrayList()

        // moving our cursor to first position.
        if (cursorPodcasts.moveToFirst()) {
            do {
                // on below line we are adding the data from cursor to our array list.
                PodcastModelArrayList.add(
                    PodcastModel(
                        cursorPodcasts.getInt(0),
                        cursorPodcasts.getString(1),
                        cursorPodcasts.getString(2),
                        cursorPodcasts.getString(3),
                        cursorPodcasts.getInt(4),
                        cursorPodcasts.getString(5),
                        cursorPodcasts.getString(6),
                        cursorPodcasts.getString(7),
                        cursorPodcasts.getString(8),
                        cursorPodcasts.getString(9),
                        cursorPodcasts.getString(10),

                    )
                )
            } while (cursorPodcasts.moveToNext())
            // moving our cursor to next.
        }
        // at last closing our cursor and returning our array list.
        cursorPodcasts.close()
        return PodcastModelArrayList
    }
    fun logPodcasts(context: Context)
    {
        Log.v("dbTest","Locating DB");
        lateinit var podcastList: List<PodcastModel>
        podcastList = ArrayList<PodcastModel>()

        podcastList = readPodcasts()!!
        if(podcastList.isNotEmpty()) {
            Log.v("dbTest","Printing DB $podcastList");

            for(item in podcastList){
                Log.v("dbTest", "$item");
            }
        }
        else
        {
            Log.v("dbTest","Error: No items in DB");
        }


    }
}
