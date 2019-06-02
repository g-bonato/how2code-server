package com.recommendersystem.recommender.models;

import java.util.Calendar;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public class Rating {
	@Id
	private ObjectId _id;
	private Double rating;
	private Calendar lastUpdate;
	private ObjectId userId;
	private List<LearningMaterial> learningMaterials;

	public Rating() {

	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Calendar getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Calendar lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public ObjectId getUserId() {
		return userId;
	}

	public void setUserId(ObjectId userId) {
		this.userId = userId;
	}

	public List<LearningMaterial> getLearningMaterials() {
		return learningMaterials;
	}

	public void setLearningMaterials(List<LearningMaterial> learningMaterials) {
		this.learningMaterials = learningMaterials;
	}

}
