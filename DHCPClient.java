import java.net.*;

public class DHCPClient {
    private static final int SERVER_PORT = 4900;
    private static final String SERVER_IP = "127.0.0.1"; // Change to server IP if needed

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_IP);

            // Create and send DHCP request with MAC address
            String macAddress = "00:11:22:33:44:55";  // Replace with actual MAC address
            byte[] request = createDHCPRequest(macAddress);
            DatagramPacket requestPacket = new DatagramPacket(request, request.length, serverAddress, SERVER_PORT);
            socket.send(requestPacket);

            // Receive the DHCP response
            byte[] buffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
            socket.receive(responsePacket);

            // Display the response
            String response = new String(responsePacket.getData()).trim();
            System.out.println("Received DHCP Response: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create a simple DHCP request
    private static byte[] createDHCPRequest(String macAddress) {
        return ("DHCP Request - MAC: " + macAddress).getBytes();
    }
}
