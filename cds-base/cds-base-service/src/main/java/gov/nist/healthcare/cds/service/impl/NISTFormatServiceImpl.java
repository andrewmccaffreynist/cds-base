package gov.nist.healthcare.cds.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import gov.nist.healthcare.cds.domain.Date;
import gov.nist.healthcare.cds.domain.Event;
import gov.nist.healthcare.cds.domain.ExpectedEvaluation;
import gov.nist.healthcare.cds.domain.ExpectedForecast;
import gov.nist.healthcare.cds.domain.FixedDate;
import gov.nist.healthcare.cds.domain.Injection;
import gov.nist.healthcare.cds.domain.MetaData;
import gov.nist.healthcare.cds.domain.Patient;
import gov.nist.healthcare.cds.domain.Product;
import gov.nist.healthcare.cds.domain.RelativeDate;
import gov.nist.healthcare.cds.domain.TestCase;
import gov.nist.healthcare.cds.domain.VaccinationEvent;
import gov.nist.healthcare.cds.domain.Vaccine;
import gov.nist.healthcare.cds.domain.exception.ProductNotFoundException;
import gov.nist.healthcare.cds.domain.exception.VaccineNotFoundException;
import gov.nist.healthcare.cds.domain.xml.ErrorModel;
import gov.nist.healthcare.cds.domain.xml.beans.*;
import gov.nist.healthcare.cds.enumeration.EvaluationReason;
import gov.nist.healthcare.cds.enumeration.EvaluationStatus;
import gov.nist.healthcare.cds.enumeration.Gender;
import gov.nist.healthcare.cds.enumeration.RelativeTo;
import gov.nist.healthcare.cds.enumeration.SerieStatus;
import gov.nist.healthcare.cds.repositories.ProductRepository;
import gov.nist.healthcare.cds.repositories.VaccineRepository;
import gov.nist.healthcare.cds.service.NISTFormatService;

@Service
public class NISTFormatServiceImpl implements NISTFormatService {

	@Autowired
	private VaccineRepository vaccineRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	final String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
	
	@Override
	public String _export(TestCase tc) {
		try {
			gov.nist.healthcare.cds.domain.xml.beans.TestCase tcp = new gov.nist.healthcare.cds.domain.xml.beans.TestCase();
			tcp.setName(tc.getName());
			tcp.setDescription(tc.getDescription());
			tcp.setEvaluationDate(date(tc.getEvalDate()));
			
			MetaDataType mdt = new MetaDataType();
			if(tc.getMetaData() != null){
				MetaData md = tc.getMetaData();
				mdt.setVersion(md.getVersion());
				mdt.setDateCreated(date(md.getDateCreated()).getFixed().getDate());
				mdt.setDateLastUpdated(date(md.getDateLastUpdated()).getFixed().getDate());
			}
			
			PatientType pt = new PatientType();
			if(tc.getPatient() != null){
				Patient p = tc.getPatient();
				if(p.getGender().equals(Gender.F)){
					pt.setGender(GenderType.FEMALE);
				}
				else {
					pt.setGender(GenderType.MALE);
				}
				pt.setDateOfBirth(date(p.getDob()));
			}
			
			Set<Event> evts = tc.getEvents();
			EventsType evtst = new EventsType();
			if(evts != null){
				for(Event e : evts){
					EventType evt = new EventType();
					evt.setType(e.getType().toString());
					evt.setEventDate(date(e.getDate()));
					VaccinationEvent vev = (VaccinationEvent) e;
					
					VaccineType vt = new VaccineType();
					Injection inject = vev.getAdministred();
					if(inject instanceof Vaccine){
						Vaccine v = (Vaccine) inject;
						vt.setCvx(v.getCvx());
						vt.setName(v.getName());
					}
					else if(inject instanceof Product){
						Product v = (Product) inject;
						vt.setCvx(v.getVx().getCvx());
						vt.setMvx(v.getMx().getMvx());
						vt.setName(v.getName());
					}
					
					evt.setAdministred(vt);
					
					Set<ExpectedEvaluation> evals = vev.getEvaluations();
					EvaluationsType evalst = new EvaluationsType();
					for(ExpectedEvaluation exe : evals){
						EvaluationType et = new EvaluationType();
						if(exe.getStatus().equals(EvaluationStatus.VALID)){
							et.setStatus(StatusType.VALID);
						}
						else {
							et.setStatus(StatusType.INVALID);
						}
						if(exe.getReason() != null){
							EvaluationReasonType evrt = new EvaluationReasonType();
							evrt.setCode(exe.getReason().name());
							evrt.setValue(exe.getReason().getDetails());
							et.setEvaluationReason(evrt);
						}
						VaccineType vte = new VaccineType();
						vte.setCvx(exe.getRelatedTo().getCvx());
						vte.setName(exe.getRelatedTo().getName());
						et.setVaccine(vte);
						
						evalst.getEvaluation().add(et);
					}
					evt.setEvaluations(evalst);
					evtst.getEvent().add(evt);
				}
			}
			
			Set<ExpectedForecast> efs = tc.getForecast();
			ForecastsType fts = new ForecastsType();
			if(efs != null){
				for(ExpectedForecast ef : efs){
					ForecastType ft = new ForecastType();
					ft.setEarliest(date(ef.getEarliest()));
					ft.setRecommended(date(ef.getRecommended()));
					ft.setPastDue(date(ef.getPastDue()));
					ft.setForecastReason(ef.getForecastReason());
					
					if(ef.getSerieStatus() != null){
						SerieStatusType sst = new SerieStatusType();
						sst.setCode(ef.getSerieStatus().toString());
						sst.setDetails(ef.getSerieStatus().getDetails());
						
						ft.setSerieStatus(sst);
					}
					
					VaccineType vt = new VaccineType();
					vt.setCvx(ef.getTarget().getCvx());
					vt.setName(ef.getTarget().getName());
					
					ft.setTarget(vt);
					fts.getForecast().add(ft);
				}
			}
			
			tcp.setPatient(pt);
			tcp.setMetaData(mdt);
			tcp.setForecasts(fts);
			tcp.setEvents(evtst);
			
			return this.objToString(tcp);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public DateType date(Date d) throws DatatypeConfigurationException{
		DateType dt = new DateType();
		
		if(d instanceof FixedDate){
			FixedDate fd = (FixedDate) d;
			GregorianCalendar gregory = new GregorianCalendar();
			gregory.setTime(fd.getDate());
			XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
			        .newXMLGregorianCalendar(
			            gregory);
			FixedDateType fdt = new FixedDateType();
			fdt.setDate(calendar);
			dt.setFixed(fdt);
		}
		else if(d instanceof RelativeDate){
			RelativeDate rd = (RelativeDate) d;
			RelativeDateType rdt = new RelativeDateType();
			rdt.setRelativeTo(rd.getRelativeTo().toString());
			rdt.setDay(rd.getDay());
			rdt.setMonth(rd.getMonth());
			rdt.setYear(rd.getYear());
			dt.setRelative(rdt);
		}
		return dt;
	}
	
	public Date date(DateType d) throws DatatypeConfigurationException{
		if(d.getFixed() != null){
			FixedDateType fd = d.getFixed();
			FixedDate f = new FixedDate(fd.getDate().toGregorianCalendar().getTime());
			return f;
		}
		else if(d.getRelative() != null){
			RelativeDateType rdt = d.getRelative();
			RelativeDate rd = new RelativeDate();
			rd.setRelativeTo(RelativeTo.valueOf(rdt.getRelativeTo()));
			rd.setDay(rdt.getDay());
			rd.setMonth(rdt.getMonth());
			rd.setYear(rdt.getYear());
			return rd;
		}
		return null;
	}
	
	private String objToString(gov.nist.healthcare.cds.domain.xml.beans.TestCase r)
			throws JAXBException, UnsupportedEncodingException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String context = "gov.nist.healthcare.cds.domain.xml.beans";
		JAXBContext jc;
		jc = JAXBContext.newInstance(context);
		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(r, baos);
		return new String(baos.toByteArray(), "UTF-8");
	}
	
	private gov.nist.healthcare.cds.domain.xml.beans.TestCase stringToObj(String r)
			throws JAXBException, UnsupportedEncodingException {
		JAXBContext jc = JAXBContext
				.newInstance("gov.nist.healthcare.cds.domain.xml.beans");
		Unmarshaller u = jc.createUnmarshaller();
		StringReader reader = new StringReader(r);
		gov.nist.healthcare.cds.domain.xml.beans.TestCase o = (gov.nist.healthcare.cds.domain.xml.beans.TestCase) u.unmarshal(reader);
		return o;
	}

	@Override
	public TestCase _import(String file) throws VaccineNotFoundException {
		try {
			gov.nist.healthcare.cds.domain.xml.beans.TestCase tcp = this.stringToObj(file);
			if(tcp == null){
				return null;
			}
			TestCase tc = new TestCase();
			tc.setName(tcp.getName());
			tc.setDescription(tcp.getDescription());
			tc.setEvalDate(date(tcp.getEvaluationDate()));
			
			MetaData md = new MetaData();
			if(tcp.getMetaData() != null){
				MetaDataType mdt = tcp.getMetaData();
				md.setVersion(mdt.getVersion());
				md.setDateCreated(new FixedDate(mdt.getDateCreated().toGregorianCalendar().getTime()));
				md.setDateLastUpdated(new FixedDate(mdt.getDateLastUpdated().toGregorianCalendar().getTime()));
			}
			
			Patient p = new Patient();
			if(tcp.getPatient() != null){
				PatientType pt = tcp.getPatient();
				if(pt.getGender().equals(GenderType.FEMALE)){
					p.setGender(Gender.F);
				}
				else {
					p.setGender(Gender.M);
				}
				p.setDob(date(pt.getDateOfBirth()));
			}
			
			EventsType evts = tcp.getEvents();
			Set<Event> evs = new HashSet<Event>();
			
			if(evts != null){
				List<EventType> etl = evts.getEvent();
				for(EventType e : etl){
					VaccinationEvent vev = new VaccinationEvent();
					vev.setType(gov.nist.healthcare.cds.enumeration.EventType.valueOf(e.getType()));
					vev.setDate(date(e.getEventDate()));
					
					if(e.getAdministred().getMvx() != null && !e.getAdministred().getMvx().isEmpty()){
						Product pr = productRepository.getProduct(e.getAdministred().getMvx(), e.getAdministred().getCvx());
						if(pr != null){
							vev.setAdministred(pr);
						} else {
							throw new ProductNotFoundException(e.getAdministred().getCvx(),e.getAdministred().getMvx());
						}
					}
					else {
						Vaccine vt = vaccineRepository.findOne(e.getAdministred().getCvx());
						if(vt != null){
							vev.setAdministred(vt);
						} else {
							throw new VaccineNotFoundException(e.getAdministred().getCvx());
						}
					}
					
					Set<ExpectedEvaluation> evals = new HashSet<ExpectedEvaluation>();
					EvaluationsType evalst = e.getEvaluations();
					if(evalst != null){
						List<EvaluationType> evtl = evalst.getEvaluation();
						for(EvaluationType exe : evtl){
							ExpectedEvaluation expe = new ExpectedEvaluation();
							if(exe.getStatus().equals(StatusType.VALID)){
								expe.setStatus(EvaluationStatus.VALID);
							}
							else {
								expe.setStatus(EvaluationStatus.INVALID);
							}
							
							if(exe.getEvaluationReason() != null){
								expe.setReason(EvaluationReason.valueOf(exe.getEvaluationReason().getCode()));
							}
							
							Vaccine vte = vaccineRepository.findOne(exe.getVaccine().getCvx());
							if(vte != null){
								expe.setRelatedTo(vte);
							} else {
								throw new VaccineNotFoundException(exe.getVaccine().getCvx());
							}
							
							evals.add(expe);
						}
					}
					
					vev.setEvaluations(evals);
					evs.add(vev);
				}
			}
			
			Set<ExpectedForecast> efs = new HashSet<ExpectedForecast>();
			ForecastsType fts = tcp.getForecasts();
			if(fts != null){
				for(ForecastType ft : fts.getForecast()){
					ExpectedForecast ef = new ExpectedForecast();
					ef.setEarliest(date(ft.getEarliest()));
					ef.setRecommended(date(ft.getRecommended()));
					ef.setPastDue(date(ft.getPastDue()));
					ef.setForecastReason(ft.getForecastReason());
					if(ft.getSerieStatus() != null){
						ef.setSerieStatus(SerieStatus.valueOf(ft.getSerieStatus().getCode()));
					}

					Vaccine vt = vaccineRepository.findOne(ft.getTarget().getCvx());
					if(vt != null){
						ef.setTarget(vt);
					} else {
						throw new VaccineNotFoundException(ft.getTarget().getCvx());
					}
					efs.add(ef);
				}
			}
			
			tc.setPatient(p);
			tc.setMetaData(md);
			tc.setForecast(efs);
			tc.setEvents(evs);
			
			return tc;
		}
//		catch(VaccineNotFoundException vne){
//			throw vne;
//		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public List<ErrorModel> _validate(String file) {
		try {
			InputStream schu = NISTFormatServiceImpl.class.getResourceAsStream("/schema/testCase.xsd");
			SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
			StringReader reader = new StringReader(file);
			Schema schema;
			schema = factory.newSchema(new StreamSource(schu));
			Validator validator = schema.newValidator();
			
			final ArrayList<ErrorModel> errors = new ArrayList<ErrorModel>();
			validator.setErrorHandler(new ErrorHandler() {
				@Override
				public void warning(SAXParseException exception)
						throws SAXException {
					errors.add(new ErrorModel(exception));
				}
	
				@Override
				public void fatalError(SAXParseException exception)
						throws SAXException {
					errors.add(new ErrorModel(exception));
				}
	
				@Override
				public void error(SAXParseException exception)
						throws SAXException {
					errors.add(new ErrorModel(exception));
				}
			});
			validator.validate(new StreamSource(reader));
			return errors;
		} catch (SAXParseException e) {
			return Arrays.asList(new ErrorModel(e));
		} catch (SAXException e) {
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

}
