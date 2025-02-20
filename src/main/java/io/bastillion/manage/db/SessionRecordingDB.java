package io.bastillion.manage.db;

import io.bastillion.manage.model.SessionRecording;
import io.bastillion.manage.util.DBUtils;
import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.GeneralSecurityException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SessionRecordingDB {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionRecordingDB.class);
    
    public static void saveRecordingPath(String sessionId, String path, Long userId, String protocol) 
            throws SQLException, GeneralSecurityException {
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(
                "INSERT INTO session_recordings " +
                "(session_id, recording_path, user_id, protocol) " +
                "VALUES (?,?,?,?)");
            stmt.setString(1, sessionId);
            stmt.setString(2, path);
            stmt.setLong(3, userId);
            stmt.setString(4, protocol);
            stmt.execute();
        } finally {
            DBUtils.closeConn(con);
        }
    }

    public static String getRecordingPath(String sessionId) throws SQLException, GeneralSecurityException {
        Connection con = null;
        try {
            con = DBUtils.getConn();
            PreparedStatement stmt = con.prepareStatement(
                "SELECT recording_path FROM session_recordings WHERE session_id=?");
            stmt.setString(1, sessionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("recording_path");
            }
            return null;
        } finally {
            DBUtils.closeConn(con);
        }
    }
} 