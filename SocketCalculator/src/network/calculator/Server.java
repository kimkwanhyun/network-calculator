package network.calculator;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

/**
 * 멀티스레드 계산기 서버
 * ThreadPool을 사용하여 여러 클라이언트 동시 처리
 */
public class Server {
    private static final int THREAD_POOL_SIZE = 10;
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private int port;
    
    public Server(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    /**
     * 서버 시작
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("====================================");
            System.out.println("계산기 서버 시작");
            System.out.println("포트: " + port);
            System.out.println("Thread Pool 크기: " + THREAD_POOL_SIZE);
            System.out.println("====================================");
            
            // 클라이언트 연결 수락
            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("\n[새 연결] " + 
                        clientSocket.getInetAddress().getHostAddress() + 
                        ":" + clientSocket.getPort());
                    
                    // ThreadPool에 클라이언트 처리 작업 제출
                    threadPool.execute(new ClientHandler(clientSocket));
                    
                } catch (IOException e) {
                    if (!serverSocket.isClosed()) {
                        System.err.println("클라이언트 연결 수락 중 오류: " + e.getMessage());
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("서버 시작 실패: " + e.getMessage());
        } finally {
            stop();
        }
    }
    
    /**
     * 서버 종료
     */
    public void stop() {
        try {
            if (threadPool != null) {
                threadPool.shutdown();
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("\n서버 종료됨");
        } catch (Exception e) {
            System.err.println("서버 종료 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 클라이언트 처리를 위한 Runnable 클래스
     */
    private static class ClientHandler implements Runnable {
        private Socket socket;
        
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }
        
        @Override
        public void run() {
            String clientAddr = socket.getInetAddress().getHostAddress() + 
                              ":" + socket.getPort();
            
            try (
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true)
            ) {
                System.out.println("[" + clientAddr + "] 연결 처리 시작");
                
                String request;
                while ((request = in.readLine()) != null) {
                    System.out.println("[" + clientAddr + "] 요청: " + request);
                    
                    // 종료 명령 확인
                    if (request.equalsIgnoreCase("QUIT") || 
                        request.equalsIgnoreCase("EXIT")) {
                        out.println("Answer: Goodbye!");
                        System.out.println("[" + clientAddr + "] 클라이언트 종료 요청");
                        break;
                    }
                    
                    // 프로토콜에 따라 요청 처리
                    String response = CalculatorProtocol.processRequest(request);
                    out.println(response);
                    
                    System.out.println("[" + clientAddr + "] 응답: " + response);
                }
                
            } catch (IOException e) {
                System.err.println("[" + clientAddr + "] 오류: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("[" + clientAddr + "] 연결 종료");
                } catch (IOException e) {
                    System.err.println("[" + clientAddr + "] 소켓 종료 실패");
                }
            }
        }
    }
    
    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        // 설정 파일 생성 (처음 실행 시)
        ServerConfig.createConfigFile("server_info.dat", "localhost", 1111);
        
        // 설정 파일에서 포트 정보 읽기
        ServerConfig config = new ServerConfig("server_info.dat");
        
        // 서버 시작
        Server server = new Server(config.getServerPort());
        
        // 종료 훅 등록
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n서버 종료 신호 수신...");
            server.stop();
        }));
        
        server.start();
    }
}