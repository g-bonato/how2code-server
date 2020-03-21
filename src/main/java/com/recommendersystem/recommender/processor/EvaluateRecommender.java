package com.recommendersystem.recommender.processor;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.eval.IRStatistics;
import org.apache.mahout.cf.taste.eval.RecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.impl.eval.GenericRecommenderIRStatsEvaluator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.recommendersystem.recommender.utils.LogUtil;

import evaluator.RecommenderBuilder;

public class EvaluateRecommender implements Runnable {

	private DataModel model;
	private UserNeighborhood neighborhood;
	private UserSimilarity similarity;
	private Integer numberRecommendations;

	public EvaluateRecommender(DataModel model, UserNeighborhood neighborhood,
			UserSimilarity similarity, Integer d) {

		this.model = model;
		this.neighborhood = neighborhood;
		this.similarity = similarity;
		this.numberRecommendations = d;
	}

	@Override
	public void run() {
		try {
			LogUtil.logDebug("Evaluation started");
			
			RecommenderBuilder recommenderBuilder = new RecommenderBuilder(
					neighborhood, similarity);

			recommenderBuilder.buildRecommender(model);

			RecommenderIRStatsEvaluator statsEvaluator = new GenericRecommenderIRStatsEvaluator();
			IRStatistics stats = statsEvaluator.evaluate(recommenderBuilder,
					null, model, null, numberRecommendations,
					GenericRecommenderIRStatsEvaluator.CHOOSE_THRESHOLD, 1.0);

			LogUtil.logDebug("F1Measure " + stats.getF1Measure());
			LogUtil.logDebug("FallOut " + stats.getFallOut());
			LogUtil.logDebug("NormalizedDiscountedCumulativeGain "
					+ stats.getNormalizedDiscountedCumulativeGain());

			LogUtil.logDebug("Precision " + stats.getPrecision());
			LogUtil.logDebug("Reach " + stats.getReach());
			LogUtil.logDebug("Recall " + stats.getRecall());
		} catch (TasteException e) {
			e.printStackTrace();
		}
	}

}
