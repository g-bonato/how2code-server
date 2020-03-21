package com.recommendersystem.recommender.processor;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;
import com.recommendersystem.recommender.utils.LogUtil;

public class AllRatings implements Runnable {

	private Set<Rating> ratings;
	private RatingRepository repository;
	private Integer index;

	private static final Integer NUMBER_OF_RATINGS_PER_PAGE = 20000;

	public AllRatings(Set<Rating> ratings, Integer index, RatingRepository repository) {
		this.index = index;
		this.ratings = ratings;
		this.repository = repository;
	}

	@Override
	public void run() {
		Iterable<Rating> listRatings = repository.findAll(
				RatingRepository.PageSpecification.constructPageSpecification(index, NUMBER_OF_RATINGS_PER_PAGE));
		
		for (Rating rating : listRatings) {
			synchronized (ratings) {
				ratings.add(rating);
			}
		}
	}

}
