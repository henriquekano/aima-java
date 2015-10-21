package aima.gui.applications.search.map;

import java.util.List;
import java.io.File;
import java.util.Map;

import aima.gui.framework.AgentAppFrame;

/**
 * Universal frame for experiments with route planning agents. It configures the
 * agent application frame with some selectors and an agent view which is
 * designed for cooperation with an {@link aima.core.environment.map.MapEnvironment}.
 * Since items for scenario, agent, destination, agent, and heuristic selection
 * are application specific, this general implementation provides items only
 * for search strategy and mode selection.
 * 
 * @author Ruediger Lunde
 */
public class MapAgentFrame extends AgentAppFrame {
	
	private static final long serialVersionUID = 1L;
	public static String SCENARIO_SEL = "ScenarioSelection";
	public static String DESTINATION_SEL = "DestinationSelection";
	public static String AGENT_SEL = "AgentSelection";
	public static String SEARCH_SEL = "SearchSelection";
	public static String SEARCH_MODE_SEL = "SearchModeSelection";
	public static String HEURISTIC_SEL = "HeuristicSelection";
	public static String MAP_SEL = "MapSelection";
	public static String NEW_MAP_SEL = "NewMapSelection";
	
	public static Object[] mapList;

	/** Standard constructor. */
	public MapAgentFrame() {
		setSelectors(
				new String[] { SCENARIO_SEL, DESTINATION_SEL, AGENT_SEL, SEARCH_SEL, SEARCH_MODE_SEL, HEURISTIC_SEL, 
						MAP_SEL }, 
				new String[] { "Select Scenario", "Select Destinations", "Select Agent", "Select Search Strategy", 
						"Select Search Mode","Select Heuristic", "Select Map" });
		setSelectorItems(SEARCH_SEL, SearchFactory.getInstance()
				.getSearchStrategyNames(), 5);
		setSelectorItems(SEARCH_MODE_SEL, SearchFactory.getInstance()
				.getSearchModeNames(), 0);
		setSelectorItems(MAP_SEL, mapList, 0);
		
		setEnvView(new MapAgentView());
		setSplitPaneResizeWeight(0.75);
		setSize(1200, 700);
	}
	
}