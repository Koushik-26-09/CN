import java.io.*;
import java.net.*;
import java.util.*;

public class DHCPServer {
    private static final int SERVER_PORT = 4900;
    private static final String SERVER_IP = "127.0.0.1"; // Change to your server's IP
    private static final String IP_ALLOCATIONS_FILE = "ip_allocations.txt";

    private static List<String> availableIpAddresses = new ArrayList<String>();
    private static Map<String, List<String>> ipAllocations = new HashMap<String, List<String>>();

    public static void main(String[] args) {
        initializeIpAddresses();
        loadIpAllocations();

        // Display available IPs after initialization
        showAvailableIpAddresses();

        try (DatagramSocket socket = new DatagramSocket(SERVER_PORT)) {
            System.out.println("DHCP Server is running...");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket requestPacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(requestPacket);

                InetAddress clientAddress = requestPacket.getAddress();
                String macAddress = extractMacAddress(new String(buffer).trim());

                List<String> allocatedIps = ipAllocations.getOrDefault(macAddress, allocateIpAddresses(macAddress));

                byte[] response = createDHCPResponse(allocatedIps);
                DatagramPacket responsePacket = new DatagramPacket(response, response.length, clientAddress, requestPacket.getPort());
                socket.send(responsePacket);

                System.out.println("Allocated IPs: " + allocatedIps + " to MAC: " + macAddress+":11:22:33:44:55");
                showAvailableIpAddresses();  // Show remaining available IPs after allocation
                saveIpAllocations();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Initialize a pool of available IP addresses
    private static void initializeIpAddresses() {
        for (int i = 2; i <= 254; i++) {
            availableIpAddresses.add("192.168.1." + i);
        }
    }

    // Display available IP addresses
    private static void showAvailableIpAddresses() {
        System.out.println("Available IP Addresses: " + availableIpAddresses);
    }

    // Simulate extracting a MAC address from the request
    private static String extractMacAddress(String request) {
        return request.split(":")[1]; 
    }

    // Allocate two IP addresses from the available pool
    private static List<String> allocateIpAddresses(String macAddress) {
        List<String> allocatedIps = new ArrayList<>();

        // Check if enough IP addresses are available
        if (availableIpAddresses.size() < 4) {
            allocatedIps.add("No available IP addresses");
            return allocatedIps;  // Handle the case where fewer than 2 IPs are available
        }

        // Allocate the first two available IP addresses
        allocatedIps.add(availableIpAddresses.remove(0));
        allocatedIps.add(availableIpAddresses.remove(0));
        allocatedIps.add(availableIpAddresses.remove(0));
        allocatedIps.add(availableIpAddresses.remove(0));
        // Bind the MAC address with the allocated IP addresses
        ipAllocations.put(macAddress, allocatedIps);

        return allocatedIps;
    }

    // Create a DHCP response for the allocated IP addresses
    private static byte[] createDHCPResponse(List<String> allocatedIps) {
        return ("Allocated IPs: " + allocatedIps).getBytes();
    }

    // Save IP allocations to a file
    private static void saveIpAllocations() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(IP_ALLOCATIONS_FILE))) {
            out.writeObject(ipAllocations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load IP allocations from a file
    private static void loadIpAllocations() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(IP_ALLOCATIONS_FILE))) {
            ipAllocations = (HashMap<String, List<String>>) in.readObject();
        } catch (FileNotFoundException e) {
            System.out.println("No previous IP allocations found.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
