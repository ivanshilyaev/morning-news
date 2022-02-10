package com.ivanshilyaev.morningnews.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CalendarEvent implements Comparable<CalendarEvent> {

    private LocalDateTime start;
    private LocalDateTime end;
    private String summary;

    @Override
    public int compareTo(CalendarEvent o) {
        return this.start.compareTo(o.start);
    }
}
