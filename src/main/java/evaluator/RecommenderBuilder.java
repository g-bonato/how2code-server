package evaluator;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class RecommenderBuilder implements org.apache.mahout.cf.taste.eval.RecommenderBuilder {

	private UserNeighborhood neighborhood;
	private UserSimilarity similarity;
	
	public RecommenderBuilder(UserNeighborhood neighborhood, UserSimilarity similarity) {
		this.neighborhood = neighborhood;
		this.similarity = similarity;
	}
	
	@Override
	public Recommender buildRecommender(DataModel dataModel) throws TasteException {
		return new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
	}
}
