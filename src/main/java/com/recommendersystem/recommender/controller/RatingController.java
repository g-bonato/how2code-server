package com.recommendersystem.recommender.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.models.User;
import com.recommendersystem.recommender.repository.RatingRepository;
import com.recommendersystem.recommender.repository.UserRepository;

@CrossOrigin
@RestController
@RequestMapping("/rating")
public class RatingController {
	@Autowired
	private RatingRepository repository;

	@Autowired
	private UserRepository userRepository;

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Map<String, Object> rating(@Valid @RequestBody Rating rating,
			@RequestHeader(name = "Authorization", required = true, defaultValue = "") String authorization) {

		Map<String, Object> response = new HashMap<>();

		Optional<User> userAux = userRepository.findBySessionId(authorization);

		if (!userAux.isPresent() || !SessionController.isValidSession(authorization, userAux.get())) {
			response.put("message", "Você deve estar logado!");
			response.put("success", false);

			return response;
		}

		String userId = userAux.get().getId();

		Rating ratingAux = new Rating();

		ratingAux.set_id(ObjectId.get());

		List<Rating> ratingsList = repository.findByUserIdAndVideoId(userId, rating.getLearningMaterial().getVideoId());

		if (ratingsList != null && !ratingsList.isEmpty()) {
			ratingAux = ratingsList.get(0);
		}

		ratingAux.setRating(rating.getRating());
		ratingAux.setLastUpdate(Calendar.getInstance());
		ratingAux.setLearningMaterial(rating.getLearningMaterial());
		ratingAux.setUserId(rating.getUserId());

		repository.save(ratingAux);

		response.put("success", true);

		return response;
	}
}
