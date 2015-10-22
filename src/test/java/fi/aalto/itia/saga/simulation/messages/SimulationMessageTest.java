package fi.aalto.itia.saga.simulation.messages;

import java.io.IOException;

import org.junit.Test;

public class SimulationMessageTest {

	@Test
	public void serializeTest() throws IOException {
		SimulationMessage.serialize(new DayAheadContentResponse() );
	}

}
