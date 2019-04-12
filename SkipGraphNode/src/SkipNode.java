import java.util.Scanner;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SkipNode extends UnicastRemoteObject implements RMIInterface{
	
	
	private static final long serialVersionUID = 1L;
	public static String address;
	public static String nameID;
	public static String numID;
	public static String IP ;
	public static String[][] lookup ;
	public static int port;
	public static int maxLevels = 5; 
	public static String introducer; 
	
	// General Notes :
	// Introducer 172.16.100.40:1099
	// Try defining a function that gets an IP and return RMIInterface instance
	// Currently we are assuming that RMI port for each node is 1099
	// Noor IP: 172.16.100.5
	
	public static void main(String args[]) throws IOException, NotBoundException {
		
		
		lookup = new String[maxLevels+1][2]; // initialize size of lookup table
		
		setInfo();
		
		ServerConnection server = new ServerConnection();
		server.start();	
		
		try {
			
			SkipNode skipNode = new SkipNode();
			
			Naming.rebind("//localhost/"+"RMIImpl",skipNode); // make methods of skipNode instance available on RMI Registry
			
			log("Rebinding Successful");
			
			while(true) {
				printMenu();
				skipNode.ask();
			}
			
		}catch(IOException e){
			
			log("Error in Rebinding");
			e.printStackTrace();
		}
		
		
	}

	/*
	 * Constructor for SkipNode class needed for RMI setup
	 */
	protected SkipNode() throws RemoteException, UnknownHostException {
		super();
		
		String st = Inet4Address.getLocalHost().getHostAddress();
		System.setProperty("java.rmi.server.hostname",st);
		System.out.println("RMI Server proptery set. Inet4Address: "+st);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * This method initializes the information of the current node
	 * and prints them to console
	 */
	public static void setInfo() {
		
		log("Enter your Name ID:");
		nameID = get();
		log("Enter your Numeric Id:");
		numID = get();
		log("Enter the address of the introducer:");
		introducer = get();
		
		log("Your INFO:\nnameID: "+nameID+"\nnumID: "+numID+"\nintroducer: "+introducer);
	}
	
	/*
	 * This method prints the options for user controlling the node to choose.
	 * More options can be appended but require the modification of ask() method
	 */
	public static void printMenu() throws IOException{
        InetAddress adrs = InetAddress.getLocalHost();
        log("Node at the address: " + address);
        log("Name ID: "+ nameID +" Number ID: " + numID);
        log("Choose a query by entering it's code and then press Enter");
        log("1-Insert");
        log("2-Search By Name ID");
        log("3-Search By Number ID");
        log("4-Print the Lookup Table");
        log("5-Print left node at a chosen level.");
        log("6-Print right node at a chose level.");
	}
	
	/*
	 * Gets the type of operation to be executed from the user
	 * and executes the corresponding operation.
	 */
	public void ask() throws MalformedURLException, RemoteException, NotBoundException {
        
		int query = Integer.parseInt(get());
		
		if(query == 1)
			insert();
		else if (query == 2) {
			log("Please Enter the name ID to be searched");
			String name = get();
			String result = searchByNameID(name);
			log("The result of search by name ID is: "+result);
		}else if(query == 3) {
			log("Please Enter the numeric ID to be searched");
			String num = get();
			String result = searchByNumID(num);
			log("The result of search by numberic ID is: "+ result);
		}else if(query == 4)
			printLookup();
		else if( query == 5) {
			log("Please Enter the required level:");
			int lvl = Integer.parseInt(get());
			if(lookup[lvl][0] == null)
				log("No left node present at level "+lvl);
			else
				log("Left node at level "+lvl+" is:" + lookup[lvl][0]);
		}
		else if (query == 6) {
			log("Please Enter the required level:");
			int lvl = Integer.parseInt(get());
			if(lookup[lvl][1] == null)
				log("No right node present at level "+lvl);
			else
				log("Right node at level "+lvl+" is:" + lookup[lvl][0]);
		}
        
    }
	
	/* 
	 * This method places the node in its correct position in the skip graph 
	 * with the help of the introducer node.
	 * It executes a search on its numID by numeric ID from the introducer using RMI
	 * and then positions itself in the appropriate positions at each level of the skip graph
	 * 
	 */
	
	public static void insert() throws MalformedURLException, RemoteException, NotBoundException {
		
		
		String left = null;
		String right = null;
		
		String[] introAddress = introducer.split(":");
	
		RMIInterface introRMI = (RMIInterface)Naming.lookup("//"+introAddress[0]+":1099/RMIImpl");
		
		
		// This block needs revision
		// get the address of the node that is supposed to be to my left at level 0
		String yourAddress = introRMI.searchByNumID(numID);
		String[] temp = yourAddress.split(":");
		String yourIP = temp[0];
		left = yourAddress ; 
		
		// use RMI to get the node to my right at level 0
		RMIInterface yourRMI = (RMIInterface)Naming.lookup("//"+temp[0]+":1099/RMIImpl");
		right  = yourRMI.getRightNode(0);
		
		// update my left and right pointers and also set myself as the right node of the left node
		lookup[0][0] = yourAddress;
		lookup[0][1] = right ;
		yourRMI.setRightNode(0, address);
		
		// in case the right node is not null set myself as its left node using RMI.
		if(right != null) {
			RMIInterface rightRMI = (RMIInterface) Naming.lookup("//"+right.split(":")[0]+":1099/RMIImpl");
			rightRMI.setLeftNode(0,address);
		}
		
		// now insert myself in higher levels depending on the name ID
		
		int level = 0;
		while(true) {
			while(true) {
				
				// if there is a left node 
				if(left != null) {
					
					RMIInterface leftRMI = (RMIInterface)Naming.lookup("//"+left.split(":")[0]+":1099/RMIImpl");
					
					if(commonPrefix(leftRMI.getNameID()) <= level)
						left = leftRMI.getLeftNode(level);
					else
						break;
				}
				
				if(right != null) {
					RMIInterface rightRMI = (RMIInterface)Naming.lookup("//"+right.split(":")[0]+":1099/RMIImpl");
					
					if(commonPrefix(rightRMI.getNameID()) <= level)
						right = rightRMI.getRightNode(level);
					else
						break;	
				}
				
				if(right == null && left == null)
						break;
			}
			
			if(left != null) {
				
				RMIInterface leftRMI = (RMIInterface)Naming.lookup("//"+left.split(":")[0]+":1099/RMIImpl");
				
				if(commonPrefix(leftRMI.getNameID()) > level) {
					
					String rightNeighbor = leftRMI.getRightNode(level+1);
					
					leftRMI.setRightNode(level+1, address);
					
					if(rightNeighbor != null) {
						RMIInterface rightNeighborRMI = (RMIInterface)Naming.lookup("//"+rightNeighbor.split(":")[0]+":1099/RMIImpl");
						rightNeighborRMI.setLeftNode(level+1, address);
					}
					
					lookup[level+1][0] = left;
					lookup[level+1][1] = rightNeighbor ;
					
					right = rightNeighbor ;
					
				}
			}
			
			if(right != null){
				
				RMIInterface rightRMI = (RMIInterface)Naming.lookup("//"+right.split(":")[0]+":1099/RMIImpl");
				
				if(commonPrefix(rightRMI.getNameID()) > level) {
					
					String leftNeighbor = rightRMI.getLeftNode(level+1);
					
					rightRMI.setLeftNode(level+1, address);
					
					if(leftNeighbor != null) {
						RMIInterface leftNeighborRMI = (RMIInterface)Naming.lookup("//"+leftNeighbor.split(":")[0]+":1099/RMIImpl");
						leftNeighborRMI.setRightNode(level+1, address);
					}
					
					lookup[level+1][0] = leftNeighbor ;
					lookup[level+1][1] = right;
					
					left = leftNeighbor ;
				}
			}
			
			level++ ;
			
			if(level > maxLevels)
				break;
			
			if(left == null && right == null)
				break;
			
		}
		
	}
	
	/* 
	 * Executes a search through the skip graph by numeric id and returns the address of the 
	 * the target node or if not found the node with closest numeric ID 
	 * @see RMIInterface#searchByNumID(java.lang.String)
	 */
	public String searchByNumID(String targetNum) throws RemoteException, MalformedURLException, NotBoundException {
		
		int level = maxLevels ; // start search at the highest level
		
		// cast target ID and this node's ID to integers not to use parsing several times again
		int numIDInt = Integer.parseInt(numID);  
		int targetInt = Integer.parseInt(targetNum); 
		
		// If the introducer exists only
		if(lookup[0][0] == null && lookup[0][1] == null) {
			return address ;
		}
		// The Target is on the right of numID
		else if (numIDInt < targetInt) {
			
			String next = null ;
			
			// as long as there is no right node keep going down
			while(level > 0 && lookup[level][1] == null)
				level--;
		
			if(level >= 0) {
				
				next = lookup[level][1]; // first process the right node at current level
				
				while(level >= 0) {
					
					RMIInterface nextRMI = (RMIInterface)Naming.lookup("//"+next.split(":")[0]+":1099/RMIImpl");
					
					// if the node to the right of the right node is not null then keep processing
					if(nextRMI.getRightNode(level) != null) {
						
						String[] nextOfNext = nextRMI.getRightNode(level).split(":");
						RMIInterface nextOfNextRMI = (RMIInterface)Naming.lookup("//"+nextOfNext[0]+":1099/RMIImpl");
						
						// if the ID of the node to right of the right node is less than the target then move to it
						if(Integer.parseInt(nextOfNextRMI.getNumID()) < targetInt) {
						
							next = nextRMI.getRightNode(level);
						
						}else if (Integer.parseInt(nextOfNextRMI.getNumID()) == targetInt) // if found return it
						
							return nextRMI.getRightNode(level);
						
						else  // otherwise go down a level
							level--;
				
					}else
						level--;
				}
			}
			
			return next ;
		
		} else { // the target is to the left of the current node.
			
			String next = null;
			
			while(level > 0 && lookup[level][0] == null)
				level--;
			
			if(level >= 0) {
				
				next = lookup[level][0];
				
				while(level >= 0) {
					
					RMIInterface nextRMI = (RMIInterface)Naming.lookup("//"+next.split(":")[0]+":1099/RMIImpl");
					
					if(nextRMI.getLeftNode(level) != null) {
						
						String[] nextOfNext = nextRMI.getLeftNode(level).split(":");	
						RMIInterface nextOfNextRMI = (RMIInterface)Naming.lookup("//"+nextOfNext[0]+":1099/RMIImpl");
						
						if(Integer.parseInt(nextOfNextRMI.getNumID()) > targetInt) {
		
							next = nextRMI.getLeftNode(level);
						
						}else if (Integer.parseInt(nextOfNextRMI.getNumID()) == targetInt)
						
							return nextRMI.getLeftNode(level);
						
						else
							level--;
						
					}else
						level--;
					
				}
				
			}
			return next ;
		}
	}
	
	/*
	 * Execute search by nameID
	 * and return the closest result
	 * @see RMIInterface#searchByNameID(java.lang.String)
	 */
	
	public String searchByNameID(String targetName) throws RemoteException, MalformedURLException, NotBoundException{
		
		String left = lookup[0][0];
		String right = lookup[0][1];
		int level = 0;
		
		
		// adjust to the appropriate level in which current node and target
		// exist in the same linked list
		int prefix = commonPrefix(nameID,targetName) ;
		if(prefix > level) {
			level = prefix ;
			left = lookup[level][0];
			right = lookup[level][1];
		}
		
		while(true) {
			
			if(left != null) {
				
				RMIInterface leftRMI = (RMIInterface)Naming.lookup("//"+left.split(":")[0]+":1099/RMIImpl");
				
				prefix =  commonPrefix(leftRMI.getNameID(),targetName) ;
				
				
				if(leftRMI.getNameID().contains(targetName))
					return left ;
				else if (prefix <= level)
					left = leftRMI.getLeftNode(level);
				else { // commonPrefix > level
					level = prefix ;
					right = leftRMI.getRightNode(level);
					left = leftRMI.getLeftNode(level);
					continue;
				}

			} else if(right != null) {
				
				RMIInterface rightRMI = (RMIInterface) Naming.lookup("//"+right.split(":")[0]+":1099/RMIImpl");
				
				prefix = commonPrefix(rightRMI.getNameID(),targetName);
				
				if(rightRMI.getNameID().contains(targetName))
					return right;
				else if (prefix <= level)
					right = rightRMI.getRightNode(level);
				else {
					level = prefix ;
					right = rightRMI.getRightNode(level);
					left = rightRMI.getLeftNode(level);
					continue;
				}
			}
			if(right == null && left == null)
				break;
			
		}
		
		return nameID ;
		
	}
	
	/*
	 * getters and setters for lookup table and numID and nameID
	 * 
	 */
	
	public String getLeftNode(int level) throws RemoteException {
		return lookup[level][0];
	}
	
	public String getRightNode(int level) throws RemoteException {
		return lookup[level][1];
	}
	public void setLeftNode(int level,String newVal) throws RemoteException{
		log("LeftNode at level "+level+" set to: "+newVal);
		lookup[level][0] = newVal;
	}
	public void setRightNode(int level,String newVal) throws RemoteException {
		log("RightNode at level "+level+" set to: "+newVal);
		lookup[level][1] = newVal ;
	}
	public String getNumID() {
		return numID;
	}
	public String getNameID() {
		return nameID ;
	}
	
	/*
	 * Returns the length of the common prefix between a string and nameID
	 */
	
	public static int commonPrefix(String name) {
			
			if(name.length() != nameID.length())
				return -1;
			
			int i = 0 ;
			for(i = 0 ; i < name.length() && name.charAt(i) == nameID.charAt(i);i++);
			
			log("Common Prefix for " + nameID + " and " + name + " is: " + i);
			
			return i ;		
		}
	
	/*
	 * Return the length of the common prefix between two strings
	 */
	public static int commonPrefix(String name1, String name2) {
		if(name1.length() != name2.length())
			return -1;
		int i = 0;
		for(i = 0; i < name1.length() && name1.charAt(i) == name2.charAt(i) ; ++i);
		
		log("Common Prefix for " + name1 + " and " + name2 + " is: " + i);
			
			return i;		
		}
	
	
	/*
	 * A shortcut for printing to console
	 */
	
	public static void log(String s) {
		System.out.println(s);
	}
	
	/* 
	 * Print the contents of the lookup table
	 */
	
	public static void printLookup() {
        System.out.println("\n");
        for(int i = maxLevels-1 ; i >= 0 ; i--)
        {
            for(int j = 0 ; j<2 ; j++)
                System.out.print(lookup[i][j]+"\t");
            System.out.println("\n");
        }
    }
	
	/*
	 * A shortcut for getting input from user
	 */
	
	public static String get() {
		Scanner in = new Scanner(System.in);
		String response = in.nextLine();
		return response;
	}
}
