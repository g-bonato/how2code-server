package com.recommendersystem.recommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.recommendersystem.recommender.models.User;

public interface UserRepository extends MongoRepository<User, String> {
	public List<User> findByEmail(String email);

	public Optional<User> findByEmailAndPassword(String email, String password);

	@Query("{ 'session.sessionId' : ?0 }")
	public Optional<User> findBySessionId(String sessionId);

	@Override
	public Optional<User> findById(String id);
}
