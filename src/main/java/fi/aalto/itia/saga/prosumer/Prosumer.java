/**
 * 
 */
package fi.aalto.itia.saga.prosumer;

import fi.aalto.itia.saga.storage.StorageController;

/**
 * @author giovanc1
 *
 */
public class Prosumer {

	private int id;
	private StorageController storageController;

	/**
	 * 
	 */
	public Prosumer(int id, StorageController sc) {
		this.id = id;
		this.storageController = sc;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public StorageController getStorageController() {
		return storageController;
	}

	public void setStorageController(StorageController storageController) {
		this.storageController = storageController;
	}

}
