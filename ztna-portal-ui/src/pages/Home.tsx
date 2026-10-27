import { useState } from 'react';
import { Search, ChevronDown } from 'lucide-react';
import './Home.css';

function Home() {
    const [search, setSearch] = useState('');
    const [appType, setAppType] = useState('App Type');
    const [dateAdded, setDateAdded] = useState('Date Added');

    return (
        <div className="hm-page">
            {/* Toolbar: Search + Filters gom vào 1 div, căn giữa */}
            <div className="hm-toolbar">
                <div className="hm-toolbar-inner">
                    <div className="hm-search">
                        <Search size={18} className="hm-search-icon" />
                        <input
                            type="text"
                            placeholder="Search by application"
                            value={search}
                            onChange={e => setSearch(e.target.value)}
                        />
                    </div>

                    <div className="hm-select">
                        <select
                            value={appType}
                            onChange={e => setAppType(e.target.value)}
                        >
                            <option>App Type</option>
                            <option>Web</option>
                            <option>SSH</option>
                            <option>RDP</option>
                        </select>
                        <ChevronDown size={16} className="hm-select-icon" />
                    </div>

                    <div className="hm-select">
                        <select
                            value={dateAdded}
                            onChange={e => setDateAdded(e.target.value)}
                        >
                            <option>Date Added</option>
                            <option>Last 7 days</option>
                            <option>Last 30 days</option>
                            <option>Last year</option>
                        </select>
                        <ChevronDown size={16} className="hm-select-icon" />
                    </div>
                </div>
            </div>

            {/* Content */}
            <div className="hm-content"></div>
        </div>
    );
}

export default Home;