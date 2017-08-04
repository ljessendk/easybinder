package org.vaadin.easybinder.testentity;

import java.util.Date;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class FlightId {
	public static enum LegType {
		DEPARTURE, ARRIVAL
	}

	static final String AIRLINE_PATTERN = "[A-Z0-9]{2}[A-Z]?";

	@NotNull
	@Pattern(regexp = AIRLINE_PATTERN)
	String airline;

	@Min(value = 1)
	@Max(value = 9999)
	int flightNumber;

	Character flightSuffix;

	@NotNull
	Date date;

	@NotNull
	LegType legType;

	/**
	 * @return the airline
	 */
	public String getAirline() {
		return airline;
	}

	/**
	 * @return the flightNumber
	 */
	public int getFlightNumber() {
		return flightNumber;
	}

	/**
	 * @return the flightSuffix
	 */
	public Character getFlightSuffix() {
		return flightSuffix;
	}

	/**
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return the legType
	 */
	public LegType getLegType() {
		return legType;
	}

	/**
	 * @param airline
	 *            the airline to set
	 */
	public void setAirline(String airline) {
		this.airline = airline;
	}

	/**
	 * @param flightNumber
	 *            the flightNumber to set
	 */
	public void setFlightNumber(int flightNumber) {
		this.flightNumber = flightNumber;
	}

	/**
	 * @param flightSuffix
	 *            the flightSuffix to set
	 */
	public void setFlightSuffix(Character flightSuffix) {
		this.flightSuffix = flightSuffix;
	}

	/**
	 * @param date
	 *            the date to set
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @param legType
	 *            the legType to set
	 */
	public void setLegType(LegType legType) {
		this.legType = legType;
	}
}
