package com.recommendersystem.recommender.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:8100")
@RestController
@RequestMapping("/recommender")
public class RecommenderController {
	@RequestMapping(value = "/{query}", method = RequestMethod.GET)
	public Map<String, Object> getVideoRecommendation(@PathVariable("query") String query) {
		Map<String, Object> response = new HashMap<>();

		response.put("videos", YoutubeSearchController.getVideos(query));
		response.put("success", true);

		return response;
	}
}
