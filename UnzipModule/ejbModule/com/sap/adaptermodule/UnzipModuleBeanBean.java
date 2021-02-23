
package com.sap.adaptermodule;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PublicKey;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;

import com.sap.aii.af.lib.mp.module.*;

import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccess;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import com.sun.org.apache.xml.internal.serializer.utils.MsgKey;

import com.sap.engine.interfaces.messaging.api.MessageDirection;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccess;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJBException;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;



@SuppressWarnings("unused")
public class UnzipModuleBeanBean implements  SessionBean,Module  {

public AuditAccess Audit = null;
 
private static final long serialVersionUID = 1L;
public void ejbRemove() {
}

public void ejbActivate() {
}

public void ejbPassivate() {
}

public void setSessionContext(SessionContext context) {
	myContext = context;
}

private SessionContext myContext;
/**
 * Create Method.
 */
public void ejbCreate() throws CreateException {
	// TODO : Implement
}
	

	public ModuleData process(ModuleContext moduleContext,ModuleData inputModuleData)
			throws ModuleException {
		// TODO Auto-generated method stub
		
		String zipFilePath = (String) moduleContext.getContextData("SourceDirectory");
		String destDir = (String) moduleContext.getContextData("DestinationDirectory");
		

        
		PublicAPIAccess pa;
		try {
		pa = PublicAPIAccessFactory.getPublicAPIAccess();
		} 
		catch (MessagingException me){
			throw new ModuleException("Exception: Accessing PublicAPIAccessFactory Failed!");					
		} 
		
		AuditAccess audit = pa.getAuditAccess();
		try
		{
		Message msg = (Message)inputModuleData.getPrincipalData();
		MessageKey msgKey = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
		Audit = pa.getAuditAccess();
		 Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS,"Adapter module called");
		 
		 //MessagePropertyKey mpk = new MessagePropertyKey("FileName","http://sap.com/xi/XI/System/File");
		 //String filename = msg.getMessageProperty(mpk);
		 Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, "zip filename is: " + zipFilePath  );
		 
		 
		 
			File dir = new File(destDir);
			if(!dir.exists()) dir.mkdirs();
			FileInputStream fis;
			byte[] buffer = new byte[1024];
			fis = new FileInputStream(zipFilePath);
			
			ZipInputStream zis = new ZipInputStream(fis);
			ZipEntry ze = zis.getNextEntry();
			
			while(ze != null)
			{
				String fn = ze.getName();
				File newFile = new File (destDir + "/" + fn);
				//Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS,"Adapter module ended1"+newFile.getName());
			
				//new File(newFile.getParent()).mkdirs();
				//Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS,"Adapter module ended2"+newFile.toString());
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
                }
                fos.close();
                
                zis.closeEntry();
                ze = zis.getNextEntry();
                }
			 zis.closeEntry();
	            zis.close();
	            fis.close(); 
	            Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS, " unziped directory is: " + destDir  );     
	            Audit.addAuditLogEntry(msgKey, AuditLogStatus.SUCCESS,"Adapter module ended");
		}
		
		catch(Exception ex)
		{
			Message msg = (Message)inputModuleData.getPrincipalData();
			 MessageKey amk = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
			Audit.addAuditLogEntry(amk, AuditLogStatus.ERROR,"Exception occurred : ");	
			 ModuleException me = new ModuleException(new MessagingException(amk, ex.getMessage()));												
			 throw me;
			
		}
		
		return inputModuleData;
	}
	
	
	

	

	@PostConstruct
	public void initialiseResources()
	{
		try
		{
			Audit = PublicAPIAccessFactory.getPublicAPIAccess().getAuditAccess();
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error in initialiseResources(): " + e.getMessage());
		}
	}

	@PreDestroy
	public void ReleaseResources()
	{
		//release resource
	}

}
