package com.example.podcastapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.podcastapi.ui.theme.PodcastAPITheme
import androidx.compose.ui.Modifier
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodcastAPITheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SearchScreen()
                }
            }
        }
    }
    fun dbExists(args: Array<String>)
    {
//        var filename = "../../../../../data/podcast_db.sqlite"
//        var file = File(filename)
//        var fileExists = file.exists()
//
//        if(fileExists){
//            //Do nothing
//        } else {
//            var isCreated = file.createNewFile()
//            if (isCreated)
//
//            else
//                println("File is not created")
//        }
    }
}
