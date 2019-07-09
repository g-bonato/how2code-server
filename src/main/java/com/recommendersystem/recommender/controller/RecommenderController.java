package com.recommendersystem.recommender.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.repository.RatingRepository;

@CrossOrigin
@RestController
@RequestMapping("/recommender")
public class RecommenderController {
	@Autowired
	private RatingRepository ratingRepository;

	@RequestMapping(value = "/{query}", method = RequestMethod.GET)
	public Map<String, Object> getVideoRecommendation(@PathVariable("query") String query,
			@CookieValue(value = "session", defaultValue = "") String sessionId,
			@CookieValue(value = "userId", defaultValue = "") String userId) {

		Map<String, Object> response = new HashMap<>();

		String pageToken = null;
		List<LearningMaterial> learningMaterials = new ArrayList<LearningMaterial>();

		while (learningMaterials.size() < 20) {
			Map<String, Object> youtubeResponse = YoutubeSearchController.getVideos(query, pageToken);

			if (!SessionController.isValidSession(sessionId, userId)) {
				response.put("videos", youtubeResponse.get("learningMaterials"));
				response.put("success", true);

				return response;
			}

			List<Rating> ratedByMe = ratingRepository.findByUserId(userId);

			Map<Long, String> codeVideoId = new HashMap<Long, String>();
			Map<String, Long> videoIdCode = new HashMap<String, Long>();

			FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();

			getRatedByMeDataModel(ratedByMe, codeVideoId, videoIdCode, userData);

			List<Rating> inCommonRatedItems = getInCommonRatedItems(ratedByMe);

			getRatedByAnotherUsersDataModel(inCommonRatedItems, videoIdCode, codeVideoId, userData);

			// TODO: Fazer a chamada para a função de calculo de recomendação, validar o
			// numero de itens, e se necessário, complementar com os videos do Youtube
		}

		response.put("videos", learningMaterials);
		response.put("success", true);

		return response;
	}

	private void getRatedByMeDataModel(List<Rating> ratedByMe, Map<Long, String> codeVideoId,
			Map<String, Long> videoIdCode, FastByIDMap<PreferenceArray> userData) {

		Long cont = 1l;

		List<Preference> preferences = new ArrayList<Preference>();

		for (Rating rating : ratedByMe) {
			codeVideoId.put(cont, rating.getLearningMaterial().getVideoId());
			videoIdCode.put(rating.getLearningMaterial().getVideoId(), cont);

			preferences.add(new GenericPreference(0, cont, rating.getRating()));

			cont++;
		}

		userData.put(0, new GenericUserPreferenceArray(preferences));
	}

	private List<Rating> getInCommonRatedItems(List<Rating> ratedByMe) {
		List<Rating> inCommonRatedItems = new ArrayList<Rating>();

		for (Rating rating : ratedByMe) {
			List<Rating> ratingsAux = ratingRepository
					.findByVideoIdAndNotUserId(rating.getLearningMaterial().getVideoId(), rating.getUserId());

			for (Rating ratingAux : ratingsAux) {
				inCommonRatedItems.add(ratingAux);
			}
		}

		return inCommonRatedItems;
	}

	private void getRatedByAnotherUsersDataModel(List<Rating> inCommonRatedItems, Map<String, Long> videoIdCode,
			Map<Long, String> codeVideoId, FastByIDMap<PreferenceArray> userData) {

		List<String> alreadyProcessedUserId = new ArrayList<String>();

		Long userCont = 1l;
		for (Rating rating : inCommonRatedItems) {
			if (alreadyProcessedUserId.contains(rating.getUserId())) {
				continue;
			}

			List<Rating> ratedItemsByUserId = ratingRepository.findByUserId(rating.getUserId());

			List<Preference> preferences = new ArrayList<Preference>();

			Long videoIdCont = (long) videoIdCode.size() + 1;

			for (Rating ratedItem : ratedItemsByUserId) {
				String videoId = ratedItem.getLearningMaterial().getVideoId();
				Float ratedValue = ratedItem.getRating();

				if (videoIdCode.containsKey(videoId)) {
					preferences.add(new GenericPreference(userCont, videoIdCode.get(videoId), ratedValue));
				} else {
					codeVideoId.put(videoIdCont, rating.getLearningMaterial().getVideoId());
					videoIdCode.put(rating.getLearningMaterial().getVideoId(), videoIdCont);

					preferences.add(new GenericPreference(userCont, videoIdCont, ratedValue));

					videoIdCont++;
				}
			}

			userData.put(userCont, new GenericUserPreferenceArray(preferences));

			userCont++;
		}
	}

}
