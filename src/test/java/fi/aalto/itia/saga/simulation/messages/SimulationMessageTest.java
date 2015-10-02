package fi.aalto.itia.saga.simulation.messages;

import java.io.IOException;

import org.junit.Test;

import fi.aalto.itia.saga.prosumer.Prosumer;

public class SimulationMessageTest {

	@Test
	public void serializeTest() throws IOException {
		SimulationMessage.serialize(new DayAheadContentResponse() );
	}

}
