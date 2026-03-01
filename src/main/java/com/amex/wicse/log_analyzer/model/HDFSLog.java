package com.amex.wicse.log_analyzer.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hdfs_logs")
public class HDFSLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "line_id")
    private Long line_id;
    @Column(name = "date")
    private String date;
    @Column(name = "time")
    private String time;
    @Column(name = "pid", columnDefinition = "integer")
    private Integer pid;
    @Column(name = "level", columnDefinition = "text")
    private String level;
    @Column(name = "component", columnDefinition = "text")
    private String component;
    @Column(name = "content", columnDefinition = "text")
    private String content;
    @Column(name = "event_id", columnDefinition = "text")
    private String event_id;
    @Column(name = "event_template", columnDefinition = "text")
    private String event_template;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLine_id() {
        return line_id;
    }

    public void setLine_id(Long line_id) {
        this.line_id = line_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getEvent_template() {
        return event_template;
    }

    public void setEvent_template(String event_template) {
        this.event_template = event_template;
    }
}
