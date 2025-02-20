package io.bastillion.manage.util;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.bastillion.common.util.AppConfig;

public class RecordingDirectoryManager {
    private static final Logger logger = LoggerFactory.getLogger(RecordingDirectoryManager.class);
    
    public static void initializeRecordingDirectory() {
        try {
            String recordingPath = AppConfig.getProperty("recordingPath")
                .replace("${user.home}", System.getProperty("user.home"));
            
            File baseDir = new File(recordingPath);
            if (!baseDir.exists()) {
                logger.info("Creating recording directory: {}", recordingPath);
                if (!baseDir.mkdirs()) {
                    logger.error("Failed to create recording directory: {}", recordingPath);
                    return;
                }
            }
            
            // Set proper permissions for Docker access
            baseDir.setWritable(true, false);  // Writable by all
            baseDir.setReadable(true, false);  // Readable by all
            baseDir.setExecutable(true, false); // Executable (for directory access)
            
            logger.info("Recording directory initialized: {}", recordingPath);
            logger.info("Permissions: read={}, write={}, execute={}", 
                baseDir.canRead(), baseDir.canWrite(), baseDir.canExecute());
        } catch (Exception e) {
            logger.error("Error initializing recording directory", e);
        }
    }
} 