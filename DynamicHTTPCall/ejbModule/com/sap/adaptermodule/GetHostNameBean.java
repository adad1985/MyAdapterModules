/**
 * 
 */
package com.sap.adaptermodule;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.net.HttpURLConnection;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.SessionSynchronization;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.ejb.CreateException;
//import javax.ejb.Local;
//import javax.ejb.LocalHome;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
//import javax.ejb.Stateless;
import java.io.*;
import java.util.Map;
import javax.xml.parsers.*;

import com.sap.aii.af.lib.mp.module.Module;
import com.sap.aii.af.lib.mp.module.ModuleContext;
import com.sap.aii.af.lib.mp.module.ModuleData;
import com.sap.aii.af.lib.mp.module.ModuleException;
import com.sap.aii.mapping.api.DynamicConfiguration;
import com.sap.aii.mapping.api.DynamicConfigurationKey;
import com.sap.aii.mapping.api.StreamTransformationConstants;
import com.sap.aii.utilxi.base64.api.Base64;
import com.sap.engine.interfaces.messaging.api.Message;
import com.sap.engine.interfaces.messaging.api.MessageKey;
import com.sap.engine.interfaces.messaging.api.MessagePropertyKey;
import com.sap.engine.interfaces.messaging.api.Payload;
import com.sap.engine.interfaces.messaging.api.XMLPayload;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccess;
import com.sap.engine.interfaces.messaging.api.PublicAPIAccessFactory;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditAccess;
import com.sap.engine.interfaces.messaging.api.auditlog.AuditLogStatus;
import com.sap.engine.interfaces.messaging.api.exception.MessagingException;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import javax.naming.*;

/**
 *
 * Channel Parameters: HTTP_URL,Path_Prefix,User:Password
 */

public class GetHostNameBean implements SessionBean {

        /**
         * 
         */
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
                                                
                                                public ModuleData process(ModuleContext moduleContext,ModuleData inputModuleData)throws ModuleException 
                                                                        {  
                                               
                                                        String URL_Input = null;
                                                        URL_Input = (String)moduleContext.getContextData("HTTP_URL");
                                                        PublicAPIAccess pa;
                                                        try 
                                                        {
                                                                pa = PublicAPIAccessFactory.getPublicAPIAccess();
                                                        } 
                                                        catch (MessagingException me)
                                                        {
                                                                        throw new ModuleException("Internal SAP PI 7.30 Error -> Accessing PublicAPIAccessFactory Failed!");                                    
                                                        }
                                                        AuditAccess audit = pa.getAuditAccess();
                                                                  try
                                                                                
                                                                  {
                                                                    
                                                                	  
                                                                    Message msg = (Message)inputModuleData.getPrincipalData();
                                                                    MessageKey amk = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
                                                                    Payload pay= msg.getDocument();
                                                                    String path = msg.getMessageProperty(new MessagePropertyKey("URL","http://sap.com/xi/XI/System"));                                         
                                                                    audit.addAuditLogEntry(amk, AuditLogStatus.SUCCESS,"AO:Path obtained from DC"+path);  
                                                                    String pathPrefix = (String)moduleContext.getContextData("Path_Prefix");
                                                                    path = pathPrefix+path;
                                                                    String finalURL = URL_Input + path;
                                                                    audit.addAuditLogEntry(amk, AuditLogStatus.SUCCESS,"AO:URL Prepared:"+finalURL); 
                                                                    URL url = new URL(finalURL);
                                                                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                                                                    String authstr = (String)moduleContext.getContextData("User:Password");
                                                                    String bytesEncoded = Base64.encode(authstr.getBytes());
                                                                    con.setRequestProperty("Authorization", "Basic "+bytesEncoded);
                                                                    con.setRequestProperty("Content-Type", "application/json");
                                                                    con.setRequestMethod("GET");
                                                                    con.connect();
                                                                    int status = con.getResponseCode();
                                                                    audit.addAuditLogEntry(amk, AuditLogStatus.SUCCESS,"AO:Response Code: "+status); 
                                                                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                                                    String inputLine;
                                                                    StringBuffer content = new StringBuffer();
                                                                    while((inputLine = in.readLine()) != null){
                                                                      content.append(inputLine);
                                                                    }
                                                                    in.close();
                                                                    
                                                                    pay.setContentType("application/json");
                                                                    pay.setContent(content.toString().getBytes());      
                                                                    msg.setMainPayload(pay);
                                                                    inputModuleData.setPrincipalData(msg);
                                                                    return inputModuleData;
                                                                    
                                                                           } 


                                                          
                                                                        
                                                                 catch(Exception e)                                                                                                                                                                                                                                     
                                                                                {
                                                                         Message msg = (Message)inputModuleData.getPrincipalData();
                                                                                MessageKey amk = new MessageKey(msg.getMessageId(), msg.getMessageDirection());
                                                                         audit.addAuditLogEntry(amk, AuditLogStatus.ERROR,"AO: Exception in Convert Method.");  
                                                                         ModuleException me = new ModuleException(new MessagingException(amk, e.getMessage()));                                                                                         
                                                                                throw me;
                                                                                 }      
                                                                 finally
                                                                   {
                                                                System.gc();    
                                                                   }
                                                                  
                                        }
}