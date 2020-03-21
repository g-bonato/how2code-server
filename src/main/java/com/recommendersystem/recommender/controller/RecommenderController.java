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
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.SpearmanCorrelationSimilarity;
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
import com.recommendersystem.recommender.processor.AllRatings;
import com.recommendersystem.recommender.processor.EvaluateRecommender;
import com.recommendersystem.recommender.repository.RatingRepository;
import com.recommendersystem.recommender.repository.UserRepository;
import com.recommendersystem.recommender.utils.LogUtil;

@CrossOrigin(origins = { "http://localhost:8100", "https://how2code.web.app" })
@RestController
@RequestMapping("/recommender")
public class RecommenderController {
	private final Integer NUMBER_RECOMENDATIONS = 30;

	@Autowired
	private RatingRepository repository;

	@Autowired
	private UserRepository userRepository;

	private List<Thread> threads = new ArrayList<Thread>();

	private boolean evaluate = false;

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{query}", method = RequestMethod.GET)
	public Map<String, Object> getVideoRecommendation(
			@PathVariable("query") String query,
			@RequestHeader(name = "Authorization", required = true, defaultValue = "") String authorization) {

		Map<String, Object> response = new HashMap<>();
		List<LearningMaterial> learningMaterials = new ArrayList<LearningMaterial>();
		List<LearningMaterial> youtubeItems = new ArrayList<LearningMaterial>();

		String pageToken = null;

		Map<String, Object> youtubeResponse = YoutubeSearchController
				.getVideos(query, pageToken);

		pageToken = (String) youtubeResponse.get("nextPageToken");

		Optional<User> userAux = userRepository.findBySessionId(authorization);

		if (!userAux.isPresent() || !SessionController
				.isValidSession(authorization, userAux.get())) {
			response.put("youtubeItems",
					youtubeResponse.get("learningMaterials"));
			response.put("success", true);

			return response;
		}

		String userId = userAux.get().getId();

		youtubeItems = (List<LearningMaterial>) youtubeResponse
				.get("learningMaterials");

		Map<Long, LearningMaterial> codeLearningMaterial = new HashMap<Long, LearningMaterial>();
		FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();

		getNewDataModel(userId, youtubeItems, codeLearningMaterial, userData);

		try {
			List<RecommendedItem> recommendations = getRecommendations(userData,
					codeLearningMaterial);

			for (RecommendedItem recommendation : recommendations) {
				if (!codeLearningMaterial
						.containsKey(recommendation.getItemID())) {
					continue;
				}

				LearningMaterial learningMaterial = codeLearningMaterial
						.get(recommendation.getItemID());

				for (LearningMaterial youtubeItem : youtubeItems) {
					if (youtubeItem.getVideoId()
							.equals(learningMaterial.getVideoId())) {
						if (recommendation.getValue() < 3) {
							break;
						}

						youtubeItems.remove(youtubeItem);

						learningMaterials.add(youtubeItem);
						break;
					}
				}
			}
		} catch (TasteException e) {
			LogUtil.logError("Failed getting recommendations" + e);

			response.put("youtubeItems", youtubeItems);
			response.put("message", "");
			response.put("success", false);

			return response;
		}

		LogUtil.logDebug("Recommendation ended");

		response.put("youtubeItems", youtubeItems);
		response.put("recommendations", learningMaterials);
		response.put("success", true);

		return response;
	}

	private Set<Rating> getAllRatings() {
		Set<Rating> ratings = new HashSet<Rating>();

		int pages = (int) Math.ceil(repository.count() / 20000);
		int lots = pages / 500;

		for (int lot = 0; lot <= lots; lot++) {
			int pagesInThisLot;

			if (pages < ((lot + 1) * 500)) {
				pagesInThisLot = (pages - (lot * 500));
			} else {
				pagesInThisLot = 499;
			}

			for (int i = 0; i <= pagesInThisLot; i++) {
				int page = (500 * lot) + i;

				AllRatings runnable = new AllRatings(ratings, page, repository);

				Thread thread = new Thread(runnable);
				thread.start();

				threads.add(thread);
			}

			threadAwaiting(threads);
		}

		return ratings;
	}

	private void getNewDataModel(String currentUserId,
			List<LearningMaterial> youtubeItems,
			Map<Long, LearningMaterial> codeLearningMaterial,
			FastByIDMap<PreferenceArray> userData) {

		Set<Rating> ratings = getAllRatings();

		Map<String, Long> videoIdCode = new HashMap<String, Long>();
		Map<String, Long> userIdCode = new HashMap<String, Long>();

		Map<Long, List<Preference>> userCodePreferences = new HashMap<Long, List<Preference>>();

		long materialCont = 0l;
		long userCont = 1l;
		for (Rating ratedItem : ratings) {
			List<Preference> preferences = new ArrayList<Preference>();
			String videoId = ratedItem.getLearningMaterial().getVideoId();
			Float rating = ratedItem.getRating();
			String userId = ratedItem.getUserId();
			LearningMaterial learningMaterial = ratedItem.getLearningMaterial();

			if (currentUserId.equals(ratedItem.getUserId())) {
				if (videoIdCode.containsKey(videoId)) {
					long videoCode = videoIdCode.get(videoId);

					if (userCodePreferences.containsKey(0l)) {
						preferences = userCodePreferences.get(0l);
						preferences.add(
								new GenericPreference(0, videoCode, rating));
						userCodePreferences.put(0l, preferences);
					} else {
						preferences.add(
								new GenericPreference(0, videoCode, rating));
						userCodePreferences.put(0l, preferences);
					}
				} else {
					if (userCodePreferences.containsKey(0l)) {
						preferences = userCodePreferences.get(0l);
						preferences.add(
								new GenericPreference(0, materialCont, rating));

						userCodePreferences.put(0l, preferences);
					} else {
						preferences.add(
								new GenericPreference(0, materialCont, rating));

						userCodePreferences.put(0l, preferences);
					}

					codeLearningMaterial.put(materialCont, learningMaterial);
					videoIdCode.put(learningMaterial.getVideoId(),
							materialCont);
					materialCont++;
				}

				userData.put(0, new GenericUserPreferenceArray(preferences));
				continue;
			}

			if (userIdCode.containsKey(userId)) {
				long userCode = userIdCode.get(userId);

				if (videoIdCode.containsKey(videoId)) {
					long videoCode = videoIdCode.get(videoId);

					if (userCodePreferences.containsKey(userCode)) {
						preferences = userCodePreferences.get(userCode);
						preferences.add(new GenericPreference(userCode,
								videoCode, rating));
						userCodePreferences.put(userCode, preferences);
					} else {
						preferences.add(new GenericPreference(userCode,
								videoCode, rating));
						userCodePreferences.put(userCode, preferences);
					}
				} else {
					if (userCodePreferences.containsKey(userCode)) {
						preferences = userCodePreferences.get(userCode);
						preferences.add(new GenericPreference(userCode,
								materialCont, rating));
						userCodePreferences.put(userCode, preferences);
					} else {
						preferences.add(new GenericPreference(userCode,
								materialCont, rating));
						userCodePreferences.put(userCode, preferences);
					}

					codeLearningMaterial.put(materialCont, learningMaterial);
					videoIdCode.put(learningMaterial.getVideoId(),
							materialCont);
					materialCont++;
				}

				userData.put(0, new GenericUserPreferenceArray(preferences));
				continue;
			}

			userIdCode.put(userId, userCont);

			if (videoIdCode.containsKey(videoId)) {
				long videoCode = videoIdCode.get(videoId);

				if (userCodePreferences.containsKey(userCont)) {
					preferences = userCodePreferences.get(userCont);
					preferences.add(
							new GenericPreference(userCont, videoCode, rating));

					userCodePreferences.put(userCont, preferences);
				} else {
					preferences.add(
							new GenericPreference(userCont, videoCode, rating));

					userCodePreferences.put(userCont, preferences);
				}
			} else {
				if (userCodePreferences.containsKey(userCont)) {
					preferences = userCodePreferences.get(userCont);
					preferences.add(new GenericPreference(userCont,
							materialCont, rating));
					userCodePreferences.put(userCont, preferences);
				} else {
					preferences.add(new GenericPreference(userCont,
							materialCont, rating));
					userCodePreferences.put(userCont, preferences);
				}

				codeLearningMaterial.put(materialCont, learningMaterial);
				videoIdCode.put(learningMaterial.getVideoId(), materialCont);
				materialCont++;
			}

			userCont++;

			continue;
		}

		for (Long userCode : userCodePreferences.keySet()) {
			userData.put(userCode, new GenericUserPreferenceArray(
					userCodePreferences.get(userCode)));
		}

	}

	private List<RecommendedItem> getRecommendations(
			FastByIDMap<PreferenceArray> userData,
			Map<Long, LearningMaterial> codeLearningMaterial)
			throws TasteException {

		DataModel model = new GenericDataModel(userData);
		UserSimilarity similarity = new SpearmanCorrelationSimilarity(model);
		UserNeighborhood neighborhood = new NearestNUserNeighborhood(
				(int) (userRepository.count() * 0.25), similarity, model);

		UserBasedRecommender recommender = new GenericUserBasedRecommender(
				model, neighborhood, similarity);

		if (evaluate) {
			EvaluateRecommender runnable = new EvaluateRecommender(model,
					neighborhood, similarity, (int) (codeLearningMaterial.size() * 0.08));

			Thread thread = new Thread(runnable);
			thread.start();
		}

		List<RecommendedItem> recommendations = recommender.recommend(0,
				(int) (codeLearningMaterial.size() * 0.08));

		LogUtil.logDebug(recommendations.toString());

		return recommendations;
	}

	private void threadAwaiting(List<Thread> threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.out.println(
						"Error on waiting for Thread" + e.getMessage());
			}
		}
	}
}