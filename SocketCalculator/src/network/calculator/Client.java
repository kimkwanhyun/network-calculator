package network.calculator;
import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * 계산기 클라이언트 프로그램
 * 서버에 연산 요청을 보내고 결과를 받음
 */
public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String serverIP;
    private int serverPort;
    
    public Client(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }
    
    /**
     * 서버에 연결
     */
    public boolean connect() {
        try {
            System.out.println("서버 연결 시도 중: " + serverIP + ":" + serverPort);
            socket = new Socket(serverIP, serverPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("====================================");
            System.out.println("서버 연결 성공!");
            System.out.println("서버 주소: " + serverIP + ":" + serverPort);
            System.out.println("====================================");
            return true;
            
        } catch (IOException e) {
            System.err.println("서버 연결 실패: " + e.getMessage());
            System.err.println("서버가 실행 중인지 확인하세요.");
            return false;
        }
    }
    
    /**
     * 서버에 요청 전송 및 응답 수신
     */
    public String sendRequest(String request) {
        try {
            out.println(request);
            String response = in.readLine();
            return response;
        } catch (IOException e) {
            return "Error: Communication failed - " + e.getMessage();
        }
    }
    
    /**
     * 연결 종료
     */
    public void disconnect() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
            System.out.println("\n서버 연결 종료");
        } catch (IOException e) {
            System.err.println("연결 종료 중 오류: " + e.getMessage());
        }
    }
    
    /**
     * 대화형 모드 실행
     */
    public void runInteractive() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n=== 계산기 클라이언트 ===");
        System.out.println("사용법: OPERATION operand1 operand2 ...");
        System.out.println("지원 연산:");
        System.out.println("  ADD a b c ... : 덧셈");
        System.out.println("  SUB a b       : 뺄셈 (a - b)");
        System.out.println("  MUL a b c ... : 곱셈");
        System.out.println("  DIV a b       : 나눗셈 (a / b)");
        System.out.println("  MIN a b c ... : 최솟값");
        System.out.println("  MAX a b c ... : 최댓값");
        System.out.println("종료: QUIT 또는 EXIT");
        System.out.println("========================\n");
        
        while (true) {
            System.out.print(">> ");
            System.out.flush(); // 출력 버퍼 비우기
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) {
                continue;
            }
            
            // 종료 명령
            if (input.equalsIgnoreCase("QUIT") || input.equalsIgnoreCase("EXIT")) {
                String response = sendRequest(input);
                System.out.println(response);
                break;
            }
            
            // 서버에 요청 전송
            String response = sendRequest(input);
            
            // 응답 출력
            if (CalculatorProtocol.isAnswer(response)) {
                System.out.println("✓ " + response);
            } else if (CalculatorProtocol.isError(response)) {
                System.out.println("✗ " + response);
            } else {
                System.out.println(response);
            }
        }
        
        scanner.close();
    }
    
    /**
     * 자동 테스트 모드
     */
    public void runAutoTest() {
        System.out.println("\n=== 자동 테스트 모드 ===\n");
        
        String[] testCases = {
            "ADD 10 20",
            "SUB 50 30",
            "MUL 5 6 2",
            "DIV 100 4",
            "DIV 25 0",
            "MIN 5 2 1",
            "MAX 10 25 15 30",
            "ADD 1 2 3 4 5",
            "UNKNOWN 1 2",
            "DIV 10"
        };
        
        for (String testCase : testCases) {
            System.out.println("요청: " + testCase);
            String response = sendRequest(testCase);
            System.out.println("응답: " + response);
            System.out.println();
            
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        System.out.println("=== 테스트 완료 ===\n");
    }
    
    /**
     * 메인 메서드
     */
    public static void main(String[] args) {
        System.out.println("클라이언트 프로그램 시작...");
        
        // 설정 파일에서 서버 정보 읽기
        ServerConfig config = new ServerConfig("server_info.dat");
        
        System.out.println("읽어온 서버 정보: " + config.getServerIP() + ":" + config.getServerPort());
        
        // 클라이언트 생성 및 연결
        Client client = new Client(config.getServerIP(), config.getServerPort());
        
        if (!client.connect()) {
            System.err.println("\n서버에 연결할 수 없습니다.");
            System.err.println("1. 서버가 실행 중인지 확인하세요.");
            System.err.println("2. server_info.dat 파일의 포트 번호를 확인하세요.");
            return;
        }
        
        // 실행 모드 선택
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n실행 모드를 선택하세요:");
        System.out.println("1. 대화형 모드 (직접 입력)");
        System.out.println("2. 자동 테스트 모드");
        System.out.print("선택 (1 또는 2): ");
        System.out.flush(); // 출력 버퍼 비우기
        
        String choice = scanner.nextLine().trim();
        System.out.println("선택한 모드: " + choice);
        
        if (choice.equals("2")) {
            client.runAutoTest();
        } else {
            client.runInteractive();
        }
        
        client.disconnect();
        scanner.close();
    }
}