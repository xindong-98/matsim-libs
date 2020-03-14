package org.matsim.core.router;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlansConfigGroup.ActivityDurationInterpretation;
import org.matsim.core.population.PopulationUtils;
import org.matsim.facilities.Facility;

public class PlanRouterTest {
	@Test
	public void testDepartureTime() {
		run(ActivityDurationInterpretation.tryEndTimeThenDuration, false);
		run(ActivityDurationInterpretation.tryEndTimeThenDuration, true);
		run(ActivityDurationInterpretation.minOfDurationAndEndTime, false);
		run(ActivityDurationInterpretation.minOfDurationAndEndTime, true);
	}

	private void run(ActivityDurationInterpretation interpretation, boolean useDuration) {
		Config config = ConfigUtils.createConfig();
		config.plans().setActivityDurationInterpretation(interpretation);

		TripRouter.Builder builder = new TripRouter.Builder(config);
		builder.setRoutingModule("generic", new GenericRoutingModule());
		TripRouter tripRouter = builder.build();

		PlanRouter router = new PlanRouter(tripRouter);

		Plan plan = PopulationUtils.createPlan();

		Activity activity;
		Leg leg;

		activity = PopulationUtils.createActivityFromCoord("generic", new Coord(0.0, 0.0));
		activity.setEndTime(1000.0);
		plan.addActivity(activity);

		leg = PopulationUtils.createLeg("generic");
		plan.addLeg(leg);

		activity = PopulationUtils.createActivityFromCoord("generic", new Coord(0.0, 0.0));
		activity.setEndTime(2000.0);
		plan.addActivity(activity);

		leg = PopulationUtils.createLeg("generic");
		plan.addLeg(leg);

		activity = PopulationUtils.createActivityFromCoord("generic", new Coord(0.0, 0.0));
		if (useDuration) activity.setMaximumDuration(500.0);
		activity.setEndTime(3000.0);
		plan.addActivity(activity);

		leg = PopulationUtils.createLeg("generic");
		plan.addLeg(leg);

		activity = PopulationUtils.createActivityFromCoord("generic", new Coord(0.0, 0.0));
		plan.addActivity(activity);

		router.run(plan);

		assertEquals(1000.0, ((Leg) plan.getPlanElements().get(1)).getDepartureTime(), 1e-3);
		assertEquals(2000.0, ((Leg) plan.getPlanElements().get(3)).getDepartureTime(), 1e-3);
		
		if (interpretation.equals(ActivityDurationInterpretation.tryEndTimeThenDuration)) {
			assertEquals(3000.0, ((Leg) plan.getPlanElements().get(5)).getDepartureTime(), 1e-3);
		} else if (!useDuration) {
			assertEquals(3000.0, ((Leg) plan.getPlanElements().get(5)).getDepartureTime(), 1e-3);
		} else {
			assertEquals(2550.0, ((Leg) plan.getPlanElements().get(5)).getDepartureTime(), 1e-3);
		}		
	}

	static private class GenericRoutingModule implements RoutingModule {
		@Override
		public List<? extends PlanElement> calcRoute(Facility fromFacility, Facility toFacility, double departureTime,
				Person person) {
			Leg leg = PopulationUtils.createLeg("generic");
			leg.setDepartureTime(departureTime);
			leg.setTravelTime(50.0);
			return Collections.singletonList(leg);
		}
	}
}
