import { useState, useEffect, useCallback, useMemo } from 'react';
import { ruleApi, type RuleSummary } from '../api/ruleApi';

export const useRules = () => {
    const [rules, setRules] = useState<RuleSummary[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchTerm, setSearchTerm] = useState('');

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const data = await ruleApi.getRules();
            setRules(data);
            setError('');
        } catch (err: any) {
            setError(err.message || 'Lỗi tải dữ liệu Rules');
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const filteredRules = useMemo(() => {
        if (!searchTerm) return rules;
        const lower = searchTerm.toLowerCase();
        return rules.filter(r =>
            r.name.toLowerCase().includes(lower) ||
            (r.description && r.description.toLowerCase().includes(lower))
        );
    }, [rules, searchTerm]);

    const toggleStatus = async (id: string, currentStatus: boolean) => {
        setRules(prev => prev.map(r => r.id === id ? { ...r, enabled: !currentStatus } : r));
        try {
            await ruleApi.toggleRuleStatus(id);
        } catch (err) {
            setRules(prev => prev.map(r => r.id === id ? { ...r, enabled: currentStatus } : r));
            alert('Không thể đổi trạng thái, vui lòng thử lại!');
        }
    };

    const deleteRule = async (id: string) => {
        await ruleApi.deleteRule(id);
        setRules(prev => prev.filter(r => r.id !== id));
    };

    return {
        rules: filteredRules,
        isLoading,
        error,
        searchTerm,
        setSearchTerm,
        toggleStatus,
        deleteRule,
        refetch: fetchData
    };
};