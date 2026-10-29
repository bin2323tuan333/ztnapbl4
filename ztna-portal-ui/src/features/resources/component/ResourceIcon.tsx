import { Globe, Network, Monitor, Terminal } from 'lucide-react';
import type { ResourceType } from '../api/resourceApi';

export const ResourceIcon = ({ type }: { type: ResourceType }) => {
    switch (type) {
        case 'SELF_HOSTED_WEB': return <div className="icon-box purple"><Globe size={16} /></div>;
        case 'NETWORK_RANGE': return <div className="icon-box gray"><Network size={16} /></div>;
        case 'RDP': return <div className="icon-box red"><Monitor size={16} /></div>;
        case 'SSH': return <div className="icon-box dark"><Terminal size={16} /></div>;
        default: return <div className="icon-box"><Globe size={16} /></div>;
    }
};