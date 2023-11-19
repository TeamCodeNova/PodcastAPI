package com.example.podcastapi

import DBHandler
import androidx.compose.ui.platform.LocalContext
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
import com.example.podcastapi.SearchForTermQuery as SearchQuery
import kotlinx.coroutines.launch

@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<SearchState>(SearchState.Empty) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dbHandler = remember { DBHandler(context) }
    var search = false
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = {
                query = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(alpha = 0.2f))
                .padding(8.dp),
            textStyle = TextStyle.Default.copy(color = Color.Black)
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Number of Searches: ${dbHandler.countSearchQueries()}")
        if(!search)
        {
            ListPreviousSearches(data = dbHandler.readSearchQueries(), length = dbHandler.countSearchQueries())
        }
        Button(onClick = {
            dbHandler.addSearchQuery(query) // Store the search query
            search = true
            scope.launch {
                state = SearchState.Loading
                try {
                    val response = apolloClient.query(SearchQuery(term = query)).execute()
                    if (response.hasErrors()) {
                        state = SearchState.Error(response.errors!!.first().message)
                    } else {
                        val podcastSeriesList = response.data?.searchForTerm?.podcastSeries
                        if (podcastSeriesList != null) {
                            val podcasts = podcastSeriesList.mapNotNull { series ->
                                series?.let {
                                    PodcastModel(
                                        id = it.uuid?.toIntOrNull() ?: 0,
                                        podcastDescription = it.name ?: "",
                                        podcastUrl = it.rssUrl ?: "",
                                        authorName = "Unknown", // Placeholder
                                        episodeCount = 0, // Placeholder
                                        podcastLanguage = "Unknown", // Placeholder
                                        latestReleaseDate = "Unknown", // Placeholder
                                        publishedDate = "Unknown", // Placeholder
                                        genre = "Unknown", // Placeholder
                                        isComplete = "Unknown", // Placeholder
                                        isExplicit = "Unknown" // Placeholder
                                    )
                                }
                            }
                            dbHandler.savePodcasts(podcasts)
                        }
                        // Read the latest podcasts from DB and update the UI
                        val podcastsFromDb = dbHandler.readPodcasts()
                        state = SearchState.Success(podcastsFromDb)
                    }
                } catch (e: ApolloException) {
                    state = SearchState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }) {
            Text(text = "Search")
        }

        when (val s = state) {
            SearchState.Loading -> CircularProgressIndicator()
            is SearchState.Error -> Text(text = s.message, color = Color.Red)
            is SearchState.Success -> PodcastList(data = s.data)
            SearchState.Empty -> {}
        }
    }
}

@Composable
fun PodcastList(data: List<PodcastModel>) {
    data.forEach { podcast ->
        Text(text = "Description: ${podcast.podcastDescription}, URL: ${podcast.podcastUrl}, Author: ${podcast.authorName}")
    }
}
@Composable
fun ListPreviousSearches(data: List<String>, length: Int)
{
    var displayLength = length
    if(length > 5)
    {
        displayLength = 5
    }
    Text(text = "Showing last ${displayLength}")
    //Dont know if this works
    data.reversed().take(displayLength).let {
        it.forEach { item ->
            //I dont have a model to go off of, I do not know what values to put here.
            Text(text = "${item?.toString()}");
        }
    }
}

private sealed interface SearchState {
    object Empty : SearchState
    object Loading : SearchState
    data class Error(val message: String) : SearchState
    data class Success(val data: List<PodcastModel>) : SearchState
}
