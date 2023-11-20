package com.example.podcastapi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.apollographql.apollo3.exception.ApolloException
import com.example.podcastapi.SearchForTermQuery
import com.example.podcastapi.SearchForTermQuery as SearchQuery
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

@Composable
fun SearchScreen() {
    // Initialize variables
    var query by remember { mutableStateOf("") } // User's search query
    var state by remember { mutableStateOf<SearchState>(SearchState.Empty) } // State of the search
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dbHandler = DbHandler(context) // Database handler

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input field for user's search query
        BasicTextField(
            value = query,
            onValueChange = {
                query = it // Update the query as the user types
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(8.dp),
            textStyle = TextStyle.Default.copy(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search button
        Button(onClick = {
            scope.launch {
                state = SearchState.Loading // Set the state to Loading
                try {
                    val response = apolloClient.query(SearchQuery(term = query)).execute()
                    if (response.hasErrors()) {
                        state = SearchState.Error(response.errors!!.first().message) // Handle Apollo query errors
                    } else {
                        state = SearchState.Success(response.data!!) // Set state to Success

                        // Save data to SQLite database
                        response.data?.searchForTerm?.podcastSeries?.forEach { podcast ->
                            dbHandler.insertData(podcast?.uuid ?: "", podcast?.name ?: "", podcast?.rssUrl ?: "")
                        }
                    }
                } catch (e: ApolloException) {
                    state = SearchState.Error(e.localizedMessage ?: "Unknown error") // Handle Apollo exception
                }
            }
        }) {
            Text(text = "Search")
        }

        // Display results based on search state
        when (val s = state) {
            SearchState.Loading -> CircularProgressIndicator() // Show loading indicator
            is SearchState.Error -> Text(text = s.message, color = Color.Red) // Show error message
            is SearchState.Success -> {
                // Retrieve data from the database and convert it to the required type
                val podcastList = mutableListOf<SearchForTermQuery.PodcastSeries?>()
                val cursor = dbHandler.getData()
                if (cursor.moveToFirst()) {
                    do {
                        val uuidIndex = cursor.getColumnIndex(DbHandler.COLUMN_UUID)
                        val nameIndex = cursor.getColumnIndex(DbHandler.COLUMN_NAME)
                        val rssUrlIndex = cursor.getColumnIndex(DbHandler.COLUMN_RSS_URL)

                        val uuid = cursor.getString(uuidIndex)
                        val name = cursor.getString(nameIndex)
                        val rssUrl = cursor.getString(rssUrlIndex)

                        // Convert PodcastEntry to SearchForTermQuery.PodcastSeries
                        val podcastSeries = SearchForTermQuery.PodcastSeries(uuid = uuid, name = name, rssUrl = rssUrl)
                        podcastList.add(podcastSeries)
                    } while (cursor.moveToNext())
                }
                cursor.close()

                // Display data in PodcastList composable
                PodcastList(data = podcastList)
            }
            SearchState.Empty -> {} // No action for empty state
        }
    }
}

// Composable function to display podcast data retrieved from the SQLite database
@Composable
fun PodcastList(data: List<SearchForTermQuery.PodcastSeries?>?) {
    data?.let {
        it.forEach { podcast ->
            Text(text = "Name: ${podcast?.name}, UUID: ${podcast?.uuid}, RSS URL: ${podcast?.rssUrl}")
        }
    }
}

// Define the possible search states
sealed interface SearchState {
    object Empty : SearchState
    object Loading : SearchState
    data class Error(val message: String) : SearchState
    data class Success(val data: SearchQuery.Data) : SearchState
}
