package io.bastillion.manage.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.guacamole.GuacamoleException;
import org.apache.guacamole.net.GuacamoleSocket;
import org.apache.guacamole.net.GuacamoleTunnel;
import org.apache.guacamole.net.InetGuacamoleSocket;
import org.apache.guacamole.net.SimpleGuacamoleTunnel;
import org.apache.guacamole.protocol.ConfiguredGuacamoleSocket;
import org.apache.guacamole.protocol.GuacamoleConfiguration;
import org.apache.guacamole.servlet.GuacamoleHTTPTunnelServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.Socket;
import io.bastillion.common.util.AuthUtil;
import java.text.SimpleDateFormat;
import java.util.Date;
import io.bastillion.manage.db.SessionRecordingDB;
import io.bastillion.common.util.AppConfig;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.UUID;
import io.bastillion.manage.util.RecordingConverter;
import io.bastillion.manage.db.SystemDB;
import java.sql.SQLException;

public class GuacamoleTunnelServlet extends GuacamoleHTTPTunnelServlet {

    private static final Logger logger = LoggerFactory.getLogger(GuacamoleTunnelServlet.class);

    @Override
    protected GuacamoleTunnel doConnect(HttpServletRequest request) throws GuacamoleException {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                logger.warn("Connection attempt without valid session from IP: {}", request.getRemoteAddr());
                throw new GuacamoleException("No valid session");
            }
            
            // Get connection parameters
            String hostname = request.getParameter("hostname");
            String protocol = request.getParameter("protocol");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            
            logger.info("Connection request - User: {}, Protocol: {}, Host: {}, Remote IP: {}", 
                AuthUtil.getUsername(session), protocol, hostname, request.getRemoteAddr());
            
            // If admin user, save the system
            if ("M".equals(session.getAttribute("userType"))) {
                try {
                    String displayName = hostname + " (" + protocol.toUpperCase() + ")";
                    int port = "ssh".equals(protocol) ? 22 : 3389;
                    SystemDB.saveSystem(displayName, hostname, username, protocol, port);
                } catch (SQLException e) {
                    logger.error("Error saving system", e);
                    // Continue with connection even if save fails
                }
            }
            
            // Verify user is authenticated
            if (AuthUtil.getUserId(session) == null) {
                logger.error("User not authenticated");
                throw new GuacamoleException("User not authenticated");
            }
            
            logger.info("Attempting connection - Protocol: {}, Host: {}, User: {}", protocol, hostname, username);
            
            // Set these attributes that our listener will read
            session.setAttribute("hostname", hostname);
            session.setAttribute("protocol", protocol);
            session.setAttribute("userAgent", request.getHeader("User-Agent"));
            session.setAttribute("clientIP", request.getRemoteAddr());
            
            // Create the Guacamole configuration
            GuacamoleConfiguration config = new GuacamoleConfiguration();
            config.setProtocol(protocol);
            config.setParameter("hostname", hostname);
            
            if ("ssh".equals(protocol)) {
                // SSH Configuration
                config.setParameter("port", "22");
                config.setParameter("username", username);
                config.setParameter("password", password);
                
                // SSH-specific settings
                config.setParameter("font-name", "monospace");
                config.setParameter("font-size", "12");
                config.setParameter("color-scheme", "white-black");
            } 
            else if ("rdp".equals(protocol)) {
                // Basic RDP Configuration
                config.setParameter("port", "3389");
                config.setParameter("username", username);
                config.setParameter("password", password);
                
                // Basic display settings
                config.setParameter("width", "1024");
                config.setParameter("height", "768");
                config.setParameter("dpi", "96");
                
                // Security settings
                config.setParameter("security", "any");
                config.setParameter("ignore-cert", "true");
                
                // Basic performance settings
                config.setParameter("enable-wallpaper", "false");
                config.setParameter("enable-theming", "false");
                config.setParameter("enable-font-smoothing", "true");
            }
            
            // Try recording setup only after basic config is done
            try {
                if (Boolean.parseBoolean(AppConfig.getProperty("recordingEnabled"))) {
                    String basePath = AppConfig.getProperty("recordingPath")
                        .replace("${user.home}", System.getProperty("user.home"));
                    String sessionUUID = UUID.randomUUID().toString();
                    String recordingPath = basePath + "/" + sessionUUID + "/recording";
                    
                    // Configure recording
                    config.setParameter("recording-path", "/recordings/" + sessionUUID);
                    config.setParameter("create-recording-path", "true");
                    config.setParameter("recording-name", "recording");
                    
                    // Save and log recording path
                    SessionRecordingDB.saveRecordingPath(session.getId(), recordingPath, AuthUtil.getUserId(session), protocol);
                    logger.info("Session recording enabled - Session: {}, Path: {}", session.getId(), recordingPath);
                }
            } catch (Exception e) {
                logger.error("Failed to setup recording: {}", e.getMessage(), e);
            }
            
            // Test guacd availability
            try (Socket testSocket = new Socket("localhost", 4822)) {
                logger.info("guacd is available");
            } catch (IOException e) {
                logger.error("guacd is not available: " + e.getMessage());
                throw new GuacamoleException("guacd service is not available");
            }
            
            // Create and store the tunnel
            GuacamoleSocket socket = new ConfiguredGuacamoleSocket(
                new InetGuacamoleSocket("localhost", 4822),
                config
            );
             // Create and return the tunnel that maintains the session
            GuacamoleTunnel tunnel = new SimpleGuacamoleTunnel(socket);

            // Store tunnel in session for lifecycle management
            session.setAttribute("GUAC_TUNNEL", tunnel);
            
            // Store connection details in session for logging
            session.setAttribute("hostname", hostname);
            session.setAttribute("protocol", protocol);
            session.setAttribute("userAgent", request.getHeader("User-Agent"));
            session.setAttribute("clientIP", request.getRemoteAddr());
            
            // Enhanced logging for tunnel creation
            logger.info("Tunnel established - User: {}, Protocol: {}, Host: {}", 
                AuthUtil.getUsername(session), protocol, hostname);
            
            return tunnel;
            
        } catch (Exception e) {
            logger.error("Connection failed - Error: {}, Stack: {}", e.getMessage(), e);
            throw new GuacamoleException(e.getMessage());
        }
    }
} 