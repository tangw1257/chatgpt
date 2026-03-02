package com.example.taskplatform.client.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class CommandExecutorService {

    public String execute(String command) {
        try {
            Process process = new ProcessBuilder(shellPrefix(command)).start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            process.waitFor();
            return output.toString().trim();
        } catch (Exception e) {
            return "execute failed: " + e.getMessage();
        }
    }

    private String[] shellPrefix(String command) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (isWindows) {
            return new String[]{"cmd", "/c", command};
        }
        return new String[]{"bash", "-lc", command};
    }
}
