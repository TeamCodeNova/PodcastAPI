package com.example.podcastapi

import java.util.Date

data class PodcastModel (
    var id: Int,
    var podcastDescription: String,
    var podcastUrl: String,
    var authorName: String,
    var episodeCount: Int,
    var podcastLanguage: String,
    var latestReleaseDate: String,
    var publishedDate: String,
    var contentType: String,
    var isComplete: String,
    var isExplicit: String
)