import type { Column } from '../../types';
import './DataTable.css';

interface DataTableProps<T> {
    columns: Column<T>[];
    data: T[];
    emptyMessage?: string;
}

function DataTable<T extends { id: number | string }>({ columns, data, emptyMessage = 'Không có dữ liệu' }: DataTableProps<T>) {
    return (
        <div className="dt-container">
            <div className="dt-wrapper">
                <table className="dt-table">
                    <thead>
                        <tr>
                            {columns.map((col) => (
                                <th key={col.key} className="dt-th" style={col.width ? { width: col.width } : undefined}>
                                    {col.header}
                                </th>
                            ))}
                        </tr>
                    </thead>
                    <tbody>
                        {data.length === 0 ? (
                            <tr>
                                <td colSpan={columns.length} className="dt-empty">
                                    {emptyMessage}
                                </td>
                            </tr>
                        ) : (
                            data.map((item) => (
                                <tr key={item.id} className="dt-row">
                                    {columns.map((col) => (
                                        <td key={col.key} className="dt-td">
                                            {col.render ? col.render(item) : String((item as Record<string, unknown>)[col.key] ?? '')}
                                        </td>
                                    ))}
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default DataTable;
