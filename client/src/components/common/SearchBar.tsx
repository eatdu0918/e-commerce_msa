import { useState } from 'react';
import { Search } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

interface SearchBarProps {
    className?: string;
    onSearch?: () => void;
}

export default function SearchBar({ className = '', onSearch }: SearchBarProps) {
    const [keyword, setKeyword] = useState('');
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!keyword.trim()) return;

        navigate(`/search?q=${encodeURIComponent(keyword.trim())}`);
        setKeyword('');
        if (onSearch) onSearch();
    };

    return (
        <form onSubmit={handleSubmit} className={`relative group ${className}`}>
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Search size={18} className="text-stone-400 group-focus-within:text-black transition-colors" />
            </div>
            <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder={t('common.search_placeholder') || 'Search products...'}
                className="w-full pl-10 pr-4 py-2 bg-stone-100 border-none rounded-full text-sm focus:ring-1 focus:ring-black focus:bg-white transition-all outline-none"
            />
        </form>
    );
}
