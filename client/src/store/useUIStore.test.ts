import { describe, it, expect, beforeEach } from 'vitest';
import { useUIStore } from './useUIStore';

describe('useUIStore', () => {
    beforeEach(() => {
        useUIStore.setState({ isLoginModalOpen: false, theme: 'light' });
    });

    it('initializes with modal closed', () => {
        const state = useUIStore.getState();
        expect(state.isLoginModalOpen).toBe(false);
    });

    it('opens login modal', () => {
        useUIStore.getState().openLoginModal();
        expect(useUIStore.getState().isLoginModalOpen).toBe(true);
    });

    it('closes login modal', () => {
        useUIStore.getState().openLoginModal();
        useUIStore.getState().closeLoginModal();
        expect(useUIStore.getState().isLoginModalOpen).toBe(false);
    });

    it('initializes with light theme', () => {
        expect(useUIStore.getState().theme).toBe('light');
    });

    it('toggles theme to dark', () => {
        useUIStore.getState().toggleTheme();
        expect(useUIStore.getState().theme).toBe('dark');
    });

    it('toggles theme back to light', () => {
        useUIStore.getState().toggleTheme();
        useUIStore.getState().toggleTheme();
        expect(useUIStore.getState().theme).toBe('light');
    });
});
