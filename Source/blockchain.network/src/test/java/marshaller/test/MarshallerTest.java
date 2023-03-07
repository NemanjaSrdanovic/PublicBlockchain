package marshaller.test;

import static org.junit.Assert.assertTrue;

import java.net.DatagramPacket;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import enumerations.EMessageEndpoint;
import marshaller.Marshaller;
import messages.Message;

/**
 * Testing the functionalities of the Marshaller object by mocking corresponding
 * objects.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 26 Jan 2022
 */
public class MarshallerTest {

	private static Marshaller testMarshaller;
	private static byte[] buffer;

	/**
	 * Running exactly once during the test run - at the very beginning before
	 * anything else is run to set up the dependencies needed for proper test
	 * execution.
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		testMarshaller = new Marshaller();
		buffer = new byte[2000];
	}

	/**
	 * Executed before each tests in this class to prepare dependencies
	 */
	@BeforeEach
	public void setUp() {

		testMarshaller = new Marshaller();

	}

	/**
	 * Executed after each tests in this class to prepare dependencies
	 */
	@AfterEach
	public void tearDown() {

		testMarshaller = null;
	}

	/**
	 * Checking the functionality of the marshaler object makeDatagramPacket()
	 * method by mocking a message object which has to be correctly transformed to a
	 * datagram packet. The returned marshaller object has to be of instance
	 * datagram packet and no exception should be thrown.
	 */
	@Test
	public void createMessageObject_CallMarshallerMakeDatagramPacket_ReturnedDatagramInstance() {

		Message testMessage = new Message("192.0.1.2:3000", "192.0.1.2:3001", EMessageEndpoint.Block, null);

		Object marshalledObject = testMarshaller.makeDatagramPacket(testMessage, buffer, testMessage.getReceiverNode());

		assertTrue(marshalledObject instanceof DatagramPacket);

	}

	/**
	 * Testing if the exception handling of the marshaller object is correctly
	 * executed by mocking a message object which has a state that will provoke the
	 * anticipated exception.
	 */
	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void insertWrongFormatForReceiverAddress_CallMarshallerMakeDatagramPacket_ArrayOutOfBoundExceptionThrown() {

		Message testMessage = new Message("3000", "192.0.1.2:3001", EMessageEndpoint.Block, null);

		testMarshaller.makeDatagramPacket(testMessage, buffer, testMessage.getReceiverNode());

	}

	/**
	 * Checking the functionality of the marshaler object makeObjectFrom() method by
	 * mocking a datagram packet object which has to be correctly transformed to a
	 * message object. The returned marshaller object has to be of instance message
	 * and no exception should be thrown.
	 */
	@Test
	public void createBlockchainObjectAndPutInDatagram_CallMarshallerMakeObjectFrom_ObjInstanceofBlockchain() {

		Message testMessage = new Message("192.0.1.2:3000", "192.0.1.2:3001", EMessageEndpoint.Block, null);

		DatagramPacket testPacket = testMarshaller.makeDatagramPacket(testMessage, buffer, "192.0.1.2:3001");

		Object unmarshalledObj = testMarshaller.makeObjectFrom(testPacket);

		assertTrue(unmarshalledObj instanceof Message);

	}

	/**
	 * Checking the functionality of the marshaler object transformObjectToByte()
	 * method by mocking a message object which has to be correctly transformed to a
	 * byte array. The returned marshaller object has to be of instance byte[] and
	 * no exception should be thrown.
	 */
	@Test
	public void createBlockObject_CallMarshallerTransformObjectToByte_InstanceofByteReturned() {

		Message testMessage = new Message("192.0.1.2:3000", "192.0.1.2:3001", EMessageEndpoint.Block, null);

		Object returnedInstance = testMarshaller.transformObjectToByte(testMessage);

		assertTrue(returnedInstance instanceof byte[]);

	}

	/**
	 * Checking the functionality of the marshaler object transformObjectToByte()
	 * method by mocking a byte array which has to be correctly transformed to a
	 * object. The returned marshaller object has to be of instance message and no
	 * exception should be thrown.
	 */
	@Test
	public void createTransactionObject_CallMarshallerTransformByteToObject_InstanceofTransactionReturned() {

		Message testMessage = new Message("192.0.1.2:3000", "192.0.1.2:3001", EMessageEndpoint.Block, null);

		byte[] byteInstance = testMarshaller.transformObjectToByte(testMessage);

		Object returnedInstance = testMarshaller.transformByteToObject(byteInstance);

		assertTrue(returnedInstance instanceof Message);

	}

}
