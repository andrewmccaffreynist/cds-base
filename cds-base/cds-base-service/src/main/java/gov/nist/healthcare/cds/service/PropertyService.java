package gov.nist.healthcare.cds.service;

import gov.nist.healthcare.cds.domain.SoftwareConfig;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.TestPlan;
import gov.nist.healthcare.cds.domain.wrapper.Report;

public interface PropertyService {

	public TestCase tcBelongsTo(String tp, String user);
	public TestPlan tpBelongsTo(String tc, String user);
	public Report reportBelongsTo(String report, String user);
	public SoftwareConfig configBelongsTo(String config, String user);
	
}
