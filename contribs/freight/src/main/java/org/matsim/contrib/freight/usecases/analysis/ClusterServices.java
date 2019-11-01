package org.matsim.contrib.freight.usecases.analysis;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.freight.carrier.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ClusterServices {

    private static Logger log = Logger.getLogger(ClusterServices.class);


    public static void main(String[] args) {

        Carriers carriers = new Carriers();
        new CarrierPlanXmlReader(carriers).readFile("C:/svn/shared-svn/studies/tschlenther/freightAV/FrachtNachfrage/KEP/PrivatkundenDirekt/carriers_woSolution.xml.gz");

        int maxCap = 230;
        log.info("START... \n \n");
        carriers.getCarriers().values().forEach(carrier -> clusterAllServicesOnTheSameLinkWithMaxCap(carrier, maxCap));
        log.info("END OF CLUSTERING. START WRITING RESULT... \n \n");

        new CarrierPlanXmlWriterV2(carriers).write("C:/svn/shared-svn/studies/tschlenther/PAVE/Daten/THALLER/thallerCarriersClusteredByLinkAndTW_maxCap" + maxCap + ".xml.gz");
        log.info("END...");
    }

    static void clusterAllServicesOnTheSameLinkWithMaxCap(Carrier carrier, int maxCapDemandPerService) {
        log.info("BEGIN CLUSTERING FOR CARRIER " + carrier.getId());
        log.info("number of services: " + carrier.getServices().size());

        //group by link id
        Map<Id<Link>, List<CarrierService>> linkServicesMap = carrier.getServices().values().stream().collect(Collectors.groupingBy(CarrierService::getLocationLinkId));
        AtomicInteger countOldServices = new AtomicInteger();
        AtomicInteger countNewServices = new AtomicInteger();
        for (Id<Link> linkId : linkServicesMap.keySet()) {
            //group by time windows
            Map<TimeWindow, List<CarrierService>> timeWindowServicesMap = linkServicesMap.get(linkId).stream().collect(Collectors.groupingBy(CarrierService::getServiceStartTimeWindow));


            timeWindowServicesMap.keySet().stream().forEach(timeWindow -> {
                int capDemand = 0;
                double duration = 0;

                int internalCountOld = 0;
                int createdServices = 0;
                for (CarrierService service : timeWindowServicesMap.get(timeWindow)) {

                    if (capDemand + service.getCapacityDemand() > maxCapDemandPerService) {
                        createdServices = createServiceAndIncrementCounter(carrier, linkId, timeWindow, capDemand, duration, createdServices);
                        capDemand = service.getCapacityDemand();
                        duration = service.getServiceDuration();
                    } else {
                        capDemand += service.getCapacityDemand();
                        duration += service.getServiceDuration();
                    }
                    internalCountOld++;
                    carrier.getServices().remove(service.getId());
                }
                createdServices = createServiceAndIncrementCounter(carrier, linkId, timeWindow, capDemand, duration, createdServices);
                log.info("clustering " + internalCountOld + " services into " + createdServices + " new services");
                countOldServices.addAndGet(internalCountOld);
                countNewServices.addAndGet(createdServices);
            });
        }
        log.info("FINISHED");
        log.info("CLUSTERED " + countOldServices + " SERVICES INTO " + countNewServices + " NEW SERVICES");
        log.info("in total, there are " + carrier.getServices().size() + " services for carrier " + carrier.getId() + " now");
    }

    private static int createServiceAndIncrementCounter(Carrier carrier, Id<Link> linkId, TimeWindow timeWindow, int capDemand, double duration, int createdServices) {
        createdServices++;

        Id<CarrierService> id = Id.create(linkId.toString() + "_" + timeWindow.getStart() + "_" + timeWindow.getEnd() + "_" + createdServices, CarrierService.class);
        carrier.getServices().put(id, CarrierService.Builder.newInstance(id, linkId)
                .setServiceDuration(duration)
                .setCapacityDemand(capDemand)
                .setServiceStartTimeWindow(timeWindow)
                .build());
        return createdServices;
    }

}
