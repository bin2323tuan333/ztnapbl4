import { Globe, Database, Terminal } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi';

export const ResourceIcon = ({ type }: { type: ResourceType }) => {
    switch (type) {
        case 'WEB': return <div className="icon-box purple"><Globe size={16} /></div>;
        case 'MYSQL': return <div className="icon-box gray"><Database size={16} /></div>;
        case 'SSH': return <div className="icon-box dark"><Terminal size={16} /></div>;
        default: return <div className="icon-box"><Globe size={16} /></div>;
    }
};