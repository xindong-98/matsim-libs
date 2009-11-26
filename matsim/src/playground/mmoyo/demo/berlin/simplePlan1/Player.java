package playground.mmoyo.demo.berlin.simplePlan1;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import playground.mmoyo.demo.ScenarioPlayer;

public class Player {

	public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
		//invoke controler first
		
		String configFile = "src/playground/mmoyo/demo/berlin/simplePlan1/config.xml";
		String scheduleFile =  "../shared-svn/studies/countries/de/berlin-bvg09/ptManuel/old/simpleTransitSchedule.xml";
		ScenarioPlayer.main(new String[]{configFile, scheduleFile});
	}
}
