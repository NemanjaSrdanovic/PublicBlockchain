package messages;

import java.io.Serializable;
import java.util.UUID;

import enumerations.EMessageEndpoint;

/**
 * This message object is used by the network system to save data regarding the
 * transactions and blocks and send them to other nodes.
 *
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 9 Nov 2021
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private String messageId;
	private String senderNode;
	private String receiverNode;
	private EMessageEndpoint endpoint;
	private Object data;

	/**
	 * Instantiates the default message object.
	 */
	public Message() {

		this.messageId = UUID.randomUUID().toString();
	}

	/**
	 * Instantiates a new message object. The parameters must not be null.
	 * 
	 * 
	 * @param destinatedMS
	 * @param source
	 * @param endpoint
	 * @param data
	 */
	public Message(String receiverNode, String senderNode, EMessageEndpoint endpoint, Object data) {

		this.messageId = UUID.randomUUID().toString();
		this.receiverNode = receiverNode;
		this.senderNode = senderNode;
		this.endpoint = endpoint;
		this.data = data;

	}

	/**
	 * Return unique messageID
	 * 
	 * @return String in format 5dadaac3-28f2-422f-a65b-62a6d3e8d81e
	 */
	public String getMessageId() {
		return messageId;
	}

	/**
	 * Returns data saved in message object.
	 * 
	 * @return Transaction or Block object.
	 */
	public Object getData() {
		return data;
	}

	/**
	 * Sets the data which the message should transport.
	 * 
	 * @param data Transaction or Block object.
	 */
	public void setData(Object data) {
		this.data = data;
	}

	/**
	 * Returns node which sends message.
	 * 
	 * @return
	 */
	public String getSenderNode() {
		return senderNode;
	}

	/**
	 * Sets the node address from node which sends message.
	 * 
	 * @param senderNode
	 */
	public void setSenderNode(String senderNode) {
		this.senderNode = senderNode;
	}

	/**
	 * Returns node address for the node which should get message.
	 * 
	 * @return
	 */
	public String getReceiverNode() {
		return receiverNode;
	}

	/**
	 * Sets address from node which should receive message.
	 * 
	 * @param receiverNode
	 */
	public void setReceiverNode(String receiverNode) {
		this.receiverNode = receiverNode;
	}

	/**
	 * Returns endpoint on which the message should be processed.
	 * 
	 * @return
	 */
	public EMessageEndpoint getEndpoint() {
		return endpoint;
	}

	/**
	 * Sets the endpoint on which the message should be processed.
	 * 
	 * @param endpoint
	 */
	public void setEndpoint(EMessageEndpoint endpoint) {
		this.endpoint = endpoint;
	}

}