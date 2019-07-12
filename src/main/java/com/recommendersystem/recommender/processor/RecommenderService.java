package com.recommendersystem.recommender.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;

@Service
public class RecommenderService {
	
	@Async("youtubeItemUserIdsProcessor")
	public static Set<String> ratedYoutubeItemUserIdsProcessor(LearningMaterial youtubeItem,
			RatingRepository repository, String currentUserId) {

		CompletableFuture<String> userIds = new CompletableFuture<String>;

		List<Rating> youtubeItemRatings = repository.findByVideoIdAndNotUserId(youtubeItem, currentUserId);

		for (Rating itemRating : youtubeItemRatings) {
			userIds.add(itemRating.getUserId());
		}
		
		return userIds;
	}
}
