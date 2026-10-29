import { useEffect, type ReactNode, type CSSProperties } from 'react';
import { createPortal } from 'react-dom';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title?: string;
    children: ReactNode;
}

const styles: Record<string, CSSProperties> = {
    overlay: {
        position: 'fixed',
        inset: 0,
        background: 'rgba(0, 0, 0, 0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
        padding: 16,
    },
    content: {
        background: '#fff',
        borderRadius: 12,
        maxWidth: 480,
        width: '100%',
        maxHeight: '90vh',
        overflowY: 'auto',
        boxShadow: '0 20px 60px rgba(0, 0, 0, 0.3)',
    },
    header: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '16px 20px',
        borderBottom: '1px solid #e5e7eb',
    },
    title: {
        margin: 0,
        fontSize: 18,
        color: '#1f2937',
    },
    closeBtn: {
        background: 'transparent',
        border: 'none',
        fontSize: 18,
        cursor: 'pointer',
        color: '#6b7280',
        padding: '4px 8px',
        borderRadius: 6,
        lineHeight: 1,
    },
    body: {
        padding: 20,
    },
};

const Modal = ({ isOpen, onClose, title, children }: ModalProps) => {
    useEffect(() => {
        if (!isOpen) return;
        const onKey = (e: KeyboardEvent) => {
            if (e.key === 'Escape') onClose();
        };
        window.addEventListener('keydown', onKey);
        return () => window.removeEventListener('keydown', onKey);
    }, [isOpen, onClose]);

    useEffect(() => {
        if (!isOpen) return;
        const prev = document.body.style.overflow;
        document.body.style.overflow = 'hidden';
        return () => {
            document.body.style.overflow = prev;
        };
    }, [isOpen]);

    if (!isOpen) return null;

    return createPortal(
        <div style={styles.overlay} onClick={onClose}>
            <div
                style={styles.content}
                onClick={(e) => e.stopPropagation()}
                role="dialog"
                aria-modal="true"
                aria-label={title}
            >
                {title && (
                    <div style={styles.header}>
                        <h3 style={styles.title}>{title}</h3>
                        <button
                            style={styles.closeBtn}
                            onClick={onClose}
                            aria-label="Đóng"
                            onMouseEnter={(e) => (e.currentTarget.style.background = '#f3f4f6')}
                            onMouseLeave={(e) => (e.currentTarget.style.background = 'transparent')}
                        >
                            ✕
                        </button>
                    </div>
                )}
                <div style={styles.body}>{children}</div>
            </div>
        </div>,
        document.body
    );
};

export default Modal;