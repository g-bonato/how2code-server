package com.recommendersystem.recommender.controller;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;

@CrossOrigin
@RestController
@RequestMapping("/rating")
public class RatingController {
	@Autowired
	private RatingRepository repository;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public Map<String, Object> getAllRatings() {
		Map<String, Object> response = new HashMap<>();

		response.put("ratings", repository.findAll());
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/byId/", method = RequestMethod.GET)
	public Map<String, Object> getRatingsByUserIdAndVideoId(@RequestBody Rating rating) {
		Map<String, Object> response = new HashMap<>();

		List<Rating> ratingsList = repository.findByUserIdAndVideoId(rating.getUserId(), rating.getLearningMaterial());

		response.put("ratings", ratingsList);
		response.put("success", true);

		return response;
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	public Map<String, Object> rating(@Valid @RequestBody Rating rating) {
		Map<String, Object> response = new HashMap<>();

		Rating ratingAux = new Rating();

		ratingAux.set_id(ObjectId.get());

		List<Rating> ratingsList = repository.findByUserIdAndVideoId(rating.getUserId(), rating.getLearningMaterial());

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
