package org.magnum.mobilecloud.video.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;

/**
 * A simple object to represent a video and its URL for viewing.
 * 
 * You must annotate this object to make it a JPA entity.
 * 
 * 
 * Feel free to modify this with whatever other metadata that you want, such as
 * the
 * 
 * 
 * @author jules, mitchell
 */
public class Video {

	private long id;

	private String title;
	private String url;
	private long duration;
	private String location;
	private String subject;
	private String contentType;
	private Map<String,Double> ratings;

	// We don't want to bother unmarshalling or marshalling
	// any owner data in the JSON. Why? We definitely don't
	// want the client trying to tell us who the owner is.
	// We also might want to keep the owner secret.
	@JsonIgnore
	private String owner;

	public Video() {
		ratings = new ConcurrentHashMap<>();
	}

	public Video(String owner, String name, String url, long duration,
			long likes, Set<String> likedBy) {
		super();
		this.owner = owner;
		this.title = name;
		this.url = url;
		this.duration = duration;
		ratings = new ConcurrentHashMap<>();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, Double> getRatings() {
		return ratings;
	}

	public void setRatings(Map<String, Double> ratings) {
		this.ratings = ratings;
	}
	
	public AverageVideoRating getRating()
	{
		int totalRatings = 0;
		double rating = 0;
		for(Double value:ratings.values())
		{
			rating += value;
			totalRatings++;
		}
		
		rating = rating/totalRatings;
		return new AverageVideoRating(rating, id, totalRatings);
	}
	
	public AverageVideoRating sendRating(String name, double rating)
	{
		ratings.put(name, rating);
		return getRating();
	}

	/**
	 * Two Videos will generate the same hashcode if they have exactly the same
	 * values for their name, url, and duration.
	 * 
	 */
	@Override
	public int hashCode() {
		// Google Guava provides great utilities for hashing
		return Objects.hashCode(title, url, duration, owner);
	}

	/**
	 * Two Videos are considered equal if they have exactly the same values for
	 * their name, url, and duration.
	 * 
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Video) {
			Video other = (Video) obj;
			// Google Guava provides great utilities for equals too!
			return Objects.equal(title, other.title)
					&& Objects.equal(url, other.url)
					&& Objects.equal(owner, other.owner)
					&& duration == other.duration;
		} else {
			return false;
		}
	}

}
