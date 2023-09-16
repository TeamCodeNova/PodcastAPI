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
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }
    var state by remember { mutableStateOf<SearchState>(SearchState.Empty) }
    val scope = rememberCoroutineScope()

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

        Button(onClick = {
            scope.launch {
                state = SearchState.Loading
                try {
                    val response = apolloClient.query(SearchQuery(term = query)).execute()
                    if (response.hasErrors()) {
                        state = SearchState.Error(response.errors!!.first().message)
                    } else {
                        state = SearchState.Success(response.data!!)
                    }
                } catch (e: ApolloException) {
                    state = SearchState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }) {
            Text(text = "Search")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            navController.navigate("about")
        }) {
            Text(text = "Go to About")
        }

        Spacer(modifier = Modifier.height(8.dp))

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
