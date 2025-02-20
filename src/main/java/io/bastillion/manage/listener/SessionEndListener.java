package io.bastillion.manage.listener;

import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import io.bastillion.manage.util.RecordingConverter;
import io.bastillion.manage.db.SessionRecordingDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.annotation.WebListener;

@WebListener
public class SessionEndListener implements HttpSessionListener {
    private static final Logger logger = LoggerFactory.getLogger(SessionEndListener.class);
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Not needed
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        try {
            String sessionId = se.getSession().getId();
            String recordingPath = SessionRecordingDB.getRecordingPath(sessionId);
            
            if (recordingPath != null) {
                // Wait for guacd to finish writing
                Thread.sleep(10000);
                
                logger.info("Session ended, converting recording: {}", recordingPath);
                RecordingConverter.convertRecording(recordingPath);
            }
        } catch (Exception e) {
            logger.error("Error converting recording after session end", e);
        }
    }
} 