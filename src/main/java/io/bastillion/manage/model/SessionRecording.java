package io.bastillion.manage.model;

import java.util.Date;

public class SessionRecording {
    private Long id;
    private String sessionId;
    private String recordingPath;
    private Date recordingDate;
    private Long userId;
    private String protocol;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public String getRecordingPath() { return recordingPath; }
    public void setRecordingPath(String recordingPath) { this.recordingPath = recordingPath; }
    
    public Date getRecordingDate() { return recordingDate; }
    public void setRecordingDate(Date recordingDate) { this.recordingDate = recordingDate; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
} 