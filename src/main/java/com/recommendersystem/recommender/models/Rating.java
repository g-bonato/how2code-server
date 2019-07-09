package com.recommendersystem.recommender.models;

import java.util.Calendar;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class Rating {
	@Id
	private ObjectId _id;
	@Indexed(unique = true)
	private String id;
	private Float rating;
	private Calendar lastUpdate;
	private String userId;
	private LearningMaterial learningMaterial;

	public Rating() {

	}

	public ObjectId get_id() {
		return _id;
	}

	public String getId() {
		return id;
	}

	public Calendar getLastUpdate() {
		return lastUpdate;
	}

	public LearningMaterial getLearningMaterial() {
		return learningMaterial;
	}

	public Float getRating() {
		return rating;
	}

	public String getUserId() {
		return userId;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
		this.id = _id.toHexString();
	}

	public void setLastUpdate(Calendar lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setLearningMaterial(LearningMaterial learningMaterial) {
		this.learningMaterial = learningMaterial;
	}

	public void setRating(Float rating) {
		this.rating = rating;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
