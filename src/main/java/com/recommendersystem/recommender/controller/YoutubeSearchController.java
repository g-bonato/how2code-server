package com.recommendersystem.recommender.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.recommendersystem.recommender.models.LearningMaterial;

public class YoutubeSearchController {
	private static final String API_KEY = "AIzaSyBkQYdTMHP2d7Ay2qa-75LiIB8A6RAdQpw";

	public static List<LearningMaterial> getVideos(String query) {
		try {
			YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
					new HttpRequestInitializer() {
						public void initialize(HttpRequest request) throws IOException {
						}
					}).setApplicationName("RecommenderSystem").build();

			YouTube.Search.List search = youtube.search().list("id,snippet");

			search.setKey(API_KEY);
			search.setQ(query);
			search.setMaxResults((long) 10);
			search.setType("video");
			search.setOrder("rating");
			search.setFields("items(id/videoId,snippet/title,snippet/thumbnails/default/url)");

			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			List<LearningMaterial> learningMaterials = new ArrayList<>();

			for (SearchResult searchResult : searchResultList) {
				LearningMaterial learningMaterial = new LearningMaterial();

				learningMaterial.setTitle(searchResult.getSnippet().getTitle());
				learningMaterial.setVideoId(searchResult.getId().getVideoId());
				learningMaterial.setThumbnail(searchResult.getSnippet().getThumbnails().getDefault().getUrl());

				learningMaterials.add(learningMaterial);
			}

			return learningMaterials;
		} catch (Exception e) {
			System.err.println("Failed getting YoutubeVideos: " + e);
			return null;
		}
	}
}