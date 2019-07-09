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

public class MahoutTest {
	public static void main(String[] args) {
		try {
			FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();

			List<Preference> pref1 = new ArrayList<Preference>();

			pref1.add(new GenericPreference(1, 10, 1.0f));
			pref1.add(new GenericPreference(1, 11, 2.0f));
			pref1.add(new GenericPreference(1, 12, 5.0f));
			pref1.add(new GenericPreference(1, 13, 5.0f));
			pref1.add(new GenericPreference(1, 14, 5.0f));
			pref1.add(new GenericPreference(1, 15, 4.0f));
			pref1.add(new GenericPreference(1, 16, 5.0f));
			pref1.add(new GenericPreference(1, 17, 1.0f));
			pref1.add(new GenericPreference(1, 18, 5.0f));

			List<Preference> pref2 = new ArrayList<Preference>();

			pref2.add(new GenericPreference(2, 10, 1.0f));
			pref2.add(new GenericPreference(2, 15, 5.0f));
			pref2.add(new GenericPreference(2, 16, 4.5f));
			pref2.add(new GenericPreference(2, 17, 1.0f));
			pref2.add(new GenericPreference(2, 18, 5.0f));

			List<Preference> pref3 = new ArrayList<Preference>();

			pref3.add(new GenericPreference(3, 11, 2.5f));
			pref3.add(new GenericPreference(3, 12, 4.5f));
			pref3.add(new GenericPreference(3, 13, 5.0f));
			pref3.add(new GenericPreference(3, 14, 3.0f));
			pref3.add(new GenericPreference(3, 15, 3.5f));
			pref3.add(new GenericPreference(3, 16, 4.5f));
			pref3.add(new GenericPreference(3, 17, 4.0f));
			pref3.add(new GenericPreference(3, 18, 5.0f));

			List<Preference> pref4 = new ArrayList<Preference>();

			pref4.add(new GenericPreference(4, 10, 5.0f));
			pref4.add(new GenericPreference(4, 11, 5.0f));
			pref4.add(new GenericPreference(4, 12, 5.0f));
			pref4.add(new GenericPreference(4, 13, 0.0f));
			pref4.add(new GenericPreference(4, 14, 2.0f));
			pref4.add(new GenericPreference(4, 15, 3.0f));
			pref4.add(new GenericPreference(4, 16, 1.0f));
			pref4.add(new GenericPreference(4, 17, 4.0f));
			pref4.add(new GenericPreference(4, 18, 1.0f));

			userData.put(1, new GenericUserPreferenceArray(pref1));
			userData.put(2, new GenericUserPreferenceArray(pref2));
			userData.put(3, new GenericUserPreferenceArray(pref3));
			userData.put(4, new GenericUserPreferenceArray(pref4));

			DataModel model = new GenericDataModel(userData);

			UserSimilarity similarity = new PearsonCorrelationSimilarity(model);

			UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);

			UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);

			List<RecommendedItem> recommendations = recommender.recommend(2, 20);
			for (RecommendedItem recommendation : recommendations) {
				System.out.println(recommendation);
			}
		} catch (TasteException e) {
			System.err.println("Failed on getting file for similarity");
		}
	}
}
