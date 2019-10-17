/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
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

package org.matsim.contrib.freight.usecases.analysis;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.contrib.freight.carrier.Carrier;
import org.matsim.contrib.freight.carrier.CarrierPlanXmlReader;
import org.matsim.contrib.freight.carrier.CarrierVehicle;
import org.matsim.contrib.freight.carrier.Carriers;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * with the help of this class, all depot locations in a carriers container can be written out to a csv file.
 * the coordinates represent the center of the depot link
 */
class AllDepots2XY {

    public static void main(String[] args){
        String carriersFile = args[0];
        String netFile = args[1];
        String outputFile = args[2];

        Network network = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getNetwork();
        new MatsimNetworkReader(network).readFile(netFile);

        Carriers carriers = new Carriers();
        new CarrierPlanXmlReader(carriers).readFile(carriersFile);

        writeAllDepotsToCSV(carriers,network,outputFile);

    }


    static void writeAllDepotsToCSV(Carriers carriers, Network network, String path){
        Map<Id<Link>, Coord> allDepots = retrieveDepots(carriers, network);
        writeDepots(path, allDepots);
    }

    static void writeAllDepotsToCSV(Carriers carriers, Network network, CoordinateTransformation transformation, String path){
        Map<Id<Link>, Coord> allDepots = retrieveDepots(carriers, network);
        transformCoords(allDepots, transformation);
        writeDepots(path, allDepots);
    }

    private static void transformCoords(Map<Id<Link>, Coord> allDepots, CoordinateTransformation transformation) {
        for (Map.Entry<Id<Link>, Coord> idCoordEntry : allDepots.entrySet()) {
            Coord transformed = transformation.transform(idCoordEntry.getValue());
            allDepots.put(idCoordEntry.getKey(),transformed);
        }
    }

    private static void writeDepots(String path, Map<Id<Link>, Coord> allDepots) {
        try  {
            String header = "DepotLinkId;X;Y\n";
            BufferedWriter writer = IOUtils.getBufferedWriter(path);
            writer.write(header);
            for(Id<Link> depotLink : allDepots.keySet()){
                writer.write(depotLink + ";" + allDepots.get(depotLink).getX() + ";" +  allDepots.get(depotLink).getY() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Map<Id<Link>, Coord> retrieveDepots(Carriers carriers, Network network) {
        Map<Id<Link>, Coord> allDepots = new HashMap<>();
        for (Carrier carrier : carriers.getCarriers().values()) {
            for (CarrierVehicle vehicle : carrier.getCarrierCapabilities().getCarrierVehicles().values()) {
                Coord coord = network.getLinks().get(vehicle.getLocation()).getCoord();
                allDepots.put(vehicle.getLocation(), coord);
            }
        }
        return allDepots;
    }

}
