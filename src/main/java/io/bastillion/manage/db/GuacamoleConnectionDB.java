package io.bastillion.manage.db;

import io.bastillion.manage.util.DBUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.Date;

public class GuacamoleConnectionDB {
    private static final Logger log = LoggerFactory.getLogger(GuacamoleConnectionDB.class);
    
    /* Commenting out DB interactions
    public static void logConnectionEvent(String sessionId, Long userId, String username, 
            String remoteHost, String clientIp, String protocol, String eventType) 
            throws GeneralSecurityException, SQLException {
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO guac_connection_history " +
                "(session_id, user_id, username, remote_host, client_ip, protocol, event_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
            
            stmt.setString(1, sessionId);
            stmt.setLong(2, userId);
            stmt.setString(3, username);
            stmt.setString(4, remoteHost);
            stmt.setString(5, clientIp);
            stmt.setString(6, protocol);
            stmt.setString(7, eventType);
            
            stmt.execute();
            DBUtils.closeStmt(stmt);
            
        } catch (Exception e) {
            log.error("Error logging connection event", e);
            throw e;
        } finally {
            DBUtils.closeConn(con);
        }
    }

    public static void logSessionStart(String sessionId, Long userId, String username, 
            String remoteHost, String clientIp, String protocol) 
            throws GeneralSecurityException, SQLException {
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO guac_connection_history " +
                "(session_id, user_id, username, remote_host, client_ip, protocol) " +
                "VALUES (?, ?, ?, ?, ?, ?)");
            
            stmt.setString(1, sessionId);
            stmt.setLong(2, userId);
            stmt.setString(3, username);
            stmt.setString(4, remoteHost);
            stmt.setString(5, clientIp);
            stmt.setString(6, protocol);
            
            stmt.execute();
            DBUtils.closeStmt(stmt);
        } finally {
            DBUtils.closeConn(con);
        }
    }

    public static void logSessionEnd(String sessionId) 
            throws GeneralSecurityException, SQLException {
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(
                "UPDATE guac_connection_history SET end_time = CURRENT_TIMESTAMP " +
                "WHERE session_id = ? AND end_time IS NULL");
            
            stmt.setString(1, sessionId);
            stmt.execute();
            DBUtils.closeStmt(stmt);
        } finally {
            DBUtils.closeConn(con);
        }
    }
    */

    // Keep logging functionality without DB interaction
    public static void logConnectionEvent(String sessionId, Long userId, String username,
            String remoteHost, String clientIp, String protocol, String eventType) {
        log.info("Connection event - Session: {}, User: {}, Host: {}, IP: {}, Protocol: {}, Event: {}, Time: {}", 
            sessionId, username, remoteHost, clientIp, protocol, eventType, new Date());
    }

    public static void logSessionStart(String sessionId, Long userId, String username,
            String remoteHost, String clientIp, String protocol) {
        log.info("Session started - Session: {}, User: {}, Host: {}, IP: {}, Protocol: {}, Start Time: {}", 
            sessionId, username, remoteHost, clientIp, protocol, new Date());
    }

    public static void logSessionEnd(String sessionId) {
        log.info("Session ended - Session: {}, End Time: {}", 
            sessionId, new Date());
    }
} 