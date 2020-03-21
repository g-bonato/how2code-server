package com.recommendersystem.recommender.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.utils.LogUtil;

@CrossOrigin
public class YoutubeSearchController {
	private static final String API_KEY = "AIzaSyBkQYdTMHP2d7Ay2qa-75LiIB8A6RAdQpw";
	private final static Boolean useMock = false;

	public static Map<String, Object> getVideos(String query, String pageToken) {
		if (useMock) {
			List<LearningMaterial> learningMaterials = new ArrayList<>();

			LearningMaterial learningMaterial = new LearningMaterial();

			learningMaterial.setTitle("title1");
			learningMaterial.setVideoId("dsemApPwZ00");
			learningMaterial.setThumbnail("thumbnail");

			learningMaterials.add(learningMaterial);

			learningMaterial = new LearningMaterial();

			learningMaterial.setTitle("title2");
			learningMaterial.setVideoId("C3ovalQhH88");
			learningMaterial.setThumbnail("thumbnail");

			learningMaterials.add(learningMaterial);

			learningMaterial = new LearningMaterial();

			learningMaterial.setTitle("title3");
			learningMaterial.setVideoId("aCIg6MBf-Uw");
			learningMaterial.setThumbnail("thumbnail");

			learningMaterials.add(learningMaterial);

			learningMaterial = new LearningMaterial();

			learningMaterial.setTitle("title4");
			learningMaterial.setVideoId("v5ahJxhruN0");
			learningMaterial.setThumbnail("thumbnail");

			learningMaterials.add(learningMaterial);

			learningMaterial = new LearningMaterial();

			learningMaterial.setTitle("title5");
			learningMaterial.setVideoId("lLvRmjVVkTY");
			learningMaterial.setThumbnail("thumbnail");

			learningMaterials.add(learningMaterial);

			Map<String, Object> response = new HashMap<String, Object>();

			response.put("learningMaterials", learningMaterials);
			response.put("nextPageToken", "testToken");

			return response;
		}

		try {
			YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
					new HttpRequestInitializer() {
						public void initialize(HttpRequest request) throws IOException {
						}
					}).setApplicationName("RecommenderSystem").build();

			YouTube.Search.List search = youtube.search().list("id,snippet");

			search.setKey(API_KEY);
			search.setQ(query);

			if (pageToken != null) {
				search.setPageToken(pageToken);
			}

			search.setMaxResults((long) 100);
			search.setType("video");
			search.setOrder("rating");
			search.setPart("snippet");
			search.setFields(
					"nextPageToken,items(id/videoId,snippet/title,snippet/thumbnails/default/url)");

			SearchListResponse searchResponse = search.execute();
			List<SearchResult> searchResultList = searchResponse.getItems();
			List<LearningMaterial> learningMaterials = new ArrayList<>();

			for (SearchResult searchResult : searchResultList) {
				LearningMaterial learningMaterial = new LearningMaterial();

				learningMaterial.setTitle(searchResult.getSnippet().getTitle());
				learningMaterial.setVideoId(searchResult.getId().getVideoId());
				learningMaterial.setThumbnail(
						searchResult.getSnippet().getThumbnails().getDefault().getUrl());

				learningMaterials.add(learningMaterial);
			}

			Map<String, Object> response = new HashMap<String, Object>();

			response.put("learningMaterials", learningMaterials);
			response.put("nextPageToken", searchResponse.getNextPageToken());

			LogUtil.logDebug("Getting youtube recommendations " + response.toString());

			return response;
		} catch (Exception e) {
			LogUtil.logError("Failed getting YoutubeVideos: " + e);
			return null;
		}
	}

	public static String getNextPageToken(String query, String pageToken) {
		try {
			YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
					new HttpRequestInitializer() {
						public void initialize(HttpRequest request) throws IOException {
						}
					}).setApplicationName("RecommenderSystem").build();

			YouTube.Search.List search = youtube.search().list("id,snippet");

			search.setKey(API_KEY);
			search.setQ(query);

			if (pageToken != null) {
				search.setPageToken(pageToken);
			}

			search.setMaxResults((long) 50);
			search.setType("video");
			search.setOrder("rating");
			search.setFields("nextPageToken");

			SearchListResponse searchResponse = search.execute();

			return searchResponse.getNextPageToken();
		} catch (Exception e) {
			System.err.println("Failed getting Youtube nextPageToken: " + e);
			return null;
		}
	}
}
