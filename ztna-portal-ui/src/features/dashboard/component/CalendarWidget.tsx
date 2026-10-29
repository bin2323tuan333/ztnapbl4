// src/features/dashboard/component/CalendarWidget.tsx
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { format, isSameMonth, isToday, isSameDay } from 'date-fns';

interface Props {
    currentDate: Date;
    selectedDate: Date | null;
    days: Date[];
    nextMonth: () => void;
    prevMonth: () => void;
    handleSelectDate: (date: Date) => void;
    getHolidayName: (date: Date) => string | undefined;
}

export const CalendarWidget = ({
    currentDate, selectedDate, days, nextMonth, prevMonth, handleSelectDate, getHolidayName
}: Props) => {
    const weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

    return (
        <div className="dashboard-panel">
            <div className="panel-header">
                <h3>Calendar</h3>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <button onClick={prevMonth} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280' }}>
                    <ChevronLeft size={20} />
                </button>
                <span style={{ fontWeight: 600, fontSize: 15, color: '#111827' }}>
                    {format(currentDate, 'MMMM yyyy')}
                </span>
                <button onClick={nextMonth} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6b7280' }}>
                    <ChevronRight size={20} />
                </button>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '4px', textAlign: 'center' }}>
                {weekDays.map(d => (
                    <div key={d} style={{ fontSize: 12, color: '#9ca3af', fontWeight: 500, marginBottom: 8 }}>{d}</div>
                ))}

                {days.map((day, i) => {
                    const holiday = getHolidayName(day);
                    const isCurrentMonth = isSameMonth(day, currentDate);
                    const isSelected = selectedDate ? isSameDay(day, selectedDate) : false;

                    // --- LOGIC MÀU SẮC MỚI ---
                    let bgColor = 'transparent';
                    let textColor = !isCurrentMonth ? '#d1d5db' : '#374151';
                    let fontWeight = 400;

                    if (isSelected) {
                        // 1. Ngày được chọn: Nền tím, chữ trắng
                        bgColor = '#8b5cf6';
                        textColor = 'white';
                        fontWeight = 600;
                    } else if (holiday) {
                        // 2. Ngày lễ: Nền đỏ nhạt, chữ đỏ
                        bgColor = '#fef2f2';
                        textColor = '#ef4444';
                        fontWeight = 600;
                    }
                    // Đã xóa hoàn toàn nhánh isTodayDate ở đây

                    return (
                        <div
                            key={i}
                            title={holiday ? `Ngày lễ: ${holiday}` : ''}
                            onClick={() => isCurrentMonth && handleSelectDate(day)}
                            style={{
                                fontSize: 13,
                                padding: '6px 0',
                                borderRadius: 6,
                                cursor: isCurrentMonth ? 'pointer' : 'default',
                                background: bgColor,
                                color: textColor,
                                fontWeight: fontWeight,
                                opacity: !isCurrentMonth ? 0.4 : 1,
                                transition: 'background 0.2s, color 0.2s'
                            }}
                        >
                            {format(day, 'd')}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};