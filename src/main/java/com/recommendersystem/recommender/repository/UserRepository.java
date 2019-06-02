package com.recommendersystem.recommender.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.recommendersystem.recommender.models.User;

public interface UserRepository extends MongoRepository<User, String> {
	public Optional<User> findById(String id);

	public List<User> findByEmail(String email);
	public List<User> findByEmailAndPassword(String email, String password);
}
