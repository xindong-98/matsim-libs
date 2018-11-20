package org.matsim.core.population;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.facilities.ActivityFacility;

public interface BasicActivity{

    Id<ActivityFacility> getFacilityId() ;

    /**
     * @return the coordinate of the activity, possibly null.
     * <p></p>
     * Note that there is deliberately no way to set the coordinate except at creation.
     * We might consider something like moveActivityTo( linkid, coord ).  kai, aug'10
     */
    Coord getCoord() ;

    /**
     * @return the if of the link to which the activity is attached.  This may start as null, but
     * is usually set automatically by the control(l)er before the zeroth iteration.
     * <p></p>
     * Note that there is deliberately no way to set the link id except at creation.
     * We might consider something like moveActivityTo( linkid, coord ).  kai, aug'10
     */
    Id<Link> getLinkId();
}
