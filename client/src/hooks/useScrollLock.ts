import { useEffect } from 'react';

/**
 * Hook to prevent scrolling on the body when a modal or overlay is open.
 * @param isOpen Whether the modal or overlay is open.
 */
export const useScrollLock = (isOpen: boolean) => {
    useEffect(() => {
        if (isOpen) {
            // Calculate scrollbar width
            const scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
            
            // Save current styles
            const originalStyle = window.getComputedStyle(document.body).overflow;
            const originalPaddingRight = window.getComputedStyle(document.body).paddingRight;

            // Prevent scrolling
            document.body.style.overflow = 'hidden';
            
            // Compensate for scrollbar width to prevent layout shift
            if (scrollbarWidth > 0) {
                document.body.style.paddingRight = `${scrollbarWidth}px`;
            }

            // Restoration on cleanup
            return () => {
                document.body.style.overflow = originalStyle;
                document.body.style.paddingRight = originalPaddingRight;
            };
        }
    }, [isOpen]);
};
