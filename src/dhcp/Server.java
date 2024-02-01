package dhcp;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class Server {
	private static final long LEASE_TIME = 60 * 1000;
	
	public static void main(String[] args) {
		new Server(System.in, System.out);
	}
	
	/**
	 * Order is: 1.2.3.4
	 * 			 [1,2,3,4]
	 */
	private static long[][][][] leases;
	
	public Server(InputStream input, OutputStream output) {
		@SuppressWarnings("resource")
		Scanner in = new Scanner(input);
		PrintWriter out = new PrintWriter(output,true);
		while(true) {
			out.print("COMMAND > ");
			out.flush();
			String cmd = in.nextLine();
			String[] cmdString = cmd.split(" ",2);
			switch(cmdString[0]) {
			case "ASK":
				int[] address = getNextAddress();
				if(address == null) {
					out.println("OUT OF ADDRESSES");
					break;
				}
				allocateAddress(address, System.currentTimeMillis() + LEASE_TIME);
				out.println("OFFER " + addrToString(address));
				break;
			case "RENEW":
				String addrString = cmdString[1];
				address = stringToAddr(addrString);
				if(!isAllocated(address)) {
					out.println("ADDRESS IS NOT ALLOCATED");
					break;
				}
				allocateAddress(address, System.currentTimeMillis() + LEASE_TIME);
				out.println("RENEW " + cmdString[1]);
				break;
			case "RELEASE":
				addrString = cmdString[1];
				address = stringToAddr(addrString);
				if(!isAllocated(address)) {
					out.println("ADDRESS IS NOT ALLOCATED");
					break;
				}
				allocateAddress(address, 0);
				out.println("RELEASED " + cmdString[1]);
				break;
			case "STATUS":
				addrString = cmdString[1];
				address = stringToAddr(addrString);
				boolean allocated = isAllocated(address);
				if(allocated)
					out.println(cmdString[1] + " ASSIGNED");
				else
					out.println(cmdString[1] + " AVAILABLE");
				break;
			default:
				break;
			}
		}
	}
	public String addrToString(int[] address) {
		return address[0] + "." + address[1] + "." + address[2] + "." + address[3];
	}
	public int[] stringToAddr(String address) {
		String[] split = address.split("\\.");
		return new int[] {Integer.valueOf(split[0]),Integer.valueOf(split[1]),Integer.valueOf(split[2]),Integer.valueOf(split[3])};
	}
	public int[] getNextAddress() {
		if(leases == null) {
			return new int[] {0,0,0,0};
		}
		for(int i1 = 0; i1 < leases.length; i1++) {
			if(leases[i1] == null) {
				return new int[] {i1,0,0,0};
			}
			for(int i2 = 0; i2 < leases.length; i2++) {
				if(leases[i1][i2] == null) {
					return new int[] {i1,i2,0,0};
				}
				for(int i3 = 0; i3 < leases.length; i3++) {
					if(leases[i1][i2][i3] == null) {
						return new int[] {i1,i2,i3,0};
					}
					for(int i4 = 0; i4 < leases.length; i4++) {
						long lease = leases[i1][i2][i3][i4];
						if(lease < System.currentTimeMillis()) {
							return new int[] {i1,i2,i3,i4};
						}
					}
				}
			}
		}
		return null;
	}
	public void allocateAddress(int[] address, long leaseTime) {
		// Don't allocate the addresses until they're needed to save using ~4 billion longs worth of memory
		if(leases == null) {
			leases = new long[256][][][];
		}
		if(leases[address[0]] == null) {
			leases[address[0]] = new long[256][][];
		}
		if(leases[address[0]][address[1]] == null) {
			leases[address[0]][address[1]] = new long[256][];
		}
		if(leases[address[0]][address[1]][address[2]] == null) {
			leases[address[0]][address[1]][address[2]] = new long[256];
		}
		leases[address[0]][address[1]][address[2]][address[3]] = leaseTime;
	}
	public boolean isAllocated(int[] address) {
		if(leases == null)
			return false;
		if(leases[address[0]] == null)
			return false;
		if(leases[address[0]][address[1]] == null)
			return false;
		if(leases[address[0]][address[1]][address[2]] == null)
			return false;
		return leases[address[0]][address[1]][address[2]][address[3]] > System.currentTimeMillis();
	}
}
