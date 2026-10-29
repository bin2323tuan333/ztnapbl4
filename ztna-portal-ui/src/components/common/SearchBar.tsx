import { Search, X } from 'lucide-react';
import './SearchBar.css';

interface Props {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    autoFocus?: boolean;
    disabled?: boolean;
    className?: string;
}

export const SearchBar = ({
    value,
    onChange,
    placeholder = 'Tìm kiếm...',
    autoFocus = false,
    disabled = false,
    className = '',
}: Props) => {
    return (
        <div className={`search-bar ${className}`.trim()}>
            <Search size={18} className="search-bar__icon" />
            <input
                type="text"
                className="search-bar__input"
                placeholder={placeholder}
                value={value}
                onChange={(e) => onChange(e.target.value)}
                autoFocus={autoFocus}
                disabled={disabled}
                aria-label={placeholder}
            />
            {value && !disabled && (
                <button
                    type="button"
                    className="search-bar__clear"
                    onClick={() => onChange('')}
                    aria-label="Xoá tìm kiếm"
                >
                    <X size={16} />
                </button>
            )}
        </div>
    );
};