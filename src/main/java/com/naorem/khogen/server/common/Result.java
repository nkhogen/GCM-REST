package com.naorem.khogen.server.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "result")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = false)
@JsonInclude(value = Include.NON_NULL)
public class Result<T> {
	private final T value;
	private final Error error;
	
	@JsonCreator
	public Result(@JsonProperty("value")final T value, @JsonProperty("error")final Error error) {
		this.value = value;
		this.error = error;
	}

	public T getValue() {
		return value;
	}

	public Error getError() {
		return error;
	}
}
