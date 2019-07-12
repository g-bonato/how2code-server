package com.recommendersystem.recommender.processor;

import java.util.List;
import java.util.Set;

import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;

public class RatedYoutubeItemUserId implements Runnable {

	private LearningMaterial youtubeItem;
	private String currentUserId;
	private Set<String> userIds;
	private RatingRepository repository;

	public RatedYoutubeItemUserId(LearningMaterial youtubeItem, String currentUserId, Set<String> userIds,
			RatingRepository repository) {

		this.youtubeItem = youtubeItem;
		this.currentUserId = currentUserId;
		this.userIds = userIds;
		this.repository = repository;

	}

	@Override
	public void run() {
		List<Rating> youtubeItemRatings = repository.findByVideoIdAndNotUserId(youtubeItem, currentUserId);

		for (Rating itemRating : youtubeItemRatings) {
			synchronized (userIds) {
				userIds.add(itemRating.getUserId());
			}
		}
	}

}
