import { Shield } from 'lucide-react';

export const RuleIcon = ({ size = 20 }: { size?: number }) => {
    return (
        <div style={{ padding: '8px', background: '#eff6ff', borderRadius: '8px', color: '#3b82f6', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Shield size={size} />
        </div>
    );
};