import { useState, useEffect } from 'react';
import {
    startOfMonth, endOfMonth, startOfWeek, endOfWeek,
    eachDayOfInterval, format, addMonths, subMonths
} from 'date-fns';
import { calendarApi, type Holiday } from '../api/calendarApi';

export const useCalendar = () => {
    const [currentDate, setCurrentDate] = useState(new Date()); // Ngày dùng để điều hướng tháng
    const [selectedDate, setSelectedDate] = useState<Date | null>(new Date()); // Ngày được chọn (mặc định là hôm nay)
    const [holidays, setHolidays] = useState<Holiday[]>([]);

    useEffect(() => {
        const year = currentDate.getFullYear();
        calendarApi.getHolidays(year).then(setHolidays);
    }, [currentDate.getFullYear()]);

    const monthStart = startOfMonth(currentDate);
    const monthEnd = endOfMonth(monthStart);
    const startDate = startOfWeek(monthStart);
    const endDate = endOfWeek(monthEnd);

    const days = eachDayOfInterval({ start: startDate, end: endDate });

    const nextMonth = () => setCurrentDate(addMonths(currentDate, 1));
    const prevMonth = () => setCurrentDate(subMonths(currentDate, 1));

    // Hàm xử lý khi click chọn ngày
    const handleSelectDate = (date: Date) => {
        setSelectedDate(date);
    };

    const getHolidayName = (date: Date) => {
        const dateString = format(date, 'yyyy-MM-dd');
        return holidays.find(h => h.date === dateString)?.localName;
    };

    return {
        currentDate,
        selectedDate,
        days,
        nextMonth,
        prevMonth,
        handleSelectDate,
        getHolidayName
    };
};