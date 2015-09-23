/**
 * 
 */
package fi.aalto.itia.saga.prosumer.storage;

import org.junit.Test;

import fi.aalto.itia.saga.prosumer.storage.StorageModel;
import static org.junit.Assert.*;

/**
 * @author giovanc1
 *
 */
public class StorageModelTest {

//	private static final Double MAX_CAPACITY_W = 10000d;
//	private static final Double INITIAL_STATE_OF_CHARGE = MAX_CAPACITY_W / 2;
//	private static final Double MAX_CHARGING_RATE_WH = MAX_CAPACITY_W / 4;
//	private static final Double MAX_DISCHARGING_RATE_WH = MAX_CAPACITY_W / 4;
//	private static final String ID = "0";
//	private static StorageModel storage;
//	private static Double tempStateOfCharge;
//
//	private StorageModel getStorageModel() {
//		return new StorageModel(ID, INITIAL_STATE_OF_CHARGE, MAX_CAPACITY_W,
//				MAX_CHARGING_RATE_WH, MAX_DISCHARGING_RATE_WH);
//	}
//
//	@Test
//	public void chargeAndDischargeTest() throws Exception {
//
//		// boundary charge
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		storage.chargeAndDischarge(MAX_CHARGING_RATE_WH, 0d);
//		assertEquals("Charging Storage with MAX_CHARGING_RATE",
//				storage.getStateOfChargeW(), tempStateOfCharge
//						+ MAX_CHARGING_RATE_WH, 0d);
//
//		// Boundary Charge and discharge
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		storage.chargeAndDischarge(MAX_CHARGING_RATE_WH,
//				MAX_DISCHARGING_RATE_WH);
//		assertEquals(
//				"Charging and DisCharging Storage with MAX_(DIS)CHARGING_RATE",
//				storage.getStateOfChargeW(), tempStateOfCharge
//						+ MAX_CHARGING_RATE_WH - MAX_DISCHARGING_RATE_WH, 0d);
//
//		// Boundary Discharge
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		storage.chargeAndDischarge(0d, MAX_DISCHARGING_RATE_WH);
//		assertEquals("DisCharging Storage with MAX_DISCHARGING_RATE",
//				storage.getStateOfChargeW(), tempStateOfCharge
//						- MAX_DISCHARGING_RATE_WH, 0d);
//
//		// No Charge
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		storage.chargeAndDischarge(0d, 0d);
//		assertEquals("No Charge/Discharge", storage.getStateOfChargeW(),
//				tempStateOfCharge);
//	}
//
//	@Test
//	public void chargeAndDischargeTestExceedRates() {
//		// Exceed of charging rate
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(2 * MAX_CHARGING_RATE_WH, 0d);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Exceed in Charging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge
//							+ MAX_CHARGING_RATE_WH, 0d);
//		}
//
//		// Exceed of discharging rate
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(0d, 2 * MAX_DISCHARGING_RATE_WH);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals(
//					"Exceed in DisCharging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge
//							- MAX_DISCHARGING_RATE_WH, 0d);
//		}
//
//		// Exceed of charging and discharging rate
//		storage = getStorageModel();
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(2 * MAX_CHARGING_RATE_WH,
//					2 * MAX_DISCHARGING_RATE_WH);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Exceed in Charging and in Discharging, Exception msg"
//					+ e.getMessage(), storage.getStateOfChargeW(),
//					tempStateOfCharge + MAX_CHARGING_RATE_WH
//							- MAX_DISCHARGING_RATE_WH, 0d);
//		}
//	}
//
//	@Test
//	public void setStateOfChargeWTest() {
//		storage = getStorageModel();
//
//		try {
//			storage.setStateOfChargeW(MAX_CAPACITY_W);
//		} catch (Exception e) {
//			fail("setStateOfChargeWTest Exception Thrown 1");
//		}
//
//		try {
//			storage.setStateOfChargeW(2 * MAX_CAPACITY_W);
//			fail("setStateOfChargeWTest Exception Thrown 2");
//		} catch (Exception e) {
//		}
//
//		try {
//			storage.setStateOfChargeW(-MAX_CAPACITY_W);
//			fail("setStateOfChargeWTest Exception Thrown 3");
//		} catch (Exception e) {
//		}
//
//		try {
//			storage.setStateOfChargeW(0d);
//		} catch (Exception e) {
//			fail("setStateOfChargeWTest Exception Thrown 4");
//		}
//	}
//
//	@Test
//	public void chargeAndDischargeTestExceedCharge() throws Exception {
//
//		// from MaxCharge Discharge
//		storage = getStorageModel();
//		storage.setStateOfChargeW(MAX_CAPACITY_W);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(0d, MAX_DISCHARGING_RATE_WH);
//		assertEquals("DisCharging Storage with MAX_CHARGING_RATE",
//				storage.getStateOfChargeW(), tempStateOfCharge
//						- MAX_DISCHARGING_RATE_WH, 0d);
//
//		// from MaxCharge charge
//		storage = getStorageModel();
//		storage.setStateOfChargeW(MAX_CAPACITY_W);
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(10d, 0d);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Exceed in Charging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//		}
//
//		// Same charge / discharge usage
//		storage = getStorageModel();
//		storage.setStateOfChargeW(MAX_CAPACITY_W);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(10d, 10d);
//		assertEquals("Charging/Discharging Storage with max capacity",
//				storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//
//		// More discharge than charge usage with Max Capacity
//		storage = getStorageModel();
//		storage.setStateOfChargeW(MAX_CAPACITY_W);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(5d, 10d);
//		assertEquals("Charging-/Discharging+ Storage with max capacity",
//				storage.getStateOfChargeW(), tempStateOfCharge - 5d, 0d);
//
//		// More charge than discharge usage with Max Capacity
//		storage = getStorageModel();
//		storage.setStateOfChargeW(MAX_CAPACITY_W);
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(15d, 10d);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Exceed in Charging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//		}
//	}
//
//	@Test
//	public void chargeAndDischargeTestLackOfCharge() throws Exception {
//		
//		// from Empty Charge charge
//		storage = getStorageModel();
//		storage.setStateOfChargeW(0d);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(MAX_CHARGING_RATE_WH, 0d);
//		assertEquals("Charging Storage with MAX_CHARGING_RATE",
//				storage.getStateOfChargeW(), tempStateOfCharge
//						+ MAX_CHARGING_RATE_WH, 0d);
//
//		// from EmptyCharge charge
//		storage = getStorageModel();
//		storage.setStateOfChargeW(0d);
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(0d, 10d);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Lack in disCharging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//		}
//
//		// Same Empty charge / discharge usage
//		storage = getStorageModel();
//		storage.setStateOfChargeW(0d);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(10d, 10d);
//		assertEquals("Charging/Discharging with empty Storage",
//				storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//		// TODO
//		// from Empty More charge than discharge usage with Empty
//		storage = getStorageModel();
//		storage.setStateOfChargeW(0d);
//		tempStateOfCharge = storage.getStateOfChargeW();
//
//		storage.chargeAndDischarge(15d, 10d);
//		assertEquals("Charge+/discharge- Storage with empty Storage",
//				storage.getStateOfChargeW(), tempStateOfCharge + 5d, 0d);
//
//		// More discharge than charge usage with Empty
//		storage = getStorageModel();
//		storage.setStateOfChargeW(0d);
//		tempStateOfCharge = storage.getStateOfChargeW();
//		try {
//			storage.chargeAndDischarge(5d, 10d);
//			fail("Exception not thrown");
//		} catch (Exception e) {
//			assertEquals("Exceed in Discharging, Exception msg" + e.getMessage(),
//					storage.getStateOfChargeW(), tempStateOfCharge, 0d);
//		}
//	}

}
