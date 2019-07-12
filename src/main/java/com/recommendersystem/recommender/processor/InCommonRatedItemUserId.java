package com.recommendersystem.recommender.processor;

import java.util.List;
import java.util.Set;

import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;

public class InCommonRatedItemUserId implements Runnable {

	private Rating rating;
	private Set<String> userIds;
	private RatingRepository repository;

	public InCommonRatedItemUserId(Rating rating, Set<String> userIds, RatingRepository repository) {
		this.rating = rating;
		this.userIds = userIds;
		this.repository = repository;
	}

	@Override
	public void run() {
		List<Rating> inCommonRatings = repository.findByVideoIdAndNotUserId(rating.getLearningMaterial(),
				rating.getUserId());

		for (Rating inCommonRating : inCommonRatings) {
			synchronized (userIds) {
				userIds.add(inCommonRating.getUserId());
			}
		}
	}

}
