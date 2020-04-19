/* *********************************************************************** *
 * project: org.matsim.*
 * TravelTimeRoleArray.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.core.trafficmonitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.network.Link;

import java.util.Arrays;

/**
 * Implementation of {@link TravelTimeData} that stores the data per time bin
 * in simple arrays. Useful if not too many empty time bins (time bins with 
 * no traffic on a link) exist, so no memory is wasted.
 *
 * @author mrieser
 */
class TravelTimeDataArray extends TravelTimeData {

	private final static Logger LOG = LogManager.getLogger(TravelTimeDataArray.class);

	private final short[] timeCnt;
	private final double[] travelTimes;
	private final Link link;

	TravelTimeDataArray(final Link link, final int numSlots) {
		this.timeCnt = new short[numSlots];
		this.travelTimes = new double[numSlots];
		this.link = link;
		resetTravelTimes();
	}

	@Override
	public void resetTravelTimes() {
		for (int i = 0; i < this.travelTimes.length; i++) {
			this.timeCnt[i] = 0;
			this.travelTimes[i] = -1.0;
		}
	}

	@Override
	public void setTravelTime( final int timeSlot, final double traveltime ) {
		this.timeCnt[timeSlot] = 1;
		this.travelTimes[timeSlot] = traveltime;
		if (traveltime <= 0) {
			LOG.warn("setting bad travel time: " + traveltime + " slot=" + timeSlot);
			Thread.dumpStack();
		}
	}

	@Override
	public void addTravelTime(final int timeSlot, final double traveltime) {
		short cnt = this.timeCnt[timeSlot];
		double sum = this.travelTimes[timeSlot] * cnt;

		sum += traveltime;
		cnt++;

		this.travelTimes[timeSlot] = sum / cnt;
		this.timeCnt[timeSlot] = cnt;
	}

	@Override
	public double getTravelTime(final int timeSlot, final double now) {
		double ttime = this.travelTimes[timeSlot];
		if (ttime >= 0.0) return ttime; // negative values are invalid.

		// ttime can only be <0 if it never accumulated anything, i.e. if cnt == 9, so just use freespeed
		double freespeed = this.link.getLength() / this.link.getFreespeed(now);
		this.travelTimes[timeSlot] = freespeed;
		return freespeed;
	}

	public void printDebug() {
		LOG.info("internal data for " + link.getId() + "  tt=" + Arrays.toString(travelTimes) + "  cnt=" + Arrays.toString(timeCnt));
	}

}
