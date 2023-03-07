package enumerations;

/**
 * Enum used by the node to identify which kind of data the sent message
 * contains and how it should be processed.
 * 
 * @author Nemanja Srdanovic
 * @version 1.0
 * @since 2 Dec 2021
 */
public enum EMessageEndpoint {
	Transaction, Block, DataRequest, DataResponse, PublicKey;

}
