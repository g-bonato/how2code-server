package com.recommendersystem.recommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.recommendersystem.recommender.models.Rating;

public interface RatingRepository extends MongoRepository<Rating, String> {
	@Override
	public Optional<Rating> findById(String id);

	@Query("{ 'userId' : ?0, 'learningMaterial' : { 'videoId' : ?1 } }")
	public List<Rating> findByUserIdAndVideoId(String userId, String videoId);

	@Query("{ 'learningMaterial' : { 'videoId' : ?0 } }")
	public List<Rating> findByVideoId(String videoId);
}
