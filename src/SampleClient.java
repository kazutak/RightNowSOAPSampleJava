import java.rmi.RemoteException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMElementImpl;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.ws.security.WSConstants;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;

import com.rightnow.ws.wsdl.RightNowSyncService;
import com.rightnow.ws.wsdl.RightNowSyncServiceStub;

import com.rightnow.ws.base.ActionEnum;
import com.rightnow.ws.base.ID;
import com.rightnow.ws.base.NamedID;
import com.rightnow.ws.base.RNObject;
import com.rightnow.ws.messages.ClientInfoHeader;
import com.rightnow.ws.messages.CreateProcessingOptions;
import com.rightnow.ws.messages.RNObjectsResult;
import com.rightnow.ws.objects.Contact;
import com.rightnow.ws.objects.Email;
import com.rightnow.ws.objects.EmailList;
import com.rightnow.ws.objects.PersonName;
import com.rightnow.ws.wsdl.RequestErrorFault;
import com.rightnow.ws.wsdl.ServerErrorFault;
import com.rightnow.ws.wsdl.UnexpectedErrorFault;

@SuppressWarnings("deprecation")
public class SampleClient {

	RightNowSyncService _service ;
	
	public SampleClient() throws AxisFault
	{
		_service = new RightNowSyncServiceStub();
	
		ServiceClient serviceClient =((org.apache.axis2.client.Stub)_service)._getServiceClient();
		serviceClient.addHeader(createSecurityHeader("USERNAME", "PASSWORD"));
	}

	public SampleClient(String username, String password) throws AxisFault
	{
		_service = new RightNowSyncServiceStub();
	
		ServiceClient serviceClient =((org.apache.axis2.client.Stub)_service)._getServiceClient();
		serviceClient.addHeader(createSecurityHeader(username, password));
	}	
	
	public long createContact() throws RemoteException, ServerErrorFault, RequestErrorFault, UnexpectedErrorFault
	{
		Contact newContact = populateContactInfo();
		
		//Set the application ID in the client info header.
		ClientInfoHeader clientInfoHeader = new ClientInfoHeader();
		clientInfoHeader.setAppID("Axis2 Getting Started");
		
		//Set the create processing options, allow external events and rules to execute
		CreateProcessingOptions createProcessingOptions = new CreateProcessingOptions();
		createProcessingOptions.setSuppressExternalEvents(false);
		createProcessingOptions.setSuppressRules(false);
		
		RNObject[] createObjects = new RNObject[] { newContact };
		
		//Invoke the create operation on the RightNow server
		RNObjectsResult createResults = _service.create(createObjects, createProcessingOptions, clientInfoHeader);
		
		//We only created a single contact, this will be at index 0 of the results
		RNObject[] rnObjects = createResults.getRNObjects();
		newContact = (Contact)rnObjects[0];
		
		return newContact.getID().getId();
	}	
	
	private Contact populateContactInfo() 
	{
		Contact newContact = new Contact();

		PersonName personName = new PersonName();
		personName.setFirst("FirstName");
		personName.setLast("LastName");
		newContact.setName(personName);

		EmailList emailList = new EmailList();
		Email[] emailArray = new Email[1];
		emailArray[0] = new Email();
		emailArray[0].setAction(ActionEnum.add);
		emailArray[0].setAddress("abcd@hogehoge.com");

		NamedID addressType = new NamedID();
		ID addressTypeID = new ID();
		addressTypeID.setId(1);
		addressType.setID(addressTypeID);

		emailArray[0].setAddressType(addressType);
		emailArray[0].setInvalid(false);

		emailList.setEmailList(emailArray);
		newContact.setEmails(emailList);
		return newContact;
	}
	
	private OMElement createSecurityHeader(String username, String password)
	{
		OMNamespaceImpl wsseNS = new OMNamespaceImpl(WSConstants.WSSE_NS, WSConstants.WSSE_PREFIX);
		OMFactory factory = new SOAP11Factory();
		OMElementImpl securityHeader;
		OMElementImpl usernameTokenElement;
		OMElementImpl usernameElement;
		OMElementImpl passwordElement;

		// create the Security header block
		securityHeader = new OMElementImpl("Security", wsseNS, factory);
		securityHeader.addAttribute("mustUnderstand", "1", null);

		// nest the UsernameToken in the Security header
		usernameTokenElement = new OMElementImpl(WSConstants.USERNAME_TOKEN_LN, wsseNS, securityHeader, factory);

		// nest the Username and Password elements
		usernameElement = new OMElementImpl(WSConstants.USERNAME_LN, wsseNS, usernameTokenElement, factory);
		usernameElement.setText(username);

		passwordElement = new OMElementImpl(WSConstants.PASSWORD_LN, wsseNS, usernameTokenElement, factory);
		passwordElement.setText(password);
		passwordElement.addAttribute(WSConstants.PASSWORD_TYPE_ATTR,
		WSConstants.PASSWORD_TEXT, null);

		return securityHeader;
	}
	
	public static void main(String[] args)
	{
		try
		{
			//SampleClient sampleClient = new SampleClient();
			SampleClient sampleClient = new SampleClient("username","password");
			
			long newContactID = sampleClient.createContact();
			System.out.println("new Contact Created ID:" + newContactID);
		}
		catch (AxisFault e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
			//e.printStackTrace();
		}
		catch (RemoteException e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (ServerErrorFault e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
		catch (RequestErrorFault e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}	
		catch (UnexpectedErrorFault e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
		}
	}
}
