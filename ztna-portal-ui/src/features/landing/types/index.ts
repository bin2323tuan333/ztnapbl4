import type { ReactNode } from 'react';

export interface Feature {
    id: number;
    icon: ReactNode;
    title: string;
    description: string;
}
