/* *********************************************************************** *
 * project: org.matsim.*
 * Activity.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007, 2009 by the members listed in the COPYING,  *
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

package org.matsim.api.core.v01.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.BasicActivity;
import org.matsim.facilities.ActivityFacility;

/**
 * Specifies the kind of activity an agent performs during its day.
 * 
 */
public interface Activity extends PlanElement, BasicActivity{

	double getEndTime();

	void setEndTime( final double seconds );

	String getType();

	void setType( final String type );

	double getStartTime();

	/**
	 * Used for reporting outcomes in the scoring. Not interpreted for the demand.
	 */
	void setStartTime( double seconds );
	
	double getMaximumDuration() ;
	
	void setMaximumDuration( double seconds ) ;

	void setLinkId( final Id<Link> id );
	
	void setFacilityId( final Id<ActivityFacility> id );

	void setCoord(Coord coord);

}
