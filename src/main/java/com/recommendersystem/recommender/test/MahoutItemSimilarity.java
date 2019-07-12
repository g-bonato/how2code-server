package com.recommendersystem.recommender.test;

import java.util.ArrayList;
import java.util.List;

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

public class MahoutItemSimilarity {
	public static void main(String[] args) {
		try {
			FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();

			List<Preference> pref1 = new ArrayList<Preference>();

			pref1.add(new GenericPreference(0, 1, 1.0f));
			pref1.add(new GenericPreference(0, 2, -1.0f));
			// pref1.add(new GenericPreference(0, 3, 1.0f));

			List<Preference> pref2 = new ArrayList<Preference>();

			pref2.add(new GenericPreference(1, 1, 1.0f));
			pref2.add(new GenericPreference(1, 2, 1.0f));
			pref2.add(new GenericPreference(1, 3, -1.0f));
			pref2.add(new GenericPreference(1, 4, -1.0f));
			pref2.add(new GenericPreference(1, 5, 1.0f));

			List<Preference> pref3 = new ArrayList<Preference>();

			pref3.add(new GenericPreference(2, 1, 1.0f));
			pref3.add(new GenericPreference(2, 2, -1.0f));
			pref3.add(new GenericPreference(2, 3, -1.0f));
			pref3.add(new GenericPreference(2, 4, 1.0f));
			pref3.add(new GenericPreference(2, 5, 1.0f));

			List<Preference> pref4 = new ArrayList<Preference>();

			pref4.add(new GenericPreference(3, 1, 1.0f));
			pref4.add(new GenericPreference(3, 2, -1.0f));
			pref4.add(new GenericPreference(3, 3, 1.0f));
			pref4.add(new GenericPreference(3, 4, -1.0f));
			pref4.add(new GenericPreference(3, 5, -1.0f));

			userData.put(0, new GenericUserPreferenceArray(pref1));
			userData.put(1, new GenericUserPreferenceArray(pref2));
			userData.put(2, new GenericUserPreferenceArray(pref3));
			userData.put(3, new GenericUserPreferenceArray(pref4));

			DataModel model = new GenericDataModel(userData);

			UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(-1.0, similarity, model);

			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

			List<RecommendedItem> recommendations = recommender.recommend(0, 20);
			for (RecommendedItem recommendation : recommendations) {
				System.out.println(recommendation);
			}
		} catch (TasteException e) {
			System.err.println("Failed on getting file for similarity");
		}
	}
}
