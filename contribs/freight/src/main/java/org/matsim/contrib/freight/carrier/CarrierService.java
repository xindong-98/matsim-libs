package org.matsim.contrib.freight.carrier;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.population.BasicActivity;
import org.matsim.facilities.ActivityFacility;


public final class CarrierService implements BasicActivity{

	private Coord coord;

	public static class Builder {

		public static Builder newInstance( Id<CarrierService> id, Id<Link> locationLinkId ){
			return new Builder(id,locationLinkId);
		}
		public static Builder newInstance(Id<CarrierService> id, Coord coord){
			return new Builder(id,coord);
		}

		private Id<CarrierService> id;
		private Id<Link> locationLinkId;
		private Coord coord;
		private String name = "service";
		
		private double serviceTime = 0.0;
		private TimeWindow timeWindow = TimeWindow.newInstance(0.0, Integer.MAX_VALUE);
		private int capacityDemand = 0;

		private Builder(Id<CarrierService> id, Id<Link> locationLinkId) {
			super();
			this.id = id;
			this.locationLinkId = locationLinkId;
		}
		private Builder(Id<CarrierService> id, Coord coord) {
			super();
			this.id = id;
			this.coord = coord ;
		}

		public Builder setName(String name){
			this.name = name;
			return this;
		}
		
		/**
		 * By default it is [0.0,Integer.MaxValue].
		 * 
		 * @param serviceDuration
		 * @return
		 */
		public Builder setServiceDuration(double serviceDuration){
			this.serviceTime = serviceDuration;
			return this;
		}
		
		/**
		 * Sets a time-window for the service.
		 * 
		 * <p>Note that the time-window restricts the start-time of the service (i.e. serviceActivity). If one works with hard time-windows (which means that
		 * time-windows must be met) than the service is allowed to start between startTimeWindow.getStart() and startTimeWindow.getEnd().
		 * 
		 * @param startTimeWindow
		 * @return
		 */
		public Builder setServiceStartTimeWindow(TimeWindow startTimeWindow){
			this.timeWindow = startTimeWindow;
			return this;
		}
		
		public CarrierService build(){
			return new CarrierService(this);
		}

		public Builder setCapacityDemand(int value) {
			this.capacityDemand = value;
			return this;
		}

		public void setLinkId( Id<Link> linkId ){
			this.locationLinkId = linkId ;
		}
	}
	
	
	private final Id<CarrierService> id;

	private Id<Link> locationId;
	
	private final String name;
	
	private final double serviceDuration;

	private final TimeWindow timeWindow;

	private final int demand;

	private CarrierService(Builder builder){
		id = builder.id;
		locationId = builder.locationLinkId;
		serviceDuration = builder.serviceTime;
		timeWindow = builder.timeWindow;
		demand = builder.capacityDemand;
		name = builder.name;
		coord = builder.coord ;
	}

	public Id<CarrierService> getId() {
		return id;
	}

	public Id<Link> getLinkId() {
		return locationId;
	}
	public void setLinkId(Id<Link> linkId) {
		// if we allow CarrierService to initially have only coordinates, then it needs to be possible to set the link later.
		// Alternatively, could generate the link when reading the file.  That, however, would imply to _always_ have a network
		// when reading the carrier file, which is impractical and was eventually removed in main matsim.  kai, nov'18

		this.locationId = linkId ;
	}

	public Id<ActivityFacility> getFacilityId() {
		return null ;
		// yyyy maybe implement at some point.  kai/kmt, nov'18
	}

	@Override
	public Coord getCoord(){
		return this.coord ;
	}

	public double getServiceDuration() {
		return serviceDuration;
	}

	public TimeWindow getServiceStartTimeWindow(){
		return timeWindow;
	}
	
	public int getCapacityDemand() {
		return demand;
	}
	
	/**
	 * @return the name
	 */
	public String getType() {
		return name;
	}

	@Override
	public String toString() {
		return "[id=" + id + "][locationId=" + locationId + "][capacityDemand=" + demand + "][serviceDuration=" + serviceDuration + "][startTimeWindow=" + timeWindow + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CarrierService other = (CarrierService) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	
	
}
