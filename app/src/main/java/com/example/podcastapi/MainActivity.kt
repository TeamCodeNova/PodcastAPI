package com.example.podcastapi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.podcastapi.ui.theme.PodcastAPITheme
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody


class MainViewModel : ViewModel() {

    private val _podcastNames = MutableStateFlow(listOf<String>())
    val podcastNames: Flow<List<String>> = _podcastNames

    private val api = Retrofit.Builder()
        .baseUrl("https://api.taddy.org")
        .addConverterFactory(GsonConverterFactory.create())
        .client(createOkHttpClient()) // Add the OkHttpClient with headers
        .build()
        .create(PodcastApi::class.java)

    fun fetchPodcastData(query: String) {
        viewModelScope.launch {
            try {
                val jsonRequest = """
                {
                    "query": "{ searchForTerm(term: \\"$query\\", filterForTypes: PODCASTSERIES) { searchId podcastSeries { uuid name rssUrl } } }"
                }
            """.trimIndent()

                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonRequest)

                val response = api.fetchPodcastData(requestBody)

                println("RESPONSE: ")
                println(response)
                // Check if the response and its data property are not null
                if (response != null && response.data != null) {
                    val podcastSeries = response.data.searchForTerm?.podcastSeries
                    if (podcastSeries != null) {
                        _podcastNames.value = podcastSeries.mapNotNull { it?.name }
                    }
                }
            } catch (e: Exception) {
                // Handle error
                Log.e("MainViewModel", "Error fetching podcast data", e)
            }
        }
    }

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("X-USER-ID", "599") // Replace with your actual user ID
                    .header(
                        "X-API-KEY",
                        "bfc65097269c742516dad0f0f2da4d4e1958eda6cadabc576d44999f512012649e41897fbb21449decf977c1d4f1b14663"
                    ) // Replace with your actual API key
                val request = requestBuilder.build()
                chain.proceed(request)
            }
            .build()
    }
}

interface PodcastApi {
    @POST("/graphql")
    suspend fun fetchPodcastData(@Body requestBody: RequestBody): ApiResponse
}

@JsonClass(generateAdapter = true)
data class ApiResponse(
    val data: SearchForTerm
)

@JsonClass(generateAdapter = true)
data class SearchForTerm(
    val searchForTerm: PodcastSeries
)

@JsonClass(generateAdapter = true)
data class PodcastSeries(
    val podcastSeries: List<PodcastDetail>
)

@JsonClass(generateAdapter = true)
data class PodcastDetail(
    @Json(name = "uuid") val uuid: String,
    @Json(name = "name") val name: String,
    @Json(name = "rssUrl") val rssUrl: String
)

@Composable
fun PodcastScreen(viewModel: MainViewModel = viewModel()) {
    val podcastNames by viewModel.podcastNames.collectAsState(initial = listOf())
    var query by remember { mutableStateOf("") }

    Column {
        TextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Podcast Query") }
        )
        Button(onClick = { viewModel.fetchPodcastData(query) }) {
            Text("Search")
        }
        LazyColumn {
            this.items(podcastNames) { name: String ->
                Text(name)
            }
        }
    }
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PodcastAPITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PodcastScreen()
                }
            }
        }

    }
}