package marshaller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This marshaller object is used by the UDP_Client/Server objects to convert
 * class objects to datagram packets/bytes and vice versa.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class Marshaller {

	private static Logger logger = LoggerFactory.getLogger(Marshaller.class);
	private ByteArrayOutputStream bs;
	private ByteArrayInputStream bis;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private Object object;

	/**
	 * Instantiates a new marshaller object.
	 */
	public Marshaller() {
	}

	/**
	 * Transforms network objects into DatagramPackets so that they can be
	 * send/received over local UDP connection.
	 * 
	 * @param obj    -> Message objects containing data in form of a Block or
	 *               Transaction object
	 * @param buffer
	 * @param ipPort
	 * @return -> DatagramPacket that can be send/received by UDP connection.
	 */
	public synchronized DatagramPacket makeDatagramPacket(Object obj, byte[] buffer, String ipPort) {
		try {
			bs = new ByteArrayOutputStream(buffer.length);
			oos = new ObjectOutputStream(bs);

			oos.writeObject(obj);
			oos.flush();

			byte[] sendBuf = bs.toByteArray();

			oos.close();
			bs.close();

			return new DatagramPacket(sendBuf, sendBuf.length, InetAddress.getByName(ipPort.split(":")[0]),
					Integer.parseUnsignedInt(ipPort.split(":")[1]));
		} catch (IOException e) {

			logger.error("Error Marshaller Making DataPacket <---------------", e);

		}
		return null;
	}

	/**
	 * Transforms datagram packets into network objects so that they can be used to
	 * activate methods, read data etc.
	 * 
	 * @param packet -> Datagram packet containing network object that contains
	 *               relevant informations.
	 * @return -> Network objects that can be read by the network system.
	 */
	public synchronized Object makeObjectFrom(DatagramPacket packet) {

		try {

			bis = new ByteArrayInputStream(packet.getData());

			ois = new ObjectInputStream(bis);

			object = (Object) ois.readObject();
			ois.close();

			return object;
		} catch (IOException e) {

			logger.error("Error Marsahller IO <----------------", e);

		} catch (ClassNotFoundException e) {

			logger.error("Error Marshaller CLASS <-----------------", e);
		}

		return null;
	}

	/**
	 * Transforms object to byte, to be stored into an database together with the
	 * rest of the message informations.
	 * 
	 * @param obj -> Network object containing relevant data.
	 * @return -> Byte which can be saved into a sql database.
	 */
	public synchronized byte[] transformObjectToByte(Object obj) {

		byte[] byteArrayObject = null;
		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);

			byteArrayObject = bos.toByteArray();

			oos.close();
			bos.close();

		} catch (Exception e) {

			logger.error("Error Marshaller Making Byte <---------------", e);

			return byteArrayObject;
		}
		return byteArrayObject;

	}

	/**
	 * Transforms byte to network objects which can be used by the network system to
	 * complete operations.
	 * 
	 * @param byteInput -> Byte from database which can be transformed into a
	 *                  network object.
	 * @return -> Network objects that can be used by the network system.
	 */
	public synchronized Object transformByteToObject(byte[] byteInput) {

		Object obj = null;
		ByteArrayInputStream bais;
		ObjectInputStream ins;

		try {

			bais = new ByteArrayInputStream(byteInput);

			ins = new ObjectInputStream(bais);
			obj = (Object) ins.readObject();

			ins.close();

		} catch (Exception e) {

			logger.error("Error Marshaller Making Object <---------------", e);
		}
		return obj;
	}
}
