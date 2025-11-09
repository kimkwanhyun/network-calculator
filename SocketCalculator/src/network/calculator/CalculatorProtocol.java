package network.calculator;

public class CalculatorProtocol {
    
    /**
     * 클라이언트 요청을 처리하고 응답 생성
     */
    public static String processRequest(String request) {
        try {
            String[] tokens = request.trim().split("\\s+");
            
            if (tokens.length < 1) {
                return "Error: Empty request";
            }
            
            String operation = tokens[0].toUpperCase();
            
            // 연산자별 필요한 피연산자 수 검증
            switch (operation) {
                case "ADD":
                case "MUL":
                case "MIN":
                case "MAX":
                    if (tokens.length < 3) {
                        return "Error: Too few arguments";
                    }
                    break;
                case "SUB":
                case "DIV":
                    if (tokens.length < 3) {
                        return "Error: Too few arguments";
                    } else if (tokens.length > 3) {
                        return "Error: Too many arguments";
                    }
                    break;
                default:
                    return "Error: Unknown operation - " + operation;
            }
            
            // 피연산자 파싱
            double[] operands = new double[tokens.length - 1];
            for (int i = 1; i < tokens.length; i++) {
                try {
                    operands[i - 1] = Double.parseDouble(tokens[i]);
                } catch (NumberFormatException e) {
                    return "Error: Invalid number format - " + tokens[i];
                }
            }
            
            // 연산 수행
            double result = calculate(operation, operands);
            return "Answer: " + result;
            
        } catch (ArithmeticException e) {
            return "Error: " + e.getMessage();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
    
    /**
     * 실제 연산 수행
     */
    private static double calculate(String operation, double[] operands) {
        switch (operation) {
            case "ADD":
                double sum = 0;
                for (double op : operands) {
                    sum += op;
                }
                return sum;
                
            case "SUB":
                return operands[0] - operands[1];
                
            case "MUL":
                double product = 1;
                for (double op : operands) {
                    product *= op;
                }
                return product;
                
            case "DIV":
                if (operands[1] == 0) {
                    throw new ArithmeticException("divided by zero");
                }
                return operands[0] / operands[1];
                
            case "MIN":
                double min = operands[0];
                for (int i = 1; i < operands.length; i++) {
                    if (operands[i] < min) {
                        min = operands[i];
                    }
                }
                return min;
                
            case "MAX":
                double max = operands[0];
                for (int i = 1; i < operands.length; i++) {
                    if (operands[i] > max) {
                        max = operands[i];
                    }
                }
                return max;
                
            default:
                throw new IllegalArgumentException("Unknown operation");
        }
    }
    
    /**
     * 응답 타입 확인
     */
    public static boolean isAnswer(String response) {
        return response.startsWith("Answer:");
    }
    
    public static boolean isError(String response) {
        return response.startsWith("Error:");
    }
}