/**
 * 
 */
package com.plectix.rulestudio.views.jobs;

import com.plectix.rulestudio.views.simulator.RsLiveData;

/**
 * @author bill
 *
 */
public interface RunSimData {

	void addConsole(String line);

	void addLiveData(RsLiveData rsLiveData);

}
