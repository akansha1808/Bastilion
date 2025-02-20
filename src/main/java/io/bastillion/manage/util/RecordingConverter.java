package io.bastillion.manage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RecordingConverter {
    private static final Logger log = LoggerFactory.getLogger(RecordingConverter.class);
    
    public static boolean checkGuacenc() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "exec", "guacd_compose", "guacenc", "--version"
            );
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("guacenc check output: {}", line);
                }
            }
            
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            log.error("Error checking guacenc availability", e);
            return false;
        }
    }

    public static void convertRecording(String recordingPath) {
        try {
            // Ensure file exists and is readable
            File recordingFile = new File(recordingPath);
            if (!recordingFile.exists()) {
                log.error("Recording file does not exist: {}", recordingPath);
                return;
            }

            // Make recording file readable by Docker
            recordingFile.setReadable(true, false);
            
            // Convert host path to Docker path
            String dockerPath = recordingPath.replaceFirst(
                ".*/bastillion/recordings", "/recordings");
            
            // Execute conversion
            ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "exec", "guacd_compose", 
                "guacenc", "-f", "-s", "1280x720", dockerPath
            );
            
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            // Monitor output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("guacenc: {}", line);
                }
            }
            
            // Handle completion
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                File mp4File = new File(recordingPath + ".m4v");
                mp4File.setReadable(true, false);
                log.info("MP4 file created: {}", mp4File.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error converting recording: " + recordingPath, e);
        }
    }
    
    public static void convertAllRecordings(String recordingsDir) {
        File dir = new File(recordingsDir);
        if (dir.exists() && dir.isDirectory()) {
            File[] sessionDirs = dir.listFiles();
            if (sessionDirs != null) {
                for (File sessionDir : sessionDirs) {
                    if (sessionDir.isDirectory()) {
                        File recordingFile = new File(sessionDir, "recording");
                        if (recordingFile.exists()) {
                            convertRecording(recordingFile.getAbsolutePath());
                        }
                    }
                }
            }
        }
    }
} 