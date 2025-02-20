package io.bastillion.manage.guacamole;

import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.event.AuthenticationFailureEvent;
import org.apache.guacamole.net.event.AuthenticationSuccessEvent;
import org.apache.guacamole.net.event.TunnelCloseEvent;
import org.apache.guacamole.net.event.listener.Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.text.SimpleDateFormat;

/**
 * A Listener that logs authentication and session events.
 */
public class BastillionGuacamoleListener implements Listener {

    private static final Logger logger = LoggerFactory.getLogger(BastillionGuacamoleListener.class);

    // Store session timing information: sessionId -> {startTime, endTime}
    private static final ConcurrentHashMap<String, SessionTiming> sessionTimings = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public static class SessionTiming {
        private final Date startTime;
        private Date endTime;
        private final String username;
        private final String host;
        private final String protocol;
        
        public SessionTiming(Date startTime, String username, String host, String protocol) {
            this.startTime = startTime;
            this.username = username;
            this.host = host;
            this.protocol = protocol;
        }
        
        public void setEndTime(Date endTime) {
            this.endTime = endTime;
        }
        
        // Getters
        public Date getStartTime() { return startTime; }
        public Date getEndTime() { return endTime; }
        public String getUsername() { return username; }
        public String getHost() { return host; }
        public String getProtocol() { return protocol; }
    }

    @Override
    public void handleEvent(Object event) throws GuacamoleException {
        if (event instanceof AuthenticationSuccessEvent) {
            //listener to read these attributes that were set in guacamoletunnelservlet
            AuthenticationSuccessEvent authEvent = (AuthenticationSuccessEvent) event;
            String username = authEvent.getCredentials().getUsername();
            HttpSession session = authEvent.getCredentials().getRequest().getSession();
            String sessionId = session.getId();
            String targetHost = (String) session.getAttribute("hostname");
            String protocol = (String) session.getAttribute("protocol");
            Date startTime = new Date();
            
            // Store session timing
            sessionTimings.put(sessionId, new SessionTiming(startTime, username, targetHost, protocol));
            
            logger.info("=== Session Started ===");
            logger.info("User: {}", username);
            logger.info("Host: {}", targetHost);
            logger.info("Protocol: {}", protocol);
            logger.info("Session started at: {}", dateFormat.format(startTime));
            logger.info("=====================");
        }
        else if (event instanceof AuthenticationFailureEvent) {
            logger.warn("Authentication failed for user '{}'", 
                ((AuthenticationFailureEvent) event)
                    .getCredentials().getUsername());
        }
        else if (event instanceof TunnelCloseEvent) {
            TunnelCloseEvent closeEvent = (TunnelCloseEvent) event;
            HttpSession session = closeEvent.getCredentials().getRequest().getSession();
            String sessionId = session.getId();
            Date endTime = new Date();
            
            // Update session timing with end time
            SessionTiming timing = sessionTimings.get(sessionId);
            if (timing != null) {
                timing.setEndTime(endTime);
                
                logger.info("=== Session Ended ===");
                logger.info("User: {}", timing.getUsername());
                logger.info("Host: {}", timing.getHost());
                logger.info("Protocol: {}", timing.getProtocol());
                logger.info("Session started at: {}", dateFormat.format(timing.getStartTime()));
                logger.info("Session ended at: {}", dateFormat.format(endTime));
                logger.info("===================");
            }
        }
    }
    
    // Method to get session timing information
    public static SessionTiming getSessionTiming(String sessionId) {
        return sessionTimings.get(sessionId);
    }
} 