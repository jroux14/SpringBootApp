package com.smarthome.webapp.user.repository;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Dashboard {
    private String dashboardId;
    
    private List<Panel> panels;
}
