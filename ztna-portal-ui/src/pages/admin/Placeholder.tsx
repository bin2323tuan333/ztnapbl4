import React from 'react';
import { Construction } from 'lucide-react';

const Placeholder: React.FC = () => {
    return (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', padding: '80px 24px', textAlign: 'center' }}>
            <div style={{ width: 64, height: 64, background: '#f3e8ff', borderRadius: 16, display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9333ea', marginBottom: 16 }}>
                <Construction size={32} />
            </div>
            <h2 style={{ fontSize: 20, fontWeight: 600, color: '#111827', margin: '0 0 8px' }}>Đang phát triển</h2>
            <p style={{ fontSize: 14, color: '#6b7280', margin: 0 }}>Trang này đang được xây dựng. Vui lòng quay lại sau.</p>
        </div>
    );
};

export default Placeholder;