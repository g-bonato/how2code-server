package com.recommendersystem.recommender.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.recommendersystem.recommender.models.LearningMaterial;
import com.recommendersystem.recommender.models.Rating;
import com.recommendersystem.recommender.models.User;
import com.recommendersystem.recommender.processor.InCommonRatedItemUserId;
import com.recommendersystem.recommender.processor.RatedYoutubeItemUserId;
import com.recommendersystem.recommender.repository.RatingRepository;
import com.recommendersystem.recommender.repository.UserRepository;

@CrossOrigin
@RestController
@RequestMapping("/recommender")
public class RecommenderController {
	@Autowired
	private RatingRepository repository;

	@Autowired
	private UserRepository userRepository;

	private List<Thread> threads = new ArrayList<Thread>();

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{query}", method = RequestMethod.GET)
	public Map<String, Object> getVideoRecommendation(@PathVariable("query") String query,
			@RequestHeader(name = "Authorization", required = true, defaultValue = "") String authorization) {

		Map<String, Object> response = new HashMap<>();

		String pageToken = null;
		List<LearningMaterial> learningMaterials = new ArrayList<LearningMaterial>();

		while (learningMaterials.size() < 50) {
			Map<String, Object> youtubeResponse = YoutubeSearchController.getVideos(query, pageToken);

			pageToken = (String) youtubeResponse.get("nextPageToken");

			Optional<User> userAux = userRepository.findBySessionId(authorization);

			if (!userAux.isPresent() || !SessionController.isValidSession(authorization, userAux.get())) {
				response.put("videos", youtubeResponse.get("learningMaterials"));
				response.put("success", true);

				return response;
			}

			String userId = userAux.get().getId();

			List<LearningMaterial> youtubeItems = new ArrayList<LearningMaterial>();

			youtubeItems = (List<LearningMaterial>) youtubeResponse.get("learningMaterials");

			Map<Long, LearningMaterial> codeLearningMaterial = new HashMap<Long, LearningMaterial>();
			Map<String, Long> videoIdCode = new HashMap<String, Long>();
			FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();

			getDataModel(userId, youtubeItems, codeLearningMaterial, videoIdCode, userData);

			try {
				List<RecommendedItem> recommendations = getRecommendations(userData);

				for (RecommendedItem recommendation : recommendations) {
					if (!codeLearningMaterial.containsKey(recommendation.getItemID())) {
						continue;
					}

					LearningMaterial learningMaterial = codeLearningMaterial.get(recommendation.getItemID());

					if (!youtubeItems.contains(learningMaterial)) {
						continue;
					}

					youtubeItems.remove(learningMaterial);

					if (recommendation.getValue() < 0) {
						continue;
					}

					learningMaterials.add(learningMaterial);
				}

				for (LearningMaterial youtubeItem : youtubeItems) {
					learningMaterials.add(youtubeItem);
				}
			} catch (TasteException e) {
				response.put("videos", youtubeItems);
				response.put("message", "");
				response.put("success", false);

				return response;
			}
		}

		response.put("videos", learningMaterials);
		response.put("success", true);

		return response;
	}

	private void getDataModel(String currentUserId, List<LearningMaterial> youtubeItems,
			Map<Long, LearningMaterial> codeLearningMaterial, Map<String, Long> videoIdCode,
			FastByIDMap<PreferenceArray> userData) {

		Set<String> userIds = new HashSet<String>();

		List<Rating> ratedByMe = repository.findByUserId(currentUserId);

		getInCommonRatedItemsUserIds(ratedByMe, userIds);

		getRatedYoutubeItemsUserIds(youtubeItems, currentUserId, userIds);

		threadAwaiting(threads);

		List<Preference> preferences = new ArrayList<Preference>();

		Long materialCont = 0l;
		for (Rating rating : ratedByMe) {
			codeLearningMaterial.put(materialCont, rating.getLearningMaterial());
			videoIdCode.put(rating.getLearningMaterial().getVideoId(), materialCont);

			preferences.add(new GenericPreference(0, materialCont, rating.getRating()));
			materialCont++;
		}

		userData.put(0, new GenericUserPreferenceArray(preferences));

		Long userCont = 1l;

		for (String userId : userIds) {
			preferences = new ArrayList<Preference>();

			List<Rating> ratedItems = repository.findByUserId(userId);

			for (Rating ratedItem : ratedItems) {
				String videoId = ratedItem.getLearningMaterial().getVideoId();
				Float rating = ratedItem.getRating();

				if (videoIdCode.containsKey(videoId)) {
					preferences.add(new GenericPreference(userCont, videoIdCode.get(videoId), rating));
					continue;
				}

				codeLearningMaterial.put(materialCont, ratedItem.getLearningMaterial());
				videoIdCode.put(ratedItem.getLearningMaterial().getVideoId(), materialCont);

				preferences.add(new GenericPreference(userCont, materialCont, rating));

				materialCont++;
			}

			userData.put(userCont, new GenericUserPreferenceArray(preferences));

			userCont++;
		}
	}

	private void getInCommonRatedItemsUserIds(List<Rating> ratedByMe, Set<String> userIds) {
		for (Rating rating : ratedByMe) {
			InCommonRatedItemUserId runnable = new InCommonRatedItemUserId(rating, userIds, repository);

			Thread thread = new Thread(runnable);
			thread.start();

			threads.add(thread);
		}
	}

	private void getRatedYoutubeItemsUserIds(List<LearningMaterial> youtubeItems, String currentUserId,
			Set<String> userIds) {

		for (LearningMaterial youtubeItem : youtubeItems) {
			RatedYoutubeItemUserId runnable = new RatedYoutubeItemUserId(youtubeItem, currentUserId, userIds,
					repository);

			Thread thread = new Thread(runnable);
			thread.start();

			threads.add(thread);
		}
	}

	private List<RecommendedItem> getRecommendations(FastByIDMap<PreferenceArray> userData) throws TasteException {
		DataModel model = new GenericDataModel(userData);

		UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

		UserNeighborhood neighborhood = new ThresholdUserNeighborhood(-1.0, similarity, model);

		UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

		List<RecommendedItem> recommendations = recommender.recommend(0, 20);

		return recommendations;
	}

	private void threadAwaiting(List<Thread> threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println("Error on waiting for Thread" + e.getMessage());
			}
		}
	}
}