package com.smarthome.webapp.user.repository;

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
public class Panel {
    private String panelId;
    
    private String panelType;
    private String panelFilterCriteria;

    private Object data;
}
