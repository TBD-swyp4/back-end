package com.example.spinlog.calendar.dto;

import com.example.spinlog.utils.NullDataConverter;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class TotalCalendarResponseDto {
    private BudgetDto budgetDto;
    private List<MonthSpend> monthSpendList;
    private List<DaySpend> daySpendList;

    @Builder
    public TotalCalendarResponseDto(BudgetDto budgetDto, List<MonthSpend> monthSpendList, List<DaySpend> daySpendList) {
        this.budgetDto = budgetDto;
        this.monthSpendList = NullDataConverter.convertList(monthSpendList);
        this.daySpendList = NullDataConverter.convertList(daySpendList);
    }
}
