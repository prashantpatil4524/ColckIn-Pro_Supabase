package com.clockinpro.api;

import com.clockinpro.dao.AttendanceDAO;
import com.clockinpro.dao.EmployeeDAO;
import com.clockinpro.model.Attendance;
import com.clockinpro.model.Employee;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClockInProApiServer {

    private static EmployeeDAO empDAO = new EmployeeDAO();
    private static AttendanceDAO attDAO = new AttendanceDAO();

    public static void startServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/register", new RegisterHandler());
            server.createContext("/api/clockin", new ClockInHandler());
            server.createContext("/api/clockout", new ClockOutHandler());
            server.createContext("/api/status", new StatusHandler());
            server.createContext("/api/history", new HistoryHandler());
            server.createContext("/api/admin/employees", new AdminEmployeesHandler());

            server.setExecutor(null);
            server.start();
            System.out.println("REST API Server started on port " + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Setup CORS headers
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // Helper to parse x-www-form-urlencoded
    private static Map<String, String> parseFormData(String body) {
        Map<String, String> map = new HashMap<>();
        if (body == null || body.isEmpty()) return map;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8), 
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        setCorsHeaders(exchange);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static void handleOptions(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            setCorsHeaders(exchange);
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            Employee emp = empDAO.login(params.get("email"), params.get("password"));
            if (emp != null) {
                String json = String.format("{\"success\":true,\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"hourlyRate\":%f,\"role\":\"%s\"}",
                    emp.getId(), escape(emp.getName()), escape(emp.getEmail()), emp.getHourlyRate(), emp.getRole() != null ? emp.getRole() : "EMPLOYEE");
                sendResponse(exchange, 200, json);
            } else {
                sendResponse(exchange, 401, "{\"success\":false,\"message\":\"Invalid credentials\"}");
            }
        }
    }

    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> params = parseFormData(body);
            
            try {
                Employee emp = new Employee();
                emp.setName(params.get("name"));
                emp.setEmail(params.get("email"));
                emp.setPassword(params.get("password"));
                emp.setHourlyRate(Double.parseDouble(params.get("hourlyRate")));
                emp.setRole(params.containsKey("role") ? params.get("role") : "EMPLOYEE");
                boolean success = empDAO.registerEmployee(emp);
                if (success) {
                    sendResponse(exchange, 200, "{\"success\":true}");
                } else {
                    sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Email already exists\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Invalid input data\"}");
            }
        }
    }

    static class ClockInHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            InputStream is = exchange.getRequestBody();
            Map<String, String> params = parseFormData(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            int empId = Integer.parseInt(params.get("employeeId"));
            
            Attendance current = attDAO.getActiveLogin(empId);
            if (current != null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Already clocked in\"}");
                return;
            }
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            boolean success = attDAO.recordLogin(empId, now);
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"time\":\"" + now.toString() + "\"}");
            } else {
                sendResponse(exchange, 500, "{\"success\":false}");
            }
        }
    }

    static class ClockOutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            InputStream is = exchange.getRequestBody();
            Map<String, String> params = parseFormData(new String(is.readAllBytes(), StandardCharsets.UTF_8));
            int empId = Integer.parseInt(params.get("employeeId"));
            
            Attendance current = attDAO.getActiveLogin(empId);
            if (current == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"message\":\"Not clocked in\"}");
                return;
            }
            
            Timestamp now = new Timestamp(System.currentTimeMillis());
            long msDiff = now.getTime() - current.getLoginTime().getTime();
            double hours = msDiff / (1000.0 * 60 * 60);

            boolean success = attDAO.recordLogout(current.getId(), now, hours);
            if (success) {
                sendResponse(exchange, 200, "{\"success\":true,\"hours\":" + hours + "}");
            } else {
                sendResponse(exchange, 500, "{\"success\":false}");
            }
        }
    }

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            String query = exchange.getRequestURI().getQuery();
            if(query != null && query.startsWith("employeeId=")) {
                int empId = Integer.parseInt(query.split("=")[1]);
                Attendance current = attDAO.getActiveLogin(empId);
                if(current != null) {
                    sendResponse(exchange, 200, "{\"online\":true,\"loginTime\":\"" + current.getLoginTime().toString() + "\"}");
                } else {
                    sendResponse(exchange, 200, "{\"online\":false}");
                }
            }
        }
    }
    
    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            String query = exchange.getRequestURI().getQuery();
            if(query != null && query.startsWith("employeeId=")) {
                int empId = Integer.parseInt(query.split("=")[1]);
                List<Attendance> records = attDAO.getAttendanceByEmployee(empId);
                
                StringBuilder sb = new StringBuilder("[");
                for(int i = 0; i < records.size(); i++) {
                    Attendance att = records.get(i);
                    sb.append("{")
                      .append("\"loginTime\":\"").append(att.getLoginTime()).append("\",")
                      .append("\"logoutTime\":\"").append(att.getLogoutTime() != null ? att.getLogoutTime() : "").append("\",")
                      .append("\"hours\":").append(att.getTotalHours())
                      .append("}");
                    if(i < records.size() - 1) sb.append(",");
                }
                sb.append("]");
                sendResponse(exchange, 200, sb.toString());
            } else {
                sendResponse(exchange, 400, "[]");
            }
        }
    }

    static class AdminEmployeesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { handleOptions(exchange); return; }
            
            List<Employee> emps = empDAO.getAllEmployees();
            StringBuilder sb = new StringBuilder("[");
            for(int i = 0; i < emps.size(); i++) {
                Employee e = emps.get(i);
                
                // Get total hours and calculate total earnings for this employee
                List<Attendance> records = attDAO.getAttendanceByEmployee(e.getId());
                double totalHours = records.stream().mapToDouble(Attendance::getTotalHours).sum();
                double earnings = totalHours * e.getHourlyRate();

                sb.append("{")
                  .append("\"id\":").append(e.getId()).append(",")
                  .append("\"name\":\"").append(escape(e.getName())).append("\",")
                  .append("\"email\":\"").append(escape(e.getEmail())).append("\",")
                  .append("\"hourlyRate\":").append(e.getHourlyRate()).append(",")
                  .append("\"role\":\"").append(e.getRole() != null ? e.getRole() : "EMPLOYEE").append("\",")
                  .append("\"totalHours\":").append(totalHours).append(",")
                  .append("\"earnings\":").append(earnings)
                  .append("}");
                if(i < emps.size() - 1) sb.append(",");
            }
            sb.append("]");
            sendResponse(exchange, 200, sb.toString());
        }
    }

    private static String escape(String s) {
        if(s==null)return "";
        return s.replace("\"", "\\\"");
    }
}
