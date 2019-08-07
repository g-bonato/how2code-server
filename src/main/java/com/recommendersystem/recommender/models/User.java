package com.recommendersystem.recommender.models;

import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

public class User {
	@Id
	private ObjectId _id;
	@Indexed(unique = true)
	private String id;
	private String email;
	private String fullname;
	private String image;
	private Boolean ifsulStudent;
	private Map<String, Double> profile;
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;
	private Session session;

	public User() {
		this.session = new Session();
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
		setId(_id.toHexString());
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullname() {
		return fullname;
	}

	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Boolean getIfsulStudent() {
		return ifsulStudent;
	}

	public void setIfsulStudent(Boolean ifsulStudent) {
		this.ifsulStudent = ifsulStudent;
	}

	public Map<String, Double> getProfile() {
		return profile;
	}

	public void setProfile(Map<String, Double> profile) {
		this.profile = profile;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		sb.append("email: ").append(this.email);
		sb.append("; profileId: ").append(this.id);
		sb.append("]");

		return email;
	}
}
