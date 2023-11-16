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
import com.example.podcastapi.SearchForTermQuery
import com.example.podcastapi.SearchForTermQuery as SearchQuery
import kotlinx.coroutines.launch



@Composable
fun SearchScreen() {
    var query by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<SearchState>(SearchState.Empty) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dbHandler = remember { DBHandler(context) }
    val searchQueriesCount = dbHandler.countSearchQueries()
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
        Text("Number of Searches: $searchQueriesCount")
        Button(onClick = {
            dbHandler.addSearchQuery(query) // Store the search query
            scope.launch {
                state = SearchState.Loading
                try {
                    // ... Existing search logic ...
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
            is SearchState.Success -> PodcastList(data = s.data.searchForTerm?.podcastSeries)
            SearchState.Empty -> {}
        }
    }
}

@Composable
fun PodcastList(data: List<SearchForTermQuery.PodcastSeries?>?) {
    data?.let {
        it.forEach { podcast ->
            Text(text = "Name: ${podcast?.name}, UUID: ${podcast?.uuid}, RSS URL: ${podcast?.rssUrl}")
        }
    }
}

private sealed interface SearchState {
    object Empty : SearchState
    object Loading : SearchState
    data class Error(val message: String) : SearchState
    data class Success(val data: SearchQuery.Data) : SearchState
}
