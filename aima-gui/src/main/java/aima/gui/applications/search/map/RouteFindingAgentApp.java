package aima.gui.applications.search.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import aima.core.environment.map.AdaptableHeuristicFunction;
import aima.core.environment.map.ExtendableMap;
import aima.core.environment.map.MapAgent;
import aima.core.environment.map.MapEnvironment;
import aima.core.environment.map.Scenario;
import aima.core.util.datastructure.Point2D;
import aima.gui.framework.AgentAppController;
import aima.gui.framework.AgentAppEnvironmentView;
import aima.gui.framework.AgentAppFrame;
import aima.gui.framework.AgentAppFrame.SelectionState;
import aima.gui.framework.MessageLogger;
import aima.gui.framework.SimpleAgentApp;

/**
 * Demo example of a route finding agent application with GUI. The main method
 * starts a map agent frame and supports runtime experiments. This
 * implementation is based on the {@link aima.core.environment.map.MapAgent} and
 * the {@link aima.core.environment.map.MapEnvironment}. It can be used as a
 * code template for creating new applications with different specialized kinds
 * of agents and environments.
 * 
 * @author Ruediger Lunde
 */
public class RouteFindingAgentApp extends SimpleAgentApp {

	private static String MAP_SOURCE_FILE = "map.txt";
	
	private static Map<String, aima.core.environment.map.Map> maps = new HashMap<String, aima.core.environment.map.Map>();
	
	private static String CURRENT_MAP = "Rio Grande do Sul";
	
	public static void createMap(File file){
		try{
			ExtendableMap map = new ExtendableMap();
			FileInputStream fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String[] lineParameters = {};
			String mapName = br.readLine();
			System.out.println("mapName: " + mapName);
			
			int localsNumber = Integer.parseInt(br.readLine());
			System.out.println("locals number" + localsNumber);
			for(int i = 0; i < localsNumber; i++){
				lineParameters = br.readLine().split(" ");
				System.out.println("local " + (i + 1) + ": " + String.join(",", lineParameters));
				map.setPosition(
						lineParameters[0], 
						Double.parseDouble(lineParameters[1]), 
						Double.parseDouble(lineParameters[2]));
			}
	
			int routesNumber = Integer.parseInt(br.readLine());
			System.out.println("routes number" + routesNumber);
			for(int i = 0; i < routesNumber; i++){
				lineParameters = br.readLine().split(" ");
				System.out.println("route " + (i + 1) + ": " + String.join(",", lineParameters));
				map.addBidirectionalLink(
						lineParameters[0], 
						lineParameters[1], 
						Double.parseDouble(lineParameters[2]));
			}
			System.out.println("New map: " + file.getName());
			maps.put(mapName, map);
		}catch(Exception e){
			e.printStackTrace();
		}
	} 

	/** Creates a <code>MapAgentView</code>. */
	public AgentAppEnvironmentView createEnvironmentView() {
		return new ExtendedMapAgentView();
	}
	
	/** Creates and configures a <code>RouteFindingAgentFrame</code>. */
	@Override
	public AgentAppFrame createFrame() {
		return new RouteFindingAgentFrame();
	}

	/** Creates a <code>RouteFindingAgentController</code>. */
	@Override
	public AgentAppController createController() {
		return new RouteFindingAgentController();
	}

	// //////////////////////////////////////////////////////////
	// local classes

	/** Frame for a graphical route finding agent application. */
	protected static class RouteFindingAgentFrame extends MapAgentFrame {
		private static final long serialVersionUID = 1L;

		public static enum MapType {
			ROMANIA, AUSTRALIA
		};

		private MapType usedMap = null;
		private static String[] DESTINATIONS = (String[]) maps.get(CURRENT_MAP).getLocations().toArray(new String[maps.get(CURRENT_MAP).getLocations().size()]);
//		private static String[] AUSTRALIA_DESTS = new String[] {
//				"to Port Hedland", "to Albany", "to Melbourne",
//				"to Random" };

		/** Creates a new frame. */
		public RouteFindingAgentFrame() {
			setTitle("RFA - the Route Finding Agent");
			setSelectorItems(SCENARIO_SEL, DESTINATIONS, 0);
			setSelectorItems(SEARCH_MODE_SEL, SearchFactory.getInstance()
					.getSearchModeNames(), 1); // change the default!
			setSelectorItems(HEURISTIC_SEL, new String[] { "=0", "SLD" }, 1);
			setSelectorItems(DESTINATION_SEL, DESTINATIONS, 0);
			setSelectorItems(MAP_SEL, maps.keySet().toArray(), 0);
		}

		/**
		 * Changes the destination selector items depending on the scenario
		 * selection if necessary, and calls the super class implementation
		 * afterwards.
		 */
		@Override
		protected void selectionChanged(String changedSelector) {
			if(changedSelector != null){
				
				if(changedSelector.equals(MapAgentFrame.MAP_SEL)){
					//Muda as origens e destinos
					SelectionState state = getSelection();
					int mapIdx = state.getIndex(MapAgentFrame.MAP_SEL);
					String newMapName = (String) MapAgentFrame.mapList[mapIdx];
					aima.core.environment.map.Map newMap = maps.get(newMapName);
					
					setSelectorItems(DESTINATION_SEL, newMap.getLocations().toArray(), 0);
					setSelectorItems(SCENARIO_SEL, newMap.getLocations().toArray(), 0);
					
				} else if(changedSelector.equals(MapAgentFrame.NEW_MAP_SEL)) {
//					mudar os selects de acordo com o mapa novo
					MapAgentFrame.mapList = maps.keySet().toArray();
					String newMapName = (String) MapAgentFrame.mapList[0];
					aima.core.environment.map.Map newMap = maps.get(newMapName);
					
					setSelectorItems(MAP_SEL, MapAgentFrame.mapList, 0);
					setSelectorItems(DESTINATION_SEL, newMap.getLocations().toArray(), 0);
					setSelectorItems(SCENARIO_SEL, newMap.getLocations().toArray(), 0);
				}
				
			}
			super.selectionChanged(changedSelector);
		}
		
		@Override
		public void fileActionListener(File file) {
			RouteFindingAgentApp.createMap(file);
			selectionChanged(MapAgentFrame.NEW_MAP_SEL);
		}
	}

	/** Controller for a graphical route finding agent application. */
	protected static class RouteFindingAgentController extends
			AbstractMapAgentController {
		/**
		 * Configures a scenario and a list of destinations. Note that for route
		 * finding problems, the size of the list needs to be 1.
		 */
		@Override
		protected void selectScenarioAndDest(int scenarioIdx, int destIdx) {		
			ExtendableMap map = (ExtendableMap) maps.get(getCURRENT_MAP());
			MapEnvironment env = new MapEnvironment(map);
			String agentLoc = map.getLocations().get(scenarioIdx);
//			switch (scenarioIdx) {
//			case 0:
//				SimplifiedRoadMapOfPartOfRomania.initMap(map);
//				agentLoc = SimplifiedRoadMapOfPartOfRomania.ARAD;
//				break;
//			case 1:
//				SimplifiedRoadMapOfPartOfRomania.initMap(map);
//				agentLoc = SimplifiedRoadMapOfPartOfRomania.LUGOJ;
//				break;
//			case 2:
//				SimplifiedRoadMapOfPartOfRomania.initMap(map);
//				agentLoc = SimplifiedRoadMapOfPartOfRomania.FAGARAS;
//				break;
//			case 3:
//				SimplifiedRoadMapOfAustralia.initMap(map);
//				agentLoc = SimplifiedRoadMapOfAustralia.SYDNEY;
//				break;
//			case 4:
//				SimplifiedRoadMapOfAustralia.initMap(map);
//				agentLoc = map.randomlyGenerateDestination();
//				break;
//			}
			scenario = new Scenario(env, map, agentLoc);
			destinations = new ArrayList<String>();
			destinations.add(map.getLocations().get(destIdx));
//			if (scenarioIdx < 3) {
//				switch (destIdx) {
//				case 0:
//					destinations.add(SimplifiedRoadMapOfPartOfRomania.BUCHAREST);
//					break;
//				case 1:
//					destinations.add(SimplifiedRoadMapOfPartOfRomania.EFORIE);
//					break;
//				case 2:
//					destinations.add(SimplifiedRoadMapOfPartOfRomania.NEAMT);
//					break;
//				case 3:
//					destinations.add(map.randomlyGenerateDestination());
//					break;
//				}
//			} else {
//				switch (destIdx) {
//				case 0:
//					destinations.add(SimplifiedRoadMapOfAustralia.PORT_HEDLAND);
//					break;
//				case 1:
//					destinations.add(SimplifiedRoadMapOfAustralia.ALBANY);
//					break;
//				case 2:
//					destinations.add(SimplifiedRoadMapOfAustralia.MELBOURNE);
//					break;
//				case 3:
//					destinations.add(map.randomlyGenerateDestination());
//					break;
//				}
//			}
		}

		/**
		 * Prepares the view for the previously specified scenario and
		 * destinations.
		 */
		@Override
		protected void prepareView() {
			ExtendedMapAgentView mEnv = (ExtendedMapAgentView) frame.getEnvView();
			mEnv.setData(scenario, destinations, null);
			mEnv.setEnvironment(scenario.getEnv());
		}

		/**
		 * Returns the trivial zero function or a simple heuristic which is
		 * based on straight-line distance computation.
		 */
		@Override
		protected AdaptableHeuristicFunction createHeuristic(int heuIdx) {
			AdaptableHeuristicFunction ahf = null;
			switch (heuIdx) {
			case 0:
				ahf = new H1();
				break;
			default:
				ahf = new H2();
			}
			return ahf.adaptToGoal(destinations.get(0), scenario
					.getAgentMap());
		}

		/**
		 * Creates a new agent and adds it to the scenario's environment.
		 */
		@Override
		public void initAgents(MessageLogger logger) {
			if (destinations.size() != 1) {
				logger.log("Error: This agent requires exact one destination.");
				return;
			}
			MapEnvironment env = scenario.getEnv();
			String goal = destinations.get(0);
			MapAgent agent = new MapAgent(env.getMap(), env, search, new String[] { goal });
			env.addAgent(agent, scenario.getInitAgentLocation());
		}

		@Override
		protected void selectMap(SelectionState state) {
			// muda o mapa
			int mapIdx = state.getIndex(MapAgentFrame.MAP_SEL);
			String newMapName = (String) MapAgentFrame.mapList[mapIdx];
			aima.core.environment.map.Map newMap = maps.get(newMapName);
			
			MapEnvironment newMapEnv = new MapEnvironment(newMap);
			
			this.scenario = new Scenario(newMapEnv, newMap, newMap.getLocations().get(state.getIndex(MapAgentFrame.SCENARIO_SEL)));
			this.destinations = new ArrayList<String>();
			this.destinations.add(newMap.getLocations().get(state.getIndex(MapAgentFrame.DESTINATION_SEL)));
		}
	}

	public static String getCURRENT_MAP() {
		return CURRENT_MAP;
	}

	public static void setCURRENT_MAP(String cURRENT_MAP) {
		CURRENT_MAP = cURRENT_MAP;
	}

	/**
	 * Returns always the heuristic value 0.
	 */
	static class H1 extends AdaptableHeuristicFunction {

		public double h(Object state) {
			return 0.0;
		}
	}

	/**
	 * A simple heuristic which interprets <code>state</code> and {@link #goal}
	 * as location names and uses the straight-line distance between them as
	 * heuristic value.
	 */
	static class H2 extends AdaptableHeuristicFunction {

		public double h(Object state) {
			double result = 0.0;
			Point2D pt1 = map.getPosition((String) state);
			Point2D pt2 = map.getPosition((String) goal);
			if (pt1 != null && pt2 != null)
				result = pt1.distance(pt2);
			return result;
		}
	}

	// //////////////////////////////////////////////////////////
	// starter method

	/** Application starter. */
	public static void main(String args[]) {
		/**Iniciando com um mapa default**/
		Class clazz = RouteFindingAgentApp.class;
		String mapSourceURI = clazz.getResource(MAP_SOURCE_FILE).getPath();
		
		File file = new File(mapSourceURI);
		createMap(file);
		
		/**Adicionando as opcoes de mapa**/
		MapAgentFrame.mapList = maps.keySet().toArray();
		
		new RouteFindingAgentApp().startApplication();
	}
}
