/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/agent-java-soapui
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.epam.reportportal.soapui.service;

public class SoapUIContext {
	private String launchName;
	private String launchId;

	private boolean isLaunchFailed;
	private boolean isTestCanceled;

	public String getLaunchName() {
		return launchName;
	}

	public void setLaunchName(String launchName) {
		this.launchName = launchName;
	}

	public String getLaunchId() {
		return launchId;
	}

	public void setLaunchId(String launchId) {
		this.launchId = launchId;
	}

	public boolean isLaunchFailed() {
		return isLaunchFailed;
	}

	public void setLaunchFailed(boolean isLaunchFailed) {
		this.isLaunchFailed = isLaunchFailed;
	}

	public boolean isTestCanceled() {
		return isTestCanceled;
	}

	public void setTestCanceled(boolean isTestCanceled) {
		this.isTestCanceled = isTestCanceled;
	}

}
