package com.recommendersystem.recommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.models.Rating;

public interface RatingRepository extends MongoRepository<Rating, String> {
	@Override
	public Optional<Rating> findById(String id);

	public List<Rating> findByUserId(String userId);

	@Query("{ 'userId' : ?0, 'learningMaterial.videoId' : ?1 }")
	public List<Rating> findByUserIdAndVideoId(String userId, String videoId);

	@Query("{ 'learningMaterial' : ?0 }")
	public List<Rating> findByVideoId(LearningMaterial learningMaterial);

	@Query("{ 'userId' : { $not : { $eq : ?1 } }, 'learningMaterial.videoId' : ?0 }")
	public List<Rating> findByVideoIdAndNotUserId(String videoId, String userId);
}
