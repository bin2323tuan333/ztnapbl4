// src/features/reports/components/ReportFilters.tsx
import { Download, Filter } from 'lucide-react';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { vi } from 'date-fns/locale';

interface Props {
    fromDate: string;
    setFromDate: (val: string) => void;
    toDate: string;
    setToDate: (val: string) => void;
    selectedApp: string;
    setSelectedApp: (val: string) => void;
    onExport: () => void;
}

export const ReportFilters = ({
    fromDate, setFromDate, toDate, setToDate, selectedApp, setSelectedApp, onExport
}: Props) => {

    // 🔥 SỬA Ở ĐÂY: Trả về Date | undefined thay vì Date | null
    const parseDate = (dateStr: string): Date | undefined => {
        return dateStr ? new Date(dateStr) : undefined;
    };

    const formatDate = (date: Date | null): string => {
        if (!date) return '';
        const offset = date.getTimezoneOffset();
        const localDate = new Date(date.getTime() - (offset * 60 * 1000));
        return localDate.toISOString().split('T')[0];
    };

    return (
        <div className="report-filters-bar">
            <div className="filter-group date-picker-group">
                <DatePicker
                    selected={parseDate(fromDate) || null} // selected có thể nhận null
                    onChange={(date: Date | null) => setFromDate(formatDate(date))}
                    selectsStart
                    startDate={parseDate(fromDate)}
                    endDate={parseDate(toDate)}
                    placeholderText="Từ ngày"
                    dateFormat="dd/MM/yyyy"
                    locale={vi}
                    className="form-input date-input"
                    maxDate={parseDate(toDate) || new Date()}
                />
                <span className="filter-separator">đến</span>
                <DatePicker
                    selected={parseDate(toDate) || null}
                    onChange={(date: Date | null) => setToDate(formatDate(date))}
                    selectsEnd
                    startDate={parseDate(fromDate)}
                    endDate={parseDate(toDate)}
                    minDate={parseDate(fromDate)} // Bây giờ không còn lỗi nữa
                    maxDate={new Date()}
                    placeholderText="Đến ngày"
                    dateFormat="dd/MM/yyyy"
                    locale={vi}
                    className="form-input date-input"
                />
            </div>

            <div className="filter-group">
                <Filter size={16} className="text-muted" />
                <select value={selectedApp} onChange={(e) => setSelectedApp(e.target.value)} className="form-input">
                    <option value="ALL">Tất cả ứng dụng</option>
                    <option value="mysql">MySQL</option>
                    <option value="aws">AWS Console</option>
                </select>
            </div>

            <button className="btn-export" onClick={onExport}>
                <Download size={16} /> Xuất báo cáo
            </button>
        </div>
    );
};