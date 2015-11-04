/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2014 by the members listed in the COPYING,        *
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
package playground.agarwalamit.mixedTraffic.patnaIndia;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ModeParams;
import org.matsim.core.config.groups.PlansCalcRouteConfigGroup.ModeRoutingParams;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup.StrategySettings;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import playground.agarwalamit.mixedTraffic.MixedTrafficVehiclesUtils;

/**
 * @author amit
 */

public class PatnaSimulationTimeWriter {
	
	private final int [] randomNumbers = {4711, 6835, 1847, 4144, 4628, 2632, 5982, 3218, 5736, 7573,4389, 1344} ;
	private static String outputFolder = "../../../../repos//runs-svn/patnaIndia/run107/output/";
	private static String inputFilesDir = "../../../../repos//runs-svn/patnaIndia/run107/input/";
	
	public static void main(String[] args) {
		
		boolean isUsingCluster = false;
		
		if (args.length != 0) isUsingCluster = true;

		if ( isUsingCluster ) {
			outputFolder = args[0];
			inputFilesDir = args[1];
		}
		
		PatnaSimulationTimeWriter pstw = new PatnaSimulationTimeWriter();

		PrintStream writer;
		try {
			writer = new PrintStream(outputFolder+"/simTime.txt");
			writer.print("scenario \t simTime \n");
		} catch (Exception e) {
			throw new RuntimeException("Data is not written. Reason : "+e);
		}

		{
			//fifo without holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.FIFO, QSimConfigGroup.TrafficDynamics.queue, inputFilesDir);
			try {
				writer.print("fifo_withoutHoles"+"\t"+data2Write);
				writer.println();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}

		{
			//fifo with holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.FIFO, QSimConfigGroup.TrafficDynamics.withHoles, inputFilesDir);
			try {
				writer.print("fifo_withHoles"+"\t"+data2Write);
				writer.println();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}
		
		{
			//passing without holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.PassingQ, QSimConfigGroup.TrafficDynamics.queue, inputFilesDir);
			try {
				writer.print("passing_withoutHoles"+"\t"+data2Write);
				writer.println();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}
		
		{
			//passing with holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.PassingQ, QSimConfigGroup.TrafficDynamics.withHoles, inputFilesDir);
			try {
				writer.print("passing_withHoles"+"\t"+data2Write);
				writer.println();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}
		
		{
			//seepage without holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.SeepageQ, QSimConfigGroup.TrafficDynamics.queue, inputFilesDir);
			try {
				writer.print("seepage_withoutHoles"+"\t"+data2Write);
				writer.println();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}
		
		{
			//seepage with holes
			String data2Write = pstw.runAndReturnSimulationTime(QSimConfigGroup.LinkDynamics.SeepageQ, QSimConfigGroup.TrafficDynamics.withHoles, inputFilesDir);
			try {
				writer.print("seepage_withHoles"+"\t"+data2Write);
				writer.println();
				writer.close();
			} catch (Exception e) {
				throw new RuntimeException("Data is not written. Reason : "+e);
			}
		}
	}
	
	private String runAndReturnSimulationTime (QSimConfigGroup.LinkDynamics ld, QSimConfigGroup.TrafficDynamics td, String inputFilesDir) {
		
		Config config = createBasicConfigSettings();
		
		config.plans().setInputFile(inputFilesDir+"/SelectedPlansOnly.xml");
		config.network().setInputFile(inputFilesDir+"/network.xml");
		config.counts().setCountsFileName(inputFilesDir+"/counts/countsCarMotorbikeBike.xml");
		
		config.qsim().setLinkDynamics(ld.toString());
		config.qsim().setTrafficDynamics(td);
		
		if(ld.equals(QSimConfigGroup.LinkDynamics.SeepageQ)) {
			config.setParam("seepage", "seepMode","bike");
			config.setParam("seepage", "isSeepModeStorageFree", "false");
			config.setParam("seepage", "isRestrictingNumberOfSeepMode", "true");
		}
		
		config.controler().setCreateGraphs(false);
		config.qsim().setVehiclesSource(QSimConfigGroup.VehiclesSource.fromVehiclesData);
		Scenario sc = ScenarioUtils.createScenario(config);
		
		Map<String, VehicleType> modesType = new HashMap<String, VehicleType>(); 
		VehicleType car = VehicleUtils.getFactory().createVehicleType(Id.create("car",VehicleType.class));
		car.setMaximumVelocity(MixedTrafficVehiclesUtils.getSpeed("car"));
		car.setPcuEquivalents(1.0);
		modesType.put("car", car);
		sc.getVehicles().addVehicleType(car);

		VehicleType motorbike = VehicleUtils.getFactory().createVehicleType(Id.create("motorbike",VehicleType.class));
		motorbike.setMaximumVelocity(MixedTrafficVehiclesUtils.getSpeed("motorbike"));
		motorbike.setPcuEquivalents(0.25);
		modesType.put("motorbike", motorbike);
		sc.getVehicles().addVehicleType(motorbike);

		VehicleType bike = VehicleUtils.getFactory().createVehicleType(Id.create("bike",VehicleType.class));
		bike.setMaximumVelocity(MixedTrafficVehiclesUtils.getSpeed("bike"));
		bike.setPcuEquivalents(0.25);
		modesType.put("bike", bike);
		sc.getVehicles().addVehicleType(bike);
		
		for(Person p:sc.getPopulation().getPersons().values()){
			Id<Vehicle> vehicleId = Id.create(p.getId(),Vehicle.class);
			String travelMode = null;
			for(PlanElement pe :p.getSelectedPlan().getPlanElements()){
				if (pe instanceof Leg) {
					travelMode = ((Leg)pe).getMode();
					break;
				}
			}
			Vehicle vehicle = VehicleUtils.getFactory().createVehicle(vehicleId,modesType.get(travelMode));
			sc.getVehicles().addVehicle(vehicle);
		}
		
		final Controler controler = new Controler(sc);
		controler.getConfig().controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.overwriteExistingFiles);
		controler.setDumpDataAtEnd(false);

		String simulationTime = "";
		for (int i = 0; i<randomNumbers.length;i++) {
			MatsimRandom.reset(randomNumbers[i]);
			double startTime = System.currentTimeMillis();
			controler.run();
			double endTime = System.currentTimeMillis();

			if(i>1 ) { // avoid two initial runs
				simulationTime = simulationTime.concat( String.valueOf(endTime - startTime) + "\t");
			}
		}
		return simulationTime;
	}
	
	private Config createBasicConfigSettings(){
		Collection <String> mainModes = Arrays.asList("car","motorbike","bike");

		Config config = ConfigUtils.createConfig();
		
		config.counts().setWriteCountsInterval(100);
		config.counts().setCountsScaleFactor(94.52); 

		config.controler().setFirstIteration(0);
		config.controler().setLastIteration(200);
		//disable writing of the following data
		config.controler().setWriteEventsInterval(0);
		config.controler().setWritePlansInterval(0);
		config.controler().setWriteSnapshotsInterval(0);	

		config.qsim().setFlowCapFactor(0.011);		//1.06% sample
		config.qsim().setStorageCapFactor(0.033);
		config.qsim().setEndTime(36*3600);
		config.qsim().setMainModes(mainModes);
		

		config.setParam("TimeAllocationMutator", "mutationAffectsDuration", "false");
		config.setParam("TimeAllocationMutator", "mutationRange", "7200.0");

		StrategySettings expChangeBeta = new StrategySettings(Id.create("1",StrategySettings.class));
		expChangeBeta.setStrategyName("ChangeExpBeta");
		expChangeBeta.setWeight(0.9);

		StrategySettings reRoute = new StrategySettings(Id.create("2",StrategySettings.class));
		reRoute.setStrategyName("ReRoute");
		reRoute.setWeight(0.1);

		StrategySettings timeAllocationMutator	= new StrategySettings(Id.create("3",StrategySettings.class));
		timeAllocationMutator.setStrategyName("TimeAllocationMutator");
		timeAllocationMutator.setWeight(0.05);

		config.strategy().addStrategySettings(expChangeBeta);
		config.strategy().addStrategySettings(reRoute);
		config.strategy().addStrategySettings(timeAllocationMutator);

		config.strategy().setFractionOfIterationsToDisableInnovation(0.8);

		//vsp default
		config.plans().setRemovingUnneccessaryPlanAttributes(true);
		config.vspExperimental().addParam("vspDefaultsCheckingLevel", "abort");
		//vsp default

		ActivityParams workAct = new ActivityParams("work");
		workAct.setTypicalDuration(8*3600);
		config.planCalcScore().addActivityParams(workAct);

		ActivityParams homeAct = new ActivityParams("home");
		homeAct.setTypicalDuration(12*3600);
		config.planCalcScore().addActivityParams(homeAct);


		config.planCalcScore().setMarginalUtlOfWaiting_utils_hr(0);// changed to 0 from (-2) earlier
		config.planCalcScore().setPerforming_utils_hr(6.0);
		
		ModeParams car = new ModeParams("car");
		car.setConstant(-3.30);
		car.setMarginalUtilityOfTraveling(0.0);
		config.planCalcScore().addModeParams(car);
		
		ModeParams bike = new ModeParams("bike");
		bike.setConstant(0.0);
		bike.setMarginalUtilityOfTraveling(0.0);
		config.planCalcScore().addModeParams(bike);
		
		ModeParams motorbike = new ModeParams("motorbike");
		motorbike.setConstant(-2.20);
		motorbike.setMarginalUtilityOfTraveling(0.0);
		config.planCalcScore().addModeParams(motorbike);
		
		ModeParams pt = new ModeParams("pt");
		pt.setConstant(-3.40);
		pt.setMarginalUtilityOfTraveling(0.0);
		config.planCalcScore().addModeParams(pt);
		
		ModeParams walk = new ModeParams("walk");
		walk.setConstant(0.0);
		walk.setMarginalUtilityOfTraveling(0.0);
		config.planCalcScore().addModeParams(walk);
		
		config.plansCalcRoute().setNetworkModes(mainModes);
		
		{
			ModeRoutingParams mrp = new ModeRoutingParams("walk");
			mrp.setTeleportedModeSpeed(4./3.6);
			config.plansCalcRoute().addModeRoutingParams(mrp);
		}
		
		{
			ModeRoutingParams mrp = new ModeRoutingParams("pt");
			mrp.setTeleportedModeSpeed(20./3.6);
			config.plansCalcRoute().addModeRoutingParams(mrp);
		}
		return config;
	}
}
