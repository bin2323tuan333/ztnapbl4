import { Network } from 'lucide-react';

export const ConnectorIcon = ({ size = 20 }: { size?: number }) => {
    return (
        <div style={{ padding: '8px', background: '#f5f3ff', borderRadius: '8px', color: '#7c3aed', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Network size={size} />
        </div>
    );
};