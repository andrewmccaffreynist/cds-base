package gov.nist.healthcare.cds.domain;

import gov.nist.healthcare.cds.domain.wrapper.MetaData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document
public class TestPlan implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String id;
	@NotNull
	private String name;
	private String description;
	private MetaData metaData;
	@JsonIgnore
	@Indexed
	private String user;
	@DBRef
	private List<TestCase> testCases;
	private List<TestCaseGroup> testCaseGroups;
	
	public TestPlan(){
//		testCaseGroups = new ArrayList<>();
//		testCases = new ArrayList<>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public MetaData getMetaData() {
		return metaData;
	}
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}
	public List<TestCase> getTestCases() {
		if(testCases == null){
			testCases = new ArrayList<>();
		}
		return testCases;
	}
	public List<TestCaseGroup> getTestCaseGroups() {
		if(this.testCaseGroups == null)
			this.testCaseGroups = new ArrayList<>();
		return testCaseGroups;
	}
	public void setTestCaseGroups(List<TestCaseGroup> testCaseGroups) {
		this.testCaseGroups = testCaseGroups;
	}
	public void setTestCases(List<TestCase> testCases) {
		this.testCases = testCases;
	}
	public void addTestCase(TestCase tc){
		if(this.testCases == null)
			this.testCases = new ArrayList<TestCase>();
		tc.setTestPlan(this.getId());
		this.testCases.add(tc);
	}
	
	public TestCaseGroup getOrCreateGroup(String id,String name){
		if(testCaseGroups != null){
			for(TestCaseGroup gr : testCaseGroups){
				if(gr.getId().equals(id)){
					return gr;
				}
			}
		}
		TestCaseGroup tcg = new TestCaseGroup();
		tcg.setId(id);
		tcg.setName(name);
		tcg.setTestPlan(this.getId());
		this.getTestCaseGroups().add(tcg);
		return tcg;
	}
	
	public TestCaseGroup createGroup(String name){
		TestCaseGroup tcg = new TestCaseGroup();
		tcg.setId(UUID.randomUUID().toString());
		tcg.setName(name);
		tcg.setTestPlan(this.getId());
		this.getTestCaseGroups().add(tcg);
		return tcg;
	}
	
	public TestCaseGroup getByNameOrCreateGroup(String name){
		if(testCaseGroups != null){
			for(TestCaseGroup gr : testCaseGroups){
				if(gr.getName().equals(name)){
					return gr;
				}
			}
		}
		TestCaseGroup tcg = new TestCaseGroup();
		tcg.setId(UUID.randomUUID().toString());
		tcg.setName(name);
		tcg.setTestPlan(this.getId());
		this.getTestCaseGroups().add(tcg);
		return tcg;
	}
	
	public TestCaseGroup getGroup(String id){
		if(testCaseGroups == null)
			return null;
		for(TestCaseGroup gr : testCaseGroups){
			if(gr.getId().equals(id)){
				return gr;
			}
		}
		return null;
	}
	
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (id == null || obj == null || getClass() != obj.getClass())
            return false;
        TestPlan that = (TestPlan) obj;
        return id.equals(that.id);
    }
    
    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
}
