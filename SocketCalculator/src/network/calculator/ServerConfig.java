package network.calculator;
import java.io.*;

public class ServerConfig {
    private String serverIP;
    private int serverPort;
    
    // 기본값 설정
    private static final String DEFAULT_IP = "localhost";
    private static final int DEFAULT_PORT = 1234;
    
    public ServerConfig(String configFile) {
        loadConfig(configFile);
    }
    
    /**
     * 설정 파일에서 서버 정보를 읽어옴
     * 파일이 없거나 읽기 실패 시 기본값 사용
     */
    private void loadConfig(String configFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            serverIP = br.readLine();
            serverPort = Integer.parseInt(br.readLine());
            System.out.println("설정 파일 로드 성공: " + serverIP + ":" + serverPort);
        } catch (IOException | NumberFormatException e) {
            System.out.println("설정 파일 읽기 실패. 기본값 사용: " + 
                             DEFAULT_IP + ":" + DEFAULT_PORT);
            serverIP = DEFAULT_IP;
            serverPort = DEFAULT_PORT;
        }
    }
    
    public String getServerIP() {
        return serverIP;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    /**
     * 설정 파일 생성 (서버용)
     */
    public static void createConfigFile(String filename, String ip, int port) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println(ip);
            pw.println(port);
            System.out.println("설정 파일 생성 완료: " + filename);
        } catch (IOException e) {
            System.err.println("설정 파일 생성 실패: " + e.getMessage());
        }
    }
}